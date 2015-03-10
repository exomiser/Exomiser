package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import javax.sql.DataSource;
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

    private DataSource dataSource;
    private OntologyService ontologyService;
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

    private Map<Integer, Float> scores = new HashMap<>();
    private Map<Integer, Double> mouseScores = new HashMap<>();
    private Map<Integer, Double> humanScores = new HashMap<>();
    private Map<Integer, Double> fishScores = new HashMap<>();

    private Map<Integer, String> humanDiseases = new HashMap<>();
    private Map<Integer, String> mouseDiseases = new HashMap<>();
    private Map<Integer, String> fishDiseases = new HashMap<>();

    //TODO: move into external caches
    private Map<String, PhenotypeTerm> hpoTerms = new HashMap<>();
    private Map<String, PhenotypeTerm> mpoTerms = new HashMap<>();
    private Map<String, PhenotypeTerm> zpoTerms = new HashMap<>();
    private Map<String, String> diseaseTerms = new HashMap<>();

    private float bestMaxScore = 0f;
    private float bestAvgScore = 0f;

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
        
        setUpOntologyCaches();
        List<PhenotypeTerm> hpoPhenotypeTerms = makeQueryTermsFromHpoIds(hpoIds);
        
        final Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpHpMatches = makeHpHpMatches(hpoIds, hpoPhenotypeTerms);
        final Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpMpMatches = makeHpMpMatches(hpoIds, hpoPhenotypeTerms);
        final Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpZpMatches = makeHpZpMatches(hpoIds, hpoPhenotypeTerms);

        if (runPpi) {
            weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(phenoGenes, scores);
        }

        List<HiPhivePriorityResult> priorityResults = new ArrayList<>(genes.size());
        logger.info("Scoring genes...");
        for (Gene gene : genes) {
            HiPhivePriorityResult priorityResult = makePrioritiserResultForGene(gene, hpoPhenotypeTerms, hpHpMatches, hpMpMatches, hpZpMatches);
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
            float geneScore = ((HiPhivePriorityResult) g.getPriorityResult(PriorityType.HI_PHIVE_PRIORITY)).getWalkerScore();
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

    private HiPhivePriorityResult makePrioritiserResultForGene(Gene gene, List<PhenotypeTerm> queryHpoTerms, Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpHpMatches, Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpMpMatches, Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpZpMatches) {
        String evidence = "";
        String humanPhenotypeEvidence = "";
        String mousePhenotypeEvidence = "";
        String fishPhenotypeEvidence = "";
        double score = 0f;
        double humanScore = 0f;
        double mouseScore = 0f;
        double fishScore = 0f;
        double walkerScore = 0f;
        // DIRECT PHENO HIT
        int entrezGeneId = gene.getEntrezGeneID();
        if (scores.containsKey(entrezGeneId)) {
            score = scores.get(entrezGeneId);
            // HUMAN
            if (humanScores.containsKey(entrezGeneId)) {
                humanScore = humanScores.get(entrezGeneId);
                String diseaseId = humanDiseases.get(entrezGeneId);
                String diseaseTerm = diseaseTerms.get(diseaseId);
                String diseaseLink = makeDiseaseLink(diseaseId, diseaseTerm);
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to %s associated with %s.</dt>", humanScores.get(gene.getEntrezGeneID()), diseaseLink, gene.getGeneSymbol());
                humanPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, humanDiseases, queryHpoTerms, hpoTerms, hpHpMatches);
                evidence = evidence + humanPhenotypeEvidence + "</dl>";
            }
            // MOUSE
            if (mouseScores.containsKey(entrezGeneId)) {
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>.</dt>", mouseScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                mouseScore = mouseScores.get(entrezGeneId);
                mousePhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, mouseDiseases, queryHpoTerms, mpoTerms, hpMpMatches);
                evidence = evidence + mousePhenotypeEvidence + "</dl>";
            }
            // FISH
            if (fishScores.containsKey(entrezGeneId)) {
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>.</dt>", fishScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                fishScore = fishScores.get(entrezGeneId);
                fishPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, fishDiseases, queryHpoTerms, zpoTerms, hpZpMatches);
                evidence = evidence + fishPhenotypeEvidence + "</dl>";
            }
        }
        //INTERACTION WITH A HIGH QUALITY MOUSE/HUMAN PHENO HIT => 0 to 0.65 once scaled
        if (runPpi && randomWalkMatrix.containsGene(entrezGeneId) && !phenoGenes.isEmpty()) {
            int columnIndex = getColumnIndexOfMostPhenotypicallySimilarGene(gene, phenoGenes);
            int rowIndex = randomWalkMatrix.getRowIndexForGene(gene.getEntrezGeneID());
            walkerScore = weightedHighQualityMatrix.get(rowIndex, columnIndex);
            if (walkerScore <= 0.00001) {
                walkerScore = 0f;
            } else {
                //walkerScore = val;
                String closestGene = phenoGeneSymbols.get(columnIndex);
                String thisGene = gene.getGeneSymbol();
                //String stringDbImageLink = "http://string-db.org/api/image/networkList?identifiers=" + thisGene + "%0D" + closestGene + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + thisGene + "%0D" + closestGene + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                entrezGeneId = phenoGenes.get(columnIndex);
                double phenoScore = scores.get(entrezGeneId);
                // HUMAN
                if (humanScores.containsKey(entrezGeneId)) {
                    double humanPPIScore = humanScores.get(entrezGeneId);
                    String diseaseId = humanDiseases.get(entrezGeneId);
                    String diseaseTerm = diseaseTerms.get(diseaseId);
                    String diseaseLink = makeDiseaseLink(diseaseId, diseaseTerm);
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> with score %s and phenotypic similarity to %s associated with %s.</dt>", stringDbLink, closestGene, humanPPIScore, diseaseLink, closestGene);
                    humanPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, humanDiseases, queryHpoTerms, hpoTerms, hpHpMatches);
                    evidence = evidence + humanPhenotypeEvidence + "</dl>";
                }
                // MOUSE
                if (mouseScores.containsKey(entrezGeneId)) {
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to mouse mutant of %s.</dt>", stringDbLink, closestGene, closestGene);
                    mousePhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, mouseDiseases, queryHpoTerms, mpoTerms, hpMpMatches);
                    evidence = evidence + mousePhenotypeEvidence + "</dl>";

                }
                // FISH
                if (fishScores.containsKey(entrezGeneId)) {
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to fish mutant of %s.</dt><dt>Best Phenotype Matches:</dt>", stringDbLink, closestGene, closestGene);
                    fishPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, fishDiseases, queryHpoTerms, zpoTerms, hpZpMatches);
                    evidence = evidence + fishPhenotypeEvidence + "</dl>";
                }
            }
        }
        // NO PHENO HIT OR PPI INTERACTION
        if (evidence.isEmpty()) {
            evidence = "<dl><dt>No phenotype or PPI evidence</dt></dl>";
        }
        return new HiPhivePriorityResult(score, evidence, humanPhenotypeEvidence, mousePhenotypeEvidence,
                fishPhenotypeEvidence, humanScore, mouseScore, fishScore, walkerScore);
    }

    private String makeBestPhenotypeMatchesHtml(int entrezGeneId, Map<Integer, String> models, List<PhenotypeTerm> queryHpoTerms, Map<String, PhenotypeTerm> otherTerms, Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> phenotypeMatches) {
        String model = models.get(entrezGeneId);
        StringBuilder stringBuilder = new StringBuilder("<dt>Best Phenotype Matches:</dt>");
        if (phenotypeMatches.containsKey(entrezGeneId) && phenotypeMatches.get(entrezGeneId).containsKey(model)) {
            for (PhenotypeTerm hpTerm : queryHpoTerms) {
                String hpId = hpTerm.getId();
//                PhenotypeTerm hpTerm = hpoTerms.get(hpId);
                if (phenotypeMatches.get(entrezGeneId).get(model).containsKey(hpId)) {
                    Set<Float> hpIdScores = phenotypeMatches.get(entrezGeneId).get(model).get(hpId).keySet();
                    for (float hpIdScore : hpIdScores) {
                        String matchId = phenotypeMatches.get(entrezGeneId).get(model).get(hpId).get(hpIdScore);
                        PhenotypeTerm matchTerm = otherTerms.get(matchId);
                        stringBuilder.append(String.format("<dd>%s (%s) - %s (%s)</dd>", hpTerm.getTerm(), hpTerm.getId(), matchTerm.getTerm(), matchTerm.getId()));
                    }
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

    private List<PhenotypeTerm> makeQueryTermsFromHpoIds(List<String> hpoIds) {
        List<PhenotypeTerm> phenotypeTerms = new ArrayList<>();
        for (String hpoId : hpoIds) {
            PhenotypeTerm hpoTerm = hpoTerms.get(hpoId);
            if (hpoTerm != null) {
                phenotypeTerms.add(hpoTerm);            
            }
        }
        return phenotypeTerms;
    }

    //TODO - this shouldn' exist. runDynamicQuery should have two variants - one for human the other for non-human
    private enum Species {
        HUMAN, MOUSE, FISH;
    }

    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> makeHpHpMatches(List<String> hpoIds, List<PhenotypeTerm> hpoPhenotypeTerms) {
        //TODO: this must always run in order that the best score is set - refactor this so that the behaviour of runDynamicQuery
        //is consistent with the mouse and fish
        // Human
        logger.info("Fetching HP-HP scores...");
        String mappingQuery = "SELECT hp_id_hit, score FROM hp_hp_mappings M WHERE M.hp_id = ?";
        for (PhenotypeTerm hpoTerm : hpoPhenotypeTerms) {
          // what do we need here? A  Map<PhenotypeTerm, Set<PhenotypeMatch>>? 
            for (PhenotypeMatch phenotypeMatch : ontologyService.getHpoMatchesForHpoTerm(hpoTerm)) {
                PhenotypeTerm queryPhenotype = phenotypeMatch.getQueryPhenotype();
                PhenotypeTerm matchPhenotype = phenotypeMatch.getMatchPhenotype();
                logger.info("{}-{}={}", queryPhenotype.getId(), matchPhenotype.getId(), phenotypeMatch.getScore());
            }
        }
        String annotationQuery = String.format("SELECT H.disease_id, hp_id, gene_id, human_gene_symbol FROM human2mouse_orthologs hm, disease_hp M, disease H WHERE hm.entrez_id=H.gene_id AND M.disease_id=H.disease_id");
        return runDynamicQuery(mappingQuery, annotationQuery, hpoIds, Species.HUMAN);
    }

    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> makeHpMpMatches(List<String> hpoIds, List<PhenotypeTerm> hpoPhenotypeTerms) {
        // Mouse
        if (runMouse) {
            String mappingQuery = "SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?";
            String annotationQuery = "SELECT mouse_model_id, mp_id, entrez_id, human_gene_symbol, M.mgi_gene_id, M.mgi_gene_symbol FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'";
            return runDynamicQuery(mappingQuery, annotationQuery, hpoIds, Species.MOUSE);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> makeHpZpMatches(List<String> hpoIds, List<PhenotypeTerm> hpoPhenotypeTerms) {
        // Fish
        if (runFish) {
            String mappingQuery = "SELECT zp_id, score FROM hp_zp_mappings M WHERE M.hp_id = ?";
            String annotationQuery = "SELECT zfin_model_id, zp_id, entrez_id, human_gene_symbol, M.zfin_gene_id, M.zfin_gene_symbol FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'";
            return runDynamicQuery(mappingQuery, annotationQuery, hpoIds, Species.FISH);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> runDynamicQuery(String mappingQuery, String findAnnotationQuery, List<String> hpoIds, Species species) {

        Set<String> hpIdsWithPhenotypeMatch = new LinkedHashSet<>();
        Map<String, Float> bestMappedTermScore = new HashMap<>();
        Map<String, String> bestMappedTermMpId = new HashMap<>();
        Map<String, Integer> knownMps = new HashMap<>();

        Map<String, Float> mappedTerms = new HashMap<>();
        for (String hpId : hpoIds) {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement findMappingStatement = connection.prepareStatement(mappingQuery);
                findMappingStatement.setString(1, hpId);
                ResultSet rs = findMappingStatement.executeQuery();
                while (rs.next()) {
                    String mpId = rs.getString(1);
                    knownMps.put(mpId, 1);
                    StringBuilder hashKey = new StringBuilder();
                    hashKey.append(hpId);
                    hashKey.append(mpId);
                    float score = rs.getFloat("score");
                    mappedTerms.put(hashKey.toString(), score);
                    if (species == Species.HUMAN && hpId.equals(mpId)) {
                        addBestMappedTerm(bestMappedTermScore, hpId, score, bestMappedTermMpId, mpId);
                        //for some hp terms e.g. HP we won't have the self hit but still want to flag found
                        hpIdsWithPhenotypeMatch.add(hpId);
                    } else {
                        if (bestMappedTermScore.containsKey(hpId)) {
                            if (score > bestMappedTermScore.get(hpId)) {
                                addBestMappedTerm(bestMappedTermScore, hpId, score, bestMappedTermMpId, mpId);
                            }
                        } else {
                            addBestMappedTerm(bestMappedTermScore, hpId, score, bestMappedTermMpId, mpId);
                            hpIdsWithPhenotypeMatch.add(hpId);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Problem setting up SQL query: {}", mappingQuery, e);
            }
        }
        logger.debug("Phenotype matches {} for {}", mappedTerms, species);
        for (Entry<String, String> bestMappedHpIdToOtherId : bestMappedTermMpId.entrySet()) {
            String hpId = bestMappedHpIdToOtherId.getKey();
            logger.info("Best match: {}-{}={}", hpId, bestMappedHpIdToOtherId.getValue(), bestMappedTermScore.get(hpId));
        }

        if (species == Species.HUMAN) {
            calculateBestScoresFromHumanPhenotypes(hpIdsWithPhenotypeMatch, bestMappedTermScore, bestMappedTermMpId, mappedTerms);
        }
        //TODO: needed here or do before? 
        if (species == Species.HUMAN && !runHuman) {
            return Collections.emptyMap();
        }

        // calculate score for this gene
        Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpMatches = new HashMap<>();
        logger.info("Fetching disease/model phenotype annotations and HUMAN-{} gene orthologs", species);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement findAnnotationStatement = connection.prepareStatement(findAnnotationQuery);
            ResultSet rs = findAnnotationStatement.executeQuery();
            while (rs.next()) {
                String hit = rs.getString(1);
                String mpIds = rs.getString(2);
                int entrezId = rs.getInt(3);
                String humanGeneSymbol = rs.getString(4);
                String[] mpInitial = mpIds.split(",");
                List<String> mpList = new ArrayList<>();
                for (String mpid : mpInitial) {
                    if (knownMps.get(mpid) != null) {
                        mpList.add(mpid);
                    }
                }
                String[] mps = new String[mpList.size()];
                mpList.toArray(mps);

                int rowColumnCount = hpIdsWithPhenotypeMatch.size() + mps.length;
                float maxScore = 0f;
                float sumBestHitRowsColumnsScore = 0f;

                for (String hpId : hpIdsWithPhenotypeMatch) {
                    float bestScore = 0f;
                    for (String mpId : mps) {
                        String hashKey = hpId + mpId;
                        if (mappedTerms.containsKey(hashKey)) {
                            float score = mappedTerms.get(hashKey);
                            // identify best match                                                                                                                                                                 
                            if (score > bestScore) {
                                bestScore = score;
                            }
                            if (score > 0) {
                                if (hpMatches.get(entrezId) == null) {
                                    hpMatches.put(entrezId, new HashMap<String, HashMap<String, HashMap<Float, String>>>());
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpId, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                } else if (hpMatches.get(entrezId).get(hit) == null) {
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpId, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpId) == null) {
                                    hpMatches.get(entrezId).get(hit).put(hpId, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpId).keySet().iterator().next() < score) {
                                    hpMatches.get(entrezId).get(hit).get(hpId).clear();
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                }
                            }
                        }
                    }
                    if (bestScore != 0) {
                        sumBestHitRowsColumnsScore += bestScore;

                        if (bestScore > maxScore) {
                            maxScore = bestScore;
                        }
                    }
                }
                // Reciprocal hits                                                                                                                                                                                 
                for (String mpId : mps) {
                    float bestScore = 0f;
                    for (String hpId : hpIdsWithPhenotypeMatch) {
                        String hashKey = hpId + mpId;
                        if (mappedTerms.containsKey(hashKey)) {
                            float score = mappedTerms.get(hashKey);
                            // identify best match                                                                                                                                                                 
                            if (score > bestScore) {
                                bestScore = score;
                            }
                            if (score > 0) {
                                if (hpMatches.get(entrezId) == null) {
                                    hpMatches.put(entrezId, new HashMap<String, HashMap<String, HashMap<Float, String>>>());
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpId, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                } else if (hpMatches.get(entrezId).get(hit) == null) {
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpId, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpId) == null) {
                                    hpMatches.get(entrezId).get(hit).put(hpId, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpId).keySet().iterator().next() < score) {
                                    hpMatches.get(entrezId).get(hit).get(hpId).clear();
                                    hpMatches.get(entrezId).get(hit).get(hpId).put(score, mpId);
                                }
                            }
                        }
                    }
                    if (bestScore != 0) {
                        sumBestHitRowsColumnsScore += bestScore;
                        if (bestScore > maxScore) {
                            maxScore = bestScore;
                        }
                    }
                }
                // calculate combined score
                if (sumBestHitRowsColumnsScore != 0) {
                    double avgBestHitRowsColumnsScore = sumBestHitRowsColumnsScore / rowColumnCount;
                    double combinedScore = 50 * (maxScore / bestMaxScore
                            + avgBestHitRowsColumnsScore / bestAvgScore);
                    if (combinedScore > 100) {
                        combinedScore = 100;
                    }
                    double score = combinedScore / 100;
                    /*
                     * Adjust human score as a hit that is 60% of the perfect
                     * (identical) HPO match is a much better match than
                     * something that is 60% of the perfect mouse match -
                     * imperfect HP-MP mapping
                     */
//                    if (species.equals("human")) {
//                        score = score + ((1 - score) / 2);
//                    }
//                    // adjust fish score - over-scoring at moment as even a perfect fish match is much worse than the mouse and human hits
//                    if (species.equals("fish")) {
//                        score = score - ((score) / 2);
//                    }
                    // code to catch hit to known disease-gene association for purposes of benchmarking i.e to simulate novel gene discovery performance
                    if ((hit == null ? diseaseId == null : hit.equals(diseaseId))
                            && (humanGeneSymbol == null ? candidateGeneSymbol == null : humanGeneSymbol.equals(candidateGeneSymbol))) {
                        //System.out.println("FOUND self hit " + disease + ":"+candGene);
                        // Decided does not make sense to build PPI to candidate gene unless another good disease/mouse/fish hit exists for it
//                        if (scores.get(entrez) != null) {
//                            phenoGenes.add(entrez);
//                            phenoGeneSymbols.add(humanGene);
//                        }
                    } else {
                        // normal behaviour when not trying to exclude candidate gene to simulate novel gene disovery in benchmarking
                        // only build PPI network for high qual hits
                        if (score > 0.6) {
                            phenoGenes.add(entrezId);
                            phenoGeneSymbols.add(humanGeneSymbol);
                        }
                        if (!scores.containsKey(entrezId) || score > scores.get(entrezId)) {
                            scores.put(entrezId, (float) score);
                        }
                        if (species == Species.HUMAN) {
                            addScoreIfAbsentOrBetter(entrezId, score, hit, humanScores, humanDiseases);
                        }
                        if (species == Species.MOUSE) {
                            addScoreIfAbsentOrBetter(entrezId, score, hit, mouseScores, mouseDiseases);
                        }
                        if (species == Species.FISH) {
                            addScoreIfAbsentOrBetter(entrezId, score, hit, fishScores, fishDiseases);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", findAnnotationQuery, e);
        }
        return hpMatches;
    }

    private void calculateBestScoresFromHumanPhenotypes(Set<String> hpIdsWithPhenotypeMatch, Map<String, Float> bestMappedTermScore, Map<String, String> bestMappedTermMpId, Map<String, Float> mappedTerms) {
        // calculate perfect model scores for human
        float sumBestScore = 0f;
        // loop over each hp id should start here
        for (String hpId : hpIdsWithPhenotypeMatch) {
            if (bestMappedTermScore.containsKey(hpId)) {
                float hpScore = bestMappedTermScore.get(hpId);
                // add in scores for best match for the HP term
                sumBestScore += hpScore;
                if (hpScore > bestMaxScore) {
                    bestMaxScore = hpScore;
                }
                //logger.info("ADDING SCORE FOR " + hpid + " TO " + bestMappedTermMpId.get(hpid) + " WITH SCORE " + hpScore + ", SUM NOW " + sumBestScore + ", MAX NOW " + bestMaxScore);
                // add in MP-HP hits
                String mpId = bestMappedTermMpId.get(hpId);
                float bestScore = 0f;
                for (String hpId2 : hpIdsWithPhenotypeMatch) {
                    StringBuilder hashKey = new StringBuilder();
                    hashKey.append(hpId2);
                    hashKey.append(mpId);
                    if (mappedTerms.get(hashKey.toString()) != null && mappedTerms.get(hashKey.toString()) > bestScore) {
                        bestScore = mappedTerms.get(hashKey.toString());
                    }
                }
                // add in scores for best match for the MP term
                sumBestScore += bestScore;
                //logger.info("ADDING RECIPROCAL SCORE FOR " + mpid + " WITH SCORE " + bestScore + ", SUM NOW " + sumBestScore + ", MAX NOW " + bestMaxScore);
                if (bestScore > bestMaxScore) {
                    bestMaxScore = bestScore;
                }
            }
        }
        bestAvgScore = sumBestScore / (2 * hpIdsWithPhenotypeMatch.size());
    }

    private void addBestMappedTerm(Map<String, Float> bestMappedTermScore, String hpId, float score, Map<String, String> bestMappedTermMpId, String mpId) {
        //TODO: this should be a PhenotypeMatch
        bestMappedTermScore.put(hpId, score);
        bestMappedTermMpId.put(hpId, mpId);
    }

    private void addScoreIfAbsentOrBetter(int entrez, double score, String hit, Map<Integer, Double> geneToScoreMap, Map<Integer, String> geneToDiseaseMap) {
        if (geneToScoreMap.get(entrez) == null || score > geneToScoreMap.get(entrez)) {
            geneToScoreMap.put(entrez, score);
            geneToDiseaseMap.put(entrez, hit);
        }
    }

    //todo: If this returned a DataMatrix things might be a bit more convienent later on... 
    private FloatMatrix makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(List<Integer> phenoGenes, Map<Integer, Float> scores) {
        logger.info("Making weighted-score Protein-Protein interaction sub-matrix from high quality phenotypic gene matches...");
        int rows = randomWalkMatrix.getMatrix().getRows();
        int cols = phenoGenes.size();
        FloatMatrix highQualityPpiMatrix = FloatMatrix.zeros(rows, cols);
        int c = 0;
        for (Integer seedGeneEntrezId : phenoGenes) {
            if (randomWalkMatrix.containsGene(seedGeneEntrezId)) {
                FloatMatrix column = randomWalkMatrix.getColumnMatrixForGene(seedGeneEntrezId);
                // weight column by phenoScore 
                float score = scores.get(seedGeneEntrezId);
                column = column.mul(score);
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
            } else if (similarGeneEntrezId == gene.getEntrezGeneID()) {//avoid self-hits now are testing genes with direct pheno-evidence as well
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

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setOntologyService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    private void setUpOntologyCaches() {
        hpoTerms = getHpoTermsCache();
        mpoTerms = getMpoTermsCache();
        zpoTerms = getZpoTermsCache();
        diseaseTerms = getDiseaseTermsCache();
    }

    private Map<String, PhenotypeTerm> getHpoTermsCache() {
        Set<PhenotypeTerm> allHpoTerms = ontologyService.getHpoTerms();
        Map<String, PhenotypeTerm> termsCache = makeGenericOntologyTermCache(allHpoTerms);
        logger.info("HPO cache initialised with {} terms", termsCache.size());
        return termsCache;
    }

    private Map<String, PhenotypeTerm> getMpoTermsCache() {
        Set<PhenotypeTerm> allMpoTerms = ontologyService.getMpoTerms();
        Map<String, PhenotypeTerm> termsCache = makeGenericOntologyTermCache(allMpoTerms);
        logger.info("MPO cache initialised with {} terms", termsCache.size());
        return termsCache;
    }

    private Map<String, PhenotypeTerm> getZpoTermsCache() {
        Set<PhenotypeTerm> allZpoTerms = ontologyService.getZpoTerms();
        Map<String, PhenotypeTerm> termsCache = makeGenericOntologyTermCache(allZpoTerms);
        logger.info("ZPO cache initialised with {} terms", termsCache.size());
        return termsCache;
    }

    private Map<String, PhenotypeTerm> makeGenericOntologyTermCache(Set<PhenotypeTerm> allHpoTerms) {
        Map<String, PhenotypeTerm> termsCache = new HashMap();
        for (PhenotypeTerm term : allHpoTerms) {
            termsCache.put(term.getId(), term);
        }
        return termsCache;
    }

    private Map<String, String> getDiseaseTermsCache() {
        Map<String, String> termsCache = new HashMap();
        String diseaseNameQuery = "SELECT disease_id, diseasename FROM disease";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ontologyTermsStatement = connection.prepareStatement(diseaseNameQuery);
                ResultSet rs = ontologyTermsStatement.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString(1);
                String term = rs.getString(2);
                id = id.trim();
                termsCache.put(id, term);
            }
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for disease terms cache", diseaseNameQuery, e);
        }
        logger.info("Disease cache initialised with {} terms", termsCache.size());
        return termsCache;
    }

}
