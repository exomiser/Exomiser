/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarAnnotationServiceTest {

    private final JannovarAnnotationService instance = new JannovarAnnotationService(TestFactory.buildDefaultJannovarData());

    @Test
    public void testAnnotateKnownGenomeVariant() {
        VariantAnnotations variantAnnotations = instance.annotateVariant("10", 123256215, "T", "G");
        assertThat(variantAnnotations.getChrName(), equalTo("10"));
        assertThat(variantAnnotations.getChr(), equalTo(10));
        assertThat(variantAnnotations.getPos(), equalTo(123256214)); //Jannovar uses and returns 0-based coordinates.
        assertThat(variantAnnotations.getRef(), equalTo("T"));
        assertThat(variantAnnotations.getAlt(), equalTo("G"));
        assertThat(variantAnnotations.hasAnnotation(), is(true));
        assertThat(variantAnnotations.getHighestImpactEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    public void testAnnotateInvalidGenomeVariant() {
        VariantAnnotations variantAnnotations = instance.annotateVariant("10", 0, "", "");
        assertThat(variantAnnotations.getChrName(), equalTo("10"));
        assertThat(variantAnnotations.getChr(), equalTo(10));
        assertThat(variantAnnotations.getPos(), equalTo(-1)); //Jannovar uses and returns 0-based coordinates.
        assertThat(variantAnnotations.getRef(), equalTo(""));
        assertThat(variantAnnotations.getAlt(), equalTo(""));
        assertThat(variantAnnotations.hasAnnotation(), is(false));
        assertThat(variantAnnotations.getHighestImpactEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
    }

    @Test
    public void testGetAnnotationsForUnknownContigVariant() {
        VariantAnnotations variantAnnotations = instance.annotateVariant("UNKNOWN", 1, "A", "T");
        assertThat(variantAnnotations, not(nullValue()));
        assertThat(variantAnnotations.getChr(), equalTo(0));
        assertThat(variantAnnotations.getPos(), equalTo(0)); //Jannovar uses and returns 0-based coordinates.
        assertThat(variantAnnotations.getRef(), equalTo("A"));
        assertThat(variantAnnotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForKnownContigVariant() {
        VariantAnnotations variantAnnotations = instance.annotateVariant("chr1", 1, "A", "T");
        assertThat(variantAnnotations.getChr(), equalTo(1));
        assertThat(variantAnnotations.getPos(), equalTo(0));  //Jannovar uses and returns 0-based coordinates.
        assertThat(variantAnnotations.getRef(), equalTo("A"));
        assertThat(variantAnnotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForKnownExonicVariant() {
        int pos = 123256215;
        String ref = "T";
        String alt = "G";

        VariantAnnotations variantAnnotations = instance.annotateVariant("10", pos, ref, alt);

        assertThat(variantAnnotations.getChr(), equalTo(10));
        assertThat(variantAnnotations.getPos(), equalTo(pos - 1)); //Jannovar uses and returns 0-based coordinates.
        assertThat(variantAnnotations.getRef(), equalTo(ref));
        assertThat(variantAnnotations.getAlt(), equalTo(alt));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedInsertionJannovarShiftsRight() {
        int pos = 118608471;
        String ref = "GT";
        String alt = "GTT";

        VariantAnnotations variantAnnotations = instance.annotateVariant("X", pos, ref, alt);

        assertThat(variantAnnotations.getChr(), equalTo(23));
        assertThat(variantAnnotations.getPos(), equalTo(118608471 + 1)); //  Jannovar trims from the left first i.e. right-shifts.
        assertThat(variantAnnotations.getRef(), equalTo(""));
        assertThat(variantAnnotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedInsertion() {
        int pos = 118608470;
        String ref = "AGT";
        String alt = "AGTT";

        VariantAnnotations variantAnnotations = instance.annotateVariant("X", pos, ref, alt);

        assertThat(variantAnnotations.getChr(), equalTo(23));
        assertThat(variantAnnotations.getPos(), equalTo(118608470 + 2));//  Jannovar trims from the left first i.e. right-shifts.
        assertThat(variantAnnotations.getRef(), equalTo(""));
        assertThat(variantAnnotations.getAlt(), equalTo("T"));
    }

    @Test
    public void testGetAnnotationsForUnTrimmedDeletionJannovarShiftsRight() {
        int pos = 118608470;
        String ref = "AGTT";
        String alt = "AGT";

        VariantAnnotations variantAnnotations = instance.annotateVariant("X", pos, ref, alt);

        assertThat(variantAnnotations.getChr(), equalTo(23));
        assertThat(variantAnnotations.getPos(), equalTo(118608471 + 1)); //Jannovar trims from the left first i.e. right-shifts.
        assertThat(variantAnnotations.getRef(), equalTo("T"));
        assertThat(variantAnnotations.getAlt(), equalTo(""));
    }

}