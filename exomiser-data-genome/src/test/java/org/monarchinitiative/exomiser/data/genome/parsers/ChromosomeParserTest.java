/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.parsers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ChromosomeParserTest {

    @Test
    public void parseNumeric() throws Exception {
        assertThat(ChromosomeParser.parseChr("1"), equalTo((byte) 1));
        assertThat(ChromosomeParser.parseChr("22"), equalTo((byte) 22));
    }

    @Test
    public void parseX() throws Exception {
        assertThat(ChromosomeParser.parseChr("X"), equalTo((byte) 23));
        assertThat(ChromosomeParser.parseChr("x"), equalTo((byte) 23));
    }

    @Test
    public void parseY() throws Exception {
        assertThat(ChromosomeParser.parseChr("Y"), equalTo((byte) 24));
        assertThat(ChromosomeParser.parseChr("y"), equalTo((byte) 24));
    }

    @Test
    public void parseM() throws Exception {
        assertThat(ChromosomeParser.parseChr("M"), equalTo((byte) 25));
        assertThat(ChromosomeParser.parseChr("MT"), equalTo((byte) 25));
        assertThat(ChromosomeParser.parseChr("m"), equalTo((byte) 25));
    }

    @Test
    public void parseVcfEmpty() throws Exception {
        assertThat(ChromosomeParser.parseChr("."), equalTo((byte) 0));
    }

    @Test
    public void parseUnknownScaffold() throws Exception {
        assertThat(ChromosomeParser.parseChr("Unk_GL000008v2"), equalTo((byte) 0));
    }

    @Test
    public void parseAltScaffold() throws Exception {
        assertThat(ChromosomeParser.parseChr("4_GL000008v2_alt"), equalTo((byte) 0));
    }

    @Test
    public void parseRandomScaffold() throws Exception {
        assertThat(ChromosomeParser.parseChr("4_GL000008v2_random"), equalTo((byte) 0));
    }

}