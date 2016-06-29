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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
        bestGeneModels.values().stream()
                .filter(options::isBenchmarkHit)
                .forEach(model -> {
                    bestGeneModels.remove(model.getEntrezGeneId(), model);
                    logger.info("Found benchmarking hit {}-{} - removing model {}", options.getDiseaseId(), options.getCandidateGeneSymbol(), model);
                });
    }

    private boolean matchesCandidateGeneSymbol(Gene gene) {
        //new Jannovar labelling can have multiple genes per var but first one is most pathogenic- we'll take this one.
        return options.getCandidateGeneSymbol().equals(gene.getGeneSymbol()) || gene.getGeneSymbol().startsWith(options.getCandidateGeneSymbol() + ",");
    }

    private ListMultimap<Integer, Model> makeBestGeneModelsForOrganisms(List<PhenotypeTerm> hpoPhenotypeTerms, Organism referenceOrganism, Set<Organism> organismsToCompare) {

        //CAUTION!! this must always run in order that the best score is set - HUMAN runs first as we are comparing HP to other phenotype ontology terms.
        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, referenceOrganism);
        logOrganismPhenotypeMatches(referenceOrganismPhenotypeMatches);

        List<OrganismPhenotypeMatches> bestOrganismPhenotypeMatches = getBestOrganismPhenotypeMatches(hpoPhenotypeTerms, referenceOrganismPhenotypeMatches, organismsToCompare);

        double bestMaxScore = referenceOrganismPhenotypeMatches.getBestMatchScore();
        double bestAvgScore = referenceOrganismPhenotypeMatches.getBestAverageScore();

        ListMultimap<Integer, Model> bestGeneModels = ArrayListMultimap.create();
        for (OrganismPhenotypeMatches organismPhenotypeMatches : bestOrganismPhenotypeMatches) {
            List<Model> organismModels = priorityService.getModelsForOrganism(organismPhenotypeMatches.getOrganism());
            Map<Integer, Set<Model>> geneModelPhenotypeMatches = calculateGeneModelPhenotypeMatches(bestMaxScore, bestAvgScore, organismPhenotypeMatches, organismModels);
            Map<Integer, Model> bestGeneModelsForOrganism = getBestGeneModelForGenes(geneModelPhenotypeMatches);
            bestGeneModelsForOrganism.entrySet().stream().forEach(entry -> bestGeneModels.put(entry.getKey(), entry.getValue()));
        }

        return bestGeneModels;
    }

    private OrganismPhenotypeMatches getMatchingPhenotypesForOrganism(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        logger.info("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = priorityService.getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new OrganismPhenotypeMatches(organism, ImmutableMap.copyOf(speciesPhenotypeMatches));
    }

    private List<OrganismPhenotypeMatches> getBestOrganismPhenotypeMatches(List<PhenotypeTerm> hpoPhenotypeTerms, OrganismPhenotypeMatches referenceOrganismPhenotypeMatches, Set<Organism> organismsToCompare) {
        ImmutableList.Builder<OrganismPhenotypeMatches> bestPossibleOrganismPhenotypeMatches = ImmutableList.builder();
        for (Organism organism : organismsToCompare) {
            if  (organism == referenceOrganismPhenotypeMatches.getOrganism()) {
                //no need to re-query the database for these
                bestPossibleOrganismPhenotypeMatches.add(referenceOrganismPhenotypeMatches);
            }
            else {
                OrganismPhenotypeMatches bestOrganismPhenotypes = getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, organism);
                bestPossibleOrganismPhenotypeMatches.add(bestOrganismPhenotypes);
                logOrganismPhenotypeMatches(bestOrganismPhenotypes);
            }
        }
        return bestPossibleOrganismPhenotypeMatches.build();
    }

    private void logOrganismPhenotypeMatches(OrganismPhenotypeMatches organismPhenotypeMatches) {
        logger.info("Best {} phenotype matches:", organismPhenotypeMatches.getOrganism());
        for (PhenotypeMatch bestMatch : organismPhenotypeMatches.getBestPhenotypeMatches()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        logger.info("bestMaxScore={} bestAvgScore={}", organismPhenotypeMatches.getBestMatchScore(), organismPhenotypeMatches.getBestAverageScore(), organismPhenotypeMatches.getBestPhenotypeMatches().size());
    }

    private Map<Integer, Set<Model>> calculateGeneModelPhenotypeMatches(double bestMaxScore, double bestAvgScore, OrganismPhenotypeMatches organismPhenotypeMatches, List<Model> organismModels) {
        // calculate best phenotype matches and scores for all genes
        //Integer = EntrezGeneId, String = GeneModelId

        //hpId
        Set<String> hpIdsWithPhenotypeMatch = organismPhenotypeMatches.getBestPhenotypeMatches().stream().map(PhenotypeMatch::getQueryPhenotypeId).collect(Collectors.toCollection(TreeSet::new));
        logger.info("hpIds with phenotype match={}", hpIdsWithPhenotypeMatch);

        //hpId, mpId, zpId
        Map<String, PhenotypeMatch> speciesPhenotypeMatches = organismPhenotypeMatches.getCompoundKeyIndexedPhenotypeMatches();
        //TODO: can probably get this directly from organismPhenotypeMatches.getTermPhenotypeMatches().values().stream().map(PhenotypeMatch::getMatchPhenotypeId).collect(Collectors.toCollection(TreeSet::new));
        Set<String> matchedPhenotypeIdsForSpecies = speciesPhenotypeMatches.values().stream().map(PhenotypeMatch::getMatchPhenotypeId).collect(Collectors.toCollection(TreeSet::new));
        logger.info("matchedPhenotypeIdsForOrganism {}={}", organismPhenotypeMatches.getOrganism(), matchedPhenotypeIdsForSpecies.size());

        Map<Integer, Set<Model>> geneModelPhenotypeMatches = new HashMap<>();
        for (Model model : organismModels) {

            List<String> matchedPhenotypeIdsForModel = model.getPhenotypeIds().stream()
                    .filter(matchedPhenotypeIdsForSpecies::contains)
                    .collect(toList());

            double maxModelMatchScore = 0d;
            double sumModelBestMatchScores = 0d;

            for (String hpId : hpIdsWithPhenotypeMatch) {
                double bestMatchScore = 0d;
                for (String mpId : matchedPhenotypeIdsForModel) {
                    String matchIds = hpId + mpId;
                    if (speciesPhenotypeMatches.containsKey(matchIds)) {
                        PhenotypeMatch match = speciesPhenotypeMatches.get(matchIds);
                        double matchScore = match.getScore();
                        // identify best match
                        bestMatchScore = Math.max(matchScore, bestMatchScore);
                        if (matchScore > 0) {
                            addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, model, match);
                        }
                    }
                }
                if (bestMatchScore != 0) {
                    sumModelBestMatchScores += bestMatchScore;
                    maxModelMatchScore = Math.max(bestMatchScore, maxModelMatchScore);
                }
            }
            // Reciprocal hits
            for (String mpId : matchedPhenotypeIdsForModel) {
                double bestMatchScore = 0f;
                for (String hpId : hpIdsWithPhenotypeMatch) {
                    String matchIds = hpId + mpId;
                    if (speciesPhenotypeMatches.containsKey(matchIds)) {
                        PhenotypeMatch match = speciesPhenotypeMatches.get(matchIds);
                        double matchScore = match.getScore();
                        // identify best match
                        bestMatchScore = Math.max(matchScore, bestMatchScore);
                        if (matchScore > 0) {
                            addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, model, match);
                        }
                    }
                }
                if (bestMatchScore != 0) {
                    sumModelBestMatchScores += bestMatchScore;
                    maxModelMatchScore = Math.max(bestMatchScore, maxModelMatchScore);
                }
            }
            /**
             * hpIdsWithPhenotypeMatch.size() = no. of HPO disease annotations for human and the no. of annotations with an entry in hp_*_mappings table for other species
             * matchedPhenotypeIDsForModel.size() = no. of annotations for model with a match in hp_*_mappings table for at least one of the disease annotations
             * Aug 2015 - changed calculation to take into account all HPO terms for averaging after DDD benchmarking - keeps consistent across species then
             */
            //int rowColumnCount = hpIdsWithPhenotypeMatch.size() + matchedPhenotypeIdsForModel.size();
            int rowColumnCount = organismPhenotypeMatches.getQueryTerms().size() + matchedPhenotypeIdsForModel.size();
            // calculate combined score
            if (sumModelBestMatchScores != 0) {
                double avgBestHitRowsColumnsScore = sumModelBestMatchScores / rowColumnCount;
                double combinedScore = 50 * (maxModelMatchScore / bestMaxScore + avgBestHitRowsColumnsScore / bestAvgScore);
                if (combinedScore > 100) {
                    combinedScore = 100;
                }
                double score = combinedScore / 100;
                model.setScore(score);
            }
        }
        return geneModelPhenotypeMatches;
    }

    //GeneId - modelId - hpId: PhenotypeMatch
    private void addGeneModelPhenotypeMatch(Map<Integer, Set<Model>> geneModelPhenotypeMatches, Model model, PhenotypeMatch match) {

        model.addMatchIfAbsentOrBetterThanCurrent(match);

        String geneSymbol = model.getHumanGeneSymbol();
        int entrezId = model.getEntrezGeneId();
        String modelId = model.getModelId();
        if (!geneModelPhenotypeMatches.containsKey(entrezId)) {
            logger.debug("Adding match for new gene {} (ENTREZ:{}) modelId {} ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore());
            Set<Model> geneModels = new HashSet<>();
            geneModels.add(model);
            geneModelPhenotypeMatches.put(entrezId, geneModels);
        } else if (!geneModelPhenotypeMatches.get(entrezId).contains(model)) {
            logger.debug("Adding match for gene {} (ENTREZ:{}) new modelId {} ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore());
            geneModelPhenotypeMatches.get(entrezId).add(model);
        }
    }

    /**
     * Remember this is just for a single species, so there can be a single best
     * mode per gene. Otherwise a gene has a best model for each organism.
     * Perhaps the GeneModel should contain it's species too.
     */
    private Map<Integer, Model> getBestGeneModelForGenes(Map<Integer, Set<Model>> geneModelPhenotypeMatches) {
        Map<Integer, Model> bestGeneModelForGenes = new HashMap<>();

        for (Entry<Integer, Set<Model>> entry : geneModelPhenotypeMatches.entrySet()) {
            Integer entrezId = entry.getKey();
            for (Model model : entry.getValue()) {
                if (!bestGeneModelForGenes.containsKey(entrezId)) {
                    logger.debug("Adding new model for {} score={} to bestGeneModels", model.getHumanGeneSymbol(), model.getScore());
                    bestGeneModelForGenes.put(entrezId, model);
                } else if (bestGeneModelForGenes.get(entrezId).getScore() < model.getScore()) {
                    logger.debug("Updating best model for {} score={}", model.getHumanGeneSymbol(), model.getScore());
                    bestGeneModelForGenes.put(entrezId, model);
                }
            }
        }
        return bestGeneModelForGenes;
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
