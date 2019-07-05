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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.springframework.cache.interceptor.SimpleKey;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantKeyGeneratorTest {

    private final VariantKeyGenerator instance = new VariantKeyGenerator();

    @Test
    public void returnsEmptyKeyForEmptyQuery() throws Exception {
        assertThat(instance.generate(new Object(), Object.class.getMethod("toString")), equalTo(SimpleKey.EMPTY));
    }

    @Test
    public void returnsKeyForVariant() throws Exception {
        Variant variant = VariantAnnotation.builder()
                .chromosome(1)
                .start(2345)
                .ref("A")
                .alt("T")
                .build();
        // AlleleKey has no genomeAssembly. This might have been a bit of an oversight, but with assembly-specific caches
        // created in version 10.1.1 its OK to use the AlleleKey as the cache key
        AlleleProto.AlleleKey expected = AlleleProto.AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(2345)
                .setRef("A")
                .setAlt("T")
                .build();

        assertThat(instance.generate(new Object(), Object.class.getMethod("toString"), variant), equalTo(expected));
    }

    @Test
    public void returnsSimpleKeyForManyThings() throws Exception {
        Object object1 = new Object();
        Object object2 = new Object();

        assertThat(instance.generate(new Object(), Object.class.getMethod("toString"), object1, object2), equalTo(new SimpleKey(object1, object2)));
    }
}