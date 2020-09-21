/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFEncoder;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class enabling trivial inter-conversion of {@link htsjdk.variant.variantcontext.VariantContext} to the VCF format and
 * back. This requires that the sampleGenotypes supplied are as an *ORDERED* set.
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VcfCodecs {

    private static final Logger logger = LoggerFactory.getLogger(VcfCodecs.class);

    // TODO: use Caffeine cache with TTL as this could get really large for a long-running process
    private static final Map<Set<String>, VCFEncoder> encoderCache = new HashMap<>();
    private static final Map<Set<String>, VCFCodec> decoderCache = new HashMap<>();

    private VcfCodecs() {
    }

    /**
     * This requires that the sampleGenotypes supplied are as an *ORDERED* set. Not supplying such a set will result in
     * incorrect encoding of sample genotypes.
     *
     * @param sampleGenotypes
     * @return a VCFEncoder for the specified sampleGenotypes
     */
    public static VCFEncoder encoder(Set<String> sampleGenotypes) {
        return encoderCache.computeIfAbsent(sampleGenotypes, key -> {
            VCFHeader vcfHeader = new VCFHeader(Collections.emptySet(), new ArrayList<>(sampleGenotypes));
            logger.debug("Making new VCFEncoder for samples {}", sampleGenotypes);
            return new VCFEncoder(vcfHeader, true, true);
        });
    }

    /**
     * This requires that the sampleGenotypes supplied are as an *ORDERED* set. Not supplying such a set will result in
     * incorrect decoding of sample genotypes.
     *
     * @param sampleGenotypes
     * @return a VCFCodec for the specified sampleGenotypes
     */
    public static VCFCodec decoder(Set<String> sampleGenotypes) {
        return decoderCache.computeIfAbsent(sampleGenotypes, key -> {
            VCFHeader vcfHeader = new VCFHeader(Collections.emptySet(), new ArrayList<>(sampleGenotypes));
            VCFCodec vcfCodec = new VCFCodec();
            vcfCodec.setVCFHeader(vcfHeader, VCFHeaderVersion.VCF4_2);
            logger.debug("Making new VCFCodec for samples {}", sampleGenotypes);
            return vcfCodec;
        });
    }
}
