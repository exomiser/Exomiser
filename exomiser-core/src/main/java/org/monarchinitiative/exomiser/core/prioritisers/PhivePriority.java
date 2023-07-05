/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;

/**
 * Filter variants according to the phenotypic similarity of the specified
 * clinical phenotypes to mouse models disrupting the same gene. We use MGI
 * annotated phenotype data and the Phenodigm/OWLSim algorithm. The filter is
 * implemented with an SQL query.
 * <P>
 * This class prioritizes the genes that have survived the initial VCF filter
 * (i.e., it is use on genes for which we have found rare, potentially
 * pathogenic variants).
 * <P>
 *
 * @author Damian Smedley
 * @author Jules Jacobsen
 * @version 0.05 (April 6, 2013)
 */
public class PhivePriority implements Prioritiser<PhivePriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(PhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.PHIVE_PRIORITY;
    private static final float NO_PHENOTYPE_HIT_SCORE = 0.1f;
    static final float NO_MOUSE_MODEL_SCORE = 0.6f;

    private final PriorityService priorityService;

    public PhivePriority(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    /**
     * Flag to output results of filtering against PhenoDigm data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    @Override
    public Stream<PhivePriorityResult> prioritise(Sample sample, List<Gene> genes) {
        List<String> hpoIds = sample.getHpoIds();
        logger.info("Starting {}", PRIORITY_TYPE);

        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);
        PhenotypeMatcher humanMousePhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(hpoPhenotypeTerms, Organism.MOUSE);

        Set<Integer> wantedGeneIds = genes.stream().map(Gene::getEntrezGeneID).collect(ImmutableSet.toImmutableSet());

        Set<GeneModel> modelsToScore = priorityService.getModelsForOrganism(Organism.MOUSE).stream()
                .filter(model -> wantedGeneIds.contains(model.getEntrezGeneId()))
                .collect(ImmutableSet.toImmutableSet());

        List<GeneModelPhenotypeMatch> scoredModels = scoreModels(humanMousePhenotypeMatcher, modelsToScore);

        //n.b. this will contain models but with a phenotype score of zero
        Map<Integer, Optional<GeneModelPhenotypeMatch>> geneModelPhenotypeMatches = scoredModels.parallelStream()
//                .filter(model -> model.getScore() > 0)
                .collect(groupingByConcurrent(GeneModelPhenotypeMatch::getEntrezGeneId, maxBy(comparingDouble(GeneModelPhenotypeMatch::getScore))));

        return genes.stream().map(getPhivePriorityResult(geneModelPhenotypeMatches));
    }

    private Function<Gene, PhivePriorityResult> getPhivePriorityResult(Map<Integer, Optional<GeneModelPhenotypeMatch>> geneModelPhenotypeMatches) {
        return gene -> geneModelPhenotypeMatches.getOrDefault(gene.getEntrezGeneID(), Optional.empty())
                    .map(makeModelPhivePriorityResult())
                    //This is set to 0.6 otherwise the performance is poor for genes with no mouse models.
                    //The rankings are quite different to hiPhive because of this - HiPhive uses 0 if there are no models.
                    //n.b. this ranks genes with no model higher than those with a model with a score of zero.
                    .orElse(new PhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), NO_MOUSE_MODEL_SCORE, null));
    }

    private Function<GeneModelPhenotypeMatch, PhivePriorityResult> makeModelPhivePriorityResult() {
        return modelPhenotypeMatch -> new PhivePriorityResult(modelPhenotypeMatch.getEntrezGeneId(), modelPhenotypeMatch.getHumanGeneSymbol(), modelPhenotypeMatch.getScore(), modelPhenotypeMatch);
    }

    private List<GeneModelPhenotypeMatch> scoreModels(PhenotypeMatcher organismPhenotypeMatcher, Collection<GeneModel> models) {
        Organism organism = organismPhenotypeMatcher.getOrganism();

        ModelScorer<GeneModel> modelScorer = PhenodigmModelScorer.forSingleCrossSpecies(organismPhenotypeMatcher);

        logger.info("Scoring {} models", organism);
        Instant timeStart = Instant.now();
        //running this in parallel here can cut the overall time for this method in half or better - ~650ms -> ~350ms on Pfeiffer test set.
        List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = models.parallelStream()
                .map(modelScorer::scoreModel)
                .map(GeneModelPhenotypeMatch::new)
                .collect(toList());

        Duration duration = Duration.between(timeStart, Instant.now());
        logger.info("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());
        return geneModelPhenotypeMatches;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(PhivePriority.class.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "PhivePriority{}";
    }

}
