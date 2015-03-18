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
    private final double score = 1.0d;
    private final int entrezGeneId = 12345;
    private final String humanGeneSymbol = "GENE1";
    private final String modelId = "model1";
    private final String modelSymbol = "Gene1";
    private final List<String> modelPhenotypeIds = new ArrayList<>(Arrays.asList("HP:0000000", "HP:0000001"));
    
    @Before
    public void setUp() {
        //TODO: want it to work more like this I think - although a HUMAN modelOrganism would indicate a DiseaseModel
//        humanGeneId = new GeneIdentifier(modelSymbol, modelId);
//        modelGeneId = new GeneIdentifier(modelSymbol, modelId);
//        modelOrganism = Organism.HUMAN
//        instance = new GeneModel(modelOrganism, humanGeneId, modelGeneId, modelSpecies, modelPhenotypeIds);
        instance = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, modelSymbol, modelPhenotypeIds);
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
    public void testGetModelSymbol() {
        assertThat(instance.getModelSymbol(), equalTo(modelSymbol));
    }
    
    @Test
    public void testGetPhenotypeIds() {
        assertThat(instance.getPhenotypeIds(), equalTo(modelPhenotypeIds));
    }

    @Test
    public void testBestPhenotypeMatchesIsEmptyByDefault() {
        assertThat(instance.getBestPhenotypeMatchForTerms().isEmpty(), is(true));
    }
    
    @Test
    public void testaddMatchIfWhenAbsent() {
        PhenotypeMatch match = new PhenotypeMatch(null, null, score, 1.0, null);
        instance.addMatchIfAbsentOrBetterThanCurrent(match);
        assertThat(instance.getBestPhenotypeMatchForTerms().containsValue(match), is(true));
    }
    
    @Test
    public void testaddMatchWhenBetterThanCurrent() {
        PhenotypeMatch match = new PhenotypeMatch(null, null, score, 0.5, null);
        instance.addMatchIfAbsentOrBetterThanCurrent(match);
        
        PhenotypeMatch betterMatch = new PhenotypeMatch(null, null, score, 1.0, null);
        instance.addMatchIfAbsentOrBetterThanCurrent(betterMatch);
        
        assertThat(instance.getBestPhenotypeMatchForTerms().containsValue(match), is(false));
        assertThat(instance.getBestPhenotypeMatchForTerms().containsValue(betterMatch), is(true));
    }
    
    
    @Test
    public void testaddMatchWhenNotBetterThanCurrent() {
        PhenotypeMatch betterMatch = new PhenotypeMatch(null, null, score, 1.0, null);
        instance.addMatchIfAbsentOrBetterThanCurrent(betterMatch);
        
        PhenotypeMatch match = new PhenotypeMatch(null, null, score, 0.5, null);
        instance.addMatchIfAbsentOrBetterThanCurrent(match);
        
        assertThat(instance.getBestPhenotypeMatchForTerms().containsValue(match), is(false));
        assertThat(instance.getBestPhenotypeMatchForTerms().containsValue(betterMatch), is(true));
    }
    
    @Test
    public void testHashCode() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, modelSymbol, modelPhenotypeIds);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, modelSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEqualsGeneId() {
        GeneModel other = new GeneModel(54321, humanGeneSymbol, modelId, modelSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsGeneSymbol() {
        GeneModel other = new GeneModel(entrezGeneId, "GENE2", modelId, modelSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsModelId() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, "wibble", modelSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsPhenotypes() {
        GeneModel other = new GeneModel(entrezGeneId, humanGeneSymbol, modelId, modelSymbol, null);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testToString() {
        instance.setScore(score);
        assertThat(instance.toString(), equalTo("GeneModel{score=" + score + ", entrezGeneId=" + entrezGeneId + ", humanGeneSymbol=" + humanGeneSymbol + ", modelId=" + modelId + ", modelSymbol=" + modelSymbol + ", phenotypeIds=" + modelPhenotypeIds + '}'));
    }
    
}
