/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package org.monarchinitiative.exomiser.core.genome;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Utility class for making variants for testing purposes. This removes the requirement for having to use actual VCF
 * files as the variants can be specified locally in the code when required.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(VariantContextBuilder.class);

    private final VCFCodec vcfCodec;

    public VariantContextBuilder(String... sampleNames) {
        vcfCodec = new VCFCodec();
        VCFHeader header = new VCFHeader(new HashSet<>(), Arrays.asList(sampleNames));
        vcfCodec.setVCFHeader(header, VCFHeaderVersion.VCF4_1);
    }

    /**
     * Accepts a VCF formatted string either tab separated or using spaces in place of tabs to produce a VariantContext.
     * Single sample:    "1 123256213 . CA CC 0 . . GT 1/1"
     * MultiSample examples:
     * Single variation: "1 123256213 . CA CC 0 . . GT 1/1 0/1"
     * Multi variantion: "1 123256213 . CA CC,CT 0 . . GT 1/1 1/2"
     *
     * @param vcfLine
     * @return
     */
    public VariantContext build(String vcfLine) {
        vcfLine = vcfLine.replaceAll("[ ]+", "\t");
        return vcfCodec.decode(vcfLine);
    }

    public List<VariantContext> build(String... vcfLines) {
       return Arrays.stream(vcfLines).map(this::build).collect(toList());
    }
}
