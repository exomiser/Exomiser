package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.exomiser.core.model.Organism;
import java.util.*;
import java.util.Map.Entry;
import org.jblas.FloatMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter genes according phenotypic similarity and to the random walk proximity
 * in the protein-protein interaction network.
 *
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(HiPhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.HI_PHIVE_PRIORITY;

    private final List<String> hpoIds;
    private final HiPhiveOptions options;
    private final DataMatrix randomWalkMatrix;

    private PriorityService priorityService;
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private final List<String> messages = new ArrayList<>();

    private List<Integer> highQualityPhenoMatchedGenes = new ArrayList<>();


    private Map<Integer, Double> geneScores = new HashMap<>();

    private double bestMaxScore = 0d;
    private double bestAvgScore = 0d;


    /**
     * This is the matrix of similarities between the seeed genes and all genes
     * in the network, i.e., p<sub>infinity</sub>.
     */
    private FloatMatrix weightedHighQualityMatrix = new FloatMatrix();

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

//        FloatMatrix weightedHighQualityMatrix = new FloatMatrix();
        if (options.runPpi()) {
            //TODO: make this local if possible
            weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(highQualityPhenoMatchedGenes, geneScores);
        }

        logger.info("Prioritising genes...");
        for (Gene gene : genes) {
            HiPhivePriorityResult priorityResult = makePriorityResultForGene(gene, hpoPhenotypeTerms, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
            gene.addPriorityResult(priorityResult);
        }
        /*
         * refactor all scores for genes that are not direct pheno-hits but in
         * PPI with them to a linear range
         */
        logger.info("Adjusting gene scores for non-pheno hits with protein-protein interactions");
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<>();
        for (Gene g : genes) {
            Float geneScore = ((HiPhivePriorityResult) g.getPriorityResult(PriorityType.HI_PHIVE_PRIORITY)).getWalkerScore();
            if (geneScore == 0f) {
                continue;
            }
            if (geneScoreMap.containsKey(geneScore)) {
                List<Gene> geneScoreGeneList = geneScoreMap.get(geneScore);
                geneScoreGeneList.add(g);
            } else {
                List<Gene> geneScoreGeneList = new ArrayList<>();
                geneScoreGeneList.add(g);
                geneScoreMap.put(geneScore, geneScoreGeneList);
            }
        }
        //changed so when have only 2 genes say in filtered set 1st one will get 0.6 and second 0.3 rather than 0.3 and 0
        float rank = 0;
        for (Float score : geneScoreMap.descendingKeySet()) {
            List<Gene> geneScoreGeneList = geneScoreMap.get(score);
            int sharedHits = geneScoreGeneList.size();
            float adjustedRank = rank;
            if (sharedHits > 1) {
                adjustedRank = rank + (sharedHits / 2);
            }
            float newScore = 0.6f - 0.6f * (adjustedRank / genes.size());
            rank = rank + sharedHits;
            for (Gene gene : geneScoreGeneList) {
                //i.e. only overwrite phenotype-based score if PPI score is larger
                HiPhivePriorityResult result = (HiPhivePriorityResult) gene.getPriorityResult(PriorityType.HI_PHIVE_PRIORITY);
                if (newScore > result.getScore()) {
                    result.setScore(newScore);
                }
            }
        }
        String message = makeStatsMessage(genes);
        messages.add(message);
    }

    private HiPhivePriorityResult makePriorityResultForGene(Gene gene, List<PhenotypeTerm> hpoPhenotypeTerms, final Map<Integer, Model> bestDiseaseModelForGene, final Map<Integer, Model> bestMouseModelForGene, final Map<Integer, Model> bestFishModelForGene) {

        Integer entrezGeneId = gene.getEntrezGeneID();
        List<Model> bestPhenotypeMatchModels = getBestPhenotypeMatchesForGene(entrezGeneId, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
        double score = 0;
        for (Model model : bestPhenotypeMatchModels) {
            score = Math.max(score, model.getScore());
        }
        List<Model> closestPhysicallyInteractingGeneModels = new ArrayList<>();
        double walkerScore = 0d;
        if (options.runPpi() && randomWalkMatrix.containsGene(entrezGeneId) && !highQualityPhenoMatchedGenes.isEmpty()) {
            Integer columnIndex = getColumnIndexOfMostPhenotypicallySimilarGene(gene, highQualityPhenoMatchedGenes);
            Integer rowIndex = randomWalkMatrix.getRowIndexForGene(entrezGeneId);
            walkerScore = weightedHighQualityMatrix.get(rowIndex, columnIndex);
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
        logger.info("Fetching HUMAN-{} phenotype matches...", species);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> humanPhenotypeMatches = getMatchingPhenotypesForSpecies(queryHpoPhenotypes, species);
        Set<PhenotypeMatch> bestMatches = getBestMatchesForQueryTerms(humanPhenotypeMatches);

        calculateBestScoresFromHumanPhenotypes(bestMatches);

        if (runHuman) {
            return runDynamicQuery(bestMatches, humanPhenotypeMatches, species);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, Model> makeHpToOtherSpeciesMatches(boolean runSpecies, List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
        if (runSpecies) {
            logger.info("Fetching HUMAN-{} phenotype matches...", species);
            Map<PhenotypeTerm, Set<PhenotypeMatch>> mousePhenotypeMatches = getMatchingPhenotypesForSpecies(queryHpoPhenotypes, species);
            Set<PhenotypeMatch> bestMatches = getBestMatchesForQueryTerms(mousePhenotypeMatches);
            return runDynamicQuery(bestMatches, mousePhenotypeMatches, species);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<PhenotypeTerm, Set<PhenotypeMatch>> getMatchingPhenotypesForSpecies(List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = priorityService.getSpeciesMatchesForHpoTerm(hpoTerm, species);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return speciesPhenotypeMatches;
    }

    private Set<PhenotypeMatch> getBestMatchesForQueryTerms(Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches) {
        Map<PhenotypeTerm, PhenotypeMatch> bestMatches = new HashMap<>();

        for (Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : allPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            for (PhenotypeMatch match : entry.getValue()) {
                double score = match.getScore();
                if (bestMatches.containsKey(queryTerm)) {
                    if (score > bestMatches.get(queryTerm).getScore()) {
                        bestMatches.put(queryTerm, match);
                    }
                } else {
                    bestMatches.put(queryTerm, match);
                }
            }
        }
        for (PhenotypeMatch bestMatch : bestMatches.values()) {
            logger.debug("Best match: {}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        return new HashSet<>(bestMatches.values());
    }

    /**
     * This method only works for same species matches e.g. HPO-HPO or MPO-MPO
     * matches as it makes the assumption that the best matches are self-hits.
     * DO NOT USE THIS FOR MIXED SPECIES HITS AS THE SCORES WILL BE WRONG.
     *
     * @param bestMatches
     */
    private void calculateBestScoresFromHumanPhenotypes(Collection<PhenotypeMatch> bestMatches) {
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
        bestAvgScore = sumBestScore / bestMatches.size();
        //input set: 
        //HP:0010055-HP:0010055=2.805085560382805
        //HP:0001363-HP:0001363=2.4418464446906243
        //HP:0001156-HP:0001156=2.048321278502726
        //HP:0011304-HP:0011304=2.749831974791806
        //bestMaxScore=2.805085560382805 bestAvgScore=2.5112713145919905 sumBestScore=10.045085258367962 hpIdsWithPhenotypeMatch=4
        logger.info("bestMaxScore={} bestAvgScore={} sumBestScore={} numBestMatches={}", bestMaxScore, bestAvgScore, sumBestScore, bestMatches.size());
    }

    private Map<Integer, Model> runDynamicQuery(Set<PhenotypeMatch> bestMatches, Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches, Organism species) {

        //TODO: take speciesPhenotypeMatches as input argument for runDynamicQuery
        //'hpId + mpId' : phenotypeMatch
        Map<String, PhenotypeMatch> speciesPhenotypeMatches = new HashMap<>();

        for (Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : allPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            String hpId = queryTerm.getId();
            for (PhenotypeMatch match : entry.getValue()) {
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                String mpId = matchTerm.getId();
                String matchIds = hpId + mpId;
                speciesPhenotypeMatches.put(matchIds, match);
            }
        }

        return calculateBestGeneModelPhenotypeMatchForSpecies(species, bestMatches, speciesPhenotypeMatches);
    }

    private Map<Integer, Model> calculateBestGeneModelPhenotypeMatchForSpecies(Organism organism, Set<PhenotypeMatch> bestMatches, Map<String, PhenotypeMatch> speciesPhenotypeMatches) {
        // calculate best phenotype matches and scores for all genes
        //Integer = EntrezGeneId, String = GeneModelId

        //hpId
        Set<String> hpIdsWithPhenotypeMatch = new TreeSet<>();
        for (PhenotypeMatch match : bestMatches) {
            hpIdsWithPhenotypeMatch.add(match.getQueryPhenotypeId());
        }
        logger.info("hpIdsWithPhenotypeMatch={}", hpIdsWithPhenotypeMatch);

        Set<String> matchedPhenotypeIdsForSpecies = new TreeSet<>();
        for (PhenotypeMatch match : speciesPhenotypeMatches.values()) {
            matchedPhenotypeIdsForSpecies.add(match.getMatchPhenotypeId());
        }
        logger.info("matchedPhenotypeIdsForspecies {}={}", organism, matchedPhenotypeIdsForSpecies.size());

        Map<Integer, Set<Model>> geneModelPhenotypeMatches = new HashMap<>();
        List<Model> diseaseGeneModels = priorityService.getModelsForOrganism(organism);
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

            int rowColumnCount = hpIdsWithPhenotypeMatch.size() + matchedPhenotypeIdsForModel.size();
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
        return makeBestGeneModelForGenes(geneModelPhenotypeMatches);
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
    private int getColumnIndexOfMostPhenotypicallySimilarGene(Gene gene, List<Integer> phenotypicallySimilarGeneIds) {
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

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
     */
    @Override
    public List<String> getMessages() {
        return messages;
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

    
//    @Override
//    public String toString() {
//        return getPriorityType().getCommandLineValue() + ", hpoIds=" + hpoIds;
//    }


    @Override
    public String toString() {
        return "HiPhivePriority{" +
                "hpoIds=" + hpoIds +
                ", options=" + options +
                '}';
    }
}
