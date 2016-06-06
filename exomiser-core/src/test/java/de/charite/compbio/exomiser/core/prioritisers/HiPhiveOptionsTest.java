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

import de.charite.compbio.exomiser.core.model.DiseaseModel;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.prioritisers.HiPhiveOptions.InvalidRunParameterException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptionsTest {


    private HiPhiveOptions instance;
    private Model model;

    @Before
    public void setUp() {
        instance = new HiPhiveOptions();
        model = new DiseaseModel("OMIM:101600", Organism.HUMAN, 12345, "Gene1", "DISEASE:1", "Test disease", new ArrayList<String>());
    }

    private void assertAllRunParamsAreTrue(HiPhiveOptions hiPhiveOptions) {
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(true));
    }

    @Test
    public void testReturnsFalseWithDefaultConstructor() {
        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void testIsNotBenchmarkingEnabledDefaultConstructor() {
        assertThat(instance.isBenchmarkingEnabled(), is(false));
    }

    @Test
    public void testIsBenchmarkingEnabledUsingParameterisedConstructor() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol");
        assertThat(instance.isBenchmarkingEnabled(), is(true));
    }

    @Test
    public void testAllRunParametersAreTrueByDefault() {
        assertAllRunParamsAreTrue(instance);
    }

    @Test
    public void testAllRunParametersAreTrueUsingParameterisedConstructorWithoutRunParameters() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol");
        assertAllRunParamsAreTrue(instance);
    }

    @Test
    public void testAllRunParametersAreTrueUsingParameterisedConstructorAndEmptyRunParameters() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "");
        assertAllRunParamsAreTrue(instance);
    }

    @Test(expected = InvalidRunParameterException.class)
    public void testThrowsInvalidRunParameterExceptionWhenEncountersUnrecognisedRunParameter() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "floorb");
    }

    @Test
    public void testSetsRunHumanTrueAllOthersFalseWhenOnlyHumanSpecifiedInParameters() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human");
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(false));
        assertThat(instance.runFish(), is(false));
        assertThat(instance.runPpi(), is(false));
    }

    @Test
    public void testSetsRunHumanMouseTrueAllOthersFalseWhenOnlyHumanMouseSpecifiedInParameters() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human,mouse");
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(false));
        assertThat(instance.runPpi(), is(false));
    }

    @Test
    public void testSetsRunHumanMouseFishTrueRunPpiFalseWhenOnlyHumanMouseFishSpecifiedInParameters() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human,mouse,fish");
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(false));
    }

    @Test
    public void testSetsRunHumanMouseFishPpiTrueHumanMouseFishPpiSpecifiedInParameters() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human,mouse,fish,ppi");
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(true));
    }

    @Test
    public void testGetDiseaseId() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.getDiseaseId(), equalTo(diseaseId));
    }

    @Test
    public void testGetCandidateGeneSymbol() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.getCandidateGeneSymbol(), equalTo(candidateGeneSymbol));
    }

    @Test
    public void returnsTrueWhenDiseaseIdMatchesModelIdAndGeneSymbolMatchesModelHumanGeneSymbolForDiseaseModel() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.isBenchmarkHit(model), is(true));
    }

    @Test
    public void returnsTrueWhenDiseaseIdMatchesModelIdAndGeneSymbolMatchesModelHumanGeneSymbolForGeneModel() {
        Model model = new GeneModel("OMIM:101600", Organism.HUMAN, 12345, "Gene1", "DISEASE:1", "Test disease", new ArrayList<String>());

        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.isBenchmarkHit(model), is(true));
    }

    @Test
    public void returnsFalseWhenDiseaseIdAndGeneSymbolAreEmpty() {
        String diseaseId = "";
        String candidateGeneSymbol = "";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void isBenchmarkingEnabledreturnsFalseWhenDiseaseIdOrGeneSymbolAreEmpty() {
        HiPhiveOptions emptyGeneSymbol = new HiPhiveOptions("disease", "");
        assertThat(emptyGeneSymbol.isBenchmarkingEnabled(), is(false));

        HiPhiveOptions emptyDiseaseId = new HiPhiveOptions("", "candidateGeneSymbol");
        assertThat(emptyDiseaseId.isBenchmarkingEnabled(), is(false));
    }

    @Test
    public void returnsFalseWhenDiseaseIdAndGeneSymbolAreNull() {
        String diseaseId = null;
        String candidateGeneSymbol = null;
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenDiseaseIdMatchesModelIdAndGeneSymbolIsEmpty() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenDiseaseIdMatchesModelIdAndGeneSymbolIsNull() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = null;
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenGeneSymbolMatchesModelHumanGeneSymbolAndDiseaseIdIsNull() {
        String diseaseId = null;
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenGeneSymbolMatchesModelHumanGeneSymbolAndDiseaseIdIsEmpty() {
        String diseaseId = "";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenModelIdIsNull() {
        Model model = new DiseaseModel(null, Organism.HUMAN, 12345, "Gene1", "DISEASE:1", "Test disease", new ArrayList<String>());

        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenHumanGeneSymbolIsNull() {
        Model model = new DiseaseModel("OMIM:101600", Organism.HUMAN, 12345, null, "DISEASE:1", "Test disease", new ArrayList<String>());

        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        instance = new HiPhiveOptions(diseaseId, candidateGeneSymbol);
        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void testHashCode() {
        HiPhiveOptions other = new HiPhiveOptions();
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testHashCodeNotEquals() {
        HiPhiveOptions other = new HiPhiveOptions("disease", "geneSymbol");
        assertThat(instance.hashCode(), not(equalTo(other.hashCode())));
    }

    @Test
    public void testEquals() {
        HiPhiveOptions other = new HiPhiveOptions();
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEquals() {
        HiPhiveOptions other = new HiPhiveOptions("disease", "geneSymbol");
        assertThat(instance.equals(other), is(false));
    }

    @Test
     public void testToStringDefaultConstructor() {
        String defaultString  = "HiPhiveOptions{diseaseId='', candidateGeneSymbol='', benchmarkingEnabled=false, runPpi=true, runHuman=true, runMouse=true, runFish=true}";
        assertThat(instance.toString(), equalTo(defaultString));
    }

    @Test
    public void testToStringParameterisedConstructor() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human,mouse");
        String defaultString  = "HiPhiveOptions{diseaseId='diseaseId', candidateGeneSymbol='geneSymbol', benchmarkingEnabled=true, runPpi=false, runHuman=true, runMouse=true, runFish=false}";
        assertThat(instance.toString(), equalTo(defaultString));
    }

    @Test
    public void testGetOrganismsToRun_AllOrganisms() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human,mouse,fish");
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.HUMAN, Organism.MOUSE, Organism.FISH)));
    }

    @Test
    public void testGetOrganismsToRun_NoOrganisms() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "ppi");
        assertThat(instance.getOrganismsToRun(), equalTo(Collections.emptySet()));
    }

    @Test
    public void testGetOrganismsToRun_NothingDefinedMeansAll() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "");
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.HUMAN, Organism.MOUSE, Organism.FISH)));
    }

    @Test
    public void testGetOrganismsToRun_HumanMouseOnly() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "human,mouse");
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.HUMAN, Organism.MOUSE)));
    }

    @Test
    public void testGetOrganismsToRun_FishOnly() {
        instance = new HiPhiveOptions("diseaseId", "geneSymbol", "fish");
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.FISH)));
    }
}
