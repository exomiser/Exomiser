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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
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

    private static final PriorityType PRIORITY_TYPE = PriorityType.EXOMISER_MOUSE_PRIORITY;

    /**
     * Threshold for filtering. Retain only those variants whose score is below
     * this threshold.
     */
    private float scoreThreshold = 2.0f;

    private List<String> hpoIds;
    private final String disease;

    private Connection connection;
    private DataSource dataSource;

    private PreparedStatement testGeneStatement;
    private PreparedStatement findMouseAnnotationStatement;

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
    private List<String> messages = new ArrayList<>();

    /**
     * Keeps track of the number of variants for which data was available in
     * Phenodigm.
     */
    private int foundDataForMgiPhenodigm;

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
        return PRIORITY_TYPE.getCommandLineValue();
    }

    /**
     * Flag to output results of filtering against PhenoDigm data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    /**
     * Sets the score threshold for variants. Note: Keeping this method for now,
     * but I do not think we need parameters for Phenodigm prioritization?
     *
     * @param score A score threshold, e.g., a float such as "0.02"
     */
    public void setScoreThreshold(float score) {
        scoreThreshold = score;
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
    public void prioritizeGenes(List<Gene> genes) {

        this.foundDataForMgiPhenodigm = 0;
        retrieveScoreData(genes);
//	for (Gene g : gene_list) {
//            ExomiserMousePriorityResult rscore = retrieve_score_data(g);
//            g.addPriorityResult(rscore, PRIORITY_TYPE);          
//	}
        this.messages.add(String.format("Data analysed for %d genes using Mouse PhenoDigm", genes.size()));
        closeConnection();
    }

    /**
     * Set hpo_ids variable based on the entered disease
     */
    private List<String> getHpoIdsForDisease(String disease) {
        String hpoListString = "";
        try (Connection connection = dataSource.getConnection()) {
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

    /**
     * @param g A gene whose relevance score is to be retrieved from the SQL
     * database by this function.
     * @return result of prioritization (represents a non-negative score)
     */
    private void retrieveScoreData(List<Gene> genes) {

        if (disease != null && !disease.isEmpty() && hpoIds.isEmpty()) {
            logger.info("Setting HPO IDs using disease annotations for {}", disease);
            hpoIds = getHpoIdsForDisease(disease);
        }

        Set<String> hpIdsWithPhenotypeMatch = new LinkedHashSet<>();
        Map<String, Float> bestMappedTermScore = new HashMap<>();
        Map<String, String> bestMappedTermMpId = new HashMap<>();
        Map<String, Integer> knownMps = new HashMap<>();

        float bestMaxScore = 0f;
        float bestAvgScore = 0f;

        String mappingQuery = "SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?";

        Map<String, Float> mappedTerms = new HashMap<>();
        for (String hpId : hpoIds) {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement findMappingStatement = connection.prepareStatement(mappingQuery);
                findMappingStatement.setString(1, hpId);
                ResultSet rs = findMappingStatement.executeQuery();
                int found = 0;
                while (rs.next()) {
                    found = 1;
                    String mpId = rs.getString(1);
                    knownMps.put(mpId, 1);
                    String hashKey = hpId + mpId;
                    float score = rs.getFloat("score");
                    mappedTerms.put(hashKey, score);
                    if (bestMappedTermScore.containsKey(hpId)) {
                        if (score > bestMappedTermScore.get(hpId)) {
                            bestMappedTermScore.put(hpId, score);
                            bestMappedTermMpId.put(hpId, mpId);
                        }
                    } else {
                        bestMappedTermScore.put(hpId, score);
                        bestMappedTermMpId.put(hpId, mpId);
                    }
                }
                if (found == 1) {
                    hpIdsWithPhenotypeMatch.add(hpId);
                }
            } catch (SQLException e) {
                logger.error("Problem setting up SQL query: {}", mappingQuery, e);
            }
        }
        // calculate perfect mouse model scores
        float sumBestScore = 0f;

        int bestHitCounter = 0;
        // loop over each hp id should start herre
        for (String hpid : hpIdsWithPhenotypeMatch) {
            if (bestMappedTermScore.get(hpid) != null) {
                float hpScore = bestMappedTermScore.get(hpid);
                // add in scores for best match for the HP term                                                                                                                                                
                sumBestScore += hpScore;
                bestHitCounter++;
                if (hpScore > bestMaxScore) {
                    bestMaxScore = hpScore;
                }
                // add in MP-HP hits                                                                                                                                                                           
                String mpId = bestMappedTermMpId.get(hpid);
                float bestScore = 0f;
                for (String hpId2 : hpIdsWithPhenotypeMatch) {
                    String hashKey = hpId2 + mpId;
                    if (mappedTerms.containsKey(hashKey) && mappedTerms.get(hashKey) > bestScore) {
                        //System.out.println("added in best score for mp term:"+mpid);
                        bestScore = mappedTerms.get(hashKey);
                    }
                }
                // add in scores for best match for the MP term                                                                                                                                                
                sumBestScore += bestScore;
                bestHitCounter++;
                if (bestScore > bestMaxScore) {
                    bestMaxScore = bestScore;
                }
            }
        }
        bestAvgScore = sumBestScore / bestHitCounter;

        for (Gene g : genes) {
            float mgiScore = Constants.UNINITIALIZED_FLOAT;
            String mgiGeneId = null;
            String mgiGeneSymbol = null;

            String genesymbol = g.getGeneSymbol();
            ResultSet rs2 = null;
            //TODO: test this using one statement only:
            // SELECT mouse_model_id, mp_id, M.mgi_gene_id, M.mgi_gene_symbol 
            // FROM mgi_mp M, human2mouse_orthologs H 
            // WHERE M.mgi_gene_id = H.mgi_gene_id AND human_gene_symbol = ?
            try (Connection connection = dataSource.getConnection()) {
                this.testGeneStatement.setString(1, genesymbol);
                rs2 = testGeneStatement.executeQuery();
                if (rs2.next()) {
                    // calculate score for this gene
                    this.findMouseAnnotationStatement.setString(1, genesymbol);
                    ResultSet rs = findMouseAnnotationStatement.executeQuery();
                    float bestCombinedScore = 0f;// keep track of best score for gene
                    while (rs.next()) {
                        int mouseModelId = rs.getInt(1);
                        //System.out.println("Calculating score for mouse model id "+mouse_model_id+" gene "+genesymbol);
                        String mpIds = rs.getString(2);
                        mgiGeneId = rs.getString(3);
                        mgiGeneSymbol = rs.getString(4);
                        String[] mpInitial = mpIds.split(",");
                        List<String> mpList = new ArrayList<>();
                        for (String mpId : mpInitial) {
                            if (knownMps.get(mpId) != null) {
                                mpList.add(mpId);
                            }
                        }

                        int rowColumnCount = hpIdsWithPhenotypeMatch.size() + mpList.size();
                        float maxScore = 0f;
                        float sumBestHitRowsColumnsScore = 0f;

                        for (String hpId : hpIdsWithPhenotypeMatch) {
                            float bestScore = 0f;
                            for (String mpId : mpList) {
                                String hashKey = hpId + mpId;
                                if (mappedTerms.containsKey(hashKey)) {
                                    float score = mappedTerms.get(hashKey);
                                    // identify best match                                                                                                                                                                 
                                    if (score > bestScore) {
                                        bestScore = score;
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
                        for (String mpId : mpList) {
                            float bestScore = 0f;
                            for (String hpId : hpIdsWithPhenotypeMatch) {
                                String hashKey = hpId + mpId;
                                if (mappedTerms.containsKey(hashKey)) {
                                    float score = mappedTerms.get(hashKey);
                                    // identify best match                                                                                                                                                                 
                                    if (score > bestScore) {
                                        bestScore = score;
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
                            float avgBestHitRowsColumnsScore = sumBestHitRowsColumnsScore / rowColumnCount;
                            float combinedScore = 50 * (maxScore / bestMaxScore
                                    + avgBestHitRowsColumnsScore / bestAvgScore);
                            if (combinedScore > 100) {
                                combinedScore = 100;
                            }
                            // is this the best score so far for this gene?
                            if (combinedScore > bestCombinedScore) {
                                bestCombinedScore = combinedScore;
                            }
                        }
                        // do next mouse model
                    }
                    rs.close();
                    mgiScore = bestCombinedScore / 100;
                    if (!(mgiScore <= 0)) {
                        foundDataForMgiPhenodigm++;
                    }
                } else {
                    mgiScore = Constants.NOPARSE_FLOAT;//use to indicate there is no phenotyped mouse model in MGI
                }
                rs2.close();
                ExomiserMousePriorityResult rscore = new ExomiserMousePriorityResult(mgiGeneId, mgiGeneSymbol, mgiScore);
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
        String mouseAnnotationQuery = "SELECT mouse_model_id, mp_id, M.mgi_gene_id, M.mgi_gene_symbol "
                + "FROM mgi_mp M, human2mouse_orthologs H "
                + "WHERE M.mgi_gene_id=H.mgi_gene_id AND human_gene_symbol = ?";

        try {
            this.findMouseAnnotationStatement = connection.prepareStatement(mouseAnnotationQuery);

        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", mouseAnnotationQuery, e);
        }

        String testGeneQuery = "SELECT human_gene_symbol "
                + "FROM mgi_mp, human2mouse_orthologs "
                + "WHERE mgi_mp.mgi_gene_id=human2mouse_orthologs.mgi_gene_id "
                + "AND human_gene_symbol = ? LIMIT 1";
        try {
            this.testGeneStatement = connection.prepareStatement(testGeneQuery);

        } catch (SQLException e) {
            logger.error("Problem setting up SQL query: {}", testGeneQuery, e);
        }

    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
    }

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

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            this.connection = dataSource.getConnection();
        } catch (SQLException ex) {
            logger.error("Unable to get connection from datasource", ex);
        }
        setUpSQLPreparedStatements();
    }

}
