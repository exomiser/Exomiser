package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.exomiser.core.prioritisers.util.Species;
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

    private PriorityService priorityService;
    private final DataMatrix randomWalkMatrix;

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private final List<String> messages = new ArrayList<>();

    private List<Integer> phenoGenes = new ArrayList<>();
    private List<String> phenoGeneSymbols = new ArrayList<>();
    private List<String> hpoIds;
    private String candidateGeneSymbol;
    private String diseaseId;

    private Map<Integer, Double> scores = new HashMap<>();
    private Map<Integer, Double> mouseScores = new HashMap<>();
    private Map<Integer, Double> humanScores = new HashMap<>();
    private Map<Integer, Double> fishScores = new HashMap<>();

    private Map<Integer, String> humanDiseases = new HashMap<>();
    private Map<Integer, String> mouseModels = new HashMap<>();
    private Map<Integer, String> fishModels = new HashMap<>();

    private double bestMaxScore = 0d;
    private double bestAvgScore = 0d;

    private boolean runPpi = false;
    private boolean runHuman = false;
    private boolean runMouse = false;
    private boolean runFish = false;

    /**
     * This is the matrix of similarities between the seeed genes and all genes
     * in the network, i.e., p<sub>infinity</sub>.
     */
    private FloatMatrix weightedHighQualityMatrix = new FloatMatrix();

    /**
     *
     * @param hpoIds
     * @param candidateGene
     * @param disease
     * @param exomiser2Params
     * @param randomWalkMatrix
     */
    public HiPhivePriority(List<String> hpoIds, String candidateGene, String disease, String exomiser2Params, DataMatrix randomWalkMatrix) {
        this.hpoIds = hpoIds;
        this.candidateGeneSymbol = candidateGene;
        this.diseaseId = disease;
        this.randomWalkMatrix = randomWalkMatrix;
        parseParams(exomiser2Params);
    }

    private void parseParams(String exomiser2Params) {
        if (exomiser2Params.isEmpty()) {
            this.runPpi = true;
            this.runHuman = true;
            this.runMouse = true;
            this.runFish = true;
        } else {
            logger.info("Received extra params: " + exomiser2Params);
            String[] paramsArray = exomiser2Params.split(",");
            for (String param : paramsArray) {
                if (param.equals("ppi")) {
                    this.runPpi = true;
                } else if (param.equals("human")) {
                    this.runHuman = true;
                } else if (param.equals("mouse")) {
                    this.runMouse = true;
                } else if (param.equals("fish")) {
                    this.runFish = true;
                }
            }
        }
    }

    @Override
    public String getPriorityName() {
        return PRIORITY_TYPE.getCommandLineValue();
    }

    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    /**
     * Prioritize a list of candidate {@link exomizer.exome.Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     * <P>
     *
     * @param genes List of candidate genes.
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {

        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        final Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> bestDiseaseModelForGene = makeHpToHumanMatches(runHuman, hpoPhenotypeTerms, Species.HUMAN);
        final Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> bestMouseModelForGene = makeHpToOtherSpeciesMatches(runMouse, hpoPhenotypeTerms, Species.MOUSE);
        final Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> bestFishModelForGene = makeHpToOtherSpeciesMatches(runFish, hpoPhenotypeTerms, Species.FISH);

        if (runPpi) {
            weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(phenoGenes, scores);
        }

        List<HiPhivePriorityResult> priorityResults = new ArrayList<>(genes.size());
        logger.info("Prioritising genes...");
        for (Gene gene : genes) {
            if (gene.getGeneSymbol().equals("FGFR2")) {
                
            }
//            if (hpHpMatches.containsKey(gene.getEntrezGeneID())) {
//                logger.info("{} best phenotype hits:", gene.getGeneSymbol());
//                Map<String, Map<PhenotypeTerm, PhenotypeMatch>> geneModelMatches = hpHpMatches.get(gene.getEntrezGeneID());
//                if (!geneModelMatches.isEmpty()) {
//                    for (Entry<String, Map<PhenotypeTerm, PhenotypeMatch>> entry : geneModelMatches.entrySet()) {
//                        logger.info("\t{}:", entry.getKey());
//                        if (!entry.getValue().isEmpty()) {
//                            for (PhenotypeMatch bestPhenotypeMatch : entry.getValue().values()) {
//                                logger.info("\t\t{}-{}={}", bestPhenotypeMatch.getQueryPhenotype().getId(), bestPhenotypeMatch.getMatchPhenotype().getId(), bestPhenotypeMatch.getScore());
//                            }
//                        }
//                    }
//                }
//            }

            HiPhivePriorityResult priorityResult = makePrioritiserResultForGene(gene, hpoPhenotypeTerms, bestDiseaseModelForGene, bestMouseModelForGene, bestFishModelForGene);
            gene.addPriorityResult(priorityResult);
            priorityResults.add(priorityResult);
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
        String message = makeStatsMessage(priorityResults, genes);
        messages.add(message);
    }

    private String makeStatsMessage(List<HiPhivePriorityResult> priorityResults, List<Gene> genes) {
        int numGenesWithPhenotypeOrPpiData = 0;
        for (HiPhivePriorityResult priorityResult : priorityResults) {
            if (priorityResult.getWalkerScore() > 0 || priorityResult.getHumanScore() > 0 || priorityResult.getMouseScore() > 0 || priorityResult.getFishScore() > 0) {
                numGenesWithPhenotypeOrPpiData++;
            }
        }
        int totalGenes = genes.size();
        return String.format("Phenotype and Protein-Protein Interaction evidence was available for %d of %d genes (%.1f%%)",
                numGenesWithPhenotypeOrPpiData, totalGenes, 100f * (numGenesWithPhenotypeOrPpiData / (float) totalGenes));
    }

    private HiPhivePriorityResult makePrioritiserResultForGene(Gene gene, List<PhenotypeTerm> queryHpoTerms, Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> hpHpMatches, Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> hpMpMatches, Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> hpZpMatches) {
        String evidence = "";
        String humanPhenotypeEvidence = "";
        String mousePhenotypeEvidence = "";
        String fishPhenotypeEvidence = "";
        double score = 0f;
        double humanScore = 0f;
        double mouseScore = 0f;
        double fishScore = 0f;
        double walkerScore = 0f;
        
        //TODO: move this up one level? 
        //TODO: Strip out the HTML stuff and move that into the HiPhivePriorityResult
        //TODO: HiPhivePriorityResult should take lists of GeneModel and List<PhenotypeTerm> queryHpoTerms
        // DIRECT PHENO HIT
        Integer entrezGeneId = gene.getEntrezGeneID();
        if (scores.containsKey(entrezGeneId)) {
            score = scores.get(entrezGeneId);
            // HUMAN
            if (humanScores.containsKey(entrezGeneId)) {
                humanScore = humanScores.get(entrezGeneId);
                //should come from GeneModel.getScore
                String diseaseId = humanDiseases.get(entrezGeneId);
                String diseaseTerm = priorityService.getDiseaseTermForId(diseaseId);
                String diseaseLink = makeDiseaseLink(diseaseId, diseaseTerm);   
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to %s associated with %s.</dt>", humanScores.get(gene.getEntrezGeneID()), diseaseLink, gene.getGeneSymbol());
                humanPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, humanDiseases, queryHpoTerms, hpHpMatches);
                evidence = evidence + humanPhenotypeEvidence + "</dl>";
            }
            // MOUSE
            if (mouseScores.containsKey(entrezGeneId)) {
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>.</dt>", mouseScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                mouseScore = mouseScores.get(entrezGeneId);
                mousePhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, mouseModels, queryHpoTerms, hpMpMatches);
                evidence = evidence + mousePhenotypeEvidence + "</dl>";
            }
            // FISH
            if (fishScores.containsKey(entrezGeneId)) {
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>.</dt>", fishScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                fishScore = fishScores.get(entrezGeneId);
                fishPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, fishModels, queryHpoTerms, hpZpMatches);
                evidence = evidence + fishPhenotypeEvidence + "</dl>";
            }
        }
        //INTERACTION WITH A HIGH QUALITY MOUSE/HUMAN PHENO HIT => 0 to 0.65 once scaled
        if (runPpi && randomWalkMatrix.containsGene(entrezGeneId) && !phenoGenes.isEmpty()) {
            Integer columnIndex = getColumnIndexOfMostPhenotypicallySimilarGene(gene, phenoGenes);
            int rowIndex = randomWalkMatrix.getRowIndexForGene(entrezGeneId);
            walkerScore = weightedHighQualityMatrix.get(rowIndex, columnIndex);
            if (walkerScore <= 0.00001) {
                walkerScore = 0f;
            } else {
                //walkerScore = val;
                String closestGeneSymbol = phenoGeneSymbols.get(columnIndex);
                Integer closestGeneId = phenoGenes.get(columnIndex);
                String thisGeneSymbol = gene.getGeneSymbol();
                String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + thisGeneSymbol + "%0D" + closestGeneSymbol + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                // HUMAN
                if (humanScores.containsKey(closestGeneId)) {
                    String diseaseId = humanDiseases.get(closestGeneId);
                    String diseaseTerm = priorityService.getDiseaseTermForId(diseaseId);
                    String diseaseLink = makeDiseaseLink(diseaseId, diseaseTerm);
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to %s associated with %s.</dt>", stringDbLink, closestGeneSymbol, diseaseLink, closestGeneSymbol);
                    humanPhenotypeEvidence = makeBestPhenotypeMatchesHtml(closestGeneId, humanDiseases, queryHpoTerms, hpHpMatches);
                    evidence = evidence + humanPhenotypeEvidence + "</dl>";
                }
                // MOUSE
                if (mouseScores.containsKey(closestGeneId)) {
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to mouse mutant of %s.</dt>", stringDbLink, closestGeneSymbol, closestGeneSymbol);
                    mousePhenotypeEvidence = makeBestPhenotypeMatchesHtml(closestGeneId, mouseModels, queryHpoTerms, hpMpMatches);
                    evidence = evidence + mousePhenotypeEvidence + "</dl>";

                }
                // FISH
                if (fishScores.containsKey(closestGeneId)) {
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to fish mutant of %s.</dt>", stringDbLink, closestGeneSymbol, closestGeneSymbol);
                    fishPhenotypeEvidence = makeBestPhenotypeMatchesHtml(closestGeneId, fishModels, queryHpoTerms, hpZpMatches);
                    evidence = evidence + fishPhenotypeEvidence + "</dl>";
                }
            }
        }
        // NO PHENO HIT OR PPI INTERACTION
        if (evidence.isEmpty()) {
            evidence = "<dl><dt>No phenotype or PPI evidence</dt></dl>";
        }
        //evidence should be made internally 
        return new HiPhivePriorityResult(score, evidence, humanPhenotypeEvidence, mousePhenotypeEvidence,
                fishPhenotypeEvidence, humanScore, mouseScore, fishScore, walkerScore);
    }

    private String makeBestPhenotypeMatchesHtml(int entrezGeneId, Map<Integer, String> models, List<PhenotypeTerm> queryHpoTerms, Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> phenotypeMatches) {
        //todo: This should work on a List<GeneModel> the entrezGeneId and phenotypeMatches are not required as they are in the GeneModel
        String model = models.get(entrezGeneId);
        StringBuilder stringBuilder = new StringBuilder("<dt>Best Phenotype Matches:</dt>");
        if (phenotypeMatches.containsKey(entrezGeneId) && phenotypeMatches.get(entrezGeneId).containsKey(model)) {
            for (PhenotypeTerm hpTerm : queryHpoTerms) {
                if (phenotypeMatches.get(entrezGeneId).get(model).containsKey(hpTerm)) {
                    PhenotypeMatch phenotypeMatch = phenotypeMatches.get(entrezGeneId).get(model).get(hpTerm);
                    stringBuilder.append(String.format("<dd>%s (%s) - %s (%s)</dd>", hpTerm.getTerm(), hpTerm.getId(), phenotypeMatch.getMatchPhenotype().getTerm(), phenotypeMatch.getMatchPhenotype().getId()));
                } else {
                    stringBuilder.append(String.format("<dd>%s (%s) - </dd>", hpTerm.getTerm(), hpTerm.getId()));
                }
            }
        }
        return stringBuilder.toString();
    }

    private String makeDiseaseLink(String diseaseId, String diseaseTerm) {
        String[] databaseNameAndIdentifier = diseaseId.split(":");
        String databaseName = databaseNameAndIdentifier[0];
        String id = databaseNameAndIdentifier[1];
        if (databaseName.equals("OMIM")) {
            return "<a href=\"http://www.omim.org/" + id + "\">" + diseaseTerm + "</a>";
        } else {
            return "<a href=\"http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=" + id + "\">" + diseaseTerm + "</a>";
        }
    }

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> makeHpToHumanMatches(boolean runHuman, List<PhenotypeTerm> queryHpoPhenotypes, Species species) {
        //TODO: this must always run in order that the best score is set - refactor this so that the behaviour of runDynamicQuery
        //is consistent with the mouse and fish
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

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> makeHpToOtherSpeciesMatches(boolean runSpecies, List<PhenotypeTerm> queryHpoPhenotypes, Species species) {
        if (runSpecies) {
            logger.info("Fetching HUMAN-{} phenotype matches...", species);
            Map<PhenotypeTerm, Set<PhenotypeMatch>> mousePhenotypeMatches = getMatchingPhenotypesForSpecies(queryHpoPhenotypes, species);
            Set<PhenotypeMatch> bestMatches = getBestMatchesForQueryTerms(mousePhenotypeMatches);
            return runDynamicQuery(bestMatches, mousePhenotypeMatches, species);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<PhenotypeTerm, Set<PhenotypeMatch>> getMatchingPhenotypesForSpecies(List<PhenotypeTerm> queryHpoPhenotypes, Species species) {
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
            logger.info("Best match: {}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
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
//                //TODO: do bestMaxScore and bestAvgScore need to be global?
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

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> runDynamicQuery(Set<PhenotypeMatch> bestMatches, Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches, Species species) {

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

        Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> geneModelPhenotypeMatches = calculateBestGeneModelPhenotypeMatches(species, bestMatches, speciesPhenotypeMatches);
        return geneModelPhenotypeMatches;
    }

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> calculateBestGeneModelPhenotypeMatches(Species species, Set<PhenotypeMatch> bestMatches, Map<String, PhenotypeMatch> speciesPhenotypeMatches) {
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
        logger.info("num matchedPhenotypeIdsForspecies {}={}", species, matchedPhenotypeIdsForSpecies.size());
        
        //TODO: return a Map<Integer, <GeneModel, Map<PhenotypeTerm, PhenotypeMatch>>> where there is only one model per geneId
        Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> geneModelPhenotypeMatches = new HashMap<>();
        List<GeneModel> geneModels = priorityService.getModelsForSpecies(species);
        for (GeneModel model : geneModels) {
            String modelId = model.getModelId();
            int entrezId = model.getEntrezGeneId();
            String humanGeneSymbol = model.getHumanGeneSymbol();

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

                // code to catch hit to known disease-gene association for purposes of benchmarking i.e to simulate novel gene discovery performance
                if ((modelId == null ? diseaseId == null : modelId.equals(diseaseId))
                        && (humanGeneSymbol == null ? candidateGeneSymbol == null : humanGeneSymbol.equals(candidateGeneSymbol))) {
                    logger.info("Found self hit {}:{} - skipping due to benchmarking", diseaseId, candidateGeneSymbol);
                } else {
                    // normal behaviour when not trying to exclude candidate gene to simulate novel gene disovery in benchmarking
                    // only build PPI network for high qual hits
                    if (score > 0.6) {
                        phenoGenes.add(entrezId);
                        phenoGeneSymbols.add(humanGeneSymbol);
                    }
                    if (!scores.containsKey(entrezId) || score > scores.get(entrezId)) {
                        scores.put(entrezId, score);
                    }
                    if (species == Species.HUMAN) {
                        addScoreIfAbsentOrBetter(entrezId, score, modelId, humanScores, humanDiseases);
                    }
                    if (species == Species.MOUSE) {
                        addScoreIfAbsentOrBetter(entrezId, score, modelId, mouseScores, mouseModels);
                    }
                    if (species == Species.FISH) {
                        addScoreIfAbsentOrBetter(entrezId, score, modelId, fishScores, fishModels);
                    }
                }
            }
        }
        return geneModelPhenotypeMatches;
    }

        //GeneId - modelId - hpId: PhenotypeMatch 
    private void addGeneModelPhenotypeMatch(Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> geneModelPhenotypeMatches, GeneModel model, PhenotypeMatch match) {
        //TODO: change this to use the model directly
        String geneSymbol = model.getHumanGeneSymbol();
        int entrezId = model.getEntrezGeneId();
        String modelId = model.getModelId();
        PhenotypeTerm hpQueryTerm = match.getQueryPhenotype();
        if (!geneModelPhenotypeMatches.containsKey(entrezId)) {
            logger.debug("Adding match for new gene {} (ENTREZ:{}) modelId {} ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore());
            geneModelPhenotypeMatches.put(entrezId, new HashMap<String, Map<PhenotypeTerm, PhenotypeMatch>>());
            geneModelPhenotypeMatches.get(entrezId).put(modelId, new LinkedHashMap<PhenotypeTerm, PhenotypeMatch>());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        } else if (!geneModelPhenotypeMatches.get(entrezId).containsKey(modelId)) {
            logger.debug("Adding match for gene {} (ENTREZ:{}) new modelId {} ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore());
            
            geneModelPhenotypeMatches.get(entrezId).put(modelId, new LinkedHashMap<PhenotypeTerm, PhenotypeMatch>());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        } else if (!geneModelPhenotypeMatches.get(entrezId).get(modelId).containsKey(hpQueryTerm)) {
            logger.debug("Adding match for gene {} (ENTREZ:{}) modelId {} new ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        } else if (geneModelPhenotypeMatches.get(entrezId).get(modelId).get(hpQueryTerm).getScore() < match.getScore()) {
            PhenotypeMatch currentBestMatch = geneModelPhenotypeMatches.get(entrezId).get(modelId).get(hpQueryTerm);
            logger.debug("Replacing match for gene {} (ENTREZ:{}) modelId {} - {}-{}={} with ({}-{}={})", geneSymbol, entrezId, modelId, currentBestMatch.getQueryPhenotypeId(), currentBestMatch.getMatchPhenotypeId(), currentBestMatch.getScore(), match.getQueryPhenotypeId(), match.getMatchPhenotypeId(), match.getScore());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        }
    }

    private void addScoreIfAbsentOrBetter(int entrezGeneId, double score, String modelId, Map<Integer, Double> geneIdToScoreMap, Map<Integer, String> geneIdToModelIdMap) {
        if (!geneIdToScoreMap.containsKey(entrezGeneId) || score > geneIdToScoreMap.get(entrezGeneId)) {
            geneIdToScoreMap.put(entrezGeneId, score);
            geneIdToModelIdMap.put(entrezGeneId, modelId);
        }
    }

    //todo: If this returned a DataMatrix things might be a bit more convienent later on... 
    private FloatMatrix makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(List<Integer> phenoGenes, Map<Integer, Double> scores) {
        logger.info("Making weighted-score Protein-Protein interaction sub-matrix from high quality phenotypic gene matches...");
        int rows = randomWalkMatrix.getMatrix().getRows();
        int cols = phenoGenes.size();
        FloatMatrix highQualityPpiMatrix = FloatMatrix.zeros(rows, cols);
        int c = 0;
        for (Integer seedGeneEntrezId : phenoGenes) {
            if (randomWalkMatrix.containsGene(seedGeneEntrezId)) {
                FloatMatrix column = randomWalkMatrix.getColumnMatrixForGene(seedGeneEntrezId);
                // weight column by phenoScore 
                Double score = scores.get(seedGeneEntrezId);
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

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
     */
    @Override
    public List<String> getMessages() {
        return this.messages;
    }

    /**
     * This causes a summary of RW prioritization to appear in the HTML output
     * of the exomizer
     *
     * @return
     */
    @Override
    public boolean displayInHTML() {
        return true;
    }

    /**
     * @return HTML code for displaying the HTML output of the Exomizer.
     */
    @Override
    public String getHTMLCode() {
        if (messages.isEmpty()) {
            return "Error running HiPhive Prioritiser";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<ul>\n");
            for (String m : messages) {
                sb.append(String.format("<li>%s</li>\n", m));
            }
            sb.append("</ul>\n");
            return sb.toString();
        }
    }

    public void setPriorityService(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

}
