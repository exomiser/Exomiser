/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantType;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Given the VariantContextAnnotator is a utility class for the VariantFactoryImpl, it is mainly tested through the
 * VariantFactoryImplTest.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantContextAnnotatorTest {

    protected VariantContextAnnotator instance;

    @BeforeEach
    void setUp() {
        instance = new VariantContextAnnotator(TestFactory.buildDefaultVariantAnnotator());
    }

    @Test
    public void testKnownSingleSampleSnp() {
        VariantContext variantContext = TestVcfParser.forSamples("Sample1")
                .toVariantContext("10\t123256215\t.\tT\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");

        List<VariantAnnotation> variants = instance.annotateAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variants.size(), equalTo(1));
        VariantAnnotation variantAnnotation = variants.get(0);
        System.out.println(variantAnnotation);

        assertThat(variantAnnotation.getChromosome(), equalTo(10));
        assertThat(variantAnnotation.getChromosomeName(), equalTo("10"));
        assertThat(variantAnnotation.getStart(), equalTo(123256215));
        assertThat(variantAnnotation.getEnd(), equalTo(123256215));
        assertThat(variantAnnotation.getLength(), equalTo(0));
        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("G"));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
        System.out.println(variantAnnotation.getTranscriptAnnotations());
        assertThat(variantAnnotation.getGeneId(), equalTo("2263"));
        assertThat(variantAnnotation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    void testStructuralVariantNoLength() {
        VariantContext variantContext = TestVcfParser.forSamples("Sample1")
                .toVariantContext("1 212471179 esv3588749 T <CN0> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVTYPE=DEL;VT=SV GT 0|1");

        List<VariantAnnotation> variants = instance.annotateAllele(variantContext, variantContext.getAlternateAllele(0));

        assertThat(variants.size(), equalTo(1));
        VariantAnnotation variantAnnotation = variants.get(0);
        // note that the VariantEffect will be 'INTERGENIC_VARIANT' unless the variant overlaps with a gene. In this case there
        // are no transcript models covering this region loaded in the test data, so 'INTERGENIC_VARIANT' is correct
        // functionality here.
        assertThat(variantAnnotation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantAnnotation.getVariantType(), equalTo(VariantType.DEL));

        assertThat(variantAnnotation.getStart(), equalTo(212471179));
        assertThat(variantAnnotation.getStartMin(), equalTo(212471179 - 471));
        assertThat(variantAnnotation.getStartMax(), equalTo(212471179));

        assertThat(variantAnnotation.getEnd(), equalTo(212472619));
        assertThat(variantAnnotation.getEndMin(), equalTo(212472619));
        assertThat(variantAnnotation.getEndMax(), equalTo(212472619 + 444));

        assertThat(variantAnnotation.getLength(), equalTo(1440));

        assertThat(variantAnnotation.getRef(), equalTo("T"));
        assertThat(variantAnnotation.getAlt(), equalTo("<CN0>"));
        assertThat(variantAnnotation.hasTranscriptAnnotations(), is(true));
    }
}