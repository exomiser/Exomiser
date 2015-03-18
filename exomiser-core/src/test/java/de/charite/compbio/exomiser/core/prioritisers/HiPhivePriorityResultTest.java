/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.GeneModel;
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
    private Map<Organism, GeneModel> phenotypeEvidence;
    private Map<Organism, GeneModel> ppiEvidence;
    private final double walkerScore = 0.6d;
    
    @Before
    public void setUp() {
        queryPhenotypeTerms = new ArrayList<>();
        phenotypeEvidence = new HashMap<>();
        ppiEvidence = new HashMap<>();
        instance = new HiPhivePriorityResult(geneSymbol, score, queryPhenotypeTerms,phenotypeEvidence, ppiEvidence, walkerScore);
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
    public void testGetMouseScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getMouseScore(), equalTo(0f));
    }

    @Test
    public void testGetFishScoreIsZeroWithNoDiseaseEvidence() {
        assertThat(instance.getFishScore(), equalTo(0f));
    }

    @Test
    public void testGetWalkerScore() {
        assertThat(instance.getWalkerScore(), equalTo((float) walkerScore));
    }
    
}
