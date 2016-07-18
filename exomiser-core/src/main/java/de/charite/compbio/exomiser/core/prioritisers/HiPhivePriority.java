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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

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
     *
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

    /**
     * Prioritize a list of candidate genes. These candidate genes may have rare, potentially pathogenic variants.
     * <P>
     *
     * @param genes List of candidate genes.
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {
        if (options.isBenchmarkingEnabled()) {
            logger.info("Running in benchmarking mode for disease: {} and candidateGene: {}", options.getDiseaseId(), options.getCandidateGeneSymbol());
        }
        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        ListMultimap<Integer, Model> bestGeneModels = makeBestGeneModelsForOrganisms(hpoPhenotypeTerms, Organism.HUMAN, options.getOrganismsToRun());

        // catch hit to known disease-gene association for purposes of benchmarking i.e to simulate novel gene discovery performance
        if (options.isBenchmarkingEnabled()) {
            removeKnownGeneDiseaseAssociationModel(bestGeneModels);
        }

        HiPhiveProteinInteractionScorer ppiScorer = HiPhiveProteinInteractionScorer.EMPTY;
        if (options.runPpi()) {
            ppiScorer = new HiPhiveProteinInteractionScorer(randomWalkMatrix, bestGeneModels, HIGH_QUALITY_SCORE_CUTOFF);
        }

        logger.info("Prioritising genes...");
        for (Gene gene : genes) {
            Integer entrezGeneId = gene.getEntrezGeneID();

            List<Model> bestPhenotypeMatchModels = bestGeneModels.get(entrezGeneId);
            double phenoScore = bestPhenotypeMatchModels.stream().mapToDouble(Model::getScore).max().orElse(0);

            GeneMatch closestPhenoMatchInNetwork = ppiScorer.getClosestPhenoMatchInNetwork(entrezGeneId);
            List<Model> closestPhysicallyInteractingGeneModels = closestPhenoMatchInNetwork.getBestMatchModels();
            double walkerScore = closestPhenoMatchInNetwork.getScore();

            double score = Double.max(phenoScore, walkerScore);
            logger.debug("Making result for {} {} score={} phenoScore={} walkerScore={}", gene.getGeneSymbol(), entrezGeneId, score, phenoScore, walkerScore);
            HiPhivePriorityResult priorityResult = new HiPhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score, hpoPhenotypeTerms, bestPhenotypeMatchModels, closestPhysicallyInteractingGeneModels, walkerScore, matchesCandidateGeneSymbol(gene));
            gene.addPriorityResult(priorityResult);
        }
    }

    private void removeKnownGeneDiseaseAssociationModel(ListMultimap<Integer, Model> bestGeneModels) {
        Iterator<Model> bestModelsIterator = bestGeneModels.values().iterator();
        while(bestModelsIterator.hasNext()){
            Model model = bestModelsIterator.next();
            if (options.isBenchmarkHit(model)) {
                bestModelsIterator.remove();
                logger.info("Found benchmarking hit {}-{} - removing model {}", options.getDiseaseId(), options.getCandidateGeneSymbol(), model);
            }
        }
//      There are issues here with removing the model from the list - doing this in a serial stream always causes a ConcurrentModificationException.
//      The parallelStream works OK, but is this just luck????
//        bestGeneModels.values().parallelStream()
//                .filter(options::isBenchmarkHit)
//                .forEach(model -> {
//                    bestGeneModels.remove(model.getEntrezGeneId(), model);
//                    logger.info("Found benchmarking hit {}-{} - removing model {}", options.getDiseaseId(), options.getCandidateGeneSymbol(), model);
//                });
    }

    private boolean matchesCandidateGeneSymbol(Gene gene) {
        //new Jannovar labelling can have multiple genes per var but first one is most pathogenic- we'll take this one.
        return options.getCandidateGeneSymbol().equals(gene.getGeneSymbol()) || gene.getGeneSymbol().startsWith(options.getCandidateGeneSymbol() + ",");
    }

    private ListMultimap<Integer, Model> makeBestGeneModelsForOrganisms(List<PhenotypeTerm> hpoPhenotypeTerms, Organism referenceOrganism, Set<Organism> organismsToCompare) {

        //CAUTION!! this must always run in order that the best score is set - HUMAN runs first as we are comparing HP to other phenotype ontology terms.
        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, referenceOrganism);
        TheoreticalModel bestTheoreticalModel = referenceOrganismPhenotypeMatches.getBestTheoreticalModel();

        List<OrganismPhenotypeMatches> bestOrganismPhenotypeMatches = getBestOrganismPhenotypeMatches(hpoPhenotypeTerms, referenceOrganismPhenotypeMatches, organismsToCompare);

        ListMultimap<Integer, Model> bestGeneModels = ArrayListMultimap.create();
        for (OrganismPhenotypeMatches organismPhenotypeMatches : bestOrganismPhenotypeMatches) {
            List<Model> organismModels = getAndScoreModels(bestTheoreticalModel, organismPhenotypeMatches);
            Map<Integer, Model> bestGeneModelsForOrganism = mapBestModelByGene(organismModels);
            bestGeneModelsForOrganism.entrySet().stream().forEach(entry -> bestGeneModels.put(entry.getKey(), entry.getValue()));
        }

        return bestGeneModels;
    }

    private List<OrganismPhenotypeMatches> getBestOrganismPhenotypeMatches(List<PhenotypeTerm> hpoPhenotypeTerms, OrganismPhenotypeMatches referenceOrganismPhenotypeMatches, Set<Organism> organismsToCompare) {
        ImmutableList.Builder<OrganismPhenotypeMatches> bestPossibleOrganismPhenotypeMatches = ImmutableList.builder();
        logTheoreticalModel(referenceOrganismPhenotypeMatches.getBestTheoreticalModel());
        for (Organism organism : organismsToCompare) {
            if  (organism == referenceOrganismPhenotypeMatches.getOrganism()) {
                //no need to re-query the database for these
                bestPossibleOrganismPhenotypeMatches.add(referenceOrganismPhenotypeMatches);
            }
            else {
                OrganismPhenotypeMatches bestOrganismPhenotypes = priorityService.getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, organism);
                bestPossibleOrganismPhenotypeMatches.add(bestOrganismPhenotypes);
                logTheoreticalModel(bestOrganismPhenotypes.getBestTheoreticalModel());
            }
        }
        return bestPossibleOrganismPhenotypeMatches.build();
    }

    private void logTheoreticalModel(TheoreticalModel theoreticalModel) {
        logger.info("Best {} phenotype matches:", theoreticalModel.getOrganism());
        for (PhenotypeMatch bestMatch : theoreticalModel.getBestPhenotypeMatches()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        logger.info("bestMaxScore={} bestAvgScore={}", theoreticalModel.getMaxMatchScore(), theoreticalModel.getBestAvgScore());
    }

    //returns a map of geneId to best model
    private Map<Integer, Model> mapBestModelByGene(List<Model> organismModels) {

        Map<Integer, Optional<Model>> geneModelPhenotypeMatches = organismModels.parallelStream()
                .filter(model -> model.getScore() > 0)
                .collect(groupingByConcurrent(Model::getEntrezGeneId, maxBy(comparingDouble(Model::getScore))));

        return geneModelPhenotypeMatches.values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toMap(Model::getEntrezGeneId, Function.identity()));
    }

    //n.b. this is *almost* identical to PhivePriority.getAndScoreModels()
    private List<Model> getAndScoreModels(TheoreticalModel bestTheoreticalModel, OrganismPhenotypeMatches organismPhenotypeMatches) {
        Organism organism = organismPhenotypeMatches.getOrganism();
        List<Model> models = priorityService.getModelsForOrganism(organism);
        logger.info("organismPhenotypeMatches {}={}", organism, organismPhenotypeMatches.getTermPhenotypeMatches().size());

        logger.info("Scoring {} models", organism);
        Instant timeStart = Instant.now();

        //running this in parallel here can cut the overall time for this method in half or better - ~650ms -> ~350ms on Pfeiffer test set.
        models.parallelStream().forEach(model -> {
            List<PhenotypeMatch> bestForwardAndBackwardMatches = organismPhenotypeMatches.getBestForwardAndReciprocalMatches(model.getPhenotypeIds());

            //Remember the model needs to collect its best matches from the forward and backward best matches otherwise the modelMaxMatchScore will be zero.
            bestForwardAndBackwardMatches.forEach(match -> model.addMatchIfAbsentOrBetterThanCurrent(match));
            double modelMaxMatchScore = model.getBestPhenotypeMatchForTerms().values().stream()
                    .mapToDouble(PhenotypeMatch::getScore)
                    .max()
                    .orElse(0);
            /**
             * hpIdsWithPhenotypeMatch.size() = no. of HPO disease annotations for human and the no. of annotations with an entry in hp_*_mappings table for other species
             * matchedPhenotypeIDsForModel.size() = no. of annotations for model with a match in hp_*_mappings table for at least one of the disease annotations
             * Aug 2015 - changed calculation to take into account all HPO terms for averaging after DDD benchmarking - keeps consistent across species then
             *i.e.
             *   pre-Aug 2015: int rowColumnCount = hpIdsWithPhenotypeMatch.size() + matchedPhenotypeIdsForModel.size();
             *  post-Aug 2015: int rowColumnCount = hpoIds.size() + matchedPhenotypeIdsForModel.size();
             */
            double modelBestAvgScore = calculateModelBestAvgScore(hpoIds.size(), bestForwardAndBackwardMatches);

            double modelScore = bestTheoreticalModel.compare(modelMaxMatchScore, modelBestAvgScore);
            model.setScore(modelScore);
        });

        Duration duration = Duration.between(timeStart, Instant.now());
        logger.info("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());
        return models;
    }

    //n.b. this is *almost* identical to PhivePriority.calculateModelBestAvgScore(), apart from the comment
    private double calculateModelBestAvgScore(int numQueryPhenotypes, List<PhenotypeMatch> bestForwardAndBackwardMatches) {
        double sumBestForwardAndBackwardMatchScores = bestForwardAndBackwardMatches.stream().mapToDouble(PhenotypeMatch::getScore).sum();
        long numMatchedModelPhenotypes = (int) bestForwardAndBackwardMatches.stream().map(PhenotypeMatch::getMatchPhenotypeId).distinct().count();

        //in hiPhive we use humanMousePhenotypeMatches.getQueryTerms().size() i.e. hpoIds.size() - these are probably always going to be the same.
        //Shouldn't hpIdsWithPhenotypeMatch actually be bestForwardAndBackwardMatches.parallelStream().map(PhenotypeMatch::getQueryPhenotypeId).distinct().count(); ?
        int totalPhenotypesWithMatch = numQueryPhenotypes + (int) numMatchedModelPhenotypes;

        return sumBestForwardAndBackwardMatchScores / totalPhenotypesWithMatch;
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
        if (!Objects.equals(this.options, other.options)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HiPhivePriority{"
                + "hpoIds=" + hpoIds
                + ", options=" + options
                + '}';
    }

}
