/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.util.OrganismPhenotypeMatches;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
public class PhivePriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(PhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.PHIVE_PRIORITY;
    private static final float NO_PHENOTYPE_HIT_SCORE = 0.1f;
    static final float NO_MOUSE_MODEL_SCORE = 0.6f;

    private final List<String> hpoIds;
    private final PriorityService priorityService;

    double bestMaxScore = 0d;
    double bestAvgScore = 0d;

    private DataSource dataSource;

    /**
     * Keeps track of the number of variants for which data was available in
     * Phenodigm.
     */
    private int foundDataForMgiPhenodigm;

    public PhivePriority(List<String> hpoIds, PriorityService priorityService) {
        this.hpoIds = hpoIds;
        this.priorityService = priorityService;
        //This can be moved into a report section - FilterReport should probably turn into an AnalysisStepReport
        //Then can remove getMessages from the interface. 
//        messages.add(String.format("<a href = \"http://www.sanger.ac.uk/resources/databases/phenodigm\">Mouse PhenoDigm Filter</a>"));
//        if (disease != null) {
//            String url = String.format("http://omim.org/%s", disease);
//            if (disease.contains("ORPHANET")) {
//                String diseaseId = disease.split(":")[1];
//                url = String.format("http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=%s", diseaseId);
//            }
//            String anchor = String.format("Mouse phenotypes for candidate genes were compared to <a href=\"%s\">%s</a>\n", url, disease);
//            this.messages.add(String.format("Mouse PhenoDigm Filter for OMIM"));
//            messages.add(anchor);
//        } else {
//            String anchor = String.format("Mouse phenotypes for candidate genes were compared to user-supplied clinical phenotypes");
//            messages.add(anchor);
//        }
    }

    /**
     * Flag to output results of filtering against PhenoDigm data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    @Override
    public void prioritizeGenes(List<Gene> genes) {

        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        OrganismPhenotypeMatches humanMousePhenotypeMatches = getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, Organism.MOUSE);
        logOrganismPhenotypeMatches(humanMousePhenotypeMatches);

        foundDataForMgiPhenodigm = 0;

        Set<String> hpIdsWithPhenotypeMatch = new LinkedHashSet<>();
//        Map<String, Double> bestMappedTermScore = new HashMap<>();
//        Map<String, String> bestMappedTermMpId = new HashMap<>();
        //knownMps.size() might be the reason that the scores from phive are slightly different to hiPhive mouse
        Set<String> knownMps = new LinkedHashSet<>();
        Map<String, Double> mappedTerms = new HashMap<>();

        String mappingQuery = "SELECT mp_id, score FROM hp_mp_mappings M WHERE M.hp_id = ?";

        for (String hpId : hpoIds) {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement findMappingStatement = connection.prepareStatement(mappingQuery);
                findMappingStatement.setString(1, hpId);
                ResultSet rs = findMappingStatement.executeQuery();
                int found = 0;
                while (rs.next()) {
                    found = 1;
                    String mpId = rs.getString(1);
                    knownMps.add(mpId);
                    String hashKey = hpId + mpId;
                    double score = rs.getDouble("score");
                    mappedTerms.put(hashKey, score);
//                    if (bestMappedTermScore.containsKey(hpId)) {
//                        if (score > bestMappedTermScore.get(hpId)) {
//                            bestMappedTermScore.put(hpId, score);
//                            bestMappedTermMpId.put(hpId, mpId);
//                        }
//                    } else {
//                        bestMappedTermScore.put(hpId, score);
//                        bestMappedTermMpId.put(hpId, mpId);
//                    }
                }
                if (found == 1) {
                    hpIdsWithPhenotypeMatch.add(hpId);
                }
            } catch (SQLException e) {
                logger.error("Problem setting up SQL query: {}", mappingQuery, e);
            }
        }

//        calculateBestPhenotypeMatchScores(hpIdsWithPhenotypeMatch, bestMappedTermScore, bestMappedTermMpId, mappedTerms);

        bestMaxScore = humanMousePhenotypeMatches.getBestMatchScore();
        bestAvgScore = humanMousePhenotypeMatches.getBestAverageScore();

        List<Model> mouseModels = priorityService.getModelsForOrganism(Organism.MOUSE);

//        Map<Integer, Set<Model>> geneModelPhenotypeMatches = calculateGeneModelPhenotypeMatches(bestMaxScore, bestAvgScore, humanMousePhenotypeMatches, mouseModels);
//        Map<Integer, Model> bestGeneModels = getBestGeneModelForGenes(geneModelPhenotypeMatches);


        for (Gene gene : genes) {
//            GeneModel bestModelForGene = bestGeneModels.get(gene.getEntrezGeneID());
//            if(bestModelForGene == null) {
//                 new PhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), NO_MOUSE_MODEL_SCORE, null, null);
//            }
//            new PhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), bestModelForGene.getScore(), bestModelForGene.getModelGeneId(), bestModelForGene.getModelGeneSymbol());

            PhivePriorityResult phiveScore = prioritiseGene(gene, knownMps, hpIdsWithPhenotypeMatch, mappedTerms);
            gene.addPriorityResult(phiveScore);
        }
//        messages.add(String.format("Data analysed for %d genes using Mouse PhenoDigm", genes.size()));
    }

    //TODO: turn this into a CrossSpeciesPhenotypeMatcher? - THIS IS CURRENTLY COPIED FROM HIPHIVEPRIORITY
    private OrganismPhenotypeMatches getMatchingPhenotypesForOrganism(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        logger.info("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = priorityService.getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new OrganismPhenotypeMatches(organism, ImmutableMap.copyOf(speciesPhenotypeMatches));
    }

    private void logOrganismPhenotypeMatches(OrganismPhenotypeMatches organismPhenotypeMatches) {
        logger.info("Best {} phenotype matches:", organismPhenotypeMatches.getOrganism());
        for (PhenotypeMatch bestMatch : organismPhenotypeMatches.getBestPhenotypeMatches()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        logger.info("bestMaxScore={} bestAvgScore={}", organismPhenotypeMatches.getBestMatchScore(), organismPhenotypeMatches.getBestAverageScore(), organismPhenotypeMatches.getBestPhenotypeMatches().size());
    }

    private void calculateBestPhenotypeMatchScores(Set<String> hpIdsWithPhenotypeMatch, Map<String, Float> bestMappedTermScore, Map<String, String> bestMappedTermMpId, Map<String, Float> mappedTerms) {
        // calculate perfect mouse model scores
        float sumBestScore = 0f;

        int bestHitCounter = 0;
        // loop over each hp id should start here
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
    }

    private PhivePriorityResult prioritiseGene(Gene gene, Set<String> knownMps, Set<String> hpIdsWithPhenotypeMatch, Map<String, Double> mappedTerms) {
        double mgiScore = NO_PHENOTYPE_HIT_SCORE;
        String mgiGeneId = null;
        String mgiGeneSymbol = null;
        String genesymbol = gene.getGeneSymbol();
        
        /* Note that there are two queries. The testGeneStatement basically tests
         * whether the gene in question is in the database; it must have both an
         * orthologue in the table {@code human2mouse_orthologs}, and there must be
         * some data on it in the table {@code  mgi_mp}.
         * 
         * Then, we select the MGI gene id (e.g., MGI:1234567), the corresponding
         * mouse gene symbol and the phenodigm score. There is currently one score
         * for each pair of OMIM diseases and MGI genes.
         */
        //TODO: test this using one statement only:
        // SELECT mouse_model_id, mp_id, M.mgi_gene_id, M.mgi_gene_symbol
        // FROM mgi_mp M, human2mouse_orthologs H
        // WHERE M.mgi_gene_id = H.mgi_gene_id AND human_gene_symbol = ?
        try (Connection connection = dataSource.getConnection()) {
            String testGeneQuery = "SELECT human_gene_symbol "
                    + "FROM mgi_mp M, human2mouse_orthologs H "
                    + "WHERE M.mgi_gene_id = H.mgi_gene_id "
                    + "AND human_gene_symbol = ? LIMIT 1";
            PreparedStatement testGeneStatement = connection.prepareStatement(testGeneQuery);
            testGeneStatement.setString(1, genesymbol);
            ResultSet rs2 = testGeneStatement.executeQuery();
            if (rs2.next()) {
                // calculate score for this gene
                String mouseAnnotationQuery = "SELECT mouse_model_id, mp_id, M.mgi_gene_id, M.mgi_gene_symbol "
                        + "FROM mgi_mp M, human2mouse_orthologs H "
                        + "WHERE M.mgi_gene_id = H.mgi_gene_id "
                        + "AND human_gene_symbol = ?";
                PreparedStatement findMouseAnnotationStatement = connection.prepareStatement(mouseAnnotationQuery);
                findMouseAnnotationStatement.setString(1, genesymbol);
                ResultSet rs = findMouseAnnotationStatement.executeQuery();
                double bestCombinedScore = 0f;// keep track of best score for gene
                while (rs.next()) {
                    int mouseModelId = rs.getInt(1);
                    //System.out.println("Calculating score for mouse model id "+mouse_model_id+" gene "+genesymbol);
                    String mpIds = rs.getString(2);
                    mgiGeneId = rs.getString(3);
                    mgiGeneSymbol = rs.getString(4);
                    String[] mpInitial = mpIds.split(",");
                    //TODO: this should be the MP terms from the model matches
                    List<String> mpList = new ArrayList<>();
                    for (String mpId : mpInitial) {
                        if (knownMps.contains(mpId)) {
                            mpList.add(mpId);
                        }
                    }

                    int rowColumnCount = hpIdsWithPhenotypeMatch.size() + mpList.size();
                    double maxScore = 0f;
                    double sumBestHitRowsColumnsScore = 0f;

                    for (String hpId : hpIdsWithPhenotypeMatch) {
                        double bestScore = 0f;
                        for (String mpId : mpList) {
                            String hashKey = hpId + mpId;
                            if (mappedTerms.containsKey(hashKey)) {
                                double score = mappedTerms.get(hashKey);
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
                        double bestScore = 0f;
                        for (String hpId : hpIdsWithPhenotypeMatch) {
                            String hashKey = hpId + mpId;
                            if (mappedTerms.containsKey(hashKey)) {
                                double score = mappedTerms.get(hashKey);
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
                        double avgBestHitRowsColumnsScore = sumBestHitRowsColumnsScore / rowColumnCount;
                        double combinedScore = 50 * (maxScore / bestMaxScore + avgBestHitRowsColumnsScore / bestAvgScore);
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
                if (mgiScore >= 0) {
                    foundDataForMgiPhenodigm++;
                }
            } else {
                // no mouse model exists in MGI for this gene
                mgiScore = NO_MOUSE_MODEL_SCORE;
            }
            rs2.close();
        } catch (SQLException e) {
            logger.error("Error executing Phenodigm query: ", e);
        }
        return new PhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), mgiScore, mgiGeneId, mgiGeneSymbol);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.hpoIds);
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
        final PhivePriority other = (PhivePriority) obj;
        if (!Objects.equals(this.hpoIds, other.hpoIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhivePriority{" + "hpoIds=" + hpoIds + '}';
    }

}
