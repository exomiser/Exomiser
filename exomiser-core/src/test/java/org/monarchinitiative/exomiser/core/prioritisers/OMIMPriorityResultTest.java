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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OMIMPriorityResultTest {

    private OMIMPriorityResult instance;

    @Test
    public void testType() {
        instance = new OMIMPriorityResult(1234, "GENE1", 0, Collections.emptyList());
        assertThat(instance.getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    @Test
    public void testGetScore() {
        instance = new OMIMPriorityResult(1234, "GENE1", 1d, Collections.emptyList());
        assertThat(instance.getScore(), equalTo(1d));
    }

    @Test
    public void testAssociatedDiseases() {
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("OMIM disease name").diseaseType(Disease.DiseaseType.DISEASE).inheritanceModeCode("D").build();
        ArrayList<Disease> diseases = Lists.newArrayList(disease);
        instance = new OMIMPriorityResult(1234, "GENE1", 1d, diseases);
        assertThat(instance.getAssociatedDiseases(), equalTo(diseases));
    }

    @Test
    public void testToHtml_noDiseases() {
        instance = new OMIMPriorityResult(1234, "GENE1", 1d, Collections.emptyList());
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtml_OmimDiseases() {
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("OMIM disease name").diseaseType(Disease.DiseaseType.DISEASE).inheritanceModeCode("D").build();
        Disease nonDisease = Disease.builder().diseaseId("OMIM:54321").diseaseName("OMIM non-disease name").diseaseType(Disease.DiseaseType.NON_DISEASE).inheritanceModeCode("U").build();
        instance = new OMIMPriorityResult(1234, "GENE1", 1d, Lists.newArrayList(disease, nonDisease));
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtml_IncompatibleOmimDiseases() {
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("Incompatible OMIM disease name").build();
        instance = new OMIMPriorityResult(1234, "GENE1", 0.5d, Lists.newArrayList(disease));
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtml_IncompatibleOrphanetDiseases() {
        Disease disease = Disease.builder().diseaseId("ORPHANET:12345").diseaseName("Incompatible Orphanet disease name").build();
        instance = new OMIMPriorityResult(1234, "GENE1", 0.5d, Lists.newArrayList(disease));
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtml_UnknownDiseaseId() {
        Disease disease = Disease.builder().diseaseId("WIBBLE:12345").diseaseName("Unknown diseaseId name").build();
        instance = new OMIMPriorityResult(1234, "GENE1", 1d, Lists.newArrayList(disease));
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testOrdering() {
        Disease disease = Disease.builder().diseaseId("WIBBLE:12345").diseaseName("Unknown diseaseId name").build();
        PriorityResult one = new OMIMPriorityResult(1111, "BEST", 1d, Lists.newArrayList(disease));
        PriorityResult two = new OMIMPriorityResult(22222, "MIDDLE_A", 0.5d, Lists.newArrayList(disease));
        PriorityResult three = new OMIMPriorityResult(33333, "MIDDLE_B", 0.5d, Lists.newArrayList(disease));
        PriorityResult four = new OMIMPriorityResult(44444, "WORST", 0.1d, Lists.newArrayList(disease));

        List<PriorityResult> actual = Arrays.asList(two, four, three, one);
        Collections.sort(actual);

        assertThat(actual, equalTo(Arrays.asList(one, two, three, four)));
    }
}