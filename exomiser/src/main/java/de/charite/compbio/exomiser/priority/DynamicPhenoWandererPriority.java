package de.charite.compbio.exomiser.priority;



import jannovar.common.Constants;

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
public class DynamicPhenoWandererPriority implements IPriority, Constants {

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
    private ArrayList<Integer> phenoGenes;
    private ArrayList<String> phenoGeneSymbols;
    private String hpo_ids;
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
    public DynamicPhenoWandererPriority(String randomWalkMatrixFileZip, String randomWalkGeneId2IndexFileZip, String hpo_ids, String candGene)
            throws ExomizerInitializationException {
        this.hpo_ids = hpo_ids;
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
        return FilterType.DYNAMIC_PHENOWANDERER_FILTER;
    }

    /**
     * Compute the distance of all genes in the Random Walk matrix to the set of
     * seed genes given by the user.
     */
    private void computeDistanceAllNodesFromStartNodes() throws ExomizerInitializationException {
        ArrayList<Integer> phenoGenes = new ArrayList<Integer>();
        ArrayList<String> phenoGeneSymbols = new ArrayList<String>();
        String mapping_query = String.format("SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?");
        PreparedStatement findMappingStatement = null;
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);

        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            throw new ExomizerInitializationException(error);
        }
        PreparedStatement findMouseAnnotationStatement = null;
        String mouse_annotation = String.format("SELECT mouse_model_id, mp_id, M.mgi_gene_id, M.mgi_gene_symbol, entrez_id, human_gene_symbol "
                + "FROM mgi_mp M, human2mouse_orthologs_new H WHERE M.mgi_gene_id=H.mgi_gene_id");
        try {
            findMouseAnnotationStatement = connection.prepareStatement(mouse_annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mouse_annotation;
            throw new ExomizerInitializationException(error);
        }
        String[] hps_initial = hpo_ids.split(",");
        ArrayList<String> hp_list = new ArrayList<String>();
        HashMap<String, Float> mapped_terms = new HashMap<String, Float>();
        HashMap<String, Float> best_mapped_term_score = new HashMap<String, Float>();
        HashMap<String, String> best_mapped_term_mpid = new HashMap<String, String>();
        HashMap<String, Integer> knownMps = new HashMap<String, Integer>();
        for (String hpid : hps_initial) {
            try {
                findMappingStatement.setString(1, hpid);
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
                    hp_list.add(hpid);
                }
            } catch (SQLException e) {
                String error = "Problem setting up SQL query:" + mouse_annotation;
                throw new ExomizerInitializationException(error);
            }
        }
        String[] hps = new String[hp_list.size()];
        hp_list.toArray(hps);

        // calculate perfect mouse model scores
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
            ResultSet rs = findMouseAnnotationStatement.executeQuery();
            while (rs.next()) {
                String mp_ids = rs.getString(2);
                int entrez = rs.getInt(5);
                String humanGene = rs.getString(6);
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
                                
                    if (scores.get(entrez) != null) {
                        if (score > (Double) scores.get(entrez)) {
                            scores.put(entrez, score);
                        }
                    } else {
                        if (score > 0.6) {// only build PPI network for high qual hits
                            phenoGenes.add(entrez);
                            phenoGeneSymbols.add(humanGene);
                        }
                        scores.put(entrez, score);
                    }
                }
            }//end of rs
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mouse_annotation;
            throw new ExomizerInitializationException(error);
        }
        // GET HUMAN SCORES
        mapping_query = String.format("SELECT hp_id_hit, score FROM hp_hp_mappings M WHERE M.hp_id = ?");
        findMappingStatement = null;
        try {
            findMappingStatement = connection.prepareStatement(mapping_query);

        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mapping_query;
            throw new ExomizerInitializationException(error);
        }
        findMouseAnnotationStatement = null;
        mouse_annotation = String.format("SELECT disease_id, hp_id, gene_id, human_gene_symbol FROM human2mouse_orthologs_new hm, "
                + "disease_hp M, omim H "
                + "WHERE hm.entrez_id=H.gene_id AND M.disease_id=concat('OMIM:',H.phenmim)");
        try {
            findMouseAnnotationStatement = connection.prepareStatement(mouse_annotation);
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mouse_annotation;
            throw new ExomizerInitializationException(error);
        }
        hps_initial = hpo_ids.split(",");
        hp_list = new ArrayList<String>();
        mapped_terms = new HashMap<String, Float>();
        best_mapped_term_score = new HashMap<String, Float>();
        best_mapped_term_mpid = new HashMap<String, String>();
        knownMps = new HashMap<String, Integer>();
        for (String hpid : hps_initial) {
            try {
                findMappingStatement.setString(1, hpid);
                ResultSet rs = findMappingStatement.executeQuery();
                int found = 0;
                while (rs.next()) {
                    String mp_id = rs.getString(1);
                    knownMps.put(mp_id, 1);
                    StringBuffer hashKey = new StringBuffer();
                    hashKey.append(hpid);
                    hashKey.append(mp_id);
                    float score = rs.getFloat(2);
                    mapped_terms.put(hashKey.toString(), score);
                    // best hit for hp vs hp should always be itself
                    if (hpid.equals(mp_id)) {
                        best_mapped_term_score.put(hpid, score);
                        best_mapped_term_mpid.put(hpid, mp_id);
                        found = 1;
                    }
                }
                if (found == 1) {
                    hp_list.add(hpid);
                }
            } catch (SQLException e) {
                String error = "Problem setting up SQL query:" + mouse_annotation;
                throw new ExomizerInitializationException(error);
            }
        }
        hps = new String[hp_list.size()];
        hp_list.toArray(hps);

        // calculate perfect mouse model scores
        sum_best_score = 0f;
        best_max_score = 0f;
        best_hit_counter = 0;
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
        best_avg_score = sum_best_score / best_hit_counter;
        //System.out.println("BEST MAX " + best_max_score + ", SUM BEST SCORE " + sum_best_score + " counter " + best_hit_counter);
        // calculate score for this gene
        try {
            ResultSet rs = findMouseAnnotationStatement.executeQuery();
            while (rs.next()) {
                String diseaseHit = rs.getString(1); 
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
                    //if (entrez == 2639) System.out.println("GENE MAX " + max_score + ", SUM gene SCORE " + sum_best_hit_rows_columns_score + " counter " + row_column_count + " score " + score);
                    if (scores.get(entrez) != null) {
                        if (score > (Double) scores.get(entrez)) {
                            scores.put(entrez, score);
                            humanScores.put(entrez, score);
                            humanDiseases.put(entrez, diseaseHit);
                        }
                    } else {
                        if (score > 0.6) {// only build PPI network for high qual hits
                            phenoGenes.add(entrez);
                            phenoGeneSymbols.add(humanGene);
                        }
                        scores.put(entrez, score);
                        humanScores.put(entrez, score);
                        humanDiseases.put(entrez, diseaseHit);
                    }
                }
            }//end of rs
        } catch (SQLException e) {
            String error = "Problem setting up SQL query:" + mouse_annotation;
            throw new ExomizerInitializationException(error);
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
     * @see exomizer.filter.IFilter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override
    public void prioritize_list_of_genes(List<Gene> gene_list) {
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
            gene.addRelevanceScore(relScore, FilterType.DYNAMIC_PHENOWANDERER_FILTER);
        }

        /*
         * refactor all scores for genes that are not direct pheno-hits but in
         * PPI with them to a linear range
         */
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<Float, List<Gene>>();
        for (Gene g : gene_list) {
            if (scores.get(g.getEntrezGeneID()) == null && randomWalkMatrix.objectid2idx.containsKey(g.getEntrezGeneID())) {// Only do for non-pheno direct hits
                float geneScore = g.getRelevanceScore(FilterType.DYNAMIC_PHENOWANDERER_FILTER);
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
                g.resetRelevanceScore(FilterType.DYNAMIC_PHENOWANDERER_FILTER, newScore);
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
    public boolean display_in_HTML() {
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