package de.charite.compbio.exomiser.core.prioritisers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import jannovar.common.Constants;

import de.charite.compbio.exomiser.core.model.Gene;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter variants according to the phenotypic similarity of the specified
 * clinical phenotypes to mouse models disrupting the same gene. We use MGI
 * annotated phenotype data and the Phenodigm/OWLSim algorithm. The filter is
 * implemented with an SQL query.
 * <P>
 * This class prioritizes the genes that have survived the initial VCF filter
 * (i.e., it is use on genes for which we have found rare, potentially
 * pathogenic variants).
 * <P>
 * This class uses a database connection that it obtains from the main Exomizer
 * driver program (if the Exomizer was started from the command line) or from a
 * tomcat server (etc.) if the Exomizer was called from a Webserver.
 *
 * @author Damian Smedley
 * @version 0.05 (April 6, 2013)
 */
public class ExomiserMousePriority implements Priority {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserMousePriority.class);

    private static final PriorityType priorityType = PriorityType.EXOMISER_MOUSE_PRIORITY;

    /**
     * Threshold for filtering. Retain only those variants whose score is below
     * this threshold.
     */
    private float score_threshold = 2.0f;
    private String hpo_ids = null;

    private List<String> hpoIds;
    private String disease;

    /**
     * Database handle to the postgreSQL database used by this application.
     */
    private Connection connection = null;
    /**
     * A prepared SQL statement for mgi phenodigm score.
     */
    private PreparedStatement findMappingStatement = null;
    /**
     * A prepared SQL statement for mgi phenodigm score.
     */
    private PreparedStatement testGeneStatement = null;
    private PreparedStatement findMouseAnnotationStatement = null;

    /**
     * A flag to indicate we could not retrieve data from the database for some
     * variant. Using the value 200% means that the variant will not fail the
     * Phenodigm filter.
     */
    private static final float NO_MGI_PHENODIGM_DATA = 2.0f;

    /**
     * score of the variant. Zero means no relevance, but the score is not
     * necessarily scaled to [0..1]
     */
    private float MGI_PHENODIGM_SCORE;

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private ArrayList<String> messages = new ArrayList<String>();

    /**
     * Keeps track of the number of variants for which data was available in
     * Phenodigm.
     */
    private int found_data_for_mgi_phenodigm;

    public ExomiserMousePriority(String hpo_ids, String disease) {
        this.hpo_ids = hpo_ids;
        this.disease = disease;
        //String url = String.format("http://omim.org/%s",disease);
        messages.add(String.format("<a href = \"http://www.sanger.ac.uk/resources/databases/phenodigm\">Mouse PhenoDigm Filter</a>"));
        if (disease != null) {
            String url = String.format("http://omim.org/%s", disease);
            if (disease.contains("ORPHANET")) {
                String diseaseId = disease.split(":")[1];
                url = String.format("http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=%s", diseaseId);
            }
            String anchor = String.format("Mouse phenotypes for candidate genes were compared to <a href=\"%s\">%s</a>\n", url, disease);
            this.messages.add(String.format("Mouse PhenoDigm Filter for OMIM"));
            messages.add(anchor);
        } else {
            String anchor = String.format("Mouse phenotypes for candidate genes were compared to user-supplied clinical phenotypes");
            messages.add(anchor);
        }
    }

    public ExomiserMousePriority(List<String> hpoIds, String disease) {
        this.hpoIds = hpoIds;
        this.disease = disease;
        messages.add(String.format("<a href = \"http://www.sanger.ac.uk/resources/databases/phenodigm\">Mouse PhenoDigm Filter</a>"));
        if (disease != null) {
            String url = String.format("http://omim.org/%s", disease);
            if (disease.contains("ORPHANET")) {
                String diseaseId = disease.split(":")[1];
                url = String.format("http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=%s", diseaseId);
            }
            String anchor = String.format("Mouse phenotypes for candidate genes were compared to <a href=\"%s\">%s</a>\n", url, disease);
            this.messages.add(String.format("Mouse PhenoDigm Filter for OMIM"));
            messages.add(anchor);
        } else {
            String anchor = String.format("Mouse phenotypes for candidate genes were compared to user-supplied clinical phenotypes");
            messages.add(anchor);
        }
    }

    /**
     * Get the name of this prioritization algorithm.
     */
    @Override
    public String getPriorityName() {
        return priorityType.getCommandLineValue();
    }

    /**
     * Flag to output results of filtering against PhenoDigm data.
     */
    @Override
    public PriorityType getPriorityType() {
        return priorityType;
    }

    /**
     * Sets the score threshold for variants. Note: Keeping this method for now,
     * but I do not think we need parameters for Phenodigm prioritization?
     *
     * @param score A score threshold, e.g., a float such as "0.02"
     */
    public void setScoreThreshold(float score) {
        score_threshold = score;
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
     */
    @Override
    public ArrayList<String> getMessages() {
        return this.messages;
    }

    @Override
    public void prioritizeGenes(List<Gene> gene_list) {

        this.found_data_for_mgi_phenodigm = 0;
        retrieve_score_data(gene_list);
//	for (Gene g : gene_list) {
//            ExomiserMousePriorityResult rscore = retrieve_score_data(g);
//            g.addPriorityResult(rscore, priorityType);          
//	}
        this.messages.add(String.format("Data analysed for %d genes using Mouse PhenoDigm", gene_list.size()));
        closeConnection();
    }

    /**
     * Set hpo_ids variable based on the entered disease
     */
    private void setHPOfromDisease(String disease) {
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
        hpoIds = parseHpoIdListFromString(hpoListString);
        logger.info("{} HPO ids retrieved for disease {} - {}", hpoIds.size(), disease, hpoIds);
    }

    private List<String> parseHpoIdListFromString(String hpoIdsString) {
        logger.info("Attempting to create HPO ID list from string: {} ", hpoIdsString);
        String[] hpoArray = hpoIdsString.split(",");
        List<String> hpoIdList = new ArrayList<>();
        for (String string : hpoArray) {
            hpoIdList.add(string.trim());
        }
        logger.info("Made list: {}", hpoIdList);
        return hpoIdList;
    }

    /**
     * @param g A gene whose relevance score is to be retrieved from the SQL
     * database by this function.
     * @return result of prioritization (represents a non-negative score)
     */
    private void retrieve_score_data(List<Gene> gene_list) {

        if (disease != null && !disease.isEmpty() && hpoIds.isEmpty()) {
            logger.info("Setting HPO IDs using disease annotations for {}", disease);
            setHPOfromDisease(disease);
        }

        HashMap<String, Float> mapped_terms = new HashMap<>();
        HashMap<String, Float> best_mapped_term_score = new HashMap<>();
        HashMap<String, String> best_mapped_term_mpid = new HashMap<>();
        HashMap<String, Integer> knownMps = new HashMap<>();
        List<String> hpList = new ArrayList<>();
        float best_max_score = 0f;
        float best_avg_score = 0f;
        try {
            for (String hpid : hpoIds) {
                this.findMappingStatement.setString(1, hpid);
                ResultSet rs = findMappingStatement.executeQuery();
                int found = 0;
                while (rs.next()) {
                    found = 1;
                    String mp_id = rs.getString(1);
                    knownMps.put(mp_id, 1);
                    StringBuffer hashKey = new StringBuffer();
                    hashKey.append(hpid);
                    hashKey.append(mp_id);
                    float score = rs.getFloat(2);
                    mapped_terms.put(hashKey.toString(), score);
                    if (best_mapped_term_score.get(hpid) != null) {
                        if (score > best_mapped_term_score.get(hpid)) {
                            best_mapped_term_score.put(hpid, score);
                            best_mapped_term_mpid.put(hpid, mp_id);
                        }
                    } else {
                        best_mapped_term_score.put(hpid, score);
                        best_mapped_term_mpid.put(hpid, mp_id);
                    }
                }
                if (found == 1) {
                    hpList.add(hpid);
                }
            }
            // calculate perfect mouse model scores
            float sum_best_score = 0f;

            int best_hit_counter = 0;
            // loop over each hp id should start herre
            for (String hpid : hpList) {
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
                    for (String hpid2 : hpList) {
                        StringBuffer hashKey = new StringBuffer();
                        hashKey.append(hpid2);
                        hashKey.append(mpid);
                        if (mapped_terms.get(hashKey.toString()) != null && mapped_terms.get(hashKey.toString()) > best_score) {
                            //System.out.println("added in best score for mp term:"+mpid);
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
            best_avg_score = sum_best_score / best_hit_counter;
        } catch (SQLException e) {
            logger.error("Error executing Phenodigm query: ", e);
        }
        for (Gene g : gene_list) {
            float MGI_SCORE = Constants.UNINITIALIZED_FLOAT;
            String MGI_GENE_ID = null;
            String MGI_GENE = null;

            String genesymbol = g.getGeneSymbol();
            ResultSet rs2 = null;
            try {
                this.testGeneStatement.setString(1, genesymbol);
                rs2 = testGeneStatement.executeQuery();
                if (rs2.next()) {

                    // calculate score for this gene
                    this.findMouseAnnotationStatement.setString(1, genesymbol);
                    ResultSet rs = findMouseAnnotationStatement.executeQuery();
                    float best_combined_score = 0f;// keep track of best score for gene
                    while (rs.next()) {
                        int mouse_model_id = rs.getInt(1);
                        //System.out.println("Calculating score for mouse model id "+mouse_model_id+" gene "+genesymbol);
                        String mp_ids = rs.getString(2);
                        MGI_GENE_ID = rs.getString(3);
                        MGI_GENE = rs.getString(4);
                        String[] mp_initial = mp_ids.split(",");
                        List<String> mpList = new ArrayList<>();
                        for (String mpid : mp_initial) {
                            if (knownMps.get(mpid) != null) {
                                mpList.add(mpid);
                            }
                        }
//		      String[] mps = new String[mpList.size()];
//		      mpList.toArray(mps);

                        int row_column_count = hpList.size() + mpList.size();
                        float max_score = 0f;
                        float sum_best_hit_rows_columns_score = 0f;

                        for (String hpid : hpList) {
                            float best_score = 0f;
                            String best_mpid = "";
                            String best_hpid = "";
                            for (String mpid : mpList) {
                                StringBuffer hashKey = new StringBuffer();
                                hashKey.append(hpid);
                                hashKey.append(mpid);
                                //System.out.println("SEEING IF ANY MAPPED TERMS FOR "+hashKey);
                                if (mapped_terms.get(hashKey.toString()) != null) {
                                    float score = mapped_terms.get(hashKey.toString());
                                    // identify best match                                                                                                                                                                 
                                    if (score > best_score) {
                                        best_mpid = mpid;
                                        best_hpid = hpid;
                                        best_score = score;
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
                        for (String mpid : mpList) {
                            float best_score = 0f;
                            String best_mpid = "";
                            String best_hpid = "";
                            for (String hpid : hpList) {
                                StringBuilder hashKey = new StringBuilder();
                                hashKey.append(hpid);
                                hashKey.append(mpid);
                                if (mapped_terms.get(hashKey.toString()) != null) {
                                    float score = mapped_terms.get(hashKey.toString());
                                    // identify best match                                                                                                                                                                 
                                    if (score > best_score) {
                                        best_mpid = mpid;
                                        best_hpid = hpid;
                                        best_score = score;
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
                            float avg_best_hit_rows_columns_score = sum_best_hit_rows_columns_score / row_column_count;
                            float combined_score = 50 * (max_score / best_max_score
                                    + avg_best_hit_rows_columns_score / best_avg_score);
                            if (combined_score > 100) {
                                combined_score = 100;
                            }
                            // is this the best score so far for this gene?
                            if (combined_score > best_combined_score) {
                                best_combined_score = combined_score;
                            }
                        }
                        // do next mouse model
                    }
                    rs.close();
                    MGI_SCORE = best_combined_score / 100;
                    if (!(MGI_SCORE <= 0)) {
                        found_data_for_mgi_phenodigm++;
                    }
                } else {
                    MGI_SCORE = Constants.NOPARSE_FLOAT;//use to indicate there is no phenotyped mouse model in MGI
                }
                rs2.close();
                ExomiserMousePriorityResult rscore = new ExomiserMousePriorityResult(MGI_GENE_ID, MGI_GENE, MGI_SCORE);
                g.addPriorityResult(rscore);
            } catch (SQLException e) {
                logger.error("Error executing Phenodigm query: ", e);
            }
        }

    }

    /**
     * Prepare the SQL query statements required for this filter.
     * <p>
     * Note that there are two queries. The testGeneStatement basically tests
     * whether the gene in question is in the database; it must have both an
     * orthologue in the table {@code human2mouse_orthologs}, and there must be
     * some data on it in the table {@code  mgi_mp}.
     * <P>
     * Then, we select the MGI gene id (e.g., MGI:1234567), the corresponding
     * mouse gene symbol and the phenodigm score. There is currently one score
     * for each pair of OMIM diseases and MGI genes.
     */
    private void setUpSQLPreparedStatements() {
        String mapping_query = String.format("SELECT mp_id, score "
                + "FROM hp_mp_mappings M "
                + "WHERE M.hp_id = ?");

        try {
            this.findMappingStatement = connection.prepareStatement(mapping_query);

        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", mapping_query, e);
        }

        String mouse_annotation = String.format("SELECT mouse_model_id, mp_id, M.mgi_gene_id, M.mgi_gene_symbol "
                + "FROM mgi_mp M, human2mouse_orthologs H "
                + "WHERE M.mgi_gene_id=H.mgi_gene_id AND human_gene_symbol = ?");

        try {
            this.findMouseAnnotationStatement = connection.prepareStatement(mouse_annotation);

        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", mouse_annotation, e);
        }

        String test_gene_query = String.format("SELECT human_gene_symbol "
                + "FROM mgi_mp, human2mouse_orthologs "
                + "WHERE mgi_mp.mgi_gene_id=human2mouse_orthologs.mgi_gene_id "
                + "AND human_gene_symbol = ? LIMIT 1");
        try {
            this.testGeneStatement = connection.prepareStatement(test_gene_query);

        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", test_gene_query, e);
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
        setUpSQLPreparedStatements();
    }

    protected void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
    }

    /**
     * To do
     * @return
     */
    @Override
    public boolean displayInHTML() {
        return true;
    }

    /**
     * @return an HTML message for the table describing the action of filters
     */
    @Override
    public String getHTMLCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>\n");
        Iterator<String> it = this.messages.iterator();
        while (it.hasNext()) {
            String s = it.next();
            sb.append("<li>" + s + "</li>\n");
        }
        sb.append("</ul>\n");
        return sb.toString();
    }

}
