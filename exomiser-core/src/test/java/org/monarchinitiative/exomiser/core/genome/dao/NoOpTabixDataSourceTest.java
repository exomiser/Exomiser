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

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NoOpTabixDataSourceTest {

    private static final TabixDataSource INSTANCE = new NoOpTabixDataSource("TEST");

    @Test
    void query() throws IOException {
        assertThat(INSTANCE.query("wibble").next(), is(nullValue()));
    }

    @Test
    void testQuery() throws IOException {
        assertThat(INSTANCE.query("1", 123, 456).next(), is(nullValue()));
    }

    @Test
    void getSource() {
        assertThat(INSTANCE.getSource(), equalTo("TEST"));
    }
}