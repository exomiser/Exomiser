package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneIdentifier;
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

    private Map<String, String> diseaseTerms = new HashMap<>();

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

        setUpOntologyCaches();
        List<PhenotypeTerm> hpoPhenotypeTerms = makeQueryTermsFromHpoIds(hpoIds);

        final Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> hpHpMatches = makeHpHpMatches(hpoPhenotypeTerms);
        final Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> hpMpMatches = makeHpMpMatches(hpoPhenotypeTerms);
        final Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> hpZpMatches = makeHpZpMatches(hpoPhenotypeTerms);

        if (runPpi) {
            weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(phenoGenes, scores);
        }

        List<HiPhivePriorityResult> priorityResults = new ArrayList<>(genes.size());
        logger.info("Prioritising genes...");
        for (Gene gene : genes) {
            if (hpHpMatches.containsKey(gene.getEntrezGeneID())) {
                logger.info("{} best phenotype hits:", gene.getGeneSymbol() );
                Map<String, Map<PhenotypeTerm, PhenotypeMatch>> geneModelMatches = hpHpMatches.get(gene.getEntrezGeneID());
                if (!geneModelMatches.isEmpty()) {
                    for (Entry<String, Map<PhenotypeTerm, PhenotypeMatch>> entry : geneModelMatches.entrySet()) {
                        logger.info("\t{}:", entry.getKey());
                        if (!entry.getValue().isEmpty()) {
                            for (PhenotypeMatch bestPhenotypeMatch : entry.getValue().values()) {
                                logger.info("\t\t{}-{}={}", bestPhenotypeMatch.getQueryPhenotype().getId(), bestPhenotypeMatch.getMatchPhenotype().getId(), bestPhenotypeMatch.getScore());                    
                            }
                        }
                    }
                }
            }

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
                humanPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, humanDiseases, queryHpoTerms, hpHpMatches);
                evidence = evidence + humanPhenotypeEvidence + "</dl>";
            }
            // MOUSE
            if (mouseScores.containsKey(entrezGeneId)) {
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>.</dt>", mouseScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                mouseScore = mouseScores.get(entrezGeneId);
                mousePhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, mouseDiseases, queryHpoTerms, hpMpMatches);
                evidence = evidence + mousePhenotypeEvidence + "</dl>";
            }
            // FISH
            if (fishScores.containsKey(entrezGeneId)) {
                evidence = evidence + String.format("<dl><dt>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>.</dt>", fishScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                fishScore = fishScores.get(entrezGeneId);
                fishPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, fishDiseases, queryHpoTerms, hpZpMatches);
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
                    humanPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, humanDiseases, queryHpoTerms, hpHpMatches);
                    evidence = evidence + humanPhenotypeEvidence + "</dl>";
                }
                // MOUSE
                if (mouseScores.containsKey(entrezGeneId)) {
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to mouse mutant of %s.</dt>", stringDbLink, closestGene, closestGene);
                    mousePhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, mouseDiseases, queryHpoTerms, hpMpMatches);
                    evidence = evidence + mousePhenotypeEvidence + "</dl>";

                }
                // FISH
                if (fishScores.containsKey(entrezGeneId)) {
                    evidence = evidence + String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to fish mutant of %s.</dt><dt>Best Phenotype Matches:</dt>", stringDbLink, closestGene, closestGene);
                    fishPhenotypeEvidence = makeBestPhenotypeMatchesHtml(entrezGeneId, fishDiseases, queryHpoTerms, hpZpMatches);
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

    private String makeBestPhenotypeMatchesHtml(int entrezGeneId, Map<Integer, String> models, List<PhenotypeTerm> queryHpoTerms, Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> phenotypeMatches) {
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

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> makeHpHpMatches(List<PhenotypeTerm> queryHpoPhenotypes) {
        //TODO: this must always run in order that the best score is set - refactor this so that the behaviour of runDynamicQuery
        //is consistent with the mouse and fish
        // Human
        logger.info("Fetching HP-HP scores...");
//        String mappingQuery = "SELECT hp_id_hit, score FROM hp_hp_mappings M WHERE M.hp_id = ?";
        Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = ontologyService.getHpoMatchesForHpoTerm(hpoTerm);
            allPhenotypeMatches.put(hpoTerm, termMatches);
        }
        String annotationQuery = "SELECT H.disease_id as model_id, hp_id as pheno_ids, gene_id as entrez_id, human_gene_symbol FROM human2mouse_orthologs hm, disease_hp M, disease H WHERE hm.entrez_id=H.gene_id AND M.disease_id=H.disease_id";
        return runDynamicQuery(allPhenotypeMatches, annotationQuery, Species.HUMAN);
    }

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> makeHpMpMatches(List<PhenotypeTerm> queryHpoPhenotypes) {
        // Mouse
        if (runMouse) {
//            String mappingQuery = "SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?";
            Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches = new LinkedHashMap<>();
            for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
                Set<PhenotypeMatch> termMatches = ontologyService.getMpoMatchesForHpoTerm(hpoTerm);
                allPhenotypeMatches.put(hpoTerm, termMatches);
            }
            String annotationQuery = "SELECT mouse_model_id as model_id, mp_id as pheno_ids, entrez_id, human_gene_symbol, M.mgi_gene_id, M.mgi_gene_symbol FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'";
            return runDynamicQuery(allPhenotypeMatches, annotationQuery, Species.MOUSE);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> makeHpZpMatches(List<PhenotypeTerm> queryHpoPhenotypes) {
        // Fish
        if (runFish) {
//            String mappingQuery = "SELECT zp_id, score FROM hp_zp_mappings M WHERE M.hp_id = ?";
            Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches = new LinkedHashMap<>();
            for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
                Set<PhenotypeMatch> termMatches = ontologyService.getZpoMatchesForHpoTerm(hpoTerm);
                allPhenotypeMatches.put(hpoTerm, termMatches);
            }
            String annotationQuery = "SELECT zfin_model_id as model_id, zp_id as pheno_ids, entrez_id, human_gene_symbol, M.zfin_gene_id, M.zfin_gene_symbol FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'";
            return runDynamicQuery(allPhenotypeMatches, annotationQuery, Species.FISH);
        } else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> runDynamicQuery(Map<PhenotypeTerm, Set<PhenotypeMatch>> allPhenotypeMatches, String findAnnotationQuery, Species species) {

        Set<String> hpIdsWithPhenotypeMatch = new LinkedHashSet<>();
        //hpId : score
        Map<String, Double> bestMappedTermsScore = new HashMap<>();
        //hpId : mpId
        Map<String, String> bestMappedTermsMpId = new HashMap<>();
        
        Set<String> matchedTermIds = new HashSet<>();
        //'hpId + mpId' : score
        Map<String, Float> matchedTermScores = new HashMap<>();
        Map<String, PhenotypeMatch> matches = new HashMap<>();

        for (Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : allPhenotypeMatches.entrySet()) {
                PhenotypeTerm queryTerm = entry.getKey();
                String hpId = queryTerm.getId();
            for (PhenotypeMatch match : entry.getValue()) {
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                String mpId = matchTerm.getId();
                matchedTermIds.add(mpId);
                String matchIds = hpId + mpId;
                double score = match.getScore();
                matchedTermScores.put(matchIds, (float) score);
                matches.put(matchIds, match);
                
                if (species == Species.HUMAN && queryTerm.equals(matchTerm)) {
                    addBestMappedTerm(match, bestMappedTermsScore, hpId, bestMappedTermsMpId, mpId);
                    //for some hp terms e.g. HP we won't have the self hit but still want to flag found
                    hpIdsWithPhenotypeMatch.add(hpId);
                } else {
                    if (bestMappedTermsScore.containsKey(hpId)) {
                        if (score > bestMappedTermsScore.get(hpId)) {
                            addBestMappedTerm(match, bestMappedTermsScore, hpId, bestMappedTermsMpId, mpId);
                        }
                    } else {
                        addBestMappedTerm(match, bestMappedTermsScore, hpId, bestMappedTermsMpId, mpId);
                        hpIdsWithPhenotypeMatch.add(hpId);
                    }
                }
            }
        }
        logger.debug("Phenotype matches {} for {}", matchedTermScores, species);
        for (Entry<String, String> bestMappedHpIdToOtherId : bestMappedTermsMpId.entrySet()) {
            String hpId = bestMappedHpIdToOtherId.getKey();
            logger.info("Best match: {}-{}={}", hpId, bestMappedHpIdToOtherId.getValue(), bestMappedTermsScore.get(hpId));
        }

        if (species == Species.HUMAN) {
            calculateBestScoresFromHumanPhenotypes(hpIdsWithPhenotypeMatch, bestMappedTermsScore, bestMappedTermsMpId, matchedTermScores);
        }
        //TODO: needed here or do before? 
        if (species == Species.HUMAN && !runHuman) {
            return Collections.emptyMap();
        }

        // calculate best phenotype matches and scores for all genes
        Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> geneModelPhenotypeMatches = new HashMap<>();
        //These are the human, mouse and fish queries
        //"SELECT H.disease_id as model_id, hp_id as pheno_ids, gene_id as entrez_id, human_gene_symbol FROM human2mouse_orthologs hm, disease_hp M, disease H WHERE hm.entrez_id=H.gene_id AND M.disease_id=H.disease_id"
        //"SELECT mouse_model_id as model_id, mp_id as pheno_ids, entrez_id, human_gene_symbol, M.mgi_gene_id, M.mgi_gene_symbol FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'"
        //"SELECT zfin_model_id as model_id, zp_id as pheno_ids, entrez_id, human_gene_symbol, M.zfin_gene_id, M.zfin_gene_symbol FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'"
        logger.info("Fetching disease/model phenotype annotations and HUMAN-{} gene orthologs", species);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement findAnnotationStatement = connection.prepareStatement(findAnnotationQuery);
            ResultSet rs = findAnnotationStatement.executeQuery();
            //each row is an animal model or disease, its phenotypes and the known causitive gene of these phenotypes. e.g.
            //MODEL_ID      ENTREZ_ID	HUMAN_GENE_SYMBOL   pheno_ids	
            //OMIM:101600   2263	FGFR2               HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
            //OMIM:101600   2260	FGFR1               HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
            //MODEL_ID      ENTREZ_ID	HUMAN_GENE_SYMBOL   pheno_ids                   MGI_GENE_ID	MGI_GENE_SYMBOL
            //115           2263	FGFR2               MP:0000031,MP:0000035,MP:0000039,MP:0000081,MP:0000111,MP:0000118,MP:0000440,MP:0000470,MP:0000492,MP:0000551,MP:0000557,MP:0000613,MP:0000629,MP:0000633,MP:0001176,MP:0001181,MP:0001199,MP:0001201,MP:0001218,MP:0001231,MP:0001244,MP:0001265,MP:0001341,MP:0002095,MP:0002428,MP:0002691,MP:0003051,MP:0003124,MP:0003308,MP:0003315,MP:0003703,MP:0003817,MP:0004135,MP:0004310,MP:0004343,MP:0004346,MP:0004507,MP:0004509,MP:0004619,MP:0004691,MP:0005298,MP:0005354,MP:0006011,MP:0006279,MP:0006287,MP:0006288,MP:0008320,MP:0009479,MP:0009509,MP:0009510,MP:0009522,MP:0009524,MP:0011026,MP:0011089,MP:0011158	2263	FGFR2	MGI:95523	Fgfr2
            //116           2263	FGFR2               MP:0009522,MP:0009525	MGI:95523	Fgfr2

            while (rs.next()) {
                String modelId = rs.getString("model_id");
                String modelPhenotypeIds = rs.getString("pheno_ids");
                int entrezId = rs.getInt("entrez_id");
                String humanGeneSymbol = rs.getString("human_gene_symbol");
                
                String[] mpInitial = modelPhenotypeIds.split(",");
                List<String> matchedPhenotypeIdsForModel = new ArrayList<>();
                for (String mpid : mpInitial) {
                    if (matchedTermIds.contains(mpid)) {
                        matchedPhenotypeIdsForModel.add(mpid);
                    }
                }

                double maxScore = 0d;
                double sumBestHitRowsColumnsScore = 0d;

                for (String hpId : hpIdsWithPhenotypeMatch) {
                    double bestScore = 0d;
                    for (String mpId : matchedPhenotypeIdsForModel) {
                        String matchIds = hpId + mpId;
                        if (matchedTermScores.containsKey(matchIds)) {
                            PhenotypeMatch match = matches.get(matchIds);
                            double score = match.getScore();
                            // identify best match                                                                                                                                                                 
                            bestScore = Math.max(score, bestScore);
                            if (score > 0) {
                                addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, humanGeneSymbol, entrezId, modelId, match);
                            }
                        }
                    }
                    if (bestScore != 0) {
                        sumBestHitRowsColumnsScore += bestScore;
                        maxScore = Math.max(bestScore, maxScore);
                    }
                }
                // Reciprocal hits                                                                                                                                                                                 
                for (String mpId : matchedPhenotypeIdsForModel) {
                    double bestScore = 0f;
                    for (String hpId : hpIdsWithPhenotypeMatch) {
                        String matchIds = hpId + mpId;
                        if (matchedTermScores.containsKey(matchIds)) {
                            PhenotypeMatch match = matches.get(matchIds);
                            double score = match.getScore();
                            // identify best match                                                                                                                                                                 
                            bestScore = Math.max(score, bestScore);
                            if (score > 0) {
                                addGeneModelPhenotypeMatch(geneModelPhenotypeMatches, humanGeneSymbol, entrezId, modelId, match);
                            }
                        }
                    }
                    if (bestScore != 0) {
                        sumBestHitRowsColumnsScore += bestScore;
                        maxScore = Math.max(bestScore, maxScore);
                    }
                }
                
                int rowColumnCount = hpIdsWithPhenotypeMatch.size() + matchedPhenotypeIdsForModel.size();
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
                    if ((modelId == null ? diseaseId == null : modelId.equals(diseaseId))
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
                            addScoreIfAbsentOrBetter(entrezId, score, modelId, humanScores, humanDiseases);
                        }
                        if (species == Species.MOUSE) {
                            addScoreIfAbsentOrBetter(entrezId, score, modelId, mouseScores, mouseDiseases);
                        }
                        if (species == Species.FISH) {
                            addScoreIfAbsentOrBetter(entrezId, score, modelId, fishScores, fishDiseases);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", findAnnotationQuery, e);
        }
        return geneModelPhenotypeMatches;
    }
                                                //GeneId - modelId - hpId: PhenotypeMatch 
    private void addGeneModelPhenotypeMatch(Map<Integer, Map<String, Map<PhenotypeTerm, PhenotypeMatch>>> geneModelPhenotypeMatches, String geneSymbol, int entrezId, String modelId, PhenotypeMatch match) {
        PhenotypeTerm hpQueryTerm = match.getQueryPhenotype();
        if (!geneModelPhenotypeMatches.containsKey(entrezId)) {
            logger.debug("Adding match for new gene {} (ENTREZ:{}) modelId {} ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotype().getId(), match.getMatchPhenotype().getId(), match.getScore());
            geneModelPhenotypeMatches.put(entrezId, new HashMap<String, Map<PhenotypeTerm, PhenotypeMatch>>());
            geneModelPhenotypeMatches.get(entrezId).put(modelId, new LinkedHashMap<PhenotypeTerm, PhenotypeMatch>());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        }
        else if (!geneModelPhenotypeMatches.get(entrezId).containsKey(modelId)) {
            logger.debug("Adding match for gene {} (ENTREZ:{}) new modelId {} ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotype().getId(), match.getMatchPhenotype().getId(), match.getScore());
            geneModelPhenotypeMatches.get(entrezId).put(modelId, new LinkedHashMap<PhenotypeTerm, PhenotypeMatch>());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        }
        else if (!geneModelPhenotypeMatches.get(entrezId).get(modelId).containsKey(hpQueryTerm)) {
            logger.debug("Adding match for gene {} (ENTREZ:{}) modelId {} new ({}-{}={})", geneSymbol, entrezId, modelId, match.getQueryPhenotype().getId(), match.getMatchPhenotype().getId(), match.getScore());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        }
        else if (geneModelPhenotypeMatches.get(entrezId).get(modelId).get(hpQueryTerm).getScore() < match.getScore()) {
            PhenotypeMatch currentBestMatch = geneModelPhenotypeMatches.get(entrezId).get(modelId).get(hpQueryTerm);
            logger.debug("Replacing match for gene {} (ENTREZ:{}) modelId {} - {}-{}={} with ({}-{}={})", geneSymbol, entrezId, modelId, currentBestMatch.getQueryPhenotype().getId(), currentBestMatch.getMatchPhenotype().getId(), currentBestMatch.getScore(), match.getQueryPhenotype().getId(), match.getMatchPhenotype().getId(), match.getScore());
            geneModelPhenotypeMatches.get(entrezId).get(modelId).put(hpQueryTerm, match);
        }
    }

    private void calculateBestScoresFromHumanPhenotypes(Set<String> hpIdsWithPhenotypeMatch, Map<String, Double> bestMappedTermScore, Map<String, String> bestMappedTermMpId, Map<String, Float> mappedTerms) {
        // calculate perfect model scores for human
        double sumBestScore = 0d;
        // loop over each hp id should start here
        for (String hpId : hpIdsWithPhenotypeMatch) {
            if (bestMappedTermScore.containsKey(hpId)) {
                double hpScore = bestMappedTermScore.get(hpId);
                // add in scores for best match for the HP term
                sumBestScore += hpScore;
                //TODO: do bestMaxScore and bestAvgScore need to be global?
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

    private void addBestMappedTerm(PhenotypeMatch match, Map<String, Double> bestMappedTermScore, String hpId, Map<String, String> bestMappedTermMpId, String mpId) {
        //TODO: this should be a PhenotypeMatch
        bestMappedTermScore.put(hpId, match.getScore());
        bestMappedTermMpId.put(hpId, mpId);
    }

    private void addScoreIfAbsentOrBetter(int entrezGeneId, double score, String modelId, Map<Integer, Double> geneIdToScoreMap, Map<Integer, String> geneIdToModelIdMap) {
        if (!geneIdToScoreMap.containsKey(entrezGeneId) || score > geneIdToScoreMap.get(entrezGeneId)) {
            geneIdToScoreMap.put(entrezGeneId, score);
            geneIdToModelIdMap.put(entrezGeneId, modelId);
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
        diseaseTerms = getDiseaseTermsCache();
    }

    private Map<String, PhenotypeTerm> getHpoTermsCache() {
        Set<PhenotypeTerm> allHpoTerms = ontologyService.getHpoTerms();
        Map<String, PhenotypeTerm> termsCache = makeGenericOntologyTermCache(allHpoTerms);
        logger.info("HPO cache initialised with {} terms", termsCache.size());
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
