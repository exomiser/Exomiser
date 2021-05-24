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
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;
import org.monarchinitiative.svart.VariantType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DbVarFreqParserTest {

    private final DbVarFreqParser instance = new DbVarFreqParser();

    @Test
    void lineWithoutFrequencyInfoReturnsEmptyList() {
        String line = "2\t145346767\tessv17884129\tA\t<INS:ME:ALU>\t.\t.\tDBVARID;SVTYPE=INS;END=145346767;SVLEN=274;EXPERIMENT=9;SAMPLE=NA20758;REGIONID=esv3826447;AN=30;AC=3";
        String line1 = "2\t145346767\tessv17884129\tA\t<INS:ME:ALU>\t.\t.\tDBVARID;SVTYPE=INS;END=145346767;SVLEN=274;EXPERIMENT=9;SAMPLE=NA20758;REGIONID=esv3826447";
        String line2 = "2\t145346767\tessv17884129\tA\t<INS:ME:ALU>\t.\t.\tDBVARID;SVTYPE=INS;END=145346767;SVLEN=274;EXPERIMENT=9;SAMPLE=NA20758;REGIONID=esv3826447";
//        assertThat(instance.parseLine(line), equalTo(List.of()));
        assertThat(instance.parseLine(line1), equalTo(List.of()));
        assertThat(instance.parseLine(line2), equalTo(List.of()));
    }

    @Test
    public void testInsertionNoFreq() {
        List<SvFrequency> actual = instance.parseLine("1\t10726\tnssv14489056\tG\t<INS>\t.\t.\tDBVARID;SVTYPE=INS;END=10726;SVLEN=58;EXPERIMENT=1;SAMPLE=HG00268;REGIONID=nsv3320784\n");
        List<SvFrequency> expected = List.of(new SvFrequency(1, 10726, 10726, 58, VariantType.INS, "nsv3320784", "DBVAR", "nssv14489056", 1, 0));
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testMultipleRegionIds() {
        List<SvFrequency> actual = instance.parseLine("1\t757864\tessv6773385\tG\t<DEL>\t.\t.\tDBVARID;SVTYPE=DEL;IMPRECISE;END=758041;CIPOS=0,.;CIEND=.,0;SVLEN=-178;EXPERIMENT=1;SAMPLE=SSM066;REGIONID=esv2725027,esv2746029,esv2747140\n");
        List<SvFrequency> expected = List.of(new SvFrequency(1, 757864, 758041, -178, VariantType.DEL, "esv2725027", "DBVAR", "essv6773385", 1, 0));
        assertThat(actual, equalTo(expected));
    }
}