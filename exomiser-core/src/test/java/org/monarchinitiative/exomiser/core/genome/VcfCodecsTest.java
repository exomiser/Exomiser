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

import com.google.common.collect.ImmutableSet;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFEncoder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VcfCodecsTest {

    @Test
    void getEncoder() {
        ImmutableSet<String> sampleGenotypes = ImmutableSet.of("Arthur", "Ford");

        VariantContext variantContext = TestVcfParser
                .forSamples("Arthur", "Ford")
                .toVariantContext("1 12345 . A T,C 100 PASS WIBBLE;FROOD GT 0/1 1/2");

        VCFEncoder encoder = VcfCodecs.encoder(sampleGenotypes);
        String encoded = encoder.encode(variantContext);

        VCFCodec decoder = VcfCodecs.decoder(sampleGenotypes);
        VariantContext decoded = decoder.decode(encoded);

        assertThat(variantContext.toStringDecodeGenotypes(), equalTo(decoded.toStringDecodeGenotypes()));
    }
}