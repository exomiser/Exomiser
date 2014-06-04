package de.charite.compbio.exomiser.priority;


import java.util.ArrayList;

import org.jblas.DoubleMatrix;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Filter genes according to the random walk proximity in the protein-protein
 * interaction network. <P> The files required for the constructor of this
 * filter should be downloaded from:
 * http://compbio.charite.de/hudson/job/randomWalkMatrix/lastSuccessfulBuild/artifact/
 * <P> This class coordinates random walk analysis as described in the paper <a
 * hred="http://www.ncbi.nlm.nih.gov/pubmed/18371930"> Walking the interactome
 * for prioritization of candidate disease genes</a>.
 *
 * @see <a
 * href="http://compbio.charite.de/hudson/job/randomWalkMatrix/">RandomWalk
 * Hudson page</a>
 * @author Sebastian Koehler
 * @version 0.09 (3 November, 2013)
 */
public class DynamicPhenoWandererPriority implements Priority {

    private Connection connection = null;
    /**
     * A list of error-messages
     */
    private ArrayList<String> error_record = null;
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private ArrayList<String> messages = null;
    /**
     * Number of variants considered by this filter
     */
    private int n_before = 0;
    /**
     * Number of variants after applying this filter.
     */
    private int n_after = 0;
    /**
     * The random walk matrix object
     */
    private DataMatrix randomWalkMatrix = null;
    private ArrayList<Integer> phenoGenes = new ArrayList<Integer>();
    private ArrayList<String> phenoGeneSymbols = new ArrayList<String>();
    private String hpo_ids;
    private String candGene;
    private String disease;
    private Map<Integer, Double> scores = new HashMap<Integer, Double>();
    private Map<Integer, Double> mouseScores = new HashMap<Integer, Double>();
    private Map<Integer, Double> humanScores = new HashMap<Integer, Double>();
    private Map<Integer, Double> fishScores = new HashMap<Integer, Double>();
    private Map<Integer, String> humanDiseases = new HashMap<Integer, String>();
    private Map<String, String> hpoTerms = new HashMap<String, String>();
    private Map<String, String> mpoTerms = new HashMap<String, String>();
    private Map<String, String> zpoTerms = new HashMap<String, String>();
    private Map<String, String> diseaseTerms = new HashMap<String, String>();
    private HashMap<Integer, HashMap<String, HashMap<Float, String>>> hpMpMatches = new HashMap<Integer, HashMap<String, HashMap<Float, String>>>();
    private HashMap<Integer, HashMap<String, HashMap<Float, String>>> hpHpMatches = new HashMap<Integer, HashMap<String, HashMap<Float, String>>>();
    private HashMap<Integer, HashMap<String, HashMap<Float, String>>> hpZpMatches = new HashMap<Integer, HashMap<String, HashMap<Float, String>>>();
    /**
     * This is the matrix of similarities between the seeed genes and all genes
     * in the network, i.e., p<sub>infinity</sub>.
     */
    private DoubleMatrix combinedProximityVector;

    /**
     * Create a new instance of the {@link GenewandererPriority}.
     *
     * Assumes the list of seed genes (Entrez gene IDs) has been set!! This
     * happens with the method
     *  {@link #setParameters}.
     *
     * @param randomWalkMatrixFileZip The zipped(!) RandomWalk matrix file.
     * @param randomWalkGeneId2IndexFileZip The zipped(!) file with the mapping
     * between Entrez-Ids and Matrix-Indices.
     * @throws ExomizerInitializationException
     * @see <a
     * href="http://compbio.charite.de/hudson/job/randomWalkMatrix/">Uberpheno
     * Hudson page</a>
     */
    public DynamicPhenoWandererPriority(String randomWalkMatrixFileZip, String randomWalkGeneId2IndexFileZip, String hpo_ids, String candGene, String disease, DataMatrix rwMatrix)
            throws ExomizerInitializationException {
        this.hpo_ids = hpo_ids;
        this.candGene = candGene;
        this.disease = disease;

        if (rwMatrix == null) {
            try {
                randomWalkMatrix = new DataMatrix(randomWalkMatrixFileZip, randomWalkGeneId2IndexFileZip, true);
            } catch (Exception e) {
                /*
                 * This exception is thrown if the files for the random walk
                 * cannot be found.
                 */
                String rwe = String.format("Unable to initialize the random walk matrix: %s", e.toString());
                throw new ExomizerInitializationException(rwe);
            }
        } else {
            System.out.println("USING bootstrapped matrix");
            randomWalkMatrix = rwMatrix;
        }
        /*
         * some logging stuff
         */
        this.error_record = new ArrayList<String>();
        this.messages = new ArrayList<String>();
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
    public PriorityType getPriorityTypeConstant() {
        return PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY;
    }

    /**
     * Set hpo_ids variable based on the entered disease
     */
    private void setHPOfromDisease(String disease) throws ExomizerInitializationException {
        String hpo_query = String.format("SELECT hp_id FROM disease_hp WHERE disease_id = ?");
        PreparedStatement hpoIdsStatement = null;
        try {
            hpoIdsStatement = connection.prepareStatement(hpo_query);
            hpoIdsStatement.setString(1, disease);
            ResultSet rs = hpoIdsStatement.executeQuery();
            rs.next();
            hpo_ids = rs.getString(1);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + hpo_query;
            throw new ExomizerInitializationException(error);
        }
    }

    private HashMap<Integer, HashMap<String, HashMap<Float, String>>> runDynamicQuery(PreparedStatement findMappingStatement, PreparedStatement findAnnotationStatement, String[] hps_initial, String species) throws ExomizerInitializationException {

        ArrayList<String> hp_list = new ArrayList<String>();
        HashMap<String, Float> mapped_terms = new HashMap<String, Float>();
        HashMap<String, Float> best_mapped_term_score = new HashMap<String, Float>();
        HashMap<String, String> best_mapped_term_mpid = new HashMap<String, String>();
        HashMap<String, Integer> knownMps = new HashMap<String, Integer>();
        HashMap<Integer, HashMap<String, HashMap<Float, String>>> hpMatches = new HashMap<Integer, HashMap<String, HashMap<Float, String>>>();
        for (String hpid : hps_initial) {
            try {
                findMappingStatement.setString(1, hpid);
                ResultSet rs = findMappingStatement.executeQuery();
                int found = 0;
                while (rs.next()) {
                    //found = 1;
                    String mp_id = rs.getString(1);
                    knownMps.put(mp_id, 1);
                    StringBuffer hashKey = new StringBuffer();
                    hashKey.append(hpid);
                    hashKey.append(mp_id);
                    float score = rs.getFloat(2);
                    mapped_terms.put(hashKey.toString(), score);
                    if (species.equals("human")) {
                        if (hpid.equals(mp_id)) {
                            best_mapped_term_score.put(hpid, score);
                            best_mapped_term_mpid.put(hpid, mp_id);
                            found = 1;
                        }
                    } else {
                        if (best_mapped_term_score.get(hpid) != null) {
                            if (score > best_mapped_term_score.get(hpid)) {
                                best_mapped_term_score.put(hpid, score);
                                best_mapped_term_mpid.put(hpid, mp_id);
                            }
                        } else {
                            best_mapped_term_score.put(hpid, score);
                            best_mapped_term_mpid.put(hpid, mp_id);
                            found = 1;
                        }
                    }
                }
                if (found == 1) {
                    hp_list.add(hpid);
                }
            } catch (SQLException e) {
                String error = "Problem setting up SQL query:";
                throw new ExomizerInitializationException(error);
            }
        }
        String[] hps = new String[hp_list.size()];
        hp_list.toArray(hps);
        // calculate perfect model scores
        float sum_best_score = 0f;
        float best_max_score = 0f;
        int best_hit_counter = 0;
        // loop over each hp id should start herre
        for (String hpid : hps) {
            if (best_mapped_term_score.get(hpid) != null) {
                float hp_score = best_mapped_term_score.get(hpid);
                // add in scores for best match for the HP term                                                                                                                                                
                sum_best_score += hp_score;
                best_hit_counter++;
                if (hp_score > best_max_score) {
                    best_max_score = hp_score;
                }
                // add in MP-HP hits                                                                                                                                                                           
                String mpid = best_mapped_term_mpid.get(hpid);
                float best_score = 0f;
                for (String hpid2 : hps) {
                    StringBuffer hashKey = new StringBuffer();
                    hashKey.append(hpid2);
                    hashKey.append(mpid);
                    if (mapped_terms.get(hashKey.toString()) != null && mapped_terms.get(hashKey.toString()) > best_score) {
                        best_score = mapped_terms.get(hashKey.toString());
                    }
                }
                // add in scores for best match for the MP term                                                                                                                                                
                sum_best_score += best_score;
                best_hit_counter++;
                if (best_score > best_max_score) {
                    best_max_score = best_score;
                }
            }
        }
        float best_avg_score = sum_best_score / best_hit_counter;
        // calculate score for this gene
        try {
            ResultSet rs = findAnnotationStatement.executeQuery();
            while (rs.next()) {
                String hit = rs.getString(1);
                String mp_ids = rs.getString(2);
                int entrez = rs.getInt(3);
                String humanGene = rs.getString(4);
                String[] mp_initial = mp_ids.split(",");
                ArrayList<String> mp_list = new ArrayList<String>();
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
                        StringBuffer hashKey = new StringBuffer();
                        hashKey.append(hpid);
                        hashKey.append(mpid);
                        if (mapped_terms.get(hashKey.toString()) != null) {
                            float score = mapped_terms.get(hashKey.toString());
                            // identify best match                                                                                                                                                                 
                            if (score > best_score) {
                                best_score = score;
                            }
                            if (score > 1.75) {
                                if (hpMatches.get(entrez) == null) {
                                    hpMatches.put(entrez, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrez).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrez).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrez).get(hpid) == null) {
                                    hpMatches.get(entrez).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrez).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrez).get(hpid).keySet().iterator().next() < score) {
                                    hpMatches.get(entrez).get(hpid).clear();
                                    hpMatches.get(entrez).get(hpid).put(score, mpid);
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
                        StringBuffer hashKey = new StringBuffer();
                        hashKey.append(hpid);
                        hashKey.append(mpid);
                        if (mapped_terms.get(hashKey.toString()) != null) {
                            float score = mapped_terms.get(hashKey.toString());
                            // identify best match                                                                                                                                                                 
                            if (score > best_score) {
                                best_score = score;
                            }
                            if (score > 1.75) {
                                if (hpMatches.get(entrez) == null) {
                                    hpMatches.put(entrez, new HashMap<String, HashMap<Float, String>>());
                                    hpMatches.get(entrez).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrez).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrez).get(hpid) == null) {
                                    hpMatches.get(entrez).put(hpid, new HashMap<Float, String>());
                                    hpMatches.get(entrez).get(hpid).put(score, mpid);
                                } else if (hpMatches.get(entrez).get(hpid).keySet().iterator().next() < score) {
                                    hpMatches.get(entrez).get(hpid).clear();
                                    hpMatches.get(entrez).get(hpid).put(score, mpid);
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
                    if (species.equals("human")) {
                        score = score + ((1 - score) / 2);
                    }
                    // adjust fish score - over-scoring at moment as even a perfect fish match is much worse than the mouse and human hits
                    if (species.equals("fish")) {
                        score = score - ((score) / 2);
                    }
                    // code to catch hit to known disease-gene association for purposes of benchmarking i.e to simulate novel gene discovery performance
                    if ((hit == null ? disease == null : hit.equals(disease))
                            && (humanGene == null ? candGene == null : humanGene.equals(candGene))) {
                        //System.out.println("FOUND self hit " + disease + ":"+candGene);
                        if (scores.get(entrez) != null) {
                            phenoGenes.add(entrez);
                            phenoGeneSymbols.add(humanGene);
                        }
                    } else {
                        if (scores.get(entrez) != null) {
                            if (score > scores.get(entrez)) {
                                scores.put(entrez, score);
                                if (species.equals("human")) {
                                    humanScores.put(entrez, score);
                                    humanDiseases.put(entrez, hit);
                                } else if (species.equals("fish")) {
                                    fishScores.put(entrez, score);
                                } else if (species.equals("mouse")) {
                                    mouseScores.put(entrez, score);
                                }
                            }
                        } else {
                            if (score > 0.65) {// only build PPI network for high qual hits
                                phenoGenes.add(entrez);
                                phenoGeneSymbols.add(humanGene);
                            }
                            scores.put(entrez, score);
                            if (species.equals("human")) {
                                humanScores.put(entrez, score);
                                humanDiseases.put(entrez, hit);
                            } else if (species.equals("fish")) {
                                fishScores.put(entrez, score);
                            } else if (species.equals("mouse")) {
                                mouseScores.put(entrez, score);
                            }
                        }
                    }
                }
            }//end of rs
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:";
            throw new ExomizerInitializationException(error);
        }
        return hpMatches;
    }

    /**
     * Compute the distance of all genes in the Random Walk matrix to the set of
     * seed genes given by the user.
     */
    private void computeDistanceAllNodesFromStartNodes() throws ExomizerInitializationException {
        if (disease != null) {
            setHPOfromDisease(disease);
        }
        // retrieve disease id to term mappings
        String disease_query = "SELECT disease_id, diseasename FROM disease";
        PreparedStatement diseaseTermsStatement = null;
        try {
            diseaseTermsStatement = connection.prepareStatement(disease_query);
            ResultSet rs = diseaseTermsStatement.executeQuery();
            while (rs.next()) {
                String diseaseId = rs.getString(1);
                String diseaseTerm = rs.getString(2);
                diseaseId = diseaseId.trim();
                diseaseTerms.put(diseaseId, diseaseTerm);
            }

        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + disease_query;
            throw new ExomizerInitializationException(error);
        }

        // retriev hp and mp id to term mappings
        String hpo_query = "select id, lcname from hpo";
        PreparedStatement hpoTermsStatement = null;
        try {
            hpoTermsStatement = connection.prepareStatement(hpo_query);
            ResultSet rs = hpoTermsStatement.executeQuery();
            while (rs.next()) {
                String hpId = rs.getString(1);
                String hpTerm = rs.getString(2);
                hpId = hpId.trim();
                hpoTerms.put(hpId, hpTerm);
            }

        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + hpo_query;
            throw new ExomizerInitializationException(error);
        }
        String mpo_query = "SELECT mp_id, mp_term FROM mp";
        PreparedStatement mpoTermsStatement = null;
        try {
            mpoTermsStatement = connection.prepareStatement(mpo_query);
            ResultSet rs = mpoTermsStatement.executeQuery();
            while (rs.next()) {
                String mpId = rs.getString(1);
                String mpTerm = rs.getString(2);
                mpId = mpId.trim();
                mpoTerms.put(mpId, mpTerm);
            }

        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + hpo_query;
            throw new ExomizerInitializationException(error);
        }
        String zpo_query = "SELECT zp_id, zp_term FROM zp";
        PreparedStatement zpoTermsStatement = null;
        try {
            zpoTermsStatement = connection.prepareStatement(zpo_query);
            ResultSet rs = zpoTermsStatement.executeQuery();
            while (rs.next()) {
                String zpId = rs.getString(1);
                String zpTerm = rs.getString(2);
                zpId = zpId.trim();
                zpoTerms.put(zpId, zpTerm);
            }

        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + hpo_query;
            throw new ExomizerInitializationException(error);
        }
        // Mouse
        String mapping_query = String.format("SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?");
        PreparedStatement findMappingStatement = null;
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            throw new ExomizerInitializationException(error);
        }
        PreparedStatement findAnnotationStatement = null;
        String annotation = String.format("SELECT mouse_model_id, mp_id, entrez_id, human_gene_symbol, M.mgi_gene_id, M.mgi_gene_symbol FROM mgi_mp M, human2mouse_orthologs H WHERE M.mgi_gene_id=H.mgi_gene_id and human_gene_symbol != 'null'");
        try {
            findAnnotationStatement = connection.prepareStatement(annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + annotation;
            throw new ExomizerInitializationException(error);
        }
        String[] hps_initial = hpo_ids.split(",");
        hpMpMatches = runDynamicQuery(findMappingStatement, findAnnotationStatement, hps_initial, "mouse");


        // Human
        mapping_query = String.format("SELECT hp_id_hit, score FROM hp_hp_mappings M WHERE M.hp_id = ?");
        findMappingStatement = null;
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            throw new ExomizerInitializationException(error);
        }
        annotation = String.format("SELECT H.disease_id, hp_id, gene_id, human_gene_symbol FROM human2mouse_orthologs hm, disease_hp M, disease H WHERE hm.entrez_id=H.gene_id AND M.disease_id=H.disease_id");
        try {
            findAnnotationStatement = connection.prepareStatement(annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + annotation;
            throw new ExomizerInitializationException(error);
        }
        hpHpMatches = runDynamicQuery(findMappingStatement, findAnnotationStatement, hps_initial, "human");

        // Fish
        mapping_query = String.format("SELECT zp_id, score FROM hp_zp_mappings M WHERE M.hp_id = ?");
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            throw new ExomizerInitializationException(error);
        }
        annotation = String.format("SELECT zfin_model_id, zp_id, entrez_id, human_gene_symbol, M.zfin_gene_id, M.zfin_gene_symbol FROM zfin_zp M, human2fish_orthologs H WHERE M.zfin_gene_id=H.zfin_gene_id and human_gene_symbol != 'null'");
        try {
            findAnnotationStatement = connection.prepareStatement(annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + annotation;
            throw new ExomizerInitializationException(error);
        }
        hps_initial = hpo_ids.split(",");
        hpZpMatches = runDynamicQuery(findMappingStatement, findAnnotationStatement, hps_initial, "fish");

        int rows = randomWalkMatrix.data.getColumn(0).getRows();
        int cols = phenoGenes.size();
        DoubleMatrix combinedProximityVector = DoubleMatrix.zeros(rows, cols);
        int c = 0;
        DoubleMatrix column = null;
        for (Integer seedGeneEntrezId : phenoGenes) {
            if (!randomWalkMatrix.objectid2idx.containsKey(seedGeneEntrezId)) {
                c++;
                continue;
            } else {
                int indexOfGene = randomWalkMatrix.objectid2idx.get(seedGeneEntrezId);
                column = randomWalkMatrix.data.getColumn(indexOfGene);
                // weight column by phenoScore 
                double score = scores.get(seedGeneEntrezId);
                column = column.mul(score);
                combinedProximityVector.putColumn(c, column);
                c++;
            }
        }
        // Take the best score
        this.combinedProximityVector = combinedProximityVector;
        //this.combinedProximityVector = combinedProximityVector.rowMaxs();
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
     */
    public ArrayList<String> getMessages() {
        if (this.error_record.size() > 0) {
            for (String s : error_record) {
                this.messages.add("Error: " + s);
            }
        }
        return this.messages;
    }

    /**
     * Prioritize a list of candidate {@link exomizer.exome.Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants). <P> You
     * have to call {@link #setParameters} before running this function.
     *
     * @param gene_list List of candidate genes.
     * @see exomizer.filter.Filter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override
    public void prioritizeGenes(List<Gene> gene_list) {
        try {
            computeDistanceAllNodesFromStartNodes();
        } catch (ExomizerInitializationException e) {
            String error = String.format("Error computing distance for all nodes", e.toString());
        }
        if (phenoGenes == null || phenoGenes.size() < 1) {
            throw new RuntimeException("Please specify a valid list of known genes!");
        }
        int PPIdataAvailable = 0;
        int totalGenes = gene_list.size();

        for (Gene gene : gene_list) {
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
            int entrezGeneID = gene.getEntrezGeneID();
            if (scores.get(entrezGeneID) != null) {
                val = scores.get(entrezGeneID);
                // HUMAN
                if (humanScores.get(entrezGeneID) != null) {
                    humanScore = humanScores.get(entrezGeneID);
                    String diseaseId = humanDiseases.get(entrezGeneID);
                    String diseaseLink = diseaseTerms.get(diseaseId);
                    if (diseaseId.split(":")[0].equals("OMIM")) {
                        diseaseId = diseaseId.split(":")[1];
                        diseaseLink = "<a href=\"http://www.omim.org/" + diseaseId + "\">" + diseaseLink + "</a>";
                    } else {
                        diseaseId = diseaseId.split(":")[1];
                        diseaseLink = "<a href=\"http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=" + diseaseId + "\">" + diseaseLink + "</a>";
                    }
                    evidence = evidence + String.format("<ul><li>Phenotypic similarity %.3f to %s associated with %s. <a class=\"op1\" id=\"" + entrezGeneID + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", humanScores.get(gene.getEntrezGeneID()), diseaseLink, gene.getGeneSymbol());
                    if (hpHpMatches.get(entrezGeneID) != null) {
                        String[] hps_initial = hpo_ids.split(",");
                        for (String hpId : hps_initial) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpHpMatches.get(entrezGeneID).get(hpId) != null) {
                                Set<Float> hpIdScores = hpHpMatches.get(entrezGeneID).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String hpIdHit = hpHpMatches.get(entrezGeneID).get(hpId).get(hpIdScore);
                                    String hpTermHit = hpoTerms.get(hpIdHit);
                                    humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("%s (%s)|%s (%s)[", hpTerm, hpId, hpTermHit, hpIdHit);
                                }
                            } else {
                                humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("%s (%s)|[", hpTerm, hpId);
                            }
                        }
                    }
                }
                // MOUSE
                if (mouseScores.get(gene.getEntrezGeneID()) != null) {
                    mouseScore = mouseScores.get(entrezGeneID);
                    evidence = evidence + String.format("<ul><li>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>. <a class=\"op2\" id=\"" + entrezGeneID + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", mouseScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                    if (hpMpMatches.get(gene.getEntrezGeneID()) != null) {
                        String[] hps_initial = hpo_ids.split(",");
                        for (String hpId : hps_initial) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpMpMatches.get(entrezGeneID).get(hpId) != null) {
                                Set<Float> hpIdScores = hpMpMatches.get(entrezGeneID).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String mpIdHit = hpMpMatches.get(entrezGeneID).get(hpId).get(hpIdScore);
                                    String mpTermHit = mpoTerms.get(mpIdHit);
                                    mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("%s (%s)|%s (%s)[", hpTerm, hpId, mpTermHit, mpIdHit);
                                }
                            } else {
                                mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("%s (%s)|[", hpTerm, hpId);
                            }
                        }
                    }
                }
                // FISH
                if (fishScores.get(gene.getEntrezGeneID()) != null) {
                    fishScore = fishScores.get(entrezGeneID);
                    evidence = evidence + String.format("<ul><li>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>. <a class=\"op3\" id=\"" + entrezGeneID + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", fishScores.get(gene.getEntrezGeneID()), gene.getGeneSymbol(), gene.getGeneSymbol());
                    //evidence = evidence + "<br>Best phenotype matches: ";
                    if (hpZpMatches.get(gene.getEntrezGeneID()) != null) {
                        String[] hps_initial = hpo_ids.split(",");
                        for (String hpId : hps_initial) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpZpMatches.get(entrezGeneID).get(hpId) != null) {
                                Set<Float> hpIdScores = hpZpMatches.get(entrezGeneID).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String zpIdHit = hpZpMatches.get(entrezGeneID).get(hpId).get(hpIdScore);
                                    String zpTermHit = zpoTerms.get(zpIdHit);
                                    fishPhenotypeEvidence = fishPhenotypeEvidence + String.format("%s (%s)|%s (%s)[", hpTerm, hpId, zpTermHit, zpIdHit);
                                }
                            } else {
                                fishPhenotypeEvidence = fishPhenotypeEvidence + String.format("%s (%s)|[", hpTerm, hpId);
                            }
                        }
                    }
                }
                ++PPIdataAvailable;
            } //INTERACTION WITH A HIGH QUALITY MOUSE/HUMAN PHENO HIT => 0 to 0.65 once scaled
            else if (randomWalkMatrix.objectid2idx.containsKey(gene.getEntrezGeneID())) {
                int col_idx = computeSimStartNodesToNode(gene);
                int row_idx = randomWalkMatrix.objectid2idx.get(gene.getEntrezGeneID());
                val = combinedProximityVector.get(row_idx, col_idx);
                walkerScore = val;
                String closestGene = phenoGeneSymbols.get(col_idx);
                String thisGene = gene.getGeneSymbol();
                //String stringDbImageLink = "http://string-db.org/api/image/networkList?identifiers=" + thisGene + "%0D" + closestGene + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + thisGene + "%0D" + closestGene + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                double phenoScore = scores.get(phenoGenes.get(col_idx));
                entrezGeneID = phenoGenes.get(col_idx);
                // HUMAN
                if (humanScores.get(phenoGenes.get(col_idx)) != null) {
                    String diseaseId = humanDiseases.get(phenoGenes.get(col_idx));
                    String diseaseLink = diseaseTerms.get(diseaseId);
                    if (diseaseId.split(":")[0].equals("OMIM")) {
                        diseaseId = diseaseId.split(":")[1];
                        diseaseLink = "<a href=\"http://www.omim.org/" + diseaseId + "\">" + diseaseLink + "</a>";
                    } else {
                        diseaseId = diseaseId.split(":")[1];
                        diseaseLink = "<a href=\"http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=" + diseaseId + "\">" + diseaseLink + "</a>";
                    }
                    evidence = evidence + String.format("<ul><li>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to %s associated with %s. <a class=\"op1\" id=\"" + gene.getEntrezGeneID() + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", stringDbLink, closestGene, diseaseLink, closestGene);
                    //evidence = evidence + "<br>Best phenotype matches";
                    if (hpHpMatches.get(phenoGenes.get(col_idx)) != null) {
                        String[] hps_initial = hpo_ids.split(",");
                        for (String hpId : hps_initial) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpHpMatches.get(entrezGeneID).get(hpId) != null) {
                                Set<Float> hpIdScores = hpHpMatches.get(entrezGeneID).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String hpIdHit = hpHpMatches.get(entrezGeneID).get(hpId).get(hpIdScore);
                                    String hpTermHit = hpoTerms.get(hpIdHit);
                                    humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("%s (%s)|%s (%s)[", hpTerm, hpId, hpTermHit, hpIdHit);
                                }
                            } else {
                                humanPhenotypeEvidence = humanPhenotypeEvidence + String.format("%s (%s)|[", hpTerm, hpId);
                            }
                        }
                    }
                }
                // MOUSE
                if (mouseScores.get(phenoGenes.get(col_idx)) != null) {
                    evidence = evidence + String.format("<ul><li>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to mouse mutant of %s. <a class=\"op2\" id=\"" + gene.getEntrezGeneID() + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", stringDbLink, closestGene, closestGene);
                    //evidence = evidence + "<br>Best phenotype matches: ";
                    if (hpMpMatches.get(phenoGenes.get(col_idx)) != null) {
                        String[] hps_initial = hpo_ids.split(",");
                        for (String hpId : hps_initial) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpMpMatches.get(entrezGeneID).get(hpId) != null) {
                                Set<Float> hpIdScores = hpMpMatches.get(entrezGeneID).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String mpIdHit = hpMpMatches.get(entrezGeneID).get(hpId).get(hpIdScore);
                                    String mpTermHit = mpoTerms.get(mpIdHit);
                                    mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("%s (%s)|%s (%s)[", hpTerm, hpId, mpTermHit, mpIdHit);
                                }
                            } else {
                                mousePhenotypeEvidence = mousePhenotypeEvidence + String.format("%s (%s)|[", hpTerm, hpId);
                            }
                        }
                    }

                }
                // FISH
                if (fishScores.get(phenoGenes.get(col_idx)) != null) {
                    evidence = evidence + String.format("<ul><li>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to fish mutant of %s.  <a class=\"op3\" id=\"" + gene.getEntrezGeneID() + "\"><button type=\"button\">Best Phenotype Matches</button></a></li></ul>", stringDbLink, closestGene, closestGene);
                    //evidence = evidence + "<br>Best phenotype matches: ";
                    if (hpZpMatches.get(phenoGenes.get(col_idx)) != null) {
                        String[] hps_initial = hpo_ids.split(",");
                        for (String hpId : hps_initial) {
                            String hpTerm = hpoTerms.get(hpId);
                            if (hpZpMatches.get(entrezGeneID).get(hpId) != null) {
                                Set<Float> hpIdScores = hpZpMatches.get(entrezGeneID).get(hpId).keySet();
                                for (float hpIdScore : hpIdScores) {
                                    String zpIdHit = hpZpMatches.get(entrezGeneID).get(hpId).get(hpIdScore);
                                    String zpTermHit = zpoTerms.get(zpIdHit);
                                    fishPhenotypeEvidence = mousePhenotypeEvidence + String.format("%s (%s)|%s (%s)[", hpTerm, hpId, zpTermHit, zpIdHit);
                                }
                            } else {
                                fishPhenotypeEvidence = mousePhenotypeEvidence + String.format("%s (%s)|[", hpTerm, hpId);
                            }
                        }
                    }
                }
                ++PPIdataAvailable;
            } // NO PHENO HIT OR PPI INTERACTION
            else {
                evidence = "<ul><li>No phenotype or PPI evidence</li></ul>";
            }
            DynamicPhenoWandererRelevanceScore relScore = new DynamicPhenoWandererRelevanceScore(val, evidence, humanPhenotypeEvidence, mousePhenotypeEvidence, 
                    fishPhenotypeEvidence, humanScore, mouseScore, fishScore, walkerScore);
            gene.addRelevanceScore(relScore, PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY);
        }

        /*
         * refactor all scores for genes that are not direct pheno-hits but in
         * PPI with them to a linear range
         */
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<Float, List<Gene>>();
        for (Gene g : gene_list) {
            if (scores.get(g.getEntrezGeneID()) == null && randomWalkMatrix.objectid2idx.containsKey(g.getEntrezGeneID())) {// Only do for non-pheno direct hits
                float geneScore = g.getRelevanceScore(PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY);
                if (geneScoreMap.containsKey(geneScore)) {
                    List<Gene> geneScoreGeneList = geneScoreMap.get(geneScore);
                    geneScoreGeneList.add(g);
                } else {
                    List<Gene> geneScoreGeneList = new ArrayList<Gene>();
                    geneScoreGeneList.add(g);
                    geneScoreMap.put(geneScore, geneScoreGeneList);
                }
            }
        }
        float rank = 1;
        Set<Float> set = geneScoreMap.descendingKeySet();
        Iterator<Float> i = set.iterator();
        while (i.hasNext()) {
            float score = i.next();
            List<Gene> geneScoreGeneList = geneScoreMap.get(score);
            int sharedHits = geneScoreGeneList.size();
            float adjustedRank = rank;
            if (sharedHits > 1) {
                adjustedRank = rank + (sharedHits / 2);
            }
            float newScore = 0.65f - 0.65f * (adjustedRank / gene_list.size());
            rank = rank + sharedHits;
            for (Gene g : geneScoreGeneList) {
                g.resetRelevanceScore(PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY, newScore);
            }
        }
        String s = String.format("Phenotype and Protein-Protein Interaction evidence was available for %d of %d genes (%.1f%%)",
                PPIdataAvailable, totalGenes, 100f * ((float) PPIdataAvailable / (float) totalGenes));
        this.messages.add(s);
        String[] hps_initial = hpo_ids.split(",");
        String hpInput = "";
        for (String hp : hps_initial) {
            String hpTerm = hpoTerms.get(hp);
            hpInput = hpInput + (String.format("%s (%s), ", hpTerm, hp));
        }
        //this.messages.add(hpInput);// now display all HPO terms on results no need for this
        this.n_before = totalGenes;
        this.n_after = totalGenes;
    }

    /**
     * This causes a summary of RW prioritization to appear in the HTML output
     * of the exomizer
     */
    public boolean displayInHTML() {
        return true;
    }

    /**
     * @return HTML code for displaying the HTML output of the Exomizer.
     */
    public String getHTMLCode() {
        if (messages == null) {
            return "Error initializing Random Walk matrix";
        } else if (messages.size() == 1) {
            return String.format("<ul><li>%s</li></ul>", messages.get(0));
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("<ul>\n");
            for (String m : messages) {
                sb.append(String.format("<li>%s</li>\n", m));
            }
            sb.append("</ul>\n");
            return sb.toString();
        }
    }

    /**
     * Get number of variants before filter was applied.
     */
    public int getBefore() {
        return this.n_before;
    }

    /**
     * Get number of variants after filter was applied.
     */
    public int getAfter() {
        return this.n_after;
    }

    /**
     * This function retrieves the random walk similarity score for the gene
     *
     * @param nodeToCompute Gene for which the RW score is to bee retrieved
     */
    private int computeSimStartNodesToNode(Gene nodeToCompute) {
        int idx = randomWalkMatrix.objectid2idx.get(nodeToCompute.getEntrezGeneID());
        int c = 0;
        double val = 0;
        int bestHitIndex = 0;
        for (Integer seedGeneEntrezId : phenoGenes) {
            if (!randomWalkMatrix.objectid2idx.containsKey(seedGeneEntrezId)) {
                c++;
                continue;
            } else {
                double cellVal = combinedProximityVector.get(idx, c);
                if (cellVal > val) {
                    val = cellVal;
                    bestHitIndex = c;
                }
                c++;
            }
        }
        return bestHitIndex;
    }

    @Override
    public void setParameters(String par) throws ExomizerInitializationException {
    }

    /**
     * Initialize the database connection and call {@link #setUpSQLPreparedStatements}
     *
     * @param connection A connection to a postgreSQL database from the exomizer
     * or tomcat.
     */
    public void setDatabaseConnection(java.sql.Connection connection)
            throws ExomizerInitializationException {
        this.connection = connection;
    }
}
