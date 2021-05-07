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

package org.monarchinitiative.exomiser.data.genome.model.parsers.sv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DbVarFreqParserTest {

    @Test
    void lineWithoutFrequencyInfoReturnsEmptyList() {
        String line = "2\t145346767\tessv17884129\tA\t<INS:ME:ALU>\t.\t.\tDBVARID;SVTYPE=INS;END=145346767;SVLEN=274;EXPERIMENT=9;SAMPLE=NA20758;REGIONID=esv3826447;AN=30;AC=3";
        String line1 = "2\t145346767\tessv17884129\tA\t<INS:ME:ALU>\t.\t.\tDBVARID;SVTYPE=INS;END=145346767;SVLEN=274;EXPERIMENT=9;SAMPLE=NA20758;REGIONID=esv3826447";
        String line2 = "2\t145346767\tessv17884129\tA\t<INS:ME:ALU>\t.\t.\tDBVARID;SVTYPE=INS;END=145346767;SVLEN=274;EXPERIMENT=9;SAMPLE=NA20758;REGIONID=esv3826447";
        DbVarFreqParser instance = new DbVarFreqParser();
//        assertThat(instance.parseLine(line), equalTo(List.of()));
        assertThat(instance.parseLine(line1), equalTo(List.of()));
        assertThat(instance.parseLine(line2), equalTo(List.of()));
    }
}