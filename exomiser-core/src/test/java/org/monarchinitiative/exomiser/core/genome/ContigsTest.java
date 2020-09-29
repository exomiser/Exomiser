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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ContigsTest {

    @Test
    void unknownOrAlternateScaffold() {
        assertThat(Contigs.parseId("."), equalTo(0));
        assertThat(Contigs.parseId("wibble"), equalTo(0));
    }

    @Test
    void testAutosomes() {
        for (int i = 1; i < 23; i++) {
            assertThat(Contigs.parseId(String.valueOf(i)), equalTo(i));
            assertThat(Contigs.parseId("chr" + i), equalTo(i));
        }
        assertThat(Contigs.parseId("1"), equalTo(1));
        assertThat(Contigs.parseId("chr1"), equalTo(1));
        assertThat(Contigs.parseId("NC_000001.10"), equalTo(1));
        assertThat(Contigs.parseId("NC_000001.11"), equalTo(1));

        assertThat(Contigs.parseId("22"), equalTo(22));
        assertThat(Contigs.parseId("chr22"), equalTo(22));
        assertThat(Contigs.parseId("NC_000022.10"), equalTo(22));
        assertThat(Contigs.parseId("NC_000022.11"), equalTo(22));
    }

    @Test
    void testX() {
        assertThat(Contigs.parseId("23"), equalTo(23));
        assertThat(Contigs.parseId("chr23"), equalTo(23));
        assertThat(Contigs.parseId("X"), equalTo(23));
        assertThat(Contigs.parseId("chrX"), equalTo(23));
        assertThat(Contigs.parseId("NC_000023.10"), equalTo(23));
        assertThat(Contigs.parseId("NC_000023.11"), equalTo(23));
    }

    @Test
    void testY() {
        assertThat(Contigs.parseId("24"), equalTo(24));
        assertThat(Contigs.parseId("chr24"), equalTo(24));
        assertThat(Contigs.parseId("Y"), equalTo(24));
        assertThat(Contigs.parseId("chrY"), equalTo(24));
        assertThat(Contigs.parseId("NC_000024.9"), equalTo(24));
        assertThat(Contigs.parseId("NC_000024.10"), equalTo(24));
    }

    @Test
    void testM() {
        assertThat(Contigs.parseId("25"), equalTo(25));
        assertThat(Contigs.parseId("chr25"), equalTo(25));
        assertThat(Contigs.parseId("M"), equalTo(25));
        assertThat(Contigs.parseId("MT"), equalTo(25));
        assertThat(Contigs.parseId("chrM"), equalTo(25));
        assertThat(Contigs.parseId("NC_012920.1"), equalTo(25));
    }
}