package de.charite.compbio.exomiser.priority;




import java.util.ArrayList;

import org.jblas.DoubleMatrix;

import de.charite.compbio.exomiser.common.FilterType;
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
public class PhenoWandererPriority implements Priority {

    private Connection connection = null;
    /**
     * A list of error-messages
     */
    private List<String> error_record = null;
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
    private ArrayList<Integer> phenoGenes;
    private ArrayList<String> phenoGeneSymbols;
    private String disease;
    private String candGene;
    private Map<Integer, Double> scores = new HashMap<Integer, Double>();
    private Map<Integer, Double> humanScores = new HashMap<Integer, Double>();
    private Map<Integer, String> humanDiseases = new HashMap<Integer, String>();
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
    public PhenoWandererPriority(String randomWalkMatrixFileZip, String randomWalkGeneId2IndexFileZip, String disease, String candGene)
            throws ExomizerInitializationException {

        this.disease = disease;
        this.candGene = candGene;
        if (randomWalkMatrix == null) {
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
        return "Uberiser";
    }

    /**
     * Flag to output results of filtering against Genewanderer.
     */
    @Override
    public FilterType getPriorityTypeConstant() {
        return FilterType.PHENOWANDERER_PRIORITY;
    }

    /**
     * Compute the distance of all genes in the Random Walk matrix to the set of
     * seed genes given by the user.
     */
    private void computeDistanceAllNodesFromStartNodes() throws ExomizerInitializationException {
        ArrayList<Integer> phenoGenes = new ArrayList<Integer>();
        ArrayList<String> phenoGeneSymbols = new ArrayList<String>();
        String score_query = "SELECT DISTINCT max_combined_perc/100, entrez_id, human_gene_symbol FROM mouse_gene_level_summary M, "
                + "human2mouse_orthologs_new H WHERE M.mgi_gene_id=H.mgi_gene_id "
                + "AND omim_disease_id = ? AND entrez_id != 0";
        PreparedStatement findScoreStatement = null;
        ResultSet rs = null;
        //System.out.println("STARTING MOUSE");

        try {
            findScoreStatement = connection.prepareStatement(score_query);
            findScoreStatement.setString(1, disease);
            rs = findScoreStatement.executeQuery();
            while (rs.next()) {
                double score = rs.getDouble(1);
                int entrez = rs.getInt(2);
                String humanGene = rs.getString(3);
                //System.out.println("testing mouse " + entrez);
                if (score > 0.6) {// only build PPI network for high qual hits
                    phenoGenes.add(entrez);
                    phenoGeneSymbols.add(humanGene);
                }
                scores.put(entrez, score);

            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQL FUBAR!!!");
        }
        //System.out.println("done mouse ");
        String human_score_query = "select combined_perc/100, gene_id, phenmim, human_gene_symbol "
                + "from human2mouse_orthologs_new hm, disease_disease_summary d, omim o "
                + "where concat('OMIM:',o.phenmim)=d.disease_hit and hm.entrez_id=o.gene_id and "
                + "d.disease_query=concat('OMIM:',?)";
        //QUERY FOR BENCHMARKING - EXCLUDE SELF HITS. IDEALLY WOULD WANT TO LEAVE IN THE OTHER DIS-GENE ASSOCIATIONS APART ONE
        // TRYING TO RECOVER SO CAN DETECT INTERACTIONS WITH THESE
        //String human_score_query = "select combined_perc/100, gene_id from disease_disease_summary d, omim o "
        //      + "where concat('OMIM:',o.phenmim)=d.disease_hit and d.disease_query=concat('OMIM:',?) "
        //    + "and d.disease_query != d.disease_hit";
        try {
            PreparedStatement findScoreStatement2 = connection.prepareStatement(human_score_query);
            findScoreStatement2.setString(1, disease);
            //System.out.println("trying to execute " + human_score_query);
            ResultSet rs2 = findScoreStatement2.executeQuery();
            //System.out.println("executed ");
            while (rs2.next()) {
                double score = rs2.getDouble(1);
                int entrez = rs2.getInt(2);
                String diseaseHit = rs2.getString(3);
                String geneHit = rs2.getString(4);
                //System.out.println("testing " + diseaseHit + " - " + geneHit);
                // catch self hits for benchmarking and don't use score
                if ((diseaseHit == null ? disease == null : diseaseHit.equals(disease))
                        && (geneHit == null ? candGene == null : geneHit.equals(candGene))) {
                    //System.out.println("FOUND self hit " + disease + ":"+candGene);
                    if (scores.get(entrez) != null) {
                        phenoGenes.add(entrez);
                        phenoGeneSymbols.add(geneHit);
                    }
                } else {
                    if (scores.get(entrez) != null) {
                        if (score > (Double) scores.get(entrez)) {
                            scores.put(entrez, score);
                            humanScores.put(entrez, score);
                            humanDiseases.put(entrez, diseaseHit);
                        }
                    } else {
                        if (score > 0.6) {// only build PPI network for high qual hits
                            phenoGenes.add(entrez);
                            phenoGeneSymbols.add(geneHit);
                        }
                        scores.put(entrez, score);
                        humanScores.put(entrez, score);
                        humanDiseases.put(entrez, diseaseHit);
                    }
                }
            }
            rs2.close();
        } catch (SQLException e) {
            System.out.println("SQL FUBAR!!!");
        }

        this.phenoGenes = phenoGenes;
        this.phenoGeneSymbols = phenoGeneSymbols;

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
                double score = (Double) scores.get(seedGeneEntrezId);
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
            double val = 0f;
            if (scores.get(gene.getEntrezGeneID()) != null) {// DIRECT PHENO HIT : scores from 0 to 1
                val = (Double) scores.get(gene.getEntrezGeneID());
                if (humanScores.get(gene.getEntrezGeneID()) != null && (Double) humanScores.get(gene.getEntrezGeneID()) >= (Double) scores.get(gene.getEntrezGeneID())) {
                    evidence = "based on phenotypic similarity to disease " + humanDiseases.get(gene.getEntrezGeneID()) + " associated with " + gene.getGeneSymbol();
                } else {
                    evidence = "based on phenotypic similarity to mouse mutant involving " + gene.getGeneSymbol();
                }
                ++PPIdataAvailable;
            } else if (randomWalkMatrix.objectid2idx.containsKey(gene.getEntrezGeneID())) { //INTERACTION WITH A HIGH QUALITY MOUSE/HUMAN PHENO HIT => 0 to 0.5 once scaled
                int col_idx = computeSimStartNodesToNode(gene);
                int row_idx = randomWalkMatrix.objectid2idx.get(gene.getEntrezGeneID());
                val = combinedProximityVector.get(row_idx, col_idx);// ? IF THIS RIGHT WAY ROUND
                String closestGene = phenoGeneSymbols.get(col_idx);
                
                double phenoScore = (Double) scores.get(phenoGenes.get(col_idx));
                if (humanScores.get(phenoGenes.get(col_idx)) != null && (Double) humanScores.get(phenoGenes.get(col_idx)) >= (Double) scores.get(phenoGenes.get(col_idx))) {
                     evidence = String.format("based on proximity in interactome to %s with score %.3f based on phenotypic similarity to disease %s",closestGene,phenoScore,humanDiseases.get(phenoGenes.get(col_idx)));   
                }
                else{
                    evidence = String.format("based on proximity in interactome to %s with score %.3f based on phenotypic similarity to mouse mutant",closestGene,phenoScore);   
                }
                ++PPIdataAvailable;
            } else {// NO PHENO HIT OR PPI INTERACTION
                evidence = "as no phenotype or PPI evidence";
            }
            PhenoWandererRelevanceScore relScore = new PhenoWandererRelevanceScore(val, evidence);
            gene.addRelevanceScore(relScore, FilterType.PHENOWANDERER_PRIORITY);
        }

        /*
         * refactor all scores for genes that are not direct pheno-hits but in
         * PPI with them to a linear range
         */
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<Float, List<Gene>>();
        for (Gene g : gene_list) {
            if (scores.get(g.getEntrezGeneID()) == null && randomWalkMatrix.objectid2idx.containsKey(g.getEntrezGeneID())) {// Only do for non-pheno direct hits
                float geneScore = g.getRelevanceScore(FilterType.PHENOWANDERER_PRIORITY);
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
        //Iterator<Float> i = set.iterator();
        //while (i.hasNext()) {

        for (float score : set) {
            List<Gene> geneScoreGeneList = geneScoreMap.get(score);
            int sharedHits = geneScoreGeneList.size();
            float adjustedRank = rank;
            if (sharedHits > 1) {
                adjustedRank = rank + (sharedHits / 2);
            }
            float newScore = 0.65f - 0.65f * (adjustedRank / gene_list.size());
            rank = rank + sharedHits;
            for (Gene g : geneScoreGeneList) {
                g.resetRelevanceScore(FilterType.PHENOWANDERER_PRIORITY, newScore);
            }
        }
        String s = String.format("Phenotype and Protein-Protein Interaction evidence was available for %d of %d genes (%.1f%%)",
                PPIdataAvailable, totalGenes, 100f * ((float) PPIdataAvailable / (float) totalGenes));
        this.n_before = totalGenes;
        this.n_after = PPIdataAvailable;
        this.messages.add(s);
//        StringBuilder sb = new StringBuilder();
//        sb.append("Seed genes:");
//        for (Integer seed : phenoGenes) {
//            sb.append(seed + "&nbsp;");
//        }
//        this.messages.add(sb.toString());
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