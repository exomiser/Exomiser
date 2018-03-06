/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.jblas.FloatMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HiPhiveProteinInteractionScorer {

    private static final Logger logger = LoggerFactory.getLogger(HiPhiveProteinInteractionScorer.class);

    private static final HiPhiveProteinInteractionScorer EMPTY = new HiPhiveProteinInteractionScorer();

    private final DataMatrix dataMatrix;
    private final ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels;

    private final List<GeneColumnIndex> weightedHighQualityMatrixIndex;
    private final FloatMatrix weightedHighQualityMatrix;

    public static HiPhiveProteinInteractionScorer empty() {
        return EMPTY;
    }

    private HiPhiveProteinInteractionScorer() {
        this.dataMatrix = DataMatrix.empty();
        this.bestGeneModels = ArrayListMultimap.create();

        this.weightedHighQualityMatrixIndex = Collections.emptyList();
        this.weightedHighQualityMatrix = FloatMatrix.EMPTY;
    }

    public HiPhiveProteinInteractionScorer(DataMatrix dataMatrix, ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels, double highQualityPhenoScoreCutOff) {
        this.dataMatrix = dataMatrix;
        this.bestGeneModels = bestGeneModels;

        this.weightedHighQualityMatrixIndex = makeWeightedHighQualityMatrixIndex(highQualityPhenoScoreCutOff, dataMatrix, bestGeneModels.values());
        this.weightedHighQualityMatrix = makeWeightedHighQualityProteinInteractionMatrix(dataMatrix, weightedHighQualityMatrixIndex);
    }

    private List<GeneColumnIndex> makeWeightedHighQualityMatrixIndex(double highQualityPhenoScoreCutOff, DataMatrix dataMatrix, Collection<GeneModelPhenotypeMatch> values) {
        List<GeneColumnIndex> highQualityMappings = new ArrayList<>();
        Map<Integer, Double> highQualityPhenoMatchedGeneScoreMap = getHighestGeneIdPhenoScoresInDataMatrix(highQualityPhenoScoreCutOff, dataMatrix, values);
        int column = 0;
        for (Map.Entry<Integer, Double> entry : highQualityPhenoMatchedGeneScoreMap.entrySet()) {
            Integer entrezGeneId = entry.getKey();
            Double score = entry.getValue();
            GeneColumnIndex geneColumnIndex = new GeneColumnIndex(entrezGeneId, score, column++);
            logger.debug("Added {}", geneColumnIndex);
            highQualityMappings.add(geneColumnIndex);
        }
        return highQualityMappings;
    }

    private Map<Integer, Double> getHighestGeneIdPhenoScoresInDataMatrix(double highQualityPhenoScoreCutOff, DataMatrix dataMatrix, Collection<GeneModelPhenotypeMatch> bestGeneModelPhenoMatches) {
        Map<Integer, Double> highestGeneIdPhenoScores = new LinkedHashMap<>();
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : bestGeneModelPhenoMatches) {
            Integer entrezId = geneModelPhenotypeMatch.getEntrezGeneId();
            Double score = geneModelPhenotypeMatch.getScore();
            // only build PPI network for high quality hits contained in the matrix
            if (score > highQualityPhenoScoreCutOff && dataMatrix.containsGene(entrezId)) {
                logger.debug("Adding high quality score for {} score={}", geneModelPhenotypeMatch.getHumanGeneSymbol(), geneModelPhenotypeMatch
                        .getScore());
                if (!highestGeneIdPhenoScores.containsKey(entrezId) || score > highestGeneIdPhenoScores.get(entrezId)) {
                    highestGeneIdPhenoScores.put(entrezId, score);
                }
            }
        }
        logger.info("Using {} high quality phenotypic gene match scores (score > {})", highestGeneIdPhenoScores.size(), highQualityPhenoScoreCutOff);
        return Collections.unmodifiableMap(highestGeneIdPhenoScores);
    }

    private FloatMatrix makeWeightedHighQualityProteinInteractionMatrix(DataMatrix dataMatrix, List<GeneColumnIndex> highQualityPhenotypeMappings) {
        logger.info("Making weighted-score Protein-Protein interaction sub-matrix from high quality phenotypic gene matches...");
        logger.info("Original data matrix ({} rows * {} columns)", dataMatrix.numRows(), dataMatrix.numColumns());
        int rows = dataMatrix.numRows();
        int cols = highQualityPhenotypeMappings.size();
        FloatMatrix highQualityPpiMatrix = FloatMatrix.zeros(rows, cols);
        for (GeneColumnIndex geneColumnIndex : highQualityPhenotypeMappings) {
            //The original DataMatrix is a symmetrical matrix this new one is asymmetrical with the original rows but only high-quality columns.
            FloatMatrix column = dataMatrix.getColumnMatrixForGene(geneColumnIndex.geneId);
            FloatMatrix weightedColumn = column.mul((float) geneColumnIndex.phenoScore);
            highQualityPpiMatrix.putColumn(geneColumnIndex.columnIndex, weightedColumn);
        }
        logger.info("Made high quality interaction matrix ({} rows * {} columns)", highQualityPpiMatrix.getRows(), highQualityPpiMatrix
                .getColumns());
        return highQualityPpiMatrix;
    }

    public GeneMatch getClosestPhenoMatchInNetwork(Integer entrezGeneId) {
        if (!dataMatrix.containsGene(entrezGeneId) || weightedHighQualityMatrixIndex.isEmpty()) {
            return GeneMatch.NO_HIT;
        }
        int rowIndex = dataMatrix.getRowIndexForGene(entrezGeneId);
        GeneColumnIndex topHighQualityGene = getGeneColumnIndexOfMostPhenotypicallySimilarGene(rowIndex, entrezGeneId);
        /* Changed method to return -1 if no hit as otherwise could not distinguish between
        no hit or hit to 1st entry in column (entrezGene 50640). When querying with 50640 this
        resulted in a self-hit being returned with a PPI score of 0.5+0.7=1.2 and also lots of
        low-scoring (0.5) PPI hits to 50640 for other genes with no PPI match
         */
        if (topHighQualityGene == null) {
            return GeneMatch.NO_HIT;
        }

        // optimal adjustment based on benchmarking to allow walker scores to compete with low phenotype scores
        double walkerScore = 0.5 + weightedHighQualityMatrix.get(rowIndex, topHighQualityGene.columnIndex);

        Integer closestGeneId = topHighQualityGene.geneId;
        List<GeneModelPhenotypeMatch> models = bestGeneModels.get(closestGeneId);

        return GeneMatch.builder()
                .queryGeneId(entrezGeneId)
                .matchGeneId(closestGeneId)
                .score(walkerScore)
                .bestMatchModels(models)
                .build();
    }

    private GeneColumnIndex getGeneColumnIndexOfMostPhenotypicallySimilarGene(int rowIndex, Integer entrezGeneId) {
        GeneColumnIndex bestGeneColumnIndex = null;
        double bestScore = 0;
        for (GeneColumnIndex geneColumnIndex : weightedHighQualityMatrixIndex) {
            //avoid self-hits now are testing genes with direct pheno-evidence as well
            if (!geneColumnIndex.geneId.equals(entrezGeneId)) {
                double cellScore = weightedHighQualityMatrix.get(rowIndex, geneColumnIndex.columnIndex);
                if (cellScore > bestScore) {
                    bestScore = cellScore;
                    bestGeneColumnIndex = geneColumnIndex;
                }
            }
        }
        return bestGeneColumnIndex;
    }

    /**
     * Mapping between an entrez gene id, its phenotype score and a column in the high-quality matrix
     */
    class GeneColumnIndex {
        private final Integer geneId;
        private final double phenoScore;
        private final int columnIndex;

        GeneColumnIndex(Integer geneId, double phenoScore, int columnIndex) {
            this.geneId = geneId;
            this.phenoScore = phenoScore;
            this.columnIndex = columnIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GeneColumnIndex that = (GeneColumnIndex) o;
            return Double.compare(that.phenoScore, phenoScore) == 0 &&
                    columnIndex == that.columnIndex &&
                    Objects.equals(geneId, that.geneId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(geneId, phenoScore, columnIndex);
        }

        @Override
        public String toString() {
            return "GeneColumnMapping{" +
                    "geneId=" + geneId +
                    ", phenoScore=" + phenoScore +
                    ", columnIndex=" + columnIndex +
                    '}';
        }
    }
}
