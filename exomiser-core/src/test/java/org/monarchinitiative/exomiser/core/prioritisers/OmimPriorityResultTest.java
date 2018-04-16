/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OmimPriorityResultTest {

    @Test
    public void testType() {
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 0, Collections.emptyList(), Collections.emptyMap());
        assertThat(instance.getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    @Test
    public void testGetScore() {
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 1d, Collections.emptyList(), Collections.emptyMap());
        assertThat(instance.getScore(), equalTo(1d));
    }

    @Test
    public void testAssociatedDiseases() {
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("OMIM disease name").diseaseType(Disease.DiseaseType.DISEASE).inheritanceModeCode("D").build();
        List<Disease> diseases = Lists.newArrayList(disease);
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 1d, diseases, Collections.emptyMap());
        assertThat(instance.getAssociatedDiseases(), equalTo(diseases));
    }

    @Test
    public void testToHtmlNoDiseases() {
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 1d, Collections.emptyList(), Collections.emptyMap());
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtmlOmimDiseases() {
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("OMIM disease name").diseaseType(Disease.DiseaseType.DISEASE).inheritanceModeCode("D").build();
        Disease nonDisease = Disease.builder().diseaseId("OMIM:54321").diseaseName("OMIM non-disease name").diseaseType(Disease.DiseaseType.NON_DISEASE).inheritanceModeCode("U").build();
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 1d, Lists.newArrayList(disease, nonDisease), Collections.emptyMap());
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtmlIncompatibleOmimDiseases() {
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("Incompatible OMIM disease name").build();
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 0.5d, Lists.newArrayList(disease), Collections.emptyMap());
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtmlIncompatibleOrphanetDiseases() {
        Disease disease = Disease.builder().diseaseId("ORPHANET:12345").diseaseName("Incompatible Orphanet disease name").build();
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 0.5d, Lists.newArrayList(disease), Collections.emptyMap());
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testToHtmlUnknownDiseaseId() {
        Disease disease = Disease.builder().diseaseId("WIBBLE:12345").diseaseName("Unknown diseaseId name").build();
        OmimPriorityResult instance = new OmimPriorityResult(1234, "GENE1", 1d, Lists.newArrayList(disease), Collections.emptyMap());
        System.out.println(instance.getHTMLCode());
    }

    @Test
    public void testOrdering() {
        Disease disease = Disease.builder().diseaseId("WIBBLE:12345").diseaseName("Unknown diseaseId name").build();
        PriorityResult one = new OmimPriorityResult(1111, "BEST", 1d, Lists.newArrayList(disease), Collections.emptyMap());
        PriorityResult two = new OmimPriorityResult(22222, "MIDDLE_A", 0.5d, Lists.newArrayList(disease), Collections.emptyMap());
        PriorityResult three = new OmimPriorityResult(33333, "MIDDLE_B", 0.5d, Lists.newArrayList(disease), Collections.emptyMap());
        PriorityResult four = new OmimPriorityResult(44444, "WORST", 0.1d, Lists.newArrayList(disease), Collections.emptyMap());

        List<PriorityResult> actual = Arrays.asList(two, four, three, one);
        Collections.sort(actual);

        assertThat(actual, equalTo(Arrays.asList(one, two, three, four)));
    }
}