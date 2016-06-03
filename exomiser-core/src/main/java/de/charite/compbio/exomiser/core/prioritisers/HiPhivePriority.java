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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.OrganismPhenotypeMatches;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import org.jblas.FloatMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

    private final List<String> hpoIds;
    private final HiPhiveOptions options;
    private final DataMatrix randomWalkMatrix;

    private PriorityService priorityService;

    private List<Integer> highQualityPhenoMatchedGenes = new ArrayList<>();


    private Map<Integer, Double> geneScores = new HashMap<>();

    private double bestMaxScore = 0d;
    private double bestAvgScore = 0d;

    /**
     *
     * @param hpoIds
     * @param options
     * @param randomWalkMatrix
     */
    public HiPhivePriority(List<String> hpoIds, HiPhiveOptions options, DataMatrix randomWalkMatrix) {
        this.hpoIds = hpoIds;
        this.options = options;

        this.randomWalkMatrix = randomWalkMatrix;
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

        //TODO: this is repetitive, surely there must be a better way to deal with these, perhaps a GeneModelMatrix class?
        final Map<Integer, Model> bestDiseaseModelForGene = makeHpToHumanMatches(options.runHuman(), hpoPhenotypeTerms, Organism.HUMAN);
        final Map<Integer, Model> bestMouseModelForGene = makeHpToOtherSpeciesMatches(options.runMouse(), hpoPhenotypeTerms, Organism.MOUSE);
        final Map<Integer, Model> bestFishModelForGene = makeHpToOtherSpeciesMatches(options.runFish(), hpoPhenotypeTerms, Organism.FISH);

        FloatMatrix weightedHighQualityMatrix = FloatMatrix.EMPTY;
        if (options.runPpi()) {
            weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(highQualityPhenoMatchedGenes, geneScores);
        }

        logger.info("Prioritising genes...");
        for (Gene gene : genes) {
            HiPhivePriorityResult priorityResult = makePriorityResultForGene(gene, hpoPhenotypeTerms, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene, weightedHighQualityMatrix);
            gene.addPriorityResult(priorityResult);
        }

    }

    private HiPhivePriorityResult makePriorityResultForGene(Gene gene, List<PhenotypeTerm> hpoPhenotypeTerms, final Map<Integer, Model> bestDiseaseModelForGene, final Map<Integer, Model> bestMouseModelForGene, final Map<Integer, Model> bestFishModelForGene, FloatMatrix weightedHighQualityMatrix) {

        Integer entrezGeneId = gene.getEntrezGeneID();
        List<Model> bestPhenotypeMatchModels = getBestPhenotypeMatchesForGene(entrezGeneId, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
        double score = 0;
        for (Model model : bestPhenotypeMatchModels) {
            score = Math.max(score, model.getScore());
        }
        List<Model> closestPhysicallyInteractingGeneModels = new ArrayList<>();
        double walkerScore = 0d;
        if (options.runPpi() && randomWalkMatrix.containsGene(entrezGeneId) && !highQualityPhenoMatchedGenes.isEmpty()) {
            Integer columnIndex = getColumnIndexOfMostPhenotypicallySimilarGene(gene, highQualityPhenoMatchedGenes, weightedHighQualityMatrix);
            Integer rowIndex = randomWalkMatrix.getRowIndexForGene(entrezGeneId);
            walkerScore = weightedHighQualityMatrix.get(rowIndex, columnIndex);
            // optimal adjustment based on benchmarking to allow walker scores to compete with low phenotype scores
            walkerScore = 0.5 +  walkerScore;
            score = Math.max(score, walkerScore);
            if (walkerScore <= 0.00001) {
                walkerScore = 0d;
            } else {
                Integer closestGeneId = highQualityPhenoMatchedGenes.get(columnIndex);
                closestPhysicallyInteractingGeneModels = getBestPhenotypeMatchesForGene(closestGeneId, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
            }
        }
        logger.debug("Making result for {} {} score={}", gene.getGeneSymbol(), entrezGeneId, score);
        return new HiPhivePriorityResult(gene.getGeneSymbol(), score, hpoPhenotypeTerms, bestPhenotypeMatchModels, closestPhysicallyInteractingGeneModels, walkerScore, matchesCandidateGene(gene));
    }

    private boolean matchesCandidateGene(Gene gene) {
        //new Jannovar labelling can have multiple genes per var but first one is most pathogenic- we'll take this one.
        return options.getCandidateGeneSymbol().equals(gene.getGeneSymbol()) || gene.getGeneSymbol().startsWith(options.getCandidateGeneSymbol() + ",");
    }
    
    private List<Model> getBestPhenotypeMatchesForGene(Integer entrezGeneId, final Map<Integer, Model> bestDiseaseModelForGene, final Map<Integer, Model> bestMouseModelForGene, final Map<Integer, Model> bestFishModelForGene) {
        List<Model> bestPhenotypeMatchModels = new ArrayList<>();
        if (bestDiseaseModelForGene.containsKey(entrezGeneId)) {
            Model bestDiseseModel = bestDiseaseModelForGene.get(entrezGeneId);
            bestPhenotypeMatchModels.add(bestDiseseModel);
        }
        if (bestMouseModelForGene.containsKey(entrezGeneId)) {
            Model bestMouseModel = bestMouseModelForGene.get(entrezGeneId);
            bestPhenotypeMatchModels.add(bestMouseModel);
        }
        if (bestFishModelForGene.containsKey(entrezGeneId)) {
            Model bestFishModel = bestFishModelForGene.get(entrezGeneId);
            bestPhenotypeMatchModels.add(bestFishModel);
        }
        return bestPhenotypeMatchModels;
    }

    private String makeStatsMessage(List<Gene> genes) {
        int numGenesWithPhenotypeOrPpiData = 0;
        for (Gene gene : genes) {
            HiPhivePriorityResult priorityResult = (HiPhivePriorityResult) gene.getPriorityResult(PRIORITY_TYPE);
            if (priorityResult.getWalkerScore() > 0 || priorityResult.getHumanScore() > 0 || priorityResult.getMouseScore() > 0 || priorityResult.getFishScore() > 0) {
                numGenesWithPhenotypeOrPpiData++;
            }
        }
        int totalGenes = genes.size();
        return String.format("Phenotype and Protein-Protein Interaction evidence was available for %d of %d genes (%.1f%%)",
                numGenesWithPhenotypeOrPpiData, totalGenes, 100f * (numGenesWithPhenotypeOrPpiData / (float) totalGenes));
    }

    private Map<Integer, Model> makeHpToHumanMatches(boolean runHuman, List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
        //TODO: this must always run in order that the best score is set 
        // Human
        OrganismPhenotypeMatches humanPhenotypeMatches = getMatchingPhenotypesForSpecies(queryHpoPhenotypes, species);
        Set<PhenotypeMatch> bestMatches = humanPhenotypeMatches.getBestPhenotypeMatches();

        calculateBestScoresFromHumanPhenotypes(bestMatches, queryHpoPhenotypes);

        if (runHuman) {
            return calculateBestGeneModelPhenotypeMatchForSpecies(humanPhenotypeMatches);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, Model> makeHpToOtherSpeciesMatches(boolean runSpecies, List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
        if (runSpecies) {
            OrganismPhenotypeMatches mousePhenotypeMatches = getMatchingPhenotypesForSpecies(queryHpoPhenotypes, species);
            return calculateBestGeneModelPhenotypeMatchForSpecies(mousePhenotypeMatches);
        } else {
            return Collections.emptyMap();
        }
    }

    private OrganismPhenotypeMatches getMatchingPhenotypesForSpecies(List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
        logger.info("Fetching HUMAN-{} phenotype matches...", species);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            //todo: make this immutable
            Set<PhenotypeMatch> termMatches = priorityService.getSpeciesMatchesForHpoTerm(hpoTerm, species);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new OrganismPhenotypeMatches(species, ImmutableMap.copyOf(speciesPhenotypeMatches));
    }

//    class BestScores
    /**
     * This method only works for same species matches e.g. HPO-HPO or MPO-MPO
     * matches as it makes the assumption that the best matches are self-hits.
     * DO NOT USE THIS FOR MIXED SPECIES HITS AS THE SCORES WILL BE WRONG.
     *
     * @param bestMatches
     */
    //TODO: return a DTO for the scores here in order to keep things in local scope
    private void calculateBestScoresFromHumanPhenotypes(Collection<PhenotypeMatch> bestMatches, List<PhenotypeTerm> queryHpoPhenotypes) {
        //this is the original algorithm for mixes species matches using hashes instead of PhenotypeMatches:

//         // calculate perfect model scores for human
//         // loop over each hp id should start here
//        for (String hpId : hpIdsWithPhenotypeMatch) {
//            if (bestMappedTermScore.containsKey(hpId)) {
//                double hpScore = bestMappedTermScore.get(hpId);
//                logger.info("Best forwardHit {}={}", hpId, hpScore);
//                // add in scores for best match for the HP term
//                sumBestScore += hpScore;
//
//                bestMaxScore = Math.max(hpScore, bestMaxScore);
//                //logger.info("ADDING SCORE FOR " + hpid + " TO " + bestMappedTermMpId.get(hpid) + " WITH SCORE " + hpScore + ", SUM NOW " + sumBestScore + ", MAX NOW " + bestMaxScore);
//                // add in MP-HP hits
//                String mpId = bestMappedTermMpId.get(hpId);
//                double bestScore = 0d;
//                String bestReciprocalHit = "";
//                for (String hpId2 : hpIdsWithPhenotypeMatch) {
//                    String hashKey = hpId2 + mpId;
//                    if (speciesPhenotypeMatches.containsKey(hashKey) && speciesPhenotypeMatches.get(hashKey).getScore() > bestScore) {
//                        bestScore = speciesPhenotypeMatches.get(hashKey).getScore();
//                        bestReciprocalHit = hashKey;
//                    }
//                }
//                logger.info("Best reciprocalHit {}={}", bestReciprocalHit, bestScore);
//                // add in scores for best match for the MP term
//                sumBestScore += bestScore;
//                //logger.info("ADDING RECIPROCAL SCORE FOR " + mpid + " WITH SCORE " + bestScore + ", SUM NOW " + sumBestScore + ", MAX NOW " + bestMaxScore);
//                bestMaxScore = Math.max(hpScore, bestMaxScore);
//            }
//        }
        double sumBestScore = 0d;
        for (PhenotypeMatch bestMatch : bestMatches) {
            double matchScore = bestMatch.getScore();
            bestMaxScore = Math.max(matchScore, bestMaxScore);
            sumBestScore += matchScore;
        }
        if (bestMatches.size() > 0){// otherwise get a NaN value that escalates to other scores and eventually throws an exception
            //bestAvgScore = sumBestScore / bestMatches.size();
            // Aug2015 - average across all HPOs to be consistent with change in calculateBestGeneModelPhenotypeMatchForSpecies
            bestAvgScore = sumBestScore / queryHpoPhenotypes.size();
        }
        //input set: 
        //HP:0010055-HP:0010055=2.805085560382805
        //HP:0001363-HP:0001363=2.4418464446906243
        //HP:0001156-HP:0001156=2.048321278502726
        //HP:0011304-HP:0011304=2.749831974791806
        //bestMaxScore=2.805085560382805 bestAvgScore=2.5112713145919905 sumBestScore=10.045085258367962 hpIdsWithPhenotypeMatch=4
        logger.info("bestMaxScore={} bestAvgScore={} sumBestScore={} numBestMatches={}", bestMaxScore, bestAvgScore, sumBestScore, queryHpoPhenotypes.size());
    }

    private Map<Integer, Model> calculateBestGeneModelPhenotypeMatchForSpecies(OrganismPhenotypeMatches organismPhenotypeMatches) {
        Organism organism = organismPhenotypeMatches.getOrganism();

        List<Model> diseaseGeneModels = priorityService.getModelsForOrganism(organism);

        Map<Integer, Set<Model>> geneModelPhenotypeMatches = calculateGeneModelPhenotypeMatches(organismPhenotypeMatches, diseaseGeneModels);

        return makeBestGeneModelForGenes(geneModelPhenotypeMatches);
    }

    private Map<Integer, Set<Model>> calculateGeneModelPhenotypeMatches(OrganismPhenotypeMatches organismPhenotypeMatches, List<Model> diseaseGeneModels) {
        Organism organism = organismPhenotypeMatches.getOrganism();
        // calculate best phenotype matches and scores for all genes
        //Integer = EntrezGeneId, String = GeneModelId

        //hpId
        Set<String> hpIdsWithPhenotypeMatch = organismPhenotypeMatches.getBestPhenotypeMatches().stream().map(PhenotypeMatch::getQueryPhenotypeId).collect(Collectors.toCollection(TreeSet::new));
        logger.info("hpIdsWithPhenotypeMatch={}", hpIdsWithPhenotypeMatch);

        //hpId, mpId, zpId
        Map<String, PhenotypeMatch> speciesPhenotypeMatches = organismPhenotypeMatches.getCompoundKeyIndexedPhenotypeMatches();
        Set<String> matchedPhenotypeIdsForSpecies = speciesPhenotypeMatches.values().stream().map(PhenotypeMatch::getMatchPhenotypeId).collect(Collectors.toCollection(TreeSet::new));
        logger.info("matchedPhenotypeIdsForspecies {}={}", organism, matchedPhenotypeIdsForSpecies.size());

        Map<Integer, Set<Model>> geneModelPhenotypeMatches = new HashMap<>();
        for (Model model : diseaseGeneModels) {

            List<String> matchedPhenotypeIdsForModel = new ArrayList<>();
            for (String mpid : model.getPhenotypeIds()) {
                if (matchedPhenotypeIdsForSpecies.contains(mpid)) {
                    matchedPhenotypeIdsForModel.add(mpid);
                }
            }

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
    private Map<Integer, Model> makeBestGeneModelForGenes(Map<Integer, Set<Model>> geneModelPhenotypeMatches) {
        Map<Integer, Model> bestGeneModelForGenes = new HashMap<>();

        for (Entry<Integer, Set<Model>> entry : geneModelPhenotypeMatches.entrySet()) {
            Integer entrezId = entry.getKey();
            
            for (Model model : entry.getValue()) {
                double score = model.getScore();

                // catch hit to known disease-gene association for purposes of benchmarking i.e to simulate novel gene discovery performance
                if (options.isBenchmarkingEnabled() && options.isBenchmarkHit(model)) {
                    logger.info("Found benchmarking hit {}:{} - skipping model", options.getDiseaseId(), options.getCandidateGeneSymbol());
                } else {
                    // normal behaviour when not trying to exclude candidate gene to simulate novel gene disovery in benchmarking
                    // only build PPI network for high qual hits
                    if (score > 0.6) {
                        logger.debug("Adding high quality score for {} score={}", model.getHumanGeneSymbol(), model.getScore());
                        //TODO: this is a bit round-the-houses as it's used in getColumnIndexOfMostPhenotypicallySimilarGene() would probably
                        //be better using a LinkedHashMap to join these two values together, or use a GeneIdentifier class....?
                        highQualityPhenoMatchedGenes.add(entrezId);
                        //TODO: geneScores is only ever queried using highQualityPhenoMatchedGenes so the two can be merged so we have the highest score from all models
                        //linked to a gene id. This is used to find the weighted high-quality matrix.
                    }
                    //why use this? Just return the high-quality gene-model matches and iterate through the fuckers.
                    //also used by makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes 
                    if (!geneScores.containsKey(entrezId) || score > geneScores.get(entrezId)) {
                        geneScores.put(entrezId, score);
                    }

                    if (!bestGeneModelForGenes.containsKey(entrezId)) {
                        logger.debug("Adding new model for {} score={} to bestGeneModels", model.getHumanGeneSymbol(), model.getScore());
                        bestGeneModelForGenes.put(entrezId, model);
                    } else if (bestGeneModelForGenes.get(entrezId).getScore() < model.getScore()) {
                        logger.debug("Updating best model for {} score={}", model.getHumanGeneSymbol(), model.getScore());
                        bestGeneModelForGenes.put(entrezId, model);
                    }
                }
            }
        }
        return bestGeneModelForGenes;
    }

    //todo: If this returned a DataMatrix things might be a bit more convienent later on... 
    //TODO: does this have to be a list and a map? Can't it just be a map of scores?
    private FloatMatrix makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(List<Integer> highQualityPhenoMatchedGenes, Map<Integer, Double> geneScores) {
        logger.info("Making weighted-score Protein-Protein interaction sub-matrix from high quality phenotypic gene matches...");
        int rows = randomWalkMatrix.getMatrix().getRows();
        int cols = highQualityPhenoMatchedGenes.size();
        FloatMatrix highQualityPpiMatrix = FloatMatrix.zeros(rows, cols);
        int c = 0;
        for (Integer seedGeneEntrezId : highQualityPhenoMatchedGenes) {
            if (randomWalkMatrix.containsGene(seedGeneEntrezId)) {
                FloatMatrix column = randomWalkMatrix.getColumnMatrixForGene(seedGeneEntrezId);
                // weight column by phenoScore 
                Double score = geneScores.get(seedGeneEntrezId);
                column = column.mul(score.floatValue());
                highQualityPpiMatrix.putColumn(c, column);
            }
            c++;
        }
        return highQualityPpiMatrix;
    }

    /**
     * This function retrieves the random walk similarity score for the gene
     *
     * @param gene for which the random walk score is to be retrieved
     */
    private int getColumnIndexOfMostPhenotypicallySimilarGene(Gene gene, List<Integer> phenotypicallySimilarGeneIds, FloatMatrix weightedHighQualityMatrix) {
        int geneIndex = randomWalkMatrix.getRowIndexForGene(gene.getEntrezGeneID());
        int columnIndex = 0;
        double bestScore = 0;
        int bestHitIndex = 0;
        for (Integer similarGeneEntrezId : phenotypicallySimilarGeneIds) {
            if (!randomWalkMatrix.containsGene(similarGeneEntrezId)) {
                columnIndex++;
                continue;
            } else if (similarGeneEntrezId == gene.getEntrezGeneID()) {
                //avoid self-hits now are testing genes with direct pheno-evidence as well
                columnIndex++;
                continue;
            } else {
                double cellScore = weightedHighQualityMatrix.get(geneIndex, columnIndex);
                if (cellScore > bestScore) {
                    bestScore = cellScore;
                    bestHitIndex = columnIndex;
                }
                columnIndex++;
            }
        }
        return bestHitIndex;
    }

    public void setPriorityService(PriorityService priorityService) {
        this.priorityService = priorityService;
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
