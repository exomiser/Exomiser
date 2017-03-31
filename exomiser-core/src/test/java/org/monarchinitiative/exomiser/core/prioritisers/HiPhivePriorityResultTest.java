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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityResultTest {
    
    private HiPhivePriorityResult instance;
    private final int geneId = 12345;
    private final String geneSymbol = "FGFR2";
    private final double score = 0.87d;
    private List<PhenotypeTerm> queryPhenotypeTerms;
    private List<GeneModelPhenotypeMatch> phenotypeEvidence;
    private List<GeneModelPhenotypeMatch> ppiEvidence;
    private final double ppiScore = 0.6d;
    private final boolean matchesCandidateGene = true;
    
    @Before
    public void setUp() {
        queryPhenotypeTerms = new ArrayList<>();
        phenotypeEvidence = new ArrayList<>();
        ppiEvidence = new ArrayList<>();
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, phenotypeEvidence, ppiEvidence, ppiScore, matchesCandidateGene);
    }

    private GeneModelPhenotypeMatch stubGeneModelPhenotypeMatch(Organism organism, double score) {
        GeneOrthologModel model = new GeneOrthologModel("gene1_model1", organism, 12345, geneSymbol, "MGI:12345", "gene1", Collections.emptyList());
        return new GeneModelPhenotypeMatch(score, model, Collections.emptyList());
    }
        
    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Test
    public void testGetGeneSymbol() {
        assertThat(instance.getGeneSymbol(), equalTo(geneSymbol));
    }

    @Test
    public void testGetScore() {
//        assertThat(instance.getScore(), closeTo(score, 0.0001));
        assertThat(instance.getScore(), equalTo(score));
    }

    @Test
    public void testGetQueryPhenotypeTerms() {
        assertThat(instance.getQueryPhenotypeTerms(), equalTo(queryPhenotypeTerms));
    }

    @Test
    public void testGetPhenotypeEvidence() {
        assertThat(instance.getPhenotypeEvidence(), equalTo(phenotypeEvidence));
    }

    @Test
    public void testGetPpiEvidence() {
        assertThat(instance.getPpiEvidence(), equalTo(ppiEvidence));
    }

    @Test
    public void testGetHTMLCode() {
        assertThat(instance.getHTMLCode(), equalTo("<dl><dt>No phenotype or PPI evidence</dt></dl>"));
    }

    @Test
    public void testGetHumanScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getHumanScore(), equalTo(0d));
    }
    
    @Test
    public void testGetHumanScoreMatchesModelScore() {
        double modelScore = 1d;
        GeneModelPhenotypeMatch geneModel = stubGeneModelPhenotypeMatch(Organism.HUMAN, modelScore);

        List<GeneModelPhenotypeMatch> models = Arrays.asList(geneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getHumanScore(), equalTo(modelScore));
    }

    @Test
    public void testGetMouseScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getMouseScore(), equalTo(0d));
    }

    @Test
    public void testGetMouseScoreMatchesModelScore() {
        double modelScore = 1d;
        GeneModelPhenotypeMatch geneModel = stubGeneModelPhenotypeMatch(Organism.MOUSE, modelScore);

        List<GeneModelPhenotypeMatch> models = Arrays.asList(geneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getMouseScore(), equalTo(modelScore));
    }
    
    @Test
    public void testGetFishScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getFishScore(), equalTo(0d));
    }

    @Test
    public void testGetFishScoreMatchesModelScore() {
        double modelScore = 1d;
        GeneModelPhenotypeMatch geneModel = stubGeneModelPhenotypeMatch(Organism.FISH, modelScore);

        List<GeneModelPhenotypeMatch> models = Arrays.asList(geneModel);
        instance = new HiPhivePriorityResult(geneId, geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, ppiScore, false);

        assertThat(instance.getFishScore(), equalTo(modelScore));
    }

    @Test
    public void testGetWalkerScore() {
        assertThat(instance.getPpiScore(), equalTo(ppiScore));
    }
    
    @Test
    public void testIsCandidateGeneMatch_MatchesConstructorArg() {
        assertThat(instance.isCandidateGeneMatch(), is(matchesCandidateGene));
    }

    @Test
    public void testToString() {
        System.out.println(instance.toString());
    }
}
