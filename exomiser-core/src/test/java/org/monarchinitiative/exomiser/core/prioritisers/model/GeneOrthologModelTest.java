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
package org.monarchinitiative.exomiser.core.prioritisers.model;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneOrthologModelTest {
    
    private GeneOrthologModel instance;
    private final Organism organism = Organism.MOUSE;
    private final int entrezGeneId = 12345;
    private final String humanGeneSymbol = "GENE1";
    private final String modelId = "gene1_model1";
    private final String modelGeneId = "MGI:12345";
    private final String modelGeneSymbol = "Gene1";
    private final List<String> modelPhenotypeIds = new ArrayList<>(Arrays.asList("HP:0000000", "HP:0000001"));
    
    @Before
    public void setUp() {
        //TODO: want it to work more like this I think - although a HUMAN modelOrganism would indicate a GeneDiseaseModel
//        humanGeneId = new HumanGeneIdentifier(humanGeneId, humanGeneSymbol);
//        modelGeneId = new HumanGeneIdentifier(modelGeneId, modelGeneSymbol);
//        diseaseId = new DiseaseIdentifier(diseaseId, diseaseTerm);
//        modelOrganism = Organism.HUMAN
//        instance = new GeneOrthologModel(modelOrganism, humanGeneId, modelGeneId, modelPhenotypeIds);
//        instance = new GeneDiseaseModel(modelOrganism, humanGeneId, diseaseId, modelPhenotypeIds);
        instance = new GeneOrthologModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
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
        assertThat(instance.getId(), equalTo(modelId));
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
    public void testHashCode() {
        GeneOrthologModel other = new GeneOrthologModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        GeneOrthologModel other = new GeneOrthologModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEqualsGeneId() {
        GeneOrthologModel other = new GeneOrthologModel(modelId, organism, 54321, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsGeneSymbol() {
        GeneOrthologModel other = new GeneOrthologModel(modelId, organism, entrezGeneId, "GENE2", modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsModelId() {
        GeneOrthologModel other = new GeneOrthologModel("different_model", organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, modelPhenotypeIds);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsPhenotypes() {
        GeneOrthologModel other = new GeneOrthologModel(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, null);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testNotEqualsOrganism() {
        GeneOrthologModel other = new GeneOrthologModel(modelId, Organism.HUMAN, entrezGeneId, humanGeneSymbol, modelId, modelGeneSymbol, null);
        assertThat(instance.equals(other), is(false));
    }
    
    @Test
    public void testToString() {
        assertThat(instance.toString(), not(startsWith("de.charite.compbio")));
    }
    
}
