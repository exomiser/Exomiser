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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableList;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */

public class VcfFilesTest {

    @Test(expected = NullPointerException.class)
    public void testCreateVariantContextsNullPath() {
        VcfFiles.readVariantContexts(null);
    }

    @Test(expected = TribbleException.class)
    public void testCreateVariantContexts_NonExistentFile() {
        Path vcfPath = Paths.get("src/test/resources/wibble.vcf");
        VcfFiles.readVariantContexts(vcfPath);
    }

    @Test
    public void testCreateVariantContexts_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        long numVariants;
        try (Stream<VariantContext> variantStream = VcfFiles.readVariantContexts(vcfPath)) {
            numVariants = variantStream.count();
        }
        assertThat(numVariants, equalTo(3L));
    }

    @Test
    public void testStreamVariantContexts_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantContext> variants = VcfFiles.readVariantContexts(vcfPath)
                .filter(variantContext -> (variantContext.getContig().equals("1")))
                .collect(toList());

        assertThat(variants.size(), equalTo(3));
    }

    @Test
    public void testCreateVariantContexts_MultipleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        List<VariantContext> variants = VcfFiles.readVariantContexts(vcfPath).collect(toList());
        assertThat(variants.size(), equalTo(1));
    }

    @Test
    public void testReadVcfHeader() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        VCFHeader header = VcfFiles.readVcfHeader(vcfPath);
        assertThat(header.getGenotypeSamples(), equalTo(ImmutableList.of("sample")));
    }
}