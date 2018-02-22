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
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for providing access to VCF files.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 9.0.0
 */
public class VcfFiles {

    private static final Logger logger = LoggerFactory.getLogger(VcfFiles.class);

    private VcfFiles() {
    }

    /**
     * Creates a {@code Stream} of {@code VariantContext} from the indicated VCF file.
     *
     * @param vcfPath path of the VCF file
     * @return a {@code Stream} of {@code VariantContext}
     */
    public static Stream<VariantContext> readVariantContexts(Path vcfPath) {
        logger.info("Reading variants from VCF file {}", vcfPath);
        try (VCFFileReader vcfReader = new VCFFileReader(vcfPath.toFile(), false)) {
            return vcfReader.iterator().stream();
        }
    }

    /**
     * Reads the header of the provided VCF file and returns a {@code VCFHeader}.
     *
     * @param vcfPath path to the VCF file
     * @return the {@code VCFHeader} object parsed from the file
     */
    public static VCFHeader readVcfHeader(Path vcfPath) {
        logger.info("Reading VCF header from file {}", vcfPath);
        return readHeader(vcfPath);
    }

    /**
     * Creates an orderd list of {@code SampleIdentifier} created from the genotype sample names as listed in the VCF
     * header.
     *
     * @param vcfPath path to the VCF file.
     * @return an ordered list of {@code SampleIdentifier} for the genotype sample names in the VCF header.
     * @since 10.0.0
     */
    public static List<SampleIdentifier> readVcfSamples(Path vcfPath) {
        logger.info("Reading VCF samples from file {}", vcfPath);
        VCFHeader vcfHeader = readHeader(vcfPath);
        List<String> genotypeSampleNames = vcfHeader.getGenotypeSamples();
        ImmutableList.Builder<SampleIdentifier> samples = ImmutableList.builder();
        for (int i = 0; i < genotypeSampleNames.size(); i++) {
            samples.add(SampleIdentifier.of(genotypeSampleNames.get(i), i));
        }
        return samples.build();
    }

    private static VCFHeader readHeader(Path vcfPath) {
        try (VCFFileReader vcfReader = new VCFFileReader(vcfPath.toFile(), false)) {
            return vcfReader.getFileHeader();
        }
    }
}
