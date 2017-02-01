package org.monarchinitiative.exomiser.core.prioritisers;

import org.jblas.FloatMatrix;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.OrganismPhenotypeMatches;
import org.monarchinitiative.exomiser.core.prioritisers.util.PriorityService;
import org.monarchinitiative.exomiser.core.prioritisers.util.TheoreticalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        OrganismPhenotypeMatches referenceOrganismPhenotypeMatches = priorityService.getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, Organism.HUMAN);
        TheoreticalModel bestTheoreticalModel = referenceOrganismPhenotypeMatches.getBestTheoreticalModel();
        logTheoreticalModel(bestTheoreticalModel);
        bestMaxScore = bestTheoreticalModel.getMaxMatchScore();
        bestAvgScore = bestTheoreticalModel.getBestAvgScore();

        //TODO: this is repetitive, surely there must be a better way to deal with these, perhaps a GeneModelMatrix class?
        final Map<Integer, ModelPhenotypeMatch> bestDiseaseModelForGene = makeHpToHumanMatches(options.runHuman(), referenceOrganismPhenotypeMatches);
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

    private Map<Integer, ModelPhenotypeMatch> makeHpToHumanMatches(boolean runHuman, OrganismPhenotypeMatches referenceOrganismPhenotypeMatches) {
        if (runHuman) {
            Map<PhenotypeTerm, Set<PhenotypeMatch>> humanPhenotypeMatches = referenceOrganismPhenotypeMatches.getTermPhenotypeMatches();
            Set<PhenotypeMatch> bestMatches = referenceOrganismPhenotypeMatches.getBestPhenotypeMatches();
            return runDynamicQuery(bestMatches, humanPhenotypeMatches, referenceOrganismPhenotypeMatches.getOrganism());
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, ModelPhenotypeMatch> makeHpToOtherSpeciesMatches(boolean runSpecies, List<PhenotypeTerm> queryHpoPhenotypes, Organism species) {
        if (runSpecies) {
            OrganismPhenotypeMatches bestOrganismPhenotypes = priorityService.getMatchingPhenotypesForOrganism(queryHpoPhenotypes, species);
            logTheoreticalModel(bestOrganismPhenotypes.getBestTheoreticalModel());

            Map<PhenotypeTerm, Set<PhenotypeMatch>> mousePhenotypeMatches = bestOrganismPhenotypes.getTermPhenotypeMatches();
            Set<PhenotypeMatch> bestMatches = bestOrganismPhenotypes.getBestPhenotypeMatches();
            return runDynamicQuery(bestMatches, mousePhenotypeMatches, bestOrganismPhenotypes.getOrganism());
        } else {
            return Collections.emptyMap();
        }
    }

    private void logTheoreticalModel(TheoreticalModel theoreticalModel) {
        logger.info("Best {} phenotype matches:", theoreticalModel.getOrganism());
        for (PhenotypeMatch bestMatch : theoreticalModel.getBestPhenotypeMatches()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        logger.info("bestMaxScore={} bestAvgScore={}", theoreticalModel.getMaxMatchScore(), theoreticalModel.getBestAvgScore());
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
//        logger.info("hpIdsWithPhenotypeMatch={}", hpIdsWithPhenotypeMatch);

        Set<String> matchedPhenotypeIdsForSpecies = new TreeSet<>();
        for (PhenotypeMatch match : speciesPhenotypeMatches.values()) {
            matchedPhenotypeIdsForSpecies.add(match.getMatchPhenotypeId());
        }
        logger.info("matchedPhenotypeIdsForspecies {}={}", organism, matchedPhenotypeIdsForSpecies.size());

//        logger.info("organismPhenotypeMatches {}={}", organism, organismPhenotypeMatches.getTermPhenotypeMatches().size());

        logger.info("Scoring {} models", organism);
        Instant timeStart = Instant.now();

        List<Model> models = priorityService.getModelsForOrganism(organism);

        List<ModelPhenotypeMatch> modelPhenotypeMatches = models.parallelStream()
                .map(model -> scoreModel(speciesPhenotypeMatches, hpIdsWithPhenotypeMatch, matchedPhenotypeIdsForSpecies, model))
                .filter(hasMatches())
                .collect(Collectors.toList());

        Map<Integer, Set<ModelPhenotypeMatch>> geneModelPhenotypeMatches = new HashMap<>();
        for (ModelPhenotypeMatch match : modelPhenotypeMatches) {
            addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, match);
        }

        Duration duration = Duration.between(timeStart, Instant.now());
        logger.info("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());

        return makeBestGeneModelForGenes(geneModelPhenotypeMatches);
    }

    private Predicate<ModelPhenotypeMatch> hasMatches() {
        return modelPhenotypeMatch -> !modelPhenotypeMatch.getBestPhenotypeMatchForTerms().isEmpty();
    }

    private ModelPhenotypeMatch scoreModel(Map<String, PhenotypeMatch> speciesPhenotypeMatches, Set<String> hpIdsWithPhenotypeMatch, Set<String> matchedPhenotypeIdsForSpecies, Model model) {
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
        double score = 0;
//        if (bestPhenotypeMatchForTerms.values().isEmpty()) {
//            logger.warn("Score is 0 {} - {}" , model, bestPhenotypeMatchForTerms.values());
//        }
        if (sumModelBestMatchScores > 0) {
            double avgBestHitRowsColumnsScore = sumModelBestMatchScores / rowColumnCount;
            double combinedScore = 50 * (maxModelMatchScore / bestMaxScore + avgBestHitRowsColumnsScore / bestAvgScore);
            if (combinedScore > 100) {
                combinedScore = 100;
            }
            score = combinedScore / 100;
        }
        return new ModelPhenotypeMatch(score, model, new ArrayList<>(bestPhenotypeMatchForTerms.values()));
    }

    private void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match, Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms) {
        PhenotypeTerm matchQueryTerm = match.getQueryPhenotype();
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm)) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        } else if (bestPhenotypeMatchForTerms.get(matchQueryTerm).getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        }
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
                    logger.info("Found benchmarking hit {}:{} - skipping model {}", options.getDiseaseId(), options.getCandidateGeneSymbol(), model);
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
//                        logger.info("Adding new model for {} score={} to bestGeneModels model={}", model.getHumanGeneSymbol(), model.getScore(), model);
                        bestGeneModelForGenes.put(entrezId, model);
                    } else if (bestGeneModelForGenes.get(entrezId).getScore() < model.getScore()) {
//                        logger.info("Updating best model for {} score={} model={}", model.getHumanGeneSymbol(), model.getScore(), model);
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
    private int getColumnIndexOfMostPhenotypicallySimilarGene(Gene gene, List<Integer> phenotypicallySimilarGeneIds) {
        int geneIndex = randomWalkMatrix.getRowIndexForGene(gene.getEntrezGeneID());
        int columnIndex = 0;
        double bestScore = 0;
        int bestHitIndex = 0;
        for (Integer similarGeneEntrezId : phenotypicallySimilarGeneIds) {
            if (!randomWalkMatrix.containsGene(similarGeneEntrezId)) {
                columnIndex++;
            } else if (similarGeneEntrezId == gene.getEntrezGeneID()) {
                //avoid self-hits now are testing genes with direct pheno-evidence as well
                columnIndex++;
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
