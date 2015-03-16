/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneModelTest {
    
    private GeneModel instance;
    private double score = 1.0d;
    private final int entrezGeneId = 12345;
    private final String humanGeneSymbol = "GENE1";
    private final String modelId = "model1";
    private final List<String> phenotypeIds = new ArrayList<>(Arrays.asList("HP:0000000", "HP:0000001"));
    
    @Before
    public void setUp() {
        instance = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, phenotypeIds);
    }

    @Test
    public void testScoreIsZeroByDefault() {
        assertThat(instance.getScore(), equalTo(0d));
    } 
    
    @Test
    public void testCanSetScore() {
        instance.setScore(score);
        assertThat(instance.getScore(), equalTo(score));
    }
    
    @Test
    public void testGetEntrezGeneId() {
        assertThat(instance.getEntrezGeneId(), equalTo(entrezGeneId));
    }

    @Test
    public void testGetHumanGeneSymbol() {
        assertThat(instance.getHumanGeneSymbol(), equalTo(humanGeneSymbol));
    }

    @Test
    public void testGetModelId() {
        assertThat(instance.getModelId(), equalTo(modelId));
    }

    @Test
    public void testGetPhenotypeIds() {
        assertThat(instance.getPhenotypeIds(), equalTo(phenotypeIds));
    }

    @Test
    public void testHashCode() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, phenotypeIds);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, phenotypeIds);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEqualsGeneId() {
        GeneModel other = new GeneModel(54321, humanGeneSymbol, modelId, phenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsGeneSymbol() {
        GeneModel other = new GeneModel(entrezGeneId, "GENE2", modelId, phenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsModelId() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, "wibble", phenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsPhenotypes() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, null);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testToString() {
        instance.setScore(score);
        assertThat(instance.toString(), equalTo("GeneModel{score=" + score + ", entrezGeneId=" + entrezGeneId + ", humanGeneSymbol=" + humanGeneSymbol + ", modelId=" + modelId + ", phenotypeIds=" + phenotypeIds + '}'));
    }
    
}
