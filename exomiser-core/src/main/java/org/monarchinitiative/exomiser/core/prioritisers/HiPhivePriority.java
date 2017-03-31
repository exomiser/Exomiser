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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.HiPhiveProteinInteractionScorer;
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
 * Filter genes according phenotypic similarity and to the random walk proximity
 * in the protein-protein interaction network.
 *
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(HiPhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.HIPHIVE_PRIORITY;
    private static final double HIGH_QUALITY_SCORE_CUTOFF = 0.6;

    private final HiPhiveOptions options;
    private final DataMatrix randomWalkMatrix;
    private final PriorityService priorityService;

    //TODO: make local
    private final List<String> hpoIds;

    /**
     * @param hpoIds
     * @param options
     * @param randomWalkMatrix
     */
    public HiPhivePriority(List<String> hpoIds, HiPhiveOptions options, DataMatrix randomWalkMatrix, PriorityService priorityService) {
        this.hpoIds = hpoIds;
        this.options = options;

        this.randomWalkMatrix = randomWalkMatrix;
        this.priorityService = priorityService;
    }

    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    @Override
    public Stream<HiPhivePriorityResult> prioritise(List<Gene> genes) {
        if (options.isBenchmarkingEnabled()) {
            logger.info("Running in benchmarking mode for disease: {} and candidateGene: {}", options.getDiseaseId(), options.getCandidateGeneSymbol());
        }
        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        Set<Integer> wantedGeneIds = genes.stream().map(Gene::getEntrezGeneID).collect(ImmutableSet.toImmutableSet());

        ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = makeBestGeneModelsForOrganisms(hpoPhenotypeTerms, Organism.HUMAN, options
                .getOrganismsToRun(), wantedGeneIds);

        HiPhiveProteinInteractionScorer ppiScorer = makeHiPhiveProteinInteractionScorer(bestGeneModels, options.runPpi());

        logger.info("Mapping results...");
        return genes.stream().map(makeHiPhivePriorityResult(hpoPhenotypeTerms, bestGeneModels, ppiScorer));
    }

    private Function<Gene, HiPhivePriorityResult> makeHiPhivePriorityResult(List<PhenotypeTerm> hpoPhenotypeTerms, ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels, HiPhiveProteinInteractionScorer ppiScorer) {
        return gene -> {
            Integer entrezGeneId = gene.getEntrezGeneID();

            String geneSymbol = gene.getGeneSymbol();

            List<GeneModelPhenotypeMatch> bestPhenotypeMatchModels = bestGeneModels.get(entrezGeneId);
            double phenoScore = bestPhenotypeMatchModels.stream()
                    .mapToDouble(GeneModelPhenotypeMatch::getScore)
                    .max()
                    .orElse(0);

            GeneMatch closestPhenoMatchInNetwork = ppiScorer.getClosestPhenoMatchInNetwork(entrezGeneId);
            List<GeneModelPhenotypeMatch> closestPhysicallyInteractingGeneModels = closestPhenoMatchInNetwork.getBestMatchModels();
            double ppiScore = closestPhenoMatchInNetwork.getScore();

            double score = Double.max(phenoScore, ppiScore);

            logger.debug("Making result for {} {} score={} phenoScore={} walkerScore={}", geneSymbol, entrezGeneId, score, phenoScore, ppiScore);
            return new HiPhivePriorityResult(entrezGeneId, geneSymbol, score, hpoPhenotypeTerms, bestPhenotypeMatchModels, closestPhysicallyInteractingGeneModels, ppiScore, matchesCandidateGeneSymbol(geneSymbol));
        };
    }

    private boolean matchesCandidateGeneSymbol(String geneSymbol) {
        //new Jannovar labelling can have multiple genes per var but first one is most pathogenic- we'll take this one.
        return options.getCandidateGeneSymbol().equals(geneSymbol) || geneSymbol.startsWith(options.getCandidateGeneSymbol() + ",");
    }

    private HiPhiveProteinInteractionScorer makeHiPhiveProteinInteractionScorer(ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels, boolean runPpi) {
        if (runPpi) {
            return new HiPhiveProteinInteractionScorer(randomWalkMatrix, bestGeneModels, HIGH_QUALITY_SCORE_CUTOFF);
        }
        return HiPhiveProteinInteractionScorer.EMPTY;
    }

    private ListMultimap<Integer, GeneModelPhenotypeMatch> makeBestGeneModelsForOrganisms(List<PhenotypeTerm> hpoPhenotypeTerms, Organism referenceOrganism, Set<Organism> organismsToCompare, Set<Integer> wantedGeneIds) {

        //CAUTION!! this must always run in order that the best score is set - HUMAN runs first as we are comparing HP to other phenotype ontology terms.
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(hpoPhenotypeTerms, referenceOrganism);
        QueryPhenotypeMatch bestQueryPhenotypeMatch = referenceOrganismPhenotypeMatcher.getQueryPhenotypeMatch();
        if (bestQueryPhenotypeMatch.getBestPhenotypeMatches().isEmpty()) {
            logger.warn("{} has no phenotype matches for input set {}", bestQueryPhenotypeMatch, hpoPhenotypeTerms);
        }
        List<PhenotypeMatcher> bestOrganismPhenotypeMatches = getBestOrganismPhenotypeMatches(hpoPhenotypeTerms, referenceOrganismPhenotypeMatcher, organismsToCompare);

        ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = ArrayListMultimap.create();
        for (PhenotypeMatcher organismPhenotypeMatcher : bestOrganismPhenotypeMatches) {
            Set<GeneModel> modelsToScore = priorityService.getModelsForOrganism(organismPhenotypeMatcher.getOrganism())
                    .stream()
                    .filter(model -> wantedGeneIds.contains(model.getEntrezGeneId()))
                    .collect(toSet());

            List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = scoreModels(bestQueryPhenotypeMatch, organismPhenotypeMatcher, modelsToScore);
            Map<Integer, GeneModelPhenotypeMatch> bestGeneModelsForOrganism = mapBestModelByGene(geneModelPhenotypeMatches);
            bestGeneModelsForOrganism.entrySet().forEach(entry -> bestGeneModels.put(entry.getKey(), entry.getValue()));
        }

        return bestGeneModels;
    }

    private List<PhenotypeMatcher> getBestOrganismPhenotypeMatches(List<PhenotypeTerm> hpoPhenotypeTerms, PhenotypeMatcher referenceOrganismPhenotypeMatcher, Set<Organism> organismsToCompare) {
        ImmutableList.Builder<PhenotypeMatcher> bestPossibleOrganismPhenotypeMatches = ImmutableList.builder();
        for (Organism organism : organismsToCompare) {
            if (organism == referenceOrganismPhenotypeMatcher.getOrganism()) {
                //no need to re-query the database for these
                bestPossibleOrganismPhenotypeMatches.add(referenceOrganismPhenotypeMatcher);
            } else {
                PhenotypeMatcher bestOrganismPhenotypes = priorityService.getPhenotypeMatcherForOrganism(hpoPhenotypeTerms, organism);
                bestPossibleOrganismPhenotypeMatches.add(bestOrganismPhenotypes);
            }
        }
        return bestPossibleOrganismPhenotypeMatches.build();
    }

    //returns a map of geneId to best model
    private Map<Integer, GeneModelPhenotypeMatch> mapBestModelByGene(List<GeneModelPhenotypeMatch> organismModels) {
        return getBestModelsByGene(organismModels)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toMap(GeneModelPhenotypeMatch::getEntrezGeneId, Function.identity()));
    }

    private Stream<Optional<GeneModelPhenotypeMatch>> getBestModelsByGene(List<GeneModelPhenotypeMatch> organismModels) {
        if (options.isBenchmarkingEnabled()) {
            return organismModels.parallelStream()
                    .filter(model -> model.getScore() > 0)
                    // catch hit to known disease-gene association for purposes of benchmarking i.e to simulate novel gene discovery performance
                    .filter(model -> !options.isBenchmarkHit(model))
                    .collect(groupingByConcurrent(GeneModelPhenotypeMatch::getEntrezGeneId, maxBy(comparingDouble(GeneModelPhenotypeMatch::getScore))))
                    .values()
                    .stream();
        }
        return organismModels.parallelStream()
                .filter(model -> model.getScore() > 0)
                .collect(groupingByConcurrent(GeneModelPhenotypeMatch::getEntrezGeneId, maxBy(comparingDouble(GeneModelPhenotypeMatch::getScore))))
                .values()
                .stream();
    }

    // n.b. this is *almost* identical to PhivePriority.scoreModels() the only difference is in HiPhive we're comparing the input terms
    // against all possible models (disease, mouse, fish), whereas in Phive we're only comparing against mouse.
    // For HiPhive the bestQueryPhenotypeMatch is going to be an HPO self-hit for every term in the query set so the
    // scoreModelPhenotypeMatch uses hpoIds.size() as the numMatchedQueryPhenotypes.
    private List<GeneModelPhenotypeMatch> scoreModels(QueryPhenotypeMatch bestQueryPhenotypeMatch, PhenotypeMatcher organismPhenotypeMatcher, Collection<GeneModel> models) {
        Organism organism = organismPhenotypeMatcher.getOrganism();

        ModelScorer modelScorer = PhenodigmModelScorer.forMultiCrossSpecies(bestQueryPhenotypeMatch, organismPhenotypeMatcher);

        logger.info("Scoring {} models", organism);
        Instant timeStart = Instant.now();
        //running this in parallel here can cut the overall time for this method in half or better - ~650ms -> ~350ms on Pfeiffer test set.
        List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = models.parallelStream()
                .map(model -> {
                    ModelPhenotypeMatch score = modelScorer.scoreModel(model);
                    return new GeneModelPhenotypeMatch(score.getScore(), model, score.getBestPhenotypeMatches());
                })
                .collect(toList());

        Duration duration = Duration.between(timeStart, Instant.now());
        logger.info("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());
        return geneModelPhenotypeMatches;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.randomWalkMatrix);
        hash = 73 * hash + Objects.hashCode(this.hpoIds);
        hash = 73 * hash + Objects.hashCode(this.options);
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
        final HiPhivePriority other = (HiPhivePriority) obj;
        if (!Objects.equals(this.randomWalkMatrix, other.randomWalkMatrix)) {
            return false;
        }
        if (!Objects.equals(this.hpoIds, other.hpoIds)) {
            return false;
        }
        return Objects.equals(this.options, other.options);
    }

    @Override
    public String toString() {
        return "HiPhivePriority{"
                + "hpoIds=" + hpoIds
                + ", options=" + options
                + '}';
    }

}
