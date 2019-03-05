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

package org.monarchinitiative.exomiser.core.genome;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ContigTest {

    @Test
    void unknownOrAlternateScaffold() {
        assertThat(Contig.parseId("."), equalTo(0));
        assertThat(Contig.parseId("wibble"), equalTo(0));
    }

    @Test
    void testAutosomes() {
        for (int i = 1; i < 23; i++) {
            assertThat(Contig.parseId(String.valueOf(i)), equalTo(i));
            assertThat(Contig.parseId("chr" + i), equalTo(i));
        }
        assertThat(Contig.parseId("1"), equalTo(1));
        assertThat(Contig.parseId("chr1"), equalTo(1));

        assertThat(Contig.parseId("22"), equalTo(22));
        assertThat(Contig.parseId("chr22"), equalTo(22));
    }

    @Test
    void testX() {
        assertThat(Contig.parseId("23"), equalTo(23));
        assertThat(Contig.parseId("chr23"), equalTo(23));
        assertThat(Contig.parseId("X"), equalTo(23));
        assertThat(Contig.parseId("chrX"), equalTo(23));
    }

    @Test
    void testY() {
        assertThat(Contig.parseId("24"), equalTo(24));
        assertThat(Contig.parseId("chr24"), equalTo(24));
        assertThat(Contig.parseId("Y"), equalTo(24));
        assertThat(Contig.parseId("chrY"), equalTo(24));
    }

    @Test
    void testM() {
        assertThat(Contig.parseId("25"), equalTo(25));
        assertThat(Contig.parseId("chr25"), equalTo(25));
        assertThat(Contig.parseId("M"), equalTo(25));
        assertThat(Contig.parseId("MT"), equalTo(25));
        assertThat(Contig.parseId("chrM"), equalTo(25));
    }
}