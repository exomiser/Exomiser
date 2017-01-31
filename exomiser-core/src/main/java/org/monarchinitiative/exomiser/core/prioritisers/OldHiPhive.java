package org.monarchinitiative.exomiser.core.prioritisers;

import org.jblas.FloatMatrix;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OldHiPhive {
    private static final Logger logger = LoggerFactory.getLogger(HiPhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.HIPHIVE_PRIORITY;

    private final List<String> hpoIds;
    private final HiPhiveOptions options;
    private final DataMatrix randomWalkMatrix;
    private final PriorityService priorityService;

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
    public OldHiPhive(List<String> hpoIds, HiPhiveOptions options, DataMatrix randomWalkMatrix, PriorityService priorityService) {
        this.hpoIds = hpoIds;
        this.options = options;

        this.randomWalkMatrix = randomWalkMatrix;
        this.priorityService = priorityService;
    }

//    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    /**
     * Prioritize a list of candidate genes. These candidate genes may have rare, potentially pathogenic variants.
     * <P>
     *
     * @param genes List of candidate genes.
     */
//    @Override
    public List<HiPhivePriorityResult> prioritizeGenes(List<Gene> genes) {
        if (options.isBenchmarkingEnabled()) {
            logger.info("Running in benchmarking mode for disease: {} and candidateGene: {}", options.getDiseaseId(), options.getCandidateGeneSymbol());
        }
        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        //TODO: this is repetitive, surely there must be a better way to deal with these, perhaps a GeneModelMatrix class?
        final Map<Integer, ModelPhenotypeMatch> bestDiseaseModelForGene = makeHpToHumanMatches(options.runHuman(), hpoPhenotypeTerms, Organism.HUMAN);
        final Map<Integer, ModelPhenotypeMatch> bestMouseModelForGene = makeHpToOtherSpeciesMatches(options.runMouse(), hpoPhenotypeTerms, Organism.MOUSE);
        final Map<Integer, ModelPhenotypeMatch> bestFishModelForGene = makeHpToOtherSpeciesMatches(options.runFish(), hpoPhenotypeTerms, Organism.FISH);

//        FloatMatrix weightedHighQualityMatrix = new FloatMatrix();
        if (options.runPpi()) {
            //TODO: make this local if possible
            weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(highQualityPhenoMatchedGenes, geneScores);
        }

        List<HiPhivePriorityResult> results = new ArrayList<>(genes.size());
        logger.info("Prioritising genes...");
        for (Gene gene : genes) {
            HiPhivePriorityResult priorityResult = makePriorityResultForGene(gene, hpoPhenotypeTerms, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
            results.add(priorityResult);
        }

        return results;
    }

    private HiPhivePriorityResult makePriorityResultForGene(Gene gene, List<PhenotypeTerm> hpoPhenotypeTerms, final Map<Integer, ModelPhenotypeMatch> bestDiseaseModelForGene, final Map<Integer, ModelPhenotypeMatch> bestMouseModelForGene, final Map<Integer, ModelPhenotypeMatch> bestFishModelForGene) {
        Integer entrezGeneId = gene.getEntrezGeneID();
//        logger.info("Checking scores for {} {}", gene.getGeneSymbol(), entrezGeneId);

        List<ModelPhenotypeMatch> bestPhenotypeMatchModels = getBestPhenotypeMatchesForGene(entrezGeneId, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
        double phenoScore = 0;
        for (ModelPhenotypeMatch model : bestPhenotypeMatchModels) {
            phenoScore = Math.max(phenoScore, model.getScore());
        }

        List<ModelPhenotypeMatch> closestPhysicallyInteractingGeneModels = new ArrayList<>();
        double walkerScore = 0d;
        if (options.runPpi() && randomWalkMatrix.containsGene(entrezGeneId) && !highQualityPhenoMatchedGenes.isEmpty()) {
            Integer columnIndex = getColumnIndexOfMostPhenotypicallySimilarGene(gene, highQualityPhenoMatchedGenes);
            Integer rowIndex = randomWalkMatrix.getRowIndexForGene(entrezGeneId);
            walkerScore = weightedHighQualityMatrix.get(rowIndex, columnIndex);
            // optimal adjustment based on benchmarking to allow walker scores to compete with low phenotype scores
            walkerScore = 0.5 +  walkerScore;
//            score = Math.max(score, walkerScore);
            if (walkerScore <= 0.00001) {
                walkerScore = 0d;
            } else {
                Integer closestGeneId = highQualityPhenoMatchedGenes.get(columnIndex);
                closestPhysicallyInteractingGeneModels = getBestPhenotypeMatchesForGene(closestGeneId, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
            }
        }
        double score = Math.max(phenoScore, walkerScore);

//        logger.info("Making result for {} {} score={}", gene.getGeneSymbol(), entrezGeneId, score);
        return new HiPhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score, hpoPhenotypeTerms, bestPhenotypeMatchModels, closestPhysicallyInteractingGeneModels, walkerScore, matchesCandidateGene(gene));
//        return new HiPhivePriorityResult(gene.getGeneSymbol(), score, hpoPhenotypeTerms, bestPhenotypeMatchModels, closestPhysicallyInteractingGeneModels, walkerScore, matchesCandidateGene(gene));
    }

    private boolean matchesCandidateGene(Gene gene) {
        //new Jannovar labelling can have multiple genes per var but first one is most pathogenic- we'll take this one.
        return options.getCandidateGeneSymbol().equals(gene.getGeneSymbol()) || gene.getGeneSymbol().startsWith(options.getCandidateGeneSymbol() + ",");
    }

    private List<ModelPhenotypeMatch> getBestPhenotypeMatchesForGene(Integer entrezGeneId, final Map<Integer, ModelPhenotypeMatch> bestDiseaseModelForGene, final Map<Integer, ModelPhenotypeMatch> bestMouseModelForGene, final Map<Integer, ModelPhenotypeMatch> bestFishModelForGene) {
        List<ModelPhenotypeMatch> bestPhenotypeMatchModels = new ArrayList<>();
        if (bestDiseaseModelForGene.containsKey(entrezGeneId)) {
            ModelPhenotypeMatch bestDiseseModel = bestDiseaseModelForGene.get(entrezGeneId);
            bestPhenotypeMatchModels.add(bestDiseseModel);
        }
        if (bestMouseModelForGene.containsKey(entrezGeneId)) {
            ModelPhenotypeMatch bestMouseModel = bestMouseModelForGene.get(entrezGeneId);
            bestPhenotypeMatchModels.add(bestMouseModel);
        }
        if (bestFishModelForGene.containsKey(entrezGeneId)) {
            ModelPhenotypeMatch bestFishModel = bestFishModelForGene.get(entrezGeneId);
            bestPhenotypeMatchModels.add(bestFishModel);
        }
        return bestPhenotypeMatchModels;
    }

    private String makeStatsMessage(List<Gene> genes) {
        int numGenesWithPhenotypeOrPpiData = 0;
        for (Gene gene : genes) {
            HiPhivePriorityResult priorityResult = (HiPhivePriorityResult) gene.getPriorityResult(PRIORITY_TYPE);
            if (priorityResult.getPpiScore() > 0 || priorityResult.getHumanScore() > 0 || priorityResult.getMouseScore() > 0 || priorityResult.getFishScore() > 0) {
                numGenesWithPhenotypeOrPpiData++;
            }
        }
        int totalGenes = genes.size();
        return String.format("Phenotype and Protein-Protein Interaction evidence was available for %d of %d genes (%.1f%%)",
                numGenesWithPhenotypeOrPpiData, totalGenes, 100f * (numGenesWithPhenotypeOrPpiData / (float) totalGenes));
    }

    private Map<Integer, ModelPhenotypeMatch> makeHpToHumanMatches(boolean runHuman, List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
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

    private Map<Integer, ModelPhenotypeMatch> makeHpToOtherSpeciesMatches(boolean runSpecies, List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
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
        double sumBestScore = 0d;
        for (PhenotypeMatch bestMatch : bestMatches) {
            double matchScore = bestMatch.getScore();
            bestMaxScore = Math.max(matchScore, bestMaxScore);
            sumBestScore += matchScore;
        }
        if (!bestMatches.isEmpty()){// otherwise get a NaN value that escalates to other scores and eventually throws an exception
            //bestAvgScore = sumBestScore / bestMatches.size();
            // Aug2015 - average across all HPOs to be consistent with change in calculateBestGeneModelPhenotypeMatchForSpecies
            bestAvgScore = sumBestScore / hpoIds.size();
        }
        //input set:
        //HP:0010055-HP:0010055=2.805085560382805
        //HP:0001363-HP:0001363=2.4418464446906243
        //HP:0001156-HP:0001156=2.048321278502726
        //HP:0011304-HP:0011304=2.749831974791806
        //bestMaxScore=2.805085560382805 bestAvgScore=2.5112713145919905 sumBestScore=10.045085258367962 hpIdsWithPhenotypeMatch=4
        logger.info("bestMaxScore={} bestAvgScore={} sumBestScore={} numBestMatches={}", bestMaxScore, bestAvgScore, sumBestScore, bestMatches.size());
    }

    private Map<Integer, ModelPhenotypeMatch> runDynamicQuery(Set<PhenotypeMatch> bestMatches, Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches, Organism species) {

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

    private Map<Integer, ModelPhenotypeMatch> calculateBestGeneModelPhenotypeMatchForSpecies(Organism organism, Set<PhenotypeMatch> bestMatches, Map<String, PhenotypeMatch> speciesPhenotypeMatches) {
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

        logger.info("Scoring {} models", organism);
        Instant timeStart = Instant.now();

        Map<Integer, Set<ModelPhenotypeMatch>> geneModelPhenotypeMatches = new HashMap<>();
        List<Model> models = priorityService.getModelsForOrganism(organism);
        for (Model model : models) {
            final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms = new LinkedHashMap<>();

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
                            addMatchIfAbsentOrBetterThanCurrent(match, bestPhenotypeMatchForTerms);
//                            addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, model, match);
                        }
                    }
                }
                if (bestMatchScore > 0) {
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
                            addMatchIfAbsentOrBetterThanCurrent(match, bestPhenotypeMatchForTerms);
//                            addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, model, match);
                        }
                    }
                }
                if (bestMatchScore > 0) {
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
            int rowColumnCount = hpoIds.size() + matchedPhenotypeIdsForModel.size();
            // calculate combined score
            if (sumModelBestMatchScores > 0) {
                double avgBestHitRowsColumnsScore = sumModelBestMatchScores / rowColumnCount;
                double combinedScore = 50 * (maxModelMatchScore / bestMaxScore + avgBestHitRowsColumnsScore / bestAvgScore);
                if (combinedScore > 100) {
                    combinedScore = 100;
                }
                double score = combinedScore / 100;
//                model.setScore(score);
                ModelPhenotypeMatch match = new ModelPhenotypeMatch(score, model, new ArrayList<>(bestPhenotypeMatchForTerms.values()));
//                logger.info("Scored model {}", match);
                addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, match);
            }
        }
        Duration duration = Duration.between(timeStart, Instant.now());
        logger.info("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());

        return makeBestGeneModelForGenes(geneModelPhenotypeMatches);
    }

    public void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match, Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms) {
        PhenotypeTerm matchQueryTerm = match.getQueryPhenotype();
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm)) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        } else if (bestPhenotypeMatchForTerms.get(matchQueryTerm).getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        }
    }

    /**
     * Remember this is just for a single species, so there can be a single best
     * mode per gene. Otherwise a gene has a best model for each organism.
     * Perhaps the GeneModel should contain it's species too.
     */
    private Map<Integer, ModelPhenotypeMatch> makeBestGeneModelForGenes(Map<Integer, Set<ModelPhenotypeMatch>> geneModelPhenotypeMatches) {
        Map<Integer, ModelPhenotypeMatch> bestGeneModelForGenes = new HashMap<>();

        for (Entry<Integer, Set<ModelPhenotypeMatch>> entry : geneModelPhenotypeMatches.entrySet()) {
            Integer entrezId = entry.getKey();

            for (ModelPhenotypeMatch model : entry.getValue()) {
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

    private void addGeneModelPhenotypeMatch(Map<Integer, Set<ModelPhenotypeMatch>> geneModelPhenotypeMatches, ModelPhenotypeMatch match) {
        int entrezId = match.getEntrezGeneId();
        if (!geneModelPhenotypeMatches.containsKey(entrezId)) {
            Set<ModelPhenotypeMatch> geneModels = new HashSet<>();
            geneModels.add(match);
            geneModelPhenotypeMatches.put(entrezId, geneModels);
        } else if (!geneModelPhenotypeMatches.get(entrezId).contains(match)) {
            geneModelPhenotypeMatches.get(entrezId).add(match);
        }
    }

    //GeneId - modelId - hpId: PhenotypeMatch
    private void addGeneModelPhenotypeMatch(Map<Integer, Set<Model>> geneModelPhenotypeMatches, Model model, PhenotypeMatch match) {

//        model.addMatchIfAbsentOrBetterThanCurrent(match);

        String geneSymbol = model.getHumanGeneSymbol();
        int entrezId = model.getEntrezGeneId();
        String modelId = model.getId();
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
        final OldHiPhive other = (OldHiPhive) obj;
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
        return "OldHiPhive{"
                + "hpoIds=" + hpoIds
                + ", options=" + options
                + '}';
    }
}
