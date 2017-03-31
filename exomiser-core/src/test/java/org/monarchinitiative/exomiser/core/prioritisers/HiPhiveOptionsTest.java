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

import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions.InvalidRunParameterException;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;

import java.util.Collections;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhiveOptionsTest {

    private final GeneModel model = new GeneDiseaseModel("OMIM:101600", Organism.HUMAN, 12345, "Gene1", "DISEASE:1", "Test disease", Collections.emptyList());

    private void assertAllRunParamsAreTrue(HiPhiveOptions hiPhiveOptions) {
        assertThat(hiPhiveOptions.runHuman(), is(true));
        assertThat(hiPhiveOptions.runMouse(), is(true));
        assertThat(hiPhiveOptions.runFish(), is(true));
        assertThat(hiPhiveOptions.runPpi(), is(true));
    }

    @Test
    public void testReturnsFalseWithDefaultConstructor() {
        HiPhiveOptions instance = HiPhiveOptions.builder().build();
        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void testDefault() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        assertDefaultOptions(instance);
    }

    @Test
    public void testBuilderDefaults() {
        HiPhiveOptions instance = HiPhiveOptions.builder().build();
        assertDefaultOptions(instance);
    }

    private void assertDefaultOptions(HiPhiveOptions instance) {
        assertThat(instance.isBenchmarkingEnabled(), is(false));
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(true));
    }

    @Test
    public void testAllRunParametersAreTrueUsingParameterisedConstructorAndEmptyRunParameters() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("").build();
        assertAllRunParamsAreTrue(instance);
    }

    @Test(expected = InvalidRunParameterException.class)
    public void testThrowsInvalidRunParameterExceptionWhenEncountersUnrecognisedRunParameter() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("floorb").build();
    }

    @Test
    public void testSetsRunHumanTrueAllOthersFalseWhenOnlyHumanSpecifiedInParameters() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("human").build();
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(false));
        assertThat(instance.runFish(), is(false));
        assertThat(instance.runPpi(), is(false));
    }

    @Test
    public void testSetsRunHumanMouseTrueAllOthersFalseWhenOnlyHumanMouseSpecifiedInParameters() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("human,mouse").build();
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(false));
        assertThat(instance.runPpi(), is(false));
    }

    @Test
    public void testSetsRunHumanMouseFishTrueRunPpiFalseWhenOnlyHumanMouseFishSpecifiedInParameters() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("human,mouse,fish").build();
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(false));
    }

    @Test
    public void testSetsRunHumanMouseFishPpiTrueHumanMouseFishPpiSpecifiedInParameters() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("human,mouse,fish,ppi").build();
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(true));
    }

    @Test
    public void testWillRemoveWhitespaceFromRunParams() {
        HiPhiveOptions instance = HiPhiveOptions.builder().runParams("human,mouse,fish , ppi").build();
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(true));
    }

    @Test
    public void testBuilderRunParams() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .runParams("human,mouse,fish,ppi")
                .build();
        assertThat(instance.isBenchmarkingEnabled(), is(false));
        assertThat(instance.runHuman(), is(true));
        assertThat(instance.runMouse(), is(true));
        assertThat(instance.runFish(), is(true));
        assertThat(instance.runPpi(), is(true));
    }


    @Test
    public void testGetDiseaseId() {
        String diseaseId = "OMIM:101600";
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .diseaseId(diseaseId)
                .build();
        assertThat(instance.getDiseaseId(), equalTo(diseaseId));
    }

    @Test
    public void testGetCandidateGeneSymbol() {
        String candidateGeneSymbol = "Gene1";
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .build();
        assertThat(instance.getCandidateGeneSymbol(), equalTo(candidateGeneSymbol));
    }

    @Test
    public void returnsTrueWhenDiseaseIdMatchesModelIdAndGeneSymbolMatchesModelHumanGeneSymbolForDiseaseModel() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();
        assertThat(instance.isBenchmarkHit(model), is(true));
    }

    @Test
    public void returnsTrueWhenDiseaseIdMatchesModelIdAndGeneSymbolMatchesModelHumanGeneSymbolForGeneModel() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";

        GeneModel model = new GeneOrthologModel(diseaseId, Organism.HUMAN, 12345, candidateGeneSymbol, "DISEASE:1", "Test disease", Collections.emptyList());

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(true));
    }

    @Test
    public void returnsFalseWhenDiseaseIdAndGeneSymbolAreEmpty() {
        String diseaseId = "";
        String candidateGeneSymbol = "";

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void isBenchmarkingEnabledReturnsFalseWhenDiseaseIdOrGeneSymbolAreEmpty() {
        HiPhiveOptions emptyGeneSymbol = HiPhiveOptions.builder()
                .candidateGeneSymbol("")
                .diseaseId("disease")
                .build();
        assertThat(emptyGeneSymbol.isBenchmarkingEnabled(), is(false));

        HiPhiveOptions emptyDiseaseId = HiPhiveOptions.builder()
                .candidateGeneSymbol("candidateGeneSymbol")
                .diseaseId("")
                .build();
        assertThat(emptyDiseaseId.isBenchmarkingEnabled(), is(false));
    }

    @Test
    public void returnsFalseWhenDiseaseIdAndGeneSymbolAreNull() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(null)
                .diseaseId(null)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenDiseaseIdMatchesModelIdAndGeneSymbolIsEmpty() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "";

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenDiseaseIdMatchesModelIdAndGeneSymbolIsNull() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = null;

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenGeneSymbolMatchesModelHumanGeneSymbolAndDiseaseIdIsNull() {
        String diseaseId = null;
        String candidateGeneSymbol = "Gene1";

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenGeneSymbolMatchesModelHumanGeneSymbolAndDiseaseIdIsEmpty() {
        String diseaseId = "";
        String candidateGeneSymbol = "Gene1";

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenModelIdIsNull() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";
        GeneModel model = new GeneDiseaseModel(null, Organism.HUMAN, 12345, candidateGeneSymbol, diseaseId, "Test disease", Collections.emptyList());

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void returnsFalseWhenHumanGeneSymbolIsNull() {
        String diseaseId = "OMIM:101600";
        String candidateGeneSymbol = "Gene1";

        GeneModel model = new GeneDiseaseModel(diseaseId, Organism.HUMAN, 12345, null, diseaseId, "Test disease", Collections.emptyList());

        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol(candidateGeneSymbol)
                .diseaseId(diseaseId)
                .build();

        assertThat(instance.isBenchmarkHit(model), is(false));
    }

    @Test
    public void testHashCode() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        HiPhiveOptions other = HiPhiveOptions.DEFAULT;
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testHashCodeNotEquals() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        HiPhiveOptions other = HiPhiveOptions.builder()
                .candidateGeneSymbol("geneSymbol")
                .diseaseId("disease")
                .build();
        assertThat(instance.hashCode(), not(equalTo(other.hashCode())));
    }

    @Test
    public void testEquals() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        HiPhiveOptions other = HiPhiveOptions.DEFAULT;
        assertThat(instance.equals(other), is(true));
    }

    @Test
    public void testNotEquals() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        HiPhiveOptions other = HiPhiveOptions.builder()
                .candidateGeneSymbol("geneSymbol")
                .diseaseId("disease")
                .build();
        assertThat(instance.equals(other), is(false));
    }

    @Test
     public void testToStringDefaultConstructor() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        String defaultString  = "HiPhiveOptions{diseaseId='', candidateGeneSymbol='', benchmarkingEnabled=false, runPpi=true, runHuman=true, runMouse=true, runFish=true}";
        assertThat(instance.toString(), equalTo(defaultString));
    }

    @Test
    public void testToStringNonDefaultRunParams() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .candidateGeneSymbol("geneSymbol")
                .diseaseId("diseaseId")
                .runParams("human,mouse")
                .build();
        String defaultString  = "HiPhiveOptions{diseaseId='diseaseId', candidateGeneSymbol='geneSymbol', benchmarkingEnabled=true, runPpi=false, runHuman=true, runMouse=true, runFish=false}";
        assertThat(instance.toString(), equalTo(defaultString));
    }

    @Test
    public void testGetOrganismsToRun_AllOrganisms() {
        HiPhiveOptions instance = HiPhiveOptions.DEFAULT;
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.HUMAN, Organism.MOUSE, Organism.FISH)));
    }

    @Test
    public void testGetOrganismsToRun_NoOrganisms() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .runParams("ppi")
                .build();
        assertThat(instance.getOrganismsToRun(), equalTo(Collections.emptySet()));
    }

    @Test
    public void testGetOrganismsToRun_NothingDefinedMeansAll() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .runParams("")
                .build();
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.HUMAN, Organism.MOUSE, Organism.FISH)));
    }

    @Test
    public void testGetOrganismsToRun_HumanMouseOnly() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .runParams("human,mouse")
                .build();
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.HUMAN, Organism.MOUSE)));
    }

    @Test
    public void testGetOrganismsToRun_FishOnly() {
        HiPhiveOptions instance = HiPhiveOptions.builder()
                .runParams("fish")
                .build();
        assertThat(instance.getOrganismsToRun(), equalTo(EnumSet.of(Organism.FISH)));
    }
}
