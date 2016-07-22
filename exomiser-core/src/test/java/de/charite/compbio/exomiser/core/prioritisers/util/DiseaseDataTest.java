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

package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.model.InheritanceMode;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DiseaseDataTest {

    @Test
    public void testDiseaseData() {
        //2263	FGFR2	OMIM:101600	Craniofacial-skeletal-dermatologic dysplasia	D	D	HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304
        DiseaseData instance = DiseaseData.builder()
                .diseaseId("OMIM:101600")
                .diseaseName("Craniofacial-skeletal-dermatologic dysplasia")
                .associatedGeneId(2263)
                .associatedGeneSymbol("FGFR2")
                .diseaseType(DiseaseData.DiseaseType.DISEASE)
                .inheritanceMode(InheritanceMode.DOMINANT)
                .phenotypeIds(Arrays.asList("HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304".split(",")))
                .build();
        System.out.println(instance);

        DiseaseData other = DiseaseData.builder()
                .diseaseId("OMIM:101600")
                .diseaseName("Craniofacial-skeletal-dermatologic dysplasia")
                .associatedGeneId(2263)
                .associatedGeneSymbol("FGFR2")
                .diseaseType(DiseaseData.DiseaseType.DISEASE)
                .inheritanceMode(InheritanceMode.DOMINANT)
                .phenotypeIds(Arrays.asList("HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304".split(",")))
                .build();
        System.out.println(other);

        assertThat(instance, equalTo(other));
    }

    @Test
    public void testInheritanceCode() {
        assertThat(DiseaseData.builder()
                .inheritanceModeCode("D")
                .build().getInheritanceMode(), equalTo(InheritanceMode.DOMINANT));
    }

    @Test
    public void testDiseaseCode() {
        assertThat(DiseaseData.builder()
                .diseaseTypeCode("D")
                .build().getDiseaseType(), equalTo(DiseaseData.DiseaseType.DISEASE));

        assertThat(DiseaseData.builder()
                .diseaseTypeCode("N")
                .build().getDiseaseType(), equalTo(DiseaseData.DiseaseType.NON_DISEASE));

        assertThat(DiseaseData.builder()
                .diseaseTypeCode("S")
                .build().getDiseaseType(), equalTo(DiseaseData.DiseaseType.SUSCEPTIBILITY));

        assertThat(DiseaseData.builder()
                .diseaseTypeCode("C")
                .build().getDiseaseType(), equalTo(DiseaseData.DiseaseType.CNV));

        assertThat(DiseaseData.builder()
                .diseaseTypeCode("U")
                .build().getDiseaseType(), equalTo(DiseaseData.DiseaseType.UNCONFIRMED));
    }
}