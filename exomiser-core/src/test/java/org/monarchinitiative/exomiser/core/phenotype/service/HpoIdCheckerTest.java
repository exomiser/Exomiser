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

package org.monarchinitiative.exomiser.core.phenotype.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class HpoIdCheckerTest {

    private static final PhenotypeTerm FOO = PhenotypeTerm.of("HP:0000315", "Abnormality of the orbital region");
    private static final PhenotypeTerm BAR = PhenotypeTerm.of("HP:0000316", "Hypertelorism");
    private static final PhenotypeTerm BAZ = PhenotypeTerm.of("HP:3000079", "Abnormality of mandibular symphysis");

    private final Map<String, PhenotypeTerm> termIdToTerms = new ImmutableMap.Builder<String, PhenotypeTerm>()
            .put("HP:0000315", FOO)
            .put("HP:0000284", FOO)

            .put("HP:0000316", BAR)
            .put("HP:0000578", BAR)
            .put("HP:0002001", BAR)
            .put("HP:0004657", BAR)
            .put("HP:0007871", BAR)

            .put("HP:3000079", BAZ)
            .build();

    private final HpoIdChecker instance = HpoIdChecker.of(termIdToTerms);

    @Test
    void throwsNullPointerFromConstructorWithNullInput() {
        assertThrows(NullPointerException.class, () -> HpoIdChecker.of(null));
    }

    @Test
    void throwsErrorWhenIdUnrecognised() {
        assertThrows(Exception.class, () -> instance.getCurrentTerm("Wibble"));
    }

    @Test
    void returnsSameIdWhenCurrent() {
        assertThat(instance.getCurrentTerm("HP:0000316"), equalTo(BAR));
        assertThat(instance.getCurrentId("HP:0000316"), equalTo("HP:0000316"));
    }

    @Test
    void testFindObsoleteTermOneAltId() {
        // [Term]
        // id: HP:0000284
        // name: obsolete Abnormality of the ocular region
        // is_obsolete: true
        // replaced_by: HP:0000315

        // [Term]
        // id: HP:0000315
        // name: Abnormality of the orbital region
        // alt_id: HP:0000284
        // synonym: "Abnormality of the eye region" EXACT layperson [ORCID:0000-0001-5889-4463]
        // synonym: "Abnormality of the region around the eyes" EXACT layperson [ORCID:0000-0001-5889-4463]
        // synonym: "Anomaly of the orbital region of the face" NARROW [ORCID:0000-0001-5889-4463]
        // synonym: "Deformity of the orbital region of the face" NARROW [ORCID:0000-0001-5889-4463]
        // synonym: "Malformation of the orbital region of the face" NARROW [ORCID:0000-0001-5889-4463]
        // xref: UMLS:C4025863
        // is_a: HP:0000271 ! Abnormality of the face

        assertThat(instance.getCurrentTerm("HP:0000284"), equalTo(FOO));
        assertThat(instance.getCurrentTerm("HP:0000315"), equalTo(FOO));
        assertThat(instance.getCurrentId("HP:0000315"), equalTo("HP:0000315"));
    }

    @Test
    void testFindObsoleteTermManyAltId() {
        // in this case the alt_terms don't exist in the ontology at all

        // [Term]
        // id: HP:0000316
        // name: Hypertelorism
        // alt_id: HP:0000578
        // alt_id: HP:0002001
        // alt_id: HP:0004657
        // alt_id: HP:0007871
        // def: "Interpupillary distance more than 2 SD above the mean (alternatively, the appearance of an increased interpupillary distance
        // or widely spaced eyes)." [pmid:19125427]
        // subset: hposlim_core
        // synonym: "Excessive orbital separation" EXACT [ORCID:0000-0001-5889-4463]
        // synonym: "Increased distance between eye sockets" EXACT [ORCID:0000-0001-5889-4463]
        // synonym: "Increased distance between eyes" EXACT [ORCID:0000-0001-5889-4463]
        // synonym: "Increased interpupillary distance" EXACT []
        // synonym: "Ocular hypertelorism" EXACT []
        // synonym: "Wide-set eyes" EXACT layperson []
        // synonym: "Widely spaced eyes" EXACT layperson []
        // synonym: "Widened interpupillary distance" EXACT []
        // xref: MSH:D006972
        // xref: SNOMEDCT_US:194021007
        // xref: SNOMEDCT_US:22006008
        // xref: UMLS:C0020534
        // is_a: HP:0100886 ! Abnormality of globe location
        assertThat(instance.getCurrentTerm("HP:0000316"), equalTo(BAR));

        assertThat(instance.getCurrentId("HP:0000578"), equalTo("HP:0000316"));
        assertThat(instance.getCurrentId("HP:0002001"), equalTo("HP:0000316"));
        assertThat(instance.getCurrentId("HP:0004657"), equalTo("HP:0000316"));
        assertThat(instance.getCurrentId("HP:0007871"), equalTo("HP:0000316"));

        assertThat(instance.getCurrentTerm("HP:0000578"), equalTo(BAR));
        assertThat(instance.getCurrentTerm("HP:0002001"), equalTo(BAR));
        assertThat(instance.getCurrentTerm("HP:0004657"), equalTo(BAR));
        assertThat(instance.getCurrentTerm("HP:0007871"), equalTo(BAR));
    }

    @Test
    void checkLastTermIsIncluded() {
        assertThat(instance.getCurrentTerm("HP:3000079"), equalTo(BAZ));
    }
}