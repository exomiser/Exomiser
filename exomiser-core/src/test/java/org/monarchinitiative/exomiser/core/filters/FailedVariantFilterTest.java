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

package org.monarchinitiative.exomiser.core.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FailedVariantFilterTest {

    private final FailedVariantFilter instance = new FailedVariantFilter();

    private final VCFCodec vcfCodec;

    public FailedVariantFilterTest() {
        vcfCodec = new VCFCodec();
        vcfCodec.setVCFHeader(new VCFHeader(), VCFHeaderVersion.VCF4_2);
    }

    private VariantEvaluation variantEvaluationWithFilterField(String filterField) {
        //CHR\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO
        String vcfLine = "1\t123456789\t.\tG\tA\t0\t" + filterField + "\t.";

        VariantContext variantContext = vcfCodec.decode(vcfLine);
        System.out.println("Made variant context with filter field '" + filterField +  "': " + variantContext + " FILTER=" + variantContext.getFilters());
        return VariantEvaluation.builder(1, 123456789, "G","A")
                .variantContext(variantContext)
                .build();
    }

    @Test
    public void testPassesVariantWithPassFilterField() {
        VariantEvaluation variant = variantEvaluationWithFilterField("PASS");
        FilterResult result = instance.runFilter(variant);
        System.out.println(result);
        assertThat(result.passed(), is(true));
    }

    @Test
    public void testPassesVariantWithUnFilteredFilterField() {
        VariantEvaluation variant = variantEvaluationWithFilterField(".");
        FilterResult result = instance.runFilter(variant);
        System.out.println(result);
        assertThat(result.passed(), is(true));
    }

    @Test
    public void testPassesVariantWithFailedFiltersInFilterField() {
        VariantEvaluation variant = variantEvaluationWithFilterField("wibble;hoopy;frood");
        FilterResult result = instance.runFilter(variant);
        System.out.println(result);
        assertThat(result.failed(), is(true));
    }

}