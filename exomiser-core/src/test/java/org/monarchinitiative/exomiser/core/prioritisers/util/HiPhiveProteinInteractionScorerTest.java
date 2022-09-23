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
import org.jblas.FloatMatrix;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveProteinInteractionScorerTest {

    //the Random-walk with restart (RWR) matrix used here is described in:
    //http://www.sciencedirect.com/science/article/pii/S0002929708001729
    // The matrix is "a global distance measure based on random walk with restart (RWR) to define similarity between genes
    // within this interaction network and to rank candidates on the basis of this similarity to known diseases genes.
    // Intuitively, the RWR algorithm calculates the similarity between two genes, i and j, on the basis of the
    // likelihood that a random walk through the interaction network starting at gene i will finish at gene j, whereby
    // all possible paths between the two genes are taken into account. In our implementation, we let the random walk
    // start with equal probability from each of the known disease-gene family members in order to search for an
    // additional family member in the linkage interval"

    //here we have 6 genes in two unconnected networks:
    // ((1-2-3-4), (5-6))
    // the probability of a walk hopping from one gene to its neighbour is 0.1

    private static final double HIGH_QUALITY_PHENO_SCORE_CUT_OFF = 0.6;
    private final DataMatrix dataMatrix = makeDataMatrix();

    private DataMatrix makeDataMatrix() {
        float[][] matrix = {
                //gene1, gene2, gene3, gene4, gene5, gene6
                {0.90f, 0.10f, 0.01f, 0.01f, 0.00f, 0.00f}, //gene1
                {0.10f, 0.90f, 0.10f, 0.10f, 0.00f, 0.00f}, //gene2
                {0.01f, 0.10f, 0.90f, 0.10f, 0.00f, 0.00f}, //gene3
                {0.01f, 0.10f, 0.10f, 0.90f, 0.00f, 0.00f}, //gene4
                {0.00f, 0.00f, 0.00f, 0.00f, 0.90f, 0.10f}, //gene5
                {0.00f, 0.00f, 0.00f, 0.00f, 0.10f, 0.90f}, //gene6
        };
        FloatMatrix testMatrix = new FloatMatrix(matrix);
        Map<Integer, Integer> geneIdToRowIndex = new HashMap<>();
        geneIdToRowIndex.put(1, 0);
        geneIdToRowIndex.put(2, 1);
        geneIdToRowIndex.put(3, 2);
        geneIdToRowIndex.put(4, 3);
        geneIdToRowIndex.put(5, 4);
        geneIdToRowIndex.put(6, 5);

        return new InMemoryDataMatrix(testMatrix, geneIdToRowIndex);
    }

    private GeneModelPhenotypeMatch geneModelMatch(int entrezGeneId, double phenoScore, String modelId) {
        GeneDiseaseModel modelForGeneId = makeModelForGeneId(modelId, entrezGeneId);
        return new GeneModelPhenotypeMatch(phenoScore, modelForGeneId, Collections.emptyList());
    }


    private GeneDiseaseModel makeModelForGeneId(String modelId, int entrezGeneId) {
        return new GeneDiseaseModel(modelId, Organism.HUMAN, entrezGeneId,"","", "", Collections.emptyList());
    }

    @Test
    public void testEmpty() {
        HiPhiveProteinInteractionScorer instance = HiPhiveProteinInteractionScorer.empty();
        assertThat(instance.getClosestPhenoMatchInNetwork(123), equalTo(GeneMatch.NO_HIT));
    }

    @Test
    public void testEmptyInputValues() {
        HiPhiveProteinInteractionScorer instance = new HiPhiveProteinInteractionScorer(DataMatrix.empty(), ArrayListMultimap.create(), 0.0);
        assertThat(instance.getClosestPhenoMatchInNetwork(123), equalTo(GeneMatch.NO_HIT));
    }

    @Test
    public void geneInNetworkButUnderPhenotypeCutoff() {
        ArrayListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = ArrayListMultimap.create();
        bestGeneModels.put(1, geneModelMatch(1, HIGH_QUALITY_PHENO_SCORE_CUT_OFF - 0.5, "MONDO:1"));

        HiPhiveProteinInteractionScorer instance = new HiPhiveProteinInteractionScorer(dataMatrix, bestGeneModels, HIGH_QUALITY_PHENO_SCORE_CUT_OFF);

        assertThat(instance.getClosestPhenoMatchInNetwork(1), equalTo(GeneMatch.NO_HIT));
    }

    @Test
    public void geneInNetworkSelfHit() {
        ArrayListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = ArrayListMultimap.create();
        bestGeneModels.put(1, geneModelMatch(1, 0.7, "MONDO:1"));

        HiPhiveProteinInteractionScorer instance = new HiPhiveProteinInteractionScorer(dataMatrix, bestGeneModels, HIGH_QUALITY_PHENO_SCORE_CUT_OFF);

        assertThat(instance.getClosestPhenoMatchInNetwork(1), equalTo(GeneMatch.NO_HIT));
    }

    @Test
    public void geneInNetworkClosestHit() {
        GeneModelPhenotypeMatch bestModel = geneModelMatch(5, 0.7, "MONDO:5");

        ArrayListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = ArrayListMultimap.create();
        bestGeneModels.put(bestModel.getEntrezGeneId(), bestModel);

        HiPhiveProteinInteractionScorer instance = new HiPhiveProteinInteractionScorer(dataMatrix, bestGeneModels, HIGH_QUALITY_PHENO_SCORE_CUT_OFF);

        int queryGeneId = 6;
        GeneMatch closestPhenoMatchInNetwork = instance.getClosestPhenoMatchInNetwork(queryGeneId);

        assertThat(closestPhenoMatchInNetwork.getQueryGeneId(), equalTo(queryGeneId));
        assertThat(closestPhenoMatchInNetwork.getMatchGeneId(), equalTo(bestModel.getEntrezGeneId()));
        assertThat(closestPhenoMatchInNetwork.getScore(), closeTo(0.57d, 0.001));
        assertThat(closestPhenoMatchInNetwork.getBestMatchModels(), equalTo(List.of(bestModel)));
    }

    @Test
    public void geneInNetworkClosestHitTwoNetworks() {

        GeneModelPhenotypeMatch model5 = geneModelMatch(5, 0.7, "MONDO:5");
        //in the interaction matrix above 1-2-3-4 are in one network and 5-6 are in another
        GeneModelPhenotypeMatch model2 = geneModelMatch(2, 0.62, "MONDO:2");
        GeneModelPhenotypeMatch model3 = geneModelMatch(3, 0.63, "MONDO:3");

        ArrayListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = ArrayListMultimap.create();
        bestGeneModels.put(model5.getEntrezGeneId(), model5);
        bestGeneModels.put(model2.getEntrezGeneId(), model2);
        bestGeneModels.put(model3.getEntrezGeneId(), model3);

        HiPhiveProteinInteractionScorer instance = new HiPhiveProteinInteractionScorer(dataMatrix, bestGeneModels, HIGH_QUALITY_PHENO_SCORE_CUT_OFF);

        int queryGeneId = 4;
        GeneMatch closestPhenoMatchInNetwork = instance.getClosestPhenoMatchInNetwork(queryGeneId);

        assertThat(closestPhenoMatchInNetwork.getQueryGeneId(), equalTo(queryGeneId));
        assertThat(closestPhenoMatchInNetwork.getMatchGeneId(), equalTo(model3.getEntrezGeneId()));
        assertThat(closestPhenoMatchInNetwork.getScore(), closeTo(0.563d, 0.001));
        assertThat(closestPhenoMatchInNetwork.getBestMatchModels(), equalTo(List.of(model3)));
    }

}