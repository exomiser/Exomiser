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

class DgvSvFreqParserTest implements SvParserTest {

    private final DgvSvFreqParser instance = new DgvSvFreqParser();

    @Test
    public void testIgnoresHeader() {
        String header = "variantaccession\tchr\tstart\tend\tvarianttype\tvariantsubtype\treference\tpubmedid\tmethod\tplatform\tmergedvariants\tsupportingvariants\tmergedorsample\tfrequency\tsamplesize\tobservedgains\tobservedlosses\tcohortdescription\tgenes\tsamples\n";
        assertThat(instance.parseLine(header), equalTo(List.of()));
    }

    @Test
    public void complex() {
        List<SvFrequency> result = instance.parseLine("nsv7879\t1\t10001\t127330\tCNV\tgain+loss\tPerry_et_al_2008\t18304495\tOligo aCGH\t\t\tnssv14786,nssv14785,nssv14773,nssv14772,nssv14781,nssv14771,nssv14775,nssv14762,nssv14764,nssv18103,nssv14766,nssv14770,nssv14777,nssv14789,nssv14782,nssv14788,nssv18117,nssv14790,nssv14791,nssv14784,nssv14776,nssv14787,nssv21423,nssv14783,nssv14763,nssv14780,nssv14774,nssv14768,nssv18113,nssv18093\tM\t\t31\t25\t1\t\tDDX11L1,FAM138A,FAM138F,MIR6859-1,MIR6859-2,OR4F5,WASH7P\tNA07029,NA07048,NA10839,NA10863,NA12155,NA12802,NA12872,NA18502,NA18504,NA18517,NA18537,NA18552,NA18563,NA18853,NA18860,NA18942,NA18972,NA18975,NA18980,NA19007,NA19132,NA19144,NA19173,NA19221,NA19240");
        List<SvFrequency> expected = List.of(
                new SvFrequency(1, 10001, 127330, 117329, VariantType.CNV_GAIN, "nsv7879", "DGV", "nsv7879", 25, 31),
                new SvFrequency(1, 10001, 127330, -117329, VariantType.DEL, "nsv7879", "DGV", "nsv7879", 1, 31)
        );
        assertThat(result, equalTo(expected));
    }

    @Test
    @Override
    public void deletion() {
        List<SvFrequency> result = instance.parseLine("dgv528n100\t1\t196738611\t196909633\tCNV\tloss\tCoe_et_al_2014\t25217958\tOligo aCGH,SNP array\t\t\tnsv1009353,nsv1007147,nsv1009472,nsv1008807,nsv1007142,nsv1001821,nsv1011169,nsv1006569,nsv1009770,nsv1014247,nsv1010429,nsv1008849,nsv1005908,nsv997733,nsv1012118,nsv1001633,nsv1011807,nsv1009949,nsv1012773,nsv1011841,nsv1002063,nsv1003214,nsv1006349,nsv1006852,nsv1010288,nsv1013698,nsv1011814,nsv1007967,nsv1012525,nsv1006085\tM\t\t29084\t0\t47\t\tCFHR1,CFHR3,CFHR4\t");
        assertThat(result, equalTo(List.of(new SvFrequency(1, 196738611, 196909633, -171022, VariantType.DEL, "nsv1009353", "DGV", "dgv528n100", 47, 29084))));
    }

//
//    @Override
//    public void insertion() {
//        SvParserTest.super.insertion();
//    }
//
//    @Override
//    public void duplication() {
//        SvParserTest.super.duplication();
//    }
//
//    @Override
//    public void inversion() {
//        SvParserTest.super.inversion();
//    }
//
//    @Override
//    public void cnvGain() {
//        SvParserTest.super.cnvGain();
//    }
//
//    @Override
//    public void cnvLoss() {
//        SvParserTest.super.cnvLoss();
//    }

//    @Override
//    public void breakend() {
//        SvParserTest.super.breakend();
//    }
}