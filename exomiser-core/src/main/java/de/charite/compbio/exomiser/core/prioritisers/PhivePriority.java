/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.util.OrganismPhenotypeMatches;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.exomiser.core.prioritisers.util.TheoreticalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.groupingByConcurrent;
import static java.util.stream.Collectors.maxBy;

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
public class PhivePriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(PhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.PHIVE_PRIORITY;
    private static final float NO_PHENOTYPE_HIT_SCORE = 0.1f;
    static final float NO_MOUSE_MODEL_SCORE = 0.6f;

    private final List<String> hpoIds;
    private final PriorityService priorityService;

    public PhivePriority(List<String> hpoIds, PriorityService priorityService) {
        this.hpoIds = hpoIds;
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
    public void prioritizeGenes(List<Gene> genes) {
        logger.info("Starting {}", PRIORITY_TYPE);
        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        OrganismPhenotypeMatches humanMousePhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, Organism.MOUSE);
        TheoreticalModel bestTheoreticalModel = humanMousePhenotypeMatches.getBestTheoreticalModel();
        logTheoreticalModel(bestTheoreticalModel);
        
        List<Model> scoredModels = getAndScoreModels(bestTheoreticalModel, humanMousePhenotypeMatches);

        //n.b. this will contain models but with a phenotype score of zero
        Map<Integer, Optional<Model>> geneModelPhenotypeMatches = scoredModels.parallelStream()
//                .filter(model -> model.getScore() > 0)
                .collect(groupingByConcurrent(Model::getEntrezGeneId, maxBy(comparingDouble(Model::getScore))));

        for (Gene gene : genes) {

            PhivePriorityResult phiveScore = geneModelPhenotypeMatches.getOrDefault(gene.getEntrezGeneID(), Optional.empty())
                    .map(makeModelPhivePriorityResult())
                    //should this *really* be set to 0.6? The rankings are quite different to hiPhive because of this - HiPhive uses 0 if there are no models.
                    //n.b. this ranks genes with no model higher than those with a model with a score of zero.
                    .orElse(new PhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), NO_MOUSE_MODEL_SCORE, null, null));

            gene.addPriorityResult(phiveScore);
        }
        logger.info("Finished {}", PRIORITY_TYPE);
    }

    private void logTheoreticalModel(TheoreticalModel theoreticalModel) {
        logger.info("Best {} phenotype matches:", theoreticalModel.getOrganism());
        for (PhenotypeMatch bestMatch : theoreticalModel.getBestPhenotypeMatches()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        logger.info("bestMaxScore={} bestAvgScore={}", theoreticalModel.getMaxMatchScore(), theoreticalModel.getBestAvgScore());
    }

    private Function<Model, PhivePriorityResult> makeModelPhivePriorityResult() {
        return model -> {
            GeneModel bestModelForGene = (GeneModel) model;
            return new PhivePriorityResult(bestModelForGene.getEntrezGeneId(), bestModelForGene.getHumanGeneSymbol(), bestModelForGene.getScore(), bestModelForGene.getModelGeneId(), bestModelForGene.getModelGeneSymbol());
        };
    }

    private List<Model> getAndScoreModels(TheoreticalModel bestTheoreticalModel, OrganismPhenotypeMatches organismPhenotypeMatches) {
        Organism organism = organismPhenotypeMatches.getOrganism();
        List<Model> models = priorityService.getModelsForOrganism(organism);

        logger.info("Scoring {} models", organism);
        Instant timeStart = Instant.now();

        int hpIdsWithPhenotypeMatch = (int) bestTheoreticalModel.getBestPhenotypeMatches().stream()
                .map(PhenotypeMatch::getQueryPhenotypeId)
                .count();
        //running this in parallel here can cut the overall time for this method in half or better - ~650ms -> ~350ms on Pfeiffer test set.
        models.parallelStream().forEach(model -> {
            List<PhenotypeMatch> bestForwardAndBackwardMatches = organismPhenotypeMatches.getBestForwardAndReciprocalMatches(model.getPhenotypeIds());

            //Remember the model needs to collect its best matches from the forward and backward best matches otherwise the modelMaxMatchScore will be zero.
            bestForwardAndBackwardMatches.forEach(model::addMatchIfAbsentOrBetterThanCurrent);
            double modelMaxMatchScore = model.getBestPhenotypeMatchForTerms().values().stream()
                    .mapToDouble(PhenotypeMatch::getScore)
                    .max()
                    .orElse(0);

            double modelBestAvgScore = calculateModelBestAvgScore(hpIdsWithPhenotypeMatch, bestForwardAndBackwardMatches);

            double modelScore = bestTheoreticalModel.compare(modelMaxMatchScore, modelBestAvgScore);
            model.setScore(modelScore);
        });

        Duration duration = Duration.between(timeStart, Instant.now());
        logger.info("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());
        return models;
    }

    //n.b. this is *almost* identical to HiPhivePriority.calculateModelBestAvgScore(), apart from the comment
    private double calculateModelBestAvgScore(int numMatchedQueryPhenotypes, List<PhenotypeMatch> bestForwardAndBackwardMatches) {
        double sumBestForwardAndBackwardMatchScores = bestForwardAndBackwardMatches.stream().mapToDouble(PhenotypeMatch::getScore).sum();
        long numMatchedModelPhenotypes = (int) bestForwardAndBackwardMatches.stream().map(PhenotypeMatch::getMatchPhenotypeId).distinct().count();

        //in hiPhive we use humanMousePhenotypeMatches.getQueryTerms().size() i.e. hpoIds.size() - these are probably always going to be the same.
        //Shouldn't hpIdsWithPhenotypeMatch actually be bestForwardAndBackwardMatches.parallelStream().map(PhenotypeMatch::getQueryPhenotypeId).distinct().count(); ?
        int totalPhenotypesWithMatch = numMatchedQueryPhenotypes + (int) numMatchedModelPhenotypes;

        return sumBestForwardAndBackwardMatchScores / totalPhenotypesWithMatch;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.hpoIds);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhivePriority other = (PhivePriority) obj;
        if (!Objects.equals(this.hpoIds, other.hpoIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhivePriority{" + "hpoIds=" + hpoIds + '}';
    }

}
