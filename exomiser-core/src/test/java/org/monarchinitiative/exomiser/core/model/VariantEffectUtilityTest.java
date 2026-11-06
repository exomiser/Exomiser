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

package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.monarchinitiative.exomiser.core.genome.TestFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantEffectUtilityTest {

    @ParameterizedTest
    @CsvSource({
            "EXON_LOSS_VARIANT, true, false",
            "MISSENSE_VARIANT, true, false",
            "INFRAME_DELETION, true, false",
            "INFRAME_INSERTION, true, false",
            "CODING_TRANSCRIPT_INTRON_VARIANT, false, false",
            "NON_CODING_TRANSCRIPT_EXON_VARIANT, false, false",
            "INTERGENIC_VARIANT, false, false",
            "FIVE_PRIME_UTR_EXON_VARIANT, false, true",
            "THREE_PRIME_UTR_EXON_VARIANT, false, true",
            "STRUCTURAL_VARIANT, false, false"
    })
    void testCodingClassification(VariantEffect effect, boolean coding, boolean regulatory) {
        assertThat(VariantEffects.affectsCodingRegion(effect), is(coding));
        assertThat(VariantEffects.isNonCodingVariant(effect), is(!coding));
        assertThat(VariantEffects.isRegulatoryNonCodingVariant(effect), is(regulatory));

        VariantEvaluation variant = TestFactory.variantBuilder(1, 1, "A", "T").variantEffect(effect).build();
        assertThat(variant.isCodingVariant(), is(coding));
        assertThat(variant.isNonCodingVariant(), is(!coding));
    }

    @ParameterizedTest
    @EnumSource(VariantEffect.class)
    void testCodingAndNonCodingAreComplementary(VariantEffect effect) {
        assertThat(VariantEffects.isNonCodingVariant(effect), is(!VariantEffects.affectsCodingRegion(effect)));
    }

    @Test
    public void testIsNonCodingVariant() throws Exception {
        assertThat(VariantEffects.isNonCodingVariant(VariantEffect.NON_CODING_TRANSCRIPT_EXON_VARIANT), is(true));
        assertThat(VariantEffects.isNonCodingVariant(VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT), is(true));
    }

    @Test
    public void testIsRegulatoryNonCodingVariant() throws Exception {
        assertThat(VariantEffects.isRegulatoryNonCodingVariant(VariantEffect.NON_CODING_TRANSCRIPT_EXON_VARIANT), is(false));
        assertThat(VariantEffects.isRegulatoryNonCodingVariant(VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT), is(true));
    }

    @Test
    public void testMissenseVariantIsNotConsideredNonCodingVariant() throws Exception {
        assertThat(VariantEffects.isNonCodingVariant(VariantEffect.MISSENSE_VARIANT), is(false));
        assertThat(VariantEffects.isRegulatoryNonCodingVariant(VariantEffect.MISSENSE_VARIANT), is(false));
    }

}
