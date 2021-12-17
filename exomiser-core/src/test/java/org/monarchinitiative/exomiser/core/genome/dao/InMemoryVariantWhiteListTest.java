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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class InMemoryVariantWhiteListTest {

    @Test
    void wontAcceptNull() {
        assertThrows(NullPointerException.class, () -> InMemoryVariantWhiteList.of(null));
    }

    @Test
    void willAcceptEmptySet() {
        assertThat(InMemoryVariantWhiteList.empty(), equalTo(InMemoryVariantWhiteList.of(Collections.emptySet())));
    }

    @Test
    void testContains() {
        Variant whiteListedVariant = TestFactory.variantBuilder(1, 234567, "A", "G").build();
        Set<AlleleProto.AlleleKey> whitelistedKeys = Set.of(AlleleProtoAdaptor.toAlleleKey(whiteListedVariant));

        VariantWhiteList instance = InMemoryVariantWhiteList.of(whitelistedKeys);
        assertThat(instance.contains(whiteListedVariant), is(true));

        VariantWhiteList emptyInstance = InMemoryVariantWhiteList.empty();
        assertThat(emptyInstance.contains(whiteListedVariant), is(false));
    }
}