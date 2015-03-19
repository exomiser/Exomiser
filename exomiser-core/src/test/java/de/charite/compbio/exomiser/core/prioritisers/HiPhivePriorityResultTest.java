/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.model.Organism;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

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
    
    @Before
    public void setUp() {
        queryPhenotypeTerms = new ArrayList<>();
        phenotypeEvidence = new ArrayList<>();
        ppiEvidence = new ArrayList<>();
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, phenotypeEvidence, ppiEvidence, walkerScore);
    }

    private GeneModel makeStubGeneModelForOrganismWithScore(Organism organism, double score) {
        GeneModel model = new GeneModel("gene1_model1", organism, 12345, geneSymbol, "MGI:12345", "gene1", null);
        model.setScore(score);
        return model;
    }
        
    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.HI_PHIVE_PRIORITY));
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
    public void testSetScore() {
        double newScore = 1.0d;
        instance.setScore(newScore);
        assertThat(instance.getScore(), equalTo((float) newScore));
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
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, walkerScore);

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
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, walkerScore);

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
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms, models, ppiEvidence, walkerScore);

        assertThat(instance.getFishScore(), equalTo((float) modelScore));
    }

    @Test
    public void testGetWalkerScore() {
        assertThat(instance.getWalkerScore(), equalTo((float) walkerScore));
    }
    
}
