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
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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
    private final String geneSymbol = "FGFR2";
    private final double score = 0.87d;
    private List<PhenotypeTerm> queryPhenotypeTerms;
    private List<Model> phenotypeEvidence;
    private List<Model> ppiEvidence;
    private final double walkerScore = 0.6d;
    private final boolean matchesCandidateGene = true;
    
    @Before
    public void setUp() {
        queryPhenotypeTerms = new ArrayList<>();
        phenotypeEvidence = new ArrayList<>();
        ppiEvidence = new ArrayList<>();
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, phenotypeEvidence, ppiEvidence, walkerScore, matchesCandidateGene);
    }

    private GeneModel makeStubGeneModelForOrganismWithScore(Organism organism, double score) {
        GeneModel model = new GeneModel("gene1_model1", organism, 12345, geneSymbol, "MGI:12345", "gene1", null);
        model.setScore(score);
        return model;
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
        assertThat(instance.getScore(), equalTo((float) score));
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
        assertThat(instance.getHumanScore(), equalTo(0f));
    }
    
    @Test
    public void testGetHumanScoreMatchesModelScore() {
        double modelScore = 1f;
        GeneModel geneModel = makeStubGeneModelForOrganismWithScore(Organism.HUMAN, modelScore);
                
        List<Model> models = new ArrayList<>();
        models.add(geneModel);
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, walkerScore, false);

        assertThat(instance.getHumanScore(), equalTo((float) modelScore));
    }

    @Test
    public void testGetMouseScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getMouseScore(), equalTo(0f));
    }

    @Test
    public void testGetMouseScoreMatchesModelScore() {
        double modelScore = 1f;
        GeneModel geneModel = makeStubGeneModelForOrganismWithScore(Organism.MOUSE, modelScore);
                
        List<Model> models = new ArrayList<>();
        models.add(geneModel);
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, walkerScore, false);

        assertThat(instance.getMouseScore(), equalTo((float) modelScore));
    }
    
    @Test
    public void testGetFishScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getFishScore(), equalTo(0f));
    }

    @Test
    public void testGetFishScoreMatchesModelScore() {
        double modelScore = 1f;
        GeneModel geneModel = makeStubGeneModelForOrganismWithScore(Organism.FISH, modelScore);
                
        List<Model> models = new ArrayList<>();
        models.add(geneModel);
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, walkerScore, false);

        assertThat(instance.getFishScore(), equalTo((float) modelScore));
    }

    @Test
    public void testGetWalkerScore() {
        assertThat(instance.getWalkerScore(), equalTo((float) walkerScore));
    }
    
    @Test
    public void testIsCandidateGeneMatch_MatchesConstructorArg() {
        assertThat(instance.isCandidateGeneMatch(), is(matchesCandidateGene));
    }
}
