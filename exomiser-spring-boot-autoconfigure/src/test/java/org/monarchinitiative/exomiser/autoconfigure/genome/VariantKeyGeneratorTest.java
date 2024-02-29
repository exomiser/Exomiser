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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;
import org.springframework.cache.interceptor.SimpleKey;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantKeyGeneratorTest {

    private final VariantKeyGenerator instance = new VariantKeyGenerator();

    @Test
    void returnsEmptyKeyForEmptyQuery() throws Exception {
        assertThat(instance.generate(new Object(), Object.class.getMethod("toString")), equalTo(SimpleKey.EMPTY));
    }

    
    @Test
    void returnsKeyForVariant() throws Exception {
        GenomicVariant genomicVariant = GenomicVariant.of(GenomeAssembly.HG19.getContigById(1), Strand.POSITIVE, CoordinateSystem.oneBased(), 2345, "A", "T");
        Variant variant = VariantEvaluation.builder().variant(genomicVariant).build();
        // AlleleKey has no genomeAssembly. This might have been a bit of an oversight, but with assembly-specific caches
        // created in version 10.1.1 it's OK to use the AlleleKey as the cache key
        AlleleProto.AlleleKey expected = AlleleProto.AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(2345)
                .setRef("A")
                .setAlt("T")
                .build();

        assertThat(instance.generate(new Object(), Object.class.getMethod("toString"), variant), equalTo(expected));
    }

    @Test
    void returnsKeyForGenomicVariant() throws Exception {
        GenomicVariant genomicVariant = GenomicVariant.of(GenomeAssembly.HG19.getContigById(1), Strand.POSITIVE, CoordinateSystem.oneBased(), 2345, "A", "T");
        AlleleProto.AlleleKey expected = AlleleProto.AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(2345)
                .setRef("A")
                .setAlt("T")
                .build();

        assertThat(instance.generate(new Object(), Object.class.getMethod("toString"), genomicVariant), equalTo(expected));
    }

    @Test
    void returnsKeyForAlleleKey() throws Exception {
        GenomicVariant genomicVariant = GenomicVariant.of(GenomeAssembly.HG19.getContigById(1), Strand.POSITIVE, CoordinateSystem.oneBased(), 2345, "A", "T");
        AlleleProto.AlleleKey alleleKey = AlleleProtoAdaptor.toAlleleKey(genomicVariant);
        AlleleProto.AlleleKey expected = AlleleProto.AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(2345)
                .setRef("A")
                .setAlt("T")
                .build();

        assertThat(instance.generate(new Object(), Object.class.getMethod("toString"), alleleKey), equalTo(expected));
    }

    @Test
    void returnsSimpleKeyForManyThings() throws Exception {
        Object object1 = new Object();
        Object object2 = new Object();

        assertThat(instance.generate(new Object(), Object.class.getMethod("toString"), object1, object2), equalTo(new SimpleKey(object1, object2)));
    }
}