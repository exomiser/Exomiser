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
package de.charite.compbio.exomiser.core.model;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneModelTest {
    
    private GeneModel instance;
    private final Organism organism = Organism.MOUSE;
    private final double score = 1.0d;
    private final int entrezGeneId = 12345;
    private final String humanGeneSymbol = "GENE1";
    private final String modelId = "gene1_model1";
    private final String modelGeneId = "MGI:12345";
    private final String modelGeneSymbol = "Gene1";
    private final List<String> modelPhenotypeIds = new ArrayList<>(Arrays.asList("HP:0000000", "HP:0000001"));
    
    @Before
    public void setUp() {
        //TODO: want it to work more like this I think - although a HUMAN modelOrganism would indicate a DiseaseModel
//        humanGeneId = new GeneIdentifier(modelGeneId, modelGeneSymbol);
//        modelGeneId = new GeneIdentifier(modelGeneId, modelGeneSymbol);
//        diseaseId = new DiseaseIdentifier(diseaseId, diseaseTerm);
//        modelOrganism = Organism.HUMAN
//        instance = new GeneModel(modelOrganism, humanGeneId, modelGeneId, modelPhenotypeIds);
//        instance = new DiseaseModel(modelOrganism, humanGeneId, diseaseId, modelPhenotypeIds);
        instance = new GeneModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
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
    public void testGetModelGeneSymbol() {
        assertThat(instance.getModelGeneSymbol(), equalTo(modelGeneSymbol));
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
        GeneModel other = new GeneModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        GeneModel other = new GeneModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEqualsGeneId() {
        GeneModel other = new GeneModel(modelId, organism, 54321, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsGeneSymbol() {
        GeneModel other = new GeneModel(modelId, organism, entrezGeneId, "GENE2", modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsModelId() {
        GeneModel other = new GeneModel("different_model", organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsPhenotypes() {
        GeneModel other = new GeneModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, null);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsOrganism() {
        GeneModel other = new GeneModel(modelId, Organism.HUMAN, entrezGeneId, humanGeneSymbol, modelId, modelGeneSymbol, null);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testToString() {
        instance.setScore(score);
        assertThat(instance.toString(), not(startsWith("de.charite.compbio")));
    }
    
}
