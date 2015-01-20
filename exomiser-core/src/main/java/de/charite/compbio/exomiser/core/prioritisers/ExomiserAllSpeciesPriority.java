package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.PrioritiserService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.jblas.FloatMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter genes according to the random walk proximity in the protein-protein
 * interaction network.
 * <P>
 * The files required for the constructor of this filter should be downloaded
 * from:
 * http://compbio.charite.de/hudson/job/randomWalkMatrix/lastSuccessfulBuild/artifact/
 * <P>
 * This class coordinates random walk analysis as described in the paper <a
 * hred="http://www.ncbi.nlm.nih.gov/pubmed/18371930"> Walking the interactome
 * for prioritization of candidate disease genes</a>.
 *
 * @see <a
 * href="http://compbio.charite.de/hudson/job/randomWalkMatrix/">RandomWalk
 * Hudson page</a>
 * @author Sebastian Koehler
 * @version 0.09 (3 November, 2013)
 */
public class ExomiserAllSpeciesPriority implements Priority {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserAllSpeciesPriority.class);

    private Connection connection = null;
    
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private ArrayList<String> messages = new ArrayList<String>();

    /**
     * The random walk matrix object
     */
    private final DataMatrix randomWalkMatrix;
    
//    private final PrioritiserService prioritiserService;
    
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
    private Map<String, String> hpoTerms = new HashMap<>();
    private Map<String, String> mpoTerms = new HashMap<>();
    private Map<String, String> zpoTerms = new HashMap<>();
    private Map<String, String> diseaseTerms = new HashMap<>();
    
    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpMpMatches = new HashMap<>();
    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpHpMatches = new HashMap<>();
    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpZpMatches = new HashMap<>();
    
    private float best_max_score = 0f;
    private float best_avg_score = 0f;
    
    private boolean runPpi = false;
    private boolean runHuman = false;
    private boolean runMouse = false;
    private boolean runFish = false;

    /**
     * This is the matrix of similarities between the seeed genes and all genes
     * in the network, i.e., p<sub>infinity</sub>.
     */
    private FloatMatrix weightedHighQualityMatrix;

    /**
     * Create a new instance of the {@link GenewandererPriority}.
     *
     * Assumes the list of seed genes (Entrez gene IDs) has been set!! This
     * happens with the method {@link #setParameters}.
     *
     * @param hpoIds
     * @param candidateGene
     * @param disease
     * @param randomWalkMatrix
     * @see <a href="http://compbio.charite.de/hudson/job/randomWalkMatrix/">Uberpheno
     * Hudson page</a>
     */
    public ExomiserAllSpeciesPriority(List<String> hpoIds, String candidateGene, String disease, String exomiser2Params, DataMatrix randomWalkMatrix) {
        this.hpoIds = hpoIds;
        this.candidateGeneSymbol = candidateGene;
        this.diseaseId = disease;
        this.randomWalkMatrix = randomWalkMatrix;
        parseParams(exomiser2Params);
        //logger.info("PPI"+ppi+":"+"HUMAN"+human+":"+"MOUSE"+mouse+":"+"FISH"+fish);
        //logger.info("Using randomWalkMatrix: {}", randomWalkMatrix);
    }

    private void parseParams(String exomiser2Params) {
        //logger.info("Params are " + exomiser2Params);
        if (exomiser2Params.isEmpty()) {
            this.runPpi = true;
            this.runHuman = true;
            this.runMouse = true;
            this.runFish = true;
        } else {
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

    /**
     * @see exomizer.priority.IPriority#getPriorityName()
     */
    @Override
    public String getPriorityName() {
        return "Phenotypic analysis";
    }

    /**
     * Flag to output results of filtering against Genewanderer.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.EXOMISER_ALLSPECIES_PRIORITY;
    }

    /**
     * Prioritize a list of candidate {@link exomizer.exome.Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     * <P>
     * You have to call {@link #setParameters} before running this function.
     *
     * @param genes List of candidate genes.
     * @see exomizer.filter.Filter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {

        if (diseaseId != null && !diseaseId.isEmpty() && hpoIds.isEmpty()) {
            logger.info("Setting HPO IDs using disease annotations for {}", diseaseId);
            hpoIds = getHpoIdsForDisease(diseaseId);
        }
        
        computeDistanceAllNodesFromStartNodes(hpoIds);
        
        int PPIdataAvailable = 0;
        int totalGenes = genes.size();
        
        logger.info("Scoring genes...");
        for (Gene gene : genes) {
            String evidence = "";
            String humanPhenotypeEvidence = "";
            String mousePhenotypeEvidence = "";
            String fishPhenotypeEvidence = "";
            double val = 0f;
            double humanScore = 0f;
            double mouseScore = 0f;
            double fishScore = 0f;
            double walkerScore = 0f;
            // DIRECT PHENO HIT
            int entrezGeneId = gene.getEntrezGeneID();
            if (scores.containsKey(entrezGeneId)) {
                val = scores.get(entrezGeneId);
                // HUMAN
                if (humanScores.containsKey(entrezGeneId)) {
                    humanScore = humanScores.get(entrezGeneId);
                    String diseaseId = humanDiseases.get(entrezGeneId);
                    String originalDiseaseId = diseaseId;
                    String diseaseTerm = diseaseTerms.get(diseaseId);
                    String diseaseLink = makeDiseaseLink(diseaseId, diseaseTerm);
                    evidence = evidence + String.format("<ul><li>Phenotypic similarity %.3f to %s associated with %s. <a class=\"op1\" id=\"" + entrezGeneId + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", humanScores.get(gene.getEntrezGeneID()), diseaseLink, gene.getGeneSymbol());
                    if (hpHpMatches.get(entrezGeneId) != null && hpHpMatches.get(entrezGeneId).get(originalDiseaseId) != null) {
                        for (String hpId : hpoIds) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpHpMatches.get(entrezGeneId).get(originalDiseaseId).get(hpId) != null) {
                                Set<Float> hpIdScores = hpHpMatches.get(entrezGeneId).get(originalDiseaseId).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String hpIdHit = hpHpMatches.get(entrezGeneId).get(originalDiseaseId).get(hpId).get(hpIdScore);
                                    String hpTermHit = hpoTerms.get(hpIdHit);
                                    humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("<ul><li>%s (%s) - %s (%s)</li></ul>", hpTerm, hpId, hpTermHit, hpIdHit);
                                }
                            } else {
                                humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("<ul><li>%s (%s) - </li></ul>", hpTerm, hpId);
                            }
                        }
                    }
                    evidence = evidence + humanPhenotypeEvidence;
                }
                // MOUSE
                if (mouseScores.containsKey(entrezGeneId)) {
                    mouseScore = mouseScores.get(entrezGeneId);
                    String mouseModel = mouseDiseases.get(entrezGeneId);
                    evidence = evidence + String.format("<ul><li>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>. <a class=\"op2\" id=\"" + entrezGeneId + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", mouseScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                    if (hpMpMatches.get(gene.getEntrezGeneID()) != null && hpMpMatches.get(gene.getEntrezGeneID()).get(mouseModel) != null) {
                        for (String hpId : hpoIds) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpMpMatches.get(entrezGeneId).get(mouseModel).get(hpId) != null) {
                                Set<Float> hpIdScores = hpMpMatches.get(entrezGeneId).get(mouseModel).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String mpIdHit = hpMpMatches.get(entrezGeneId).get(mouseModel).get(hpId).get(hpIdScore);
                                    String mpTermHit = mpoTerms.get(mpIdHit);
                                    mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("<ul><li>%s (%s) - %s (%s)</li></ul>", hpTerm, hpId, mpTermHit, mpIdHit);
                                }
                            } else {
                                mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("<ul><li>%s (%s) - </li></ul>", hpTerm, hpId);
                            }
                        }
                    }
                    evidence = evidence + mousePhenotypeEvidence;
                }
                // FISH
                if (fishScores.containsKey(entrezGeneId)) {
                    fishScore = fishScores.get(entrezGeneId);
                    String fishModel = fishDiseases.get(entrezGeneId);
                    evidence = evidence + String.format("<ul><li>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>. <a class=\"op3\" id=\"" + entrezGeneId + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", fishScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                    //evidence = evidence + "<br>Best phenotype matches: ";
                    if (hpZpMatches.get(gene.getEntrezGeneID()) != null && hpZpMatches.get(gene.getEntrezGeneID()).get(fishModel) != null) {
                        for (String hpId : hpoIds) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpZpMatches.get(entrezGeneId).get(fishModel).get(hpId) != null) {
                                Set<Float> hpIdScores = hpZpMatches.get(entrezGeneId).get(fishModel).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String zpIdHit = hpZpMatches.get(entrezGeneId).get(fishModel).get(hpId).get(hpIdScore);
                                    String zpTermHit = zpoTerms.get(zpIdHit);
                                    fishPhenotypeEvidence = fishPhenotypeEvidence + String.format("<ul><li>%s (%s) - %s (%s)</li></ul>", hpTerm, hpId, zpTermHit, zpIdHit);
                                }
                            } else {
                                fishPhenotypeEvidence = fishPhenotypeEvidence + String.format("<ul><li>%s (%s) - </li></ul>", hpTerm, hpId);
                            }
                        }
                    }
                    evidence = evidence + fishPhenotypeEvidence;
                }
                ++PPIdataAvailable;
            } //INTERACTION WITH A HIGH QUALITY MOUSE/HUMAN PHENO HIT => 0 to 0.65 once scaled
            if (runPpi && randomWalkMatrix.containsGene(entrezGeneId) && !phenoGenes.isEmpty()) {
                int col_idx = getColumnIndexOfMostPhenotypicallySimilarGene(gene, phenoGenes);
                int row_idx = randomWalkMatrix.getRowIndexForGene(gene.getEntrezGeneID());
                walkerScore = weightedHighQualityMatrix.get(row_idx, col_idx);
                if (walkerScore <= 0.00001) {
                    walkerScore = 0f;
                } else {
                    //walkerScore = val;
                    String closestGene = phenoGeneSymbols.get(col_idx);
                    String thisGene = gene.getGeneSymbol();
                    //String stringDbImageLink = "http://string-db.org/api/image/networkList?identifiers=" + thisGene + "%0D" + closestGene + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                    String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + thisGene + "%0D" + closestGene + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                    humanPhenotypeEvidence = "";
                    mousePhenotypeEvidence = "";
                    fishPhenotypeEvidence = "";
                    entrezGeneId = phenoGenes.get(col_idx);
                    double phenoScore = scores.get(entrezGeneId);
                    // HUMAN
                    if (humanScores.containsKey(entrezGeneId)) {
                        double humanPPIScore = humanScores.get(entrezGeneId);
                        String diseaseId = humanDiseases.get(entrezGeneId);
                        String originalDiseaseId = diseaseId;
                        String diseaseTerm = diseaseTerms.get(diseaseId);
                        String diseaseLink = makeDiseaseLink(diseaseId, diseaseTerm);
                        evidence = evidence + String.format("<ul><li>Proximity in <a href=\"%s\">interactome to %s</a> with score %s and phenotypic similarity to %s associated with %s. <a class=\"op1\" id=\"" + gene.getEntrezGeneID() + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", stringDbLink, closestGene, humanPPIScore, diseaseLink, closestGene);
                        //evidence = evidence + "<br>Best phenotype matches";
                        if (hpHpMatches.containsKey(entrezGeneId) && hpHpMatches.get(entrezGeneId).containsKey(originalDiseaseId)) {
                            for (String hpId : hpoIds) {
                                String hpTerm = hpoTerms.get(hpId);
                                if (hpHpMatches.get(entrezGeneId).get(originalDiseaseId).get(hpId) != null) {
                                    Set<Float> hpIdScores = hpHpMatches.get(entrezGeneId).get(originalDiseaseId).get(hpId).keySet();
                                    for (float hpIdScore : hpIdScores) {
                                        String hpIdHit = hpHpMatches.get(entrezGeneId).get(originalDiseaseId).get(hpId).get(hpIdScore);
                                        String hpTermHit = hpoTerms.get(hpIdHit);
                                        humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("<ul><li>%s (%s) - %s (%s)</li></ul>", hpTerm, hpId, hpTermHit, hpIdHit);
                                    }
                                } else {
                                    humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("<ul><li>%s (%s) - </li></ul>", hpTerm, hpId);
                                }
                            }
                        }
                        evidence = evidence + humanPhenotypeEvidence;
                    }
                    // MOUSE
                    if (mouseScores.containsKey(entrezGeneId)) {
                        String mouseModel = mouseDiseases.get(entrezGeneId);
                        evidence = evidence + String.format("<ul><li>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to mouse mutant of %s. <a class=\"op2\" id=\"" + gene.getEntrezGeneID() + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", stringDbLink, closestGene, closestGene);
                        //evidence = evidence + "<br>Best phenotype matches: ";
                        if (hpMpMatches.containsKey(entrezGeneId) && hpMpMatches.get(entrezGeneId).containsKey(mouseModel)) {
                            for (String hpId : hpoIds) {
                                String hpTerm = hpoTerms.get(hpId);
                                if (hpMpMatches.get(entrezGeneId).get(mouseModel).get(hpId) != null) {
                                    Set<Float> hpIdScores = hpMpMatches.get(entrezGeneId).get(mouseModel).get(hpId).keySet();
                                    for (float hpIdScore : hpIdScores) {
                                        String mpIdHit = hpMpMatches.get(entrezGeneId).get(mouseModel).get(hpId).get(hpIdScore);
                                        String mpTermHit = mpoTerms.get(mpIdHit);
                                        mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("<ul><li>%s (%s) - %s (%s)</li></ul>", hpTerm, hpId, mpTermHit, mpIdHit);
                                    }
                                } else {
                                    mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("<ul><li>%s (%s) - </li></ul>", hpTerm, hpId);
                                }
                            }
                        }
                        evidence = evidence + mousePhenotypeEvidence;

                    }
                    // FISH
                    if (fishScores.containsKey(entrezGeneId)) {
                        String fishModel = fishDiseases.get(entrezGeneId);
                        evidence = evidence + String.format("<ul><li>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to fish mutant of %s.  <a class=\"op3\" id=\"" + gene.getEntrezGeneID() + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", stringDbLink, closestGene, closestGene);
                        //evidence = evidence + "<br>Best phenotype matches: ";
                        if (hpZpMatches.containsKey(entrezGeneId) && hpZpMatches.get(entrezGeneId).containsKey(fishModel)) {
                            for (String hpId : hpoIds) {
                                String hpTerm = hpoTerms.get(hpId);
                                if (hpZpMatches.get(entrezGeneId).get(fishModel).get(hpId) != null) {
                                    Set<Float> hpIdScores = hpZpMatches.get(entrezGeneId).get(fishModel).get(hpId).keySet();
                                    for (float hpIdScore : hpIdScores) {
                                        String zpIdHit = hpZpMatches.get(entrezGeneId).get(fishModel).get(hpId).get(hpIdScore);
                                        String zpTermHit = zpoTerms.get(zpIdHit);
                                        fishPhenotypeEvidence = fishPhenotypeEvidence + String.format("<ul><li>%s (%s) - %s (%s)</li></ul>", hpTerm, hpId, zpTermHit, zpIdHit);
                                    }
                                } else {
                                    fishPhenotypeEvidence = fishPhenotypeEvidence + String.format("<ul><li>%s (%s) - </li></ul>", hpTerm, hpId);
                                }
                            }
                        }
                        evidence = evidence + fishPhenotypeEvidence;
                    }
                    ++PPIdataAvailable;
                }
            } // NO PHENO HIT OR PPI INTERACTION
            if (evidence.equals("")) {
                evidence = "<ul><li>No phenotype or PPI evidence</li></ul>";
            }
            ExomiserAllSpeciesPriorityResult relScore = new ExomiserAllSpeciesPriorityResult(val, evidence, humanPhenotypeEvidence, mousePhenotypeEvidence,
                    fishPhenotypeEvidence, humanScore, mouseScore, fishScore, walkerScore);
            gene.addPriorityResult(relScore);
        }

        /*
         * refactor all scores for genes that are not direct pheno-hits but in
         * PPI with them to a linear range
         */
        logger.info("Adjusting gene scores for non-pheno hits with protein-protein interactions");
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<Float, List<Gene>>();
        for (Gene g : genes) {
            //if (randomWalkMatrix.getObjectid2idx().containsKey(g.getEntrezGeneID())) {// Only do for PPI hits
            //float geneScore = g.getPriorityResult(PriorityType.EXOMISER_ALLSPECIES_PRIORITY);
            float geneScore = ((ExomiserAllSpeciesPriorityResult) g.getPriorityResult(PriorityType.EXOMISER_ALLSPECIES_PRIORITY)).getWalkerScore();
            if (geneScore == 0f) {
                continue;
            }
            if (geneScoreMap.containsKey(geneScore)) {
                List<Gene> geneScoreGeneList = geneScoreMap.get(geneScore);
                geneScoreGeneList.add(g);
            } else {
                List<Gene> geneScoreGeneList = new ArrayList<Gene>();
                geneScoreGeneList.add(g);
                geneScoreMap.put(geneScore, geneScoreGeneList);
            }
            //}
        }
        float rank = 0;//changed so when have only 2 genes say in filtered set 1st one will get 0.6 and second 0.3 rather than 0.3 and 0
        Set<Float> set = geneScoreMap.descendingKeySet();
        Iterator<Float> i = set.iterator();
        while (i.hasNext()) {
            Float score = i.next();
            List<Gene> geneScoreGeneList = geneScoreMap.get(score);
            int sharedHits = geneScoreGeneList.size();
            float adjustedRank = rank;
            if (sharedHits > 1) {
                adjustedRank = rank + (sharedHits / 2);
            }
            //float newScore = 0.65f - 0.65f * (adjustedRank / gene_list.size());
            float newScore = 0.6f - 0.6f * (adjustedRank / genes.size());
            rank = rank + sharedHits;
            for (Gene gene : geneScoreGeneList) {
                //i.e. only overwrite phenotype-based score if PPI score is larger
                ExomiserAllSpeciesPriorityResult result = (ExomiserAllSpeciesPriorityResult) gene.getPriorityResult(PriorityType.EXOMISER_ALLSPECIES_PRIORITY);
                if (newScore > result.getScore()) {
                    result.setScore(newScore);
                }
            }
        }
        String s = String.format("Phenotype and Protein-Protein Interaction evidence was available for %d of %d genes (%.1f%%)",
                PPIdataAvailable, totalGenes, 100f * ((float) PPIdataAvailable / (float) totalGenes));
        messages.add(s);
        String hpInput = "";
        for (String hp : hpoIds) {
            String hpTerm = hpoTerms.get(hp);
            hpInput = hpInput + (String.format("%s (%s), ", hpTerm, hp));
        }
        //this.messages.add(hpInput);// now display all HPO terms on results no need for this
        closeConnection();
    }

    private String makeDiseaseLink(String diseaseId, String diseaseTerm) {
        if (diseaseId.split(":")[0].equals("OMIM")) {
            diseaseId = diseaseId.split(":")[1];
            return "<a href=\"http://www.omim.org/" + diseaseId + "\">" + diseaseTerm + "</a>";
        } else {
            diseaseId = diseaseId.split(":")[1];
            return "<a href=\"http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=" + diseaseId + "\">" + diseaseTerm + "</a>";
        }
    }
    
    //TODO - this shouldn' exist. runDynamicQuery should have two variants - one for human the other for non-human
    private enum Species {
        HUMAN, MOUSE, FISH;
    }
 
    /**
     * Compute the distance of all genes in the Random Walk matrix to the set of
     * seed genes given by the user.
     */
    private void computeDistanceAllNodesFromStartNodes(List<String> hpoIds) {
        
        // Human
        String mapping_query = String.format("SELECT hp_id_hit, score FROM hp_hp_mappings M WHERE M.hp_id = ?");
        PreparedStatement findMappingStatement = null;
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            logger.error(error, e);
        }
        PreparedStatement findAnnotationStatement = null;
        String annotation = String.format("SELECT H.disease_id, hp_id, gene_id, human_gene_symbol FROM human2mouse_orthologs hm, disease_hp M, disease H WHERE hm.entrez_id=H.gene_id AND M.disease_id=H.disease_id");
        try {
            findAnnotationStatement = connection.prepareStatement(annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + annotation;
            logger.error(error, e);
        }
        hpHpMatches = runDynamicQuery(findMappingStatement, findAnnotationStatement, hpoIds, Species.HUMAN);
        
        // Mouse
        mapping_query = String.format("SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?");
        findMappingStatement = null;
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            logger.error(error, e);
        }
        annotation = String.format("SELECT mouse_model_id, mp_id, entrez_id, human_gene_symbol, M.mgi_gene_id, M.mgi_gene_symbol FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'");
        try {
            findAnnotationStatement = connection.prepareStatement(annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + annotation;
            logger.error(error, e);
        }
        hpMpMatches = runDynamicQuery(findMappingStatement, findAnnotationStatement, hpoIds, Species.MOUSE);

        // Fish
        mapping_query = String.format("SELECT zp_id, score FROM hp_zp_mappings M WHERE M.hp_id = ?");
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            logger.error(error, e);
        }
        annotation = String.format("SELECT zfin_model_id, zp_id, entrez_id, human_gene_symbol, M.zfin_gene_id, M.zfin_gene_symbol FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'");
        try {
            findAnnotationStatement = connection.prepareStatement(annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + annotation;
            logger.error(error, e);
        }
        hpZpMatches = runDynamicQuery(findMappingStatement, findAnnotationStatement, hpoIds, Species.FISH);
        
        logger.info("Making weighted-score Protein-Protein interaction sub-matrix from high quality phenotypic gene matches...");
        weightedHighQualityMatrix = makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(phenoGenes, scores);
    }

    private Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> runDynamicQuery(PreparedStatement findMappingStatement, PreparedStatement findAnnotationStatement, List<String> hps_initial, Species species) {

        ArrayList<String> hp_list = new ArrayList<>();
        HashMap<String, Float> mapped_terms = new HashMap<>();
        HashMap<String, Float> best_mapped_term_score = new HashMap<>();
        HashMap<String, String> best_mapped_term_mpid = new HashMap<>();
        HashMap<String, Integer> knownMps = new HashMap<>();
        for (String hpid : hps_initial) {
            try {
                findMappingStatement.setString(1, hpid);
                ResultSet rs = findMappingStatement.executeQuery();
                int found = 0;
                while (rs.next()) {
                    String mp_id = rs.getString(1);
                    knownMps.put(mp_id, 1);
                    StringBuilder hashKey = new StringBuilder();
                    hashKey.append(hpid);
                    hashKey.append(mp_id);
                    float score = rs.getFloat(2);
                    mapped_terms.put(hashKey.toString(), score);
                    if (species == Species.HUMAN && hpid.equals(mp_id)) {
                        addBestMappedTerm(best_mapped_term_score, hpid, score, best_mapped_term_mpid, mp_id);
                        found = 1;//for some hp terms e.g. HP we won't have the self hit but still want to flag found
                    } else {
                        if (best_mapped_term_score.containsKey(hpid)) {
                            if (score > best_mapped_term_score.get(hpid)) {
                                addBestMappedTerm(best_mapped_term_score, hpid, score, best_mapped_term_mpid, mp_id);
                            }
                        } else {
                            addBestMappedTerm(best_mapped_term_score, hpid, score, best_mapped_term_mpid, mp_id);
                            found = 1;
                        }
                    }
                }
                if (found == 1) {
                    hp_list.add(hpid);
                }
            } catch (SQLException e) {
                String error = "Problem setting up SQL query:";
                logger.error(error, e);
            }
        }
        String[] hps = new String[hp_list.size()];
        hp_list.toArray(hps);
        // calculate perfect model scores for human
        if (species == Species.HUMAN) {
            float sum_best_score = 0f;
            int best_hit_counter = 0;
            // loop over each hp id should start here
            for (String hpid : hps) {
                if (best_mapped_term_score.get(hpid) != null) {
                    float hp_score = best_mapped_term_score.get(hpid);
                    // add in scores for best match for the HP term                                                                                                                                                
                    sum_best_score += hp_score;

                    best_hit_counter++;
                    if (hp_score > best_max_score) {
                        this.best_max_score = hp_score;
                    }
                    //logger.info("ADDING SCORE FOR " + hpid + " TO " + best_mapped_term_mpid.get(hpid) + " WITH SCORE " + hp_score + ", SUM NOW " + sum_best_score + ", MAX NOW " + this.best_max_score);
                    // add in MP-HP hits                                                                                                                                                                           
                    String mpid = best_mapped_term_mpid.get(hpid);
                    float best_score = 0f;
                    for (String hpid2 : hps) {
                        StringBuilder hashKey = new StringBuilder();
                        hashKey.append(hpid2);
                        hashKey.append(mpid);
                        if (mapped_terms.get(hashKey.toString()) != null && mapped_terms.get(hashKey.toString()) > best_score) {
                            best_score = mapped_terms.get(hashKey.toString());
                        }
                    }
                    // add in scores for best match for the MP term                                                                                                                                                
                    sum_best_score += best_score;
                    //logger.info("ADDING RECIPROCAL SCORE FOR " + mpid + " WITH SCORE " + best_score + ", SUM NOW " + sum_best_score + ", MAX NOW " + this.best_max_score);
                    best_hit_counter++;
                    if (best_score > best_max_score) {
                        this.best_max_score = best_score;
                    }
                }
            }
            //this.best_avg_score = sum_best_score / best_hit_counter;
            this.best_avg_score = sum_best_score / (2 * hps.length);
        }
        
        if (species == Species.HUMAN && !runHuman) {
            return Collections.EMPTY_MAP;
        }
        if (species == Species.MOUSE && !runMouse) {
            return Collections.EMPTY_MAP;
        }
        if (species == Species.FISH && !runFish) {
            return Collections.EMPTY_MAP;
        }

        // calculate score for this gene
        Map<Integer, HashMap<String, HashMap<String, HashMap<Float, String>>>> hpMatches = new HashMap<>();
        try {
            logger.info("Fetching disease/model phenotype annotations and human model organism gene orthologs");
            ResultSet rs = findAnnotationStatement.executeQuery();
            while (rs.next()) {
                String hit = rs.getString(1);
                String mp_ids = rs.getString(2);
                int entrezId = rs.getInt(3);
                String humanGeneSymbol = rs.getString(4);
                String[] mp_initial = mp_ids.split(",");
                ArrayList<String> mp_list = new ArrayList<>();
                for (String mpid : mp_initial) {
                    if (knownMps.get(mpid) != null) {
                        mp_list.add(mpid);
                    }
                }
                String[] mps = new String[mp_list.size()];
                mp_list.toArray(mps);

                int row_column_count = hps.length + mps.length;
                float max_score = 0f;
                float sum_best_hit_rows_columns_score = 0f;

                for (String hpid : hps) {
                    float best_score = 0f;
                    for (String mpid : mps) {
                        StringBuilder hashKey = new StringBuilder();
                        hashKey.append(hpid);
                        hashKey.append(mpid);
                        if (mapped_terms.get(hashKey.toString()) != null) {
                            float score = mapped_terms.get(hashKey.toString());
                            // identify best match                                                                                                                                                                 
                            if (score > best_score) {
                                best_score = score;
                            }
                            if (score > 0) {
                                if (hpMatches.get(entrezId) == null) {
                                    hpMatches.put(entrezId, new HashMap<String, HashMap<String, HashMap<Float, String>>>());
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrezId).get(hit) == null) {
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpid) == null) {
                                    hpMatches.get(entrezId).get(hit).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpid).keySet().iterator().next() < score) {
                                    hpMatches.get(entrezId).get(hit).get(hpid).clear();
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                }
                            }
                        }
                    }
                    if (best_score != 0) {
                        sum_best_hit_rows_columns_score += best_score;

                        if (best_score > max_score) {
                            max_score = best_score;
                        }
                    }
                }
                // Reciprocal hits                                                                                                                                                                                 
                for (String mpid : mps) {
                    float best_score = 0f;
                    for (String hpid : hps) {
                        StringBuilder hashKey = new StringBuilder();
                        hashKey.append(hpid);
                        hashKey.append(mpid);
                        if (mapped_terms.get(hashKey.toString()) != null) {
                            float score = mapped_terms.get(hashKey.toString());
                            // identify best match                                                                                                                                                                 
                            if (score > best_score) {
                                best_score = score;
                            }
                            if (score > 0) {
                                if (hpMatches.get(entrezId) == null) {
                                    hpMatches.put(entrezId, new HashMap<String, HashMap<String, HashMap<Float, String>>>());
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrezId).get(hit) == null) {
                                    hpMatches.get(entrezId).put(hit, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrezId).get(hit).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpid) == null) {
                                    hpMatches.get(entrezId).get(hit).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrezId).get(hit).get(hpid).keySet().iterator().next() < score) {
                                    hpMatches.get(entrezId).get(hit).get(hpid).clear();
                                    hpMatches.get(entrezId).get(hit).get(hpid).put(score, mpid);
                                }
                            }
                        }
                    }
                    if (best_score != 0) {
                        sum_best_hit_rows_columns_score += best_score;
                        if (best_score > max_score) {
                            max_score = best_score;
                        }
                    }
                }
                // calculate combined score
                if (sum_best_hit_rows_columns_score != 0) {
                    double avg_best_hit_rows_columns_score = sum_best_hit_rows_columns_score / row_column_count;
                    double combined_score = 50 * (max_score / best_max_score
                            + avg_best_hit_rows_columns_score / best_avg_score);
                    if (combined_score > 100) {
                        combined_score = 100;
                    }
                    double score = combined_score / 100;
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
                        if (score > 0.6) {// only build PPI network for high qual hits
                            phenoGenes.add(entrezId);
                            phenoGeneSymbols.add(humanGeneSymbol);
                        }
                        if (scores.get(entrezId) == null || score > scores.get(entrezId)) {
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
            }//end of rs
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:";
            logger.error(error, e);
        }
        return hpMatches;
    }

    private void addBestMappedTerm(HashMap<String, Float> best_mapped_term_score, String hpid, float score, HashMap<String, String> best_mapped_term_mpid, String mp_id) {
        best_mapped_term_score.put(hpid, score);
        best_mapped_term_mpid.put(hpid, mp_id);
    }

    private void addScoreIfAbsentOrBetter(int entrez, double score, String hit, Map<Integer, Double> geneToScoreMap, Map<Integer, String> geneToDiseaseMap) {
        if (geneToScoreMap.get(entrez) == null || score > geneToScoreMap.get(entrez)) {
            geneToScoreMap.put(entrez, score);
            geneToDiseaseMap.put(entrez, hit);
        }
    }

   //todo: If this returned a DataMatrix things might be a bit more convienent later on... 
    private FloatMatrix makeWeightedProteinInteractionMatrixFromHighQualityPhenotypeMatchedGenes(List<Integer> phenoGenes, Map<Integer, Float> scores) {
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
    public ArrayList<String> getMessages() {

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
            return "Error initializing Random Walk matrix";
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

    /**
     * Initialize the database connection and call
     * {@link #setUpSQLPreparedStatements}
     *
     * @param connection A connection to a postgreSQL database from the exomizer
     * or tomcat.
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
        setUpOntologyCaches();
    }

    private void setUpOntologyCaches() {
        setUpDiseaseTermsCache();
        setUpHpoTermsCache();
        setUpMpoTermsCache();
        setUpZpoTermsCache();
    }

    private void setUpZpoTermsCache() {
        try {
            PreparedStatement zpoTermsStatement = connection.prepareStatement("SELECT zp_id, zp_term FROM zp");
            ResultSet rs = zpoTermsStatement.executeQuery();
            while (rs.next()) {
                String zpId = rs.getString(1);
                String zpTerm = rs.getString(2);
                zpId = zpId.trim();
                zpoTerms.put(zpId, zpTerm);
            }
        } catch (SQLException e) {
            logger.error("Unable to fetch ZPO terms", e);
        }
        logger.info("ZPO cache initialised with {} terms", zpoTerms.size());
    }

    private void setUpMpoTermsCache() {
        try {
            PreparedStatement mpoTermsStatement = connection.prepareStatement("SELECT mp_id, mp_term FROM mp");
            ResultSet rs = mpoTermsStatement.executeQuery();
            while (rs.next()) {
                String mpId = rs.getString(1);
                String mpTerm = rs.getString(2);
                mpId = mpId.trim();
                mpoTerms.put(mpId, mpTerm);
            }
        } catch (SQLException e) {
            logger.error("Unable to fetch MPO terms", e);
        }
        logger.info("MPO cache initialised with {} terms", mpoTerms.size());
    }

    private void setUpHpoTermsCache() {
        // retrieve hp and mp id to term mappings
        try {
            PreparedStatement hpoTermsStatement = connection.prepareStatement("select id, lcname from hpo");
            ResultSet rs = hpoTermsStatement.executeQuery();
            while (rs.next()) {
                String hpId = rs.getString(1);
                String hpTerm = rs.getString(2);
                hpId = hpId.trim();
                hpoTerms.put(hpId, hpTerm);
            }
            
        } catch (SQLException e) {
            logger.error("Unable to retrieve HPO terms", e);
        }
        logger.info("HPO cache initialised with {} terms", hpoTerms.size());
    }

    private void setUpDiseaseTermsCache() {
        // retrieve disease id to term mappings
        try {
            PreparedStatement diseaseTermsStatement = connection.prepareStatement("SELECT disease_id, diseasename FROM disease");
            ResultSet rs = diseaseTermsStatement.executeQuery();
            while (rs.next()) {
                String diseaseId = rs.getString(1);
                String diseaseTerm = rs.getString(2);
                diseaseId = diseaseId.trim();
                diseaseTerms.put(diseaseId, diseaseTerm);
            }
            
        } catch (SQLException e) {
            logger.error("Unable to fetch disease terms", e);
        }
        logger.info("Disease cache initialised with {} terms", diseaseTerms.size());
    }
    
    /**
     * Set hpo_ids variable based on the entered disease
     */
    private List<String> getHpoIdsForDisease(String disease) {
        String hpoListString = "";
        try {
            PreparedStatement hpoIdsStatement = connection.prepareStatement("SELECT hp_id FROM disease_hp WHERE disease_id = ?");
            hpoIdsStatement.setString(1, disease);
            ResultSet rs = hpoIdsStatement.executeQuery();
            rs.next();
            hpoListString = rs.getString(1);
        } catch (SQLException e) {
            logger.error("Unable to retrieve HPO terms for disease {}", disease, e);
        }
        List<String> diseaseHpoIds = parseHpoIdListFromString(hpoListString);
        logger.info("{} HPO ids retrieved for disease {} - {}", diseaseHpoIds.size(), disease, diseaseHpoIds);
        return diseaseHpoIds;
    }
    
    private List<String> parseHpoIdListFromString(String hpoIdsString) {
        String[] hpoArray = hpoIdsString.split(",");
        List<String> hpoIdList = new ArrayList<>();
        for (String string : hpoArray) {
            hpoIdList.add(string.trim());
        }
        return hpoIdList;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
    }
}
