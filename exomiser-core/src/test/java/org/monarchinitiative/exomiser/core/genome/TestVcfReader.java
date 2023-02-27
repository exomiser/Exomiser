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

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for making variants for testing purposes. This removes the requirement for having to use actual VCF
 * files as the variants can be specified locally in the code when required.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class TestVcfReader implements VcfReader {

    private final VCFCodec vcfCodec;
    private final List<String> vcfLines;

    private TestVcfReader(List<String> sampleNames, List<String> vcfLines) {
        this.vcfCodec = getVcfCodecForSamples(List.copyOf(sampleNames));
        this.vcfLines = List.copyOf(vcfLines);
    }

    public static TestVcfReader of(List<String> sampleNames, String... vcfLines) {
        return new TestVcfReader(sampleNames, List.of(vcfLines));
    }

    public static TestVcfReader forSamples(String... sampleNames) {
        return new TestVcfReader(List.of(sampleNames), List.of());
    }

    private VCFCodec getVcfCodecForSamples(List<String> sampleNames) {
        VCFCodec vcfCodec = new VCFCodec();
        vcfCodec.setVCFHeader(new VCFHeader(Collections.emptySet(), sampleNames), VCFHeaderVersion.VCF4_2);
        return vcfCodec;
    }

    @Override
    public List<String> readSampleIdentifiers() {
        return vcfCodec.getHeader().getGenotypeSamples();
    }

    @Override
    public Stream<VariantContext> readVariantContexts() {
        if (vcfLines.isEmpty()) {
            throw new IllegalStateException("No  lines to read!");
        }
        return vcfLines.stream().map(toVariantContext());
    }

    /**
     * Accepts a VCF formatted strings either tab separated or using spaces in place of tabs to produce a
     * Stream<VariantContext>
     * <p>
     * Single sample:    "1 123256213 . CA CC 0 . . GT 1/1"
     * MultiSample examples:
     * Single variation: "1 123256213 . CA CC 0 . . GT 1/1 0/1"
     * Multi variantion: "1 123256213 . CA CC,CT 0 . . GT 1/1 1/2"
     *
     * @param lines
     * @return a Stream of VariantContext for the line provided.
     */
    public Stream<VariantContext> readVariantContexts(String... lines) {
        return Stream.of(lines).map(toVariantContext());
    }

    private Function<String, VariantContext> toVariantContext() {
        return this::readVariantContext;
    }

    /**
     * Accepts a VCF formatted line either tab separated or using spaces in place of tabs to produce a VariantContext.
     * <p>
     * Single sample:    "1 123256213 . CA CC 0 . . GT 1/1"
     * MultiSample examples:
     * Single variation: "1 123256213 . CA CC 0 . . GT 1/1 0/1"
     * Multi variation: "1 123256213 . CA CC,CT 0 . . GT 1/1 1/2"
     *
     * @param line
     * @return a VariantContext for the line provided.
     */
    public VariantContext readVariantContext(String line) {
        return vcfCodec.decode(line.replaceAll("\\s+", "\t").trim());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> sampleNames = List.of();
        private List<String> vcfLines = List.of();

        public Builder samples(String... sample) {
            this.sampleNames = List.of(sample);
            return this;
        }

        public Builder vcfLines(String... vcfLine) {
            this.vcfLines = List.of(vcfLine);
            return this;
        }

        public TestVcfReader build() {
            return new TestVcfReader(sampleNames, vcfLines);
        }
    }
}
