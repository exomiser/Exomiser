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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class TranscriptSourceTest {

    @Test
    void parseValue() {
        assertThat(TranscriptSource.parseValue("EnsEMBL"), equalTo(TranscriptSource.ENSEMBL));
        assertThat(TranscriptSource.parseValue("ensembl"), equalTo(TranscriptSource.ENSEMBL));

        assertThat(TranscriptSource.parseValue("RefSeq"), equalTo(TranscriptSource.REFSEQ));
        assertThat(TranscriptSource.parseValue("refseq"), equalTo(TranscriptSource.REFSEQ));

        assertThat(TranscriptSource.parseValue("UCSC"), equalTo(TranscriptSource.UCSC));
        assertThat(TranscriptSource.parseValue("ucsc"), equalTo(TranscriptSource.UCSC));

        assertThrows(RuntimeException.class,
                () -> TranscriptSource.parseValue("wibble"),
                "'wibble' is not a valid/supported transcript source. Valid sources are: [ensembl, refseq, ucsc]");
    }

    @Test
    void testToString() {
        assertThat(TranscriptSource.ENSEMBL.toString(), equalTo("ensembl"));
        assertThat(TranscriptSource.REFSEQ.toString(), equalTo("refseq"));
        assertThat(TranscriptSource.UCSC.toString(), equalTo("ucsc"));
    }

}