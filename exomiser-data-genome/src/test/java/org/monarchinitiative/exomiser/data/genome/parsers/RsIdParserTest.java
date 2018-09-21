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
public class RsIdParserTest {

    @Test
    public void parseRsIdOneRsId() throws Exception {
        assertThat(RsIdParser.parseRsId("rs12345"), equalTo("rs12345"));
    }

    @Test
    public void parseRsIdConcatRsId() throws Exception {
        assertThat(RsIdParser.parseRsId("rs12345;rs54321"), equalTo("rs12345"));
    }

    @Test
    public void parseRsIdVcfEmptyRsId() throws Exception {
        assertThat(RsIdParser.parseRsId("."), equalTo(""));
    }

    @Test
    public void parseRsIdReallyEmptyRsId() throws Exception {
        assertThat(RsIdParser.parseRsId(""), equalTo(""));
    }
}