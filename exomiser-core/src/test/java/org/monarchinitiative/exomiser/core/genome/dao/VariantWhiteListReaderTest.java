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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class VariantWhiteListReaderTest {

    private final Path testDataDir = Paths.get("src/test/resources/whitelist");

    @Test
    void loadEmptyVariantWhiteList() {
        Set<AlleleProto.AlleleKey> loaded = VariantWhiteListReader.readVariantWhiteList(testDataDir.resolve("empty.tsv.gz"));
        assertThat(loaded, equalTo(Collections.emptySet()));
    }

    @Test
    void loadEmptyVariantWhiteListWithHeader() {
        Set<AlleleProto.AlleleKey> loaded = VariantWhiteListReader.readVariantWhiteList(testDataDir.resolve("empty-with-header.tsv.gz"));
        assertThat(loaded, equalTo(Collections.emptySet()));
    }

    @Test
    void loadVariantWhiteListWithTooFewFields() {
        Set<AlleleProto.AlleleKey> loaded = VariantWhiteListReader.readVariantWhiteList(testDataDir.resolve("too-few-fields.tsv.gz"));
        assertThat(loaded, equalTo(Collections.emptySet()));
    }

    @Test
    void loadVariantWhiteList() {
        Set<AlleleProto.AlleleKey> loaded = VariantWhiteListReader.readVariantWhiteList(testDataDir.resolve("whitelist.tsv.gz"));

        Set<AlleleProto.AlleleKey> keys = Set.of(
                AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(12345).setRef("A").setAlt("G").build(),
                AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(985052).setRef("C").setAlt("T").build()
        );
        assertThat(loaded, equalTo(keys));
    }
}