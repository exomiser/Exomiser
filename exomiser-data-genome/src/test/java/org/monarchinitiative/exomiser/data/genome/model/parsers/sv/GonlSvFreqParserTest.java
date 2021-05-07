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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;
import org.monarchinitiative.svart.VariantType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

// datasource: 20161013_GoNL_AF_genotyped_SVs.vcf.gz
public class GonlSvFreqParserTest {

    private final GonlSvFreqParser instance = new GonlSvFreqParser();

    @Nested
    public class symbolicTests implements SvParserTest {
        @Test
        @Override
        public void deletion() {
            List<SvFrequency> result = instance.parseLine("19\t27756483\t.\tN\t<DEL>\t.\tPASS\tAC=3;AF=0.0019505851755526658;AN=1538;DISCOVERY=123SV,BD,DWACSEQ,GSTRIP;GENOTYPING=MATE-CLEVER;SVLEN=-11256;SVTYPE=DEL\n");
            assertThat(result, equalTo(List.of(new SvFrequency(19, 27756483, 27767739, -11256, VariantType.DEL, "", "GONL", "", 3, 1538))));
        }

        @Override
        public void insertion() {
            // See parameterized tests
            assertTrue(true);
        }

        @ParameterizedTest
        @CsvSource({
                "'AC=1;AF=0.0006510416666666666;AN=1536;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=L1;MEIEVIDENCE=1;MEITYPE=Unknown_L1;SVTYPE=INS:ME',  INS_ME_LINE1, 6000",
                "'AC=1;AF=0.001953125;AN=1536;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=SVA;MEIEVIDENCE=1;MEITYPE=Unknown_SVA;SVTYPE=INS:ME',  INS_ME_SVA, 2000",
                "'AC=1;AF=0.0034965034965034965;AN=1536;CIPOS=-1,0;DISCOVERY=MOBSTER;GENOTYPING=MOBSTER;MEICLASS=ALU;MEIEVIDENCE=1;MEITYPE=AluSq2;SVTYPE=INS:ME',  INS_ME_ALU, 300",
        })
        public void insertion(String info, VariantType variantType, int svlen) {
            List<SvFrequency> result = instance.parseLine("22\t29012405\t.\tN\t<INS:ME>\t.\tPASS\t" + info + "\n");
            assertThat(result, equalTo(List.of(new SvFrequency(22, 29012405, 29012405, svlen, variantType, "", "GONL", "", 1, 1536))));
        }


        @Test
        @Override
        public void duplication() {
            List<SvFrequency> result = instance.parseLine("2\t89862475\t.\tN\t<DUP>\t.\tPASS\tAC=2;AF=0.00133511348465;AN=1498;CHR2=2;CIEND=-15,15;CIPOS=-15,15;CT=5to3;DISCOVERY=123SV,BD,DWACSEQ;END=89869794;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;MAPQ=60;PE=39;SVLEN=7319;SVTYPE=DUP\n");
            assertThat(result, equalTo(List.of(new SvFrequency(2, 89862475, 89869794, 7319, VariantType.DUP, "", "GONL", "", 2, 1498))));
        }

        @Test
        @Override
        public void inversion() {
            List<SvFrequency> result = instance.parseLine("1\t44058936\t.\tN\t<INV>\t.\tPASS\t1000G_ID=INV_delly_INV00003623;AC=1418;AF=1.0;AN=1418;CHR2=1;CIEND=-423,423;CIPOS=-423,423;CT=3to3;DGV_ID=esv7506;DGV_PUBMED=19470904;DGV_SOURCE=Ahn_et_al_2009;DISCOVERY=123SV,ASM,BD;END=44059940;GENOTYPING=EMBL.DELLYv0.5.9;IMPRECISE;KNOWN=DGV,1000G,CHM1;MAPQ=60;PE=102;SVLEN=1004;SVTYPE=INV;VALIDATED;VALIDATION_SAMPLE=A105c\n");
            assertThat(result, equalTo(List.of(new SvFrequency(1, 44058936, 44059940, 1004, VariantType.INV, "esv7506", "GONL", "", 1418, 1418))));
        }

        @Test
        @Override
        public void cnvGain() {
            // not present in dataset
            assertTrue(true);
        }

        @Test
        @Override
        public void cnvLoss() {
            // not present in dataset
            assertTrue(true);
        }

        @Test
        @Override
        public void breakend() {
            // not present in dataset
            assertTrue(true);
        }
    }

    @Nested
    public class preciseTests implements SvParserTest {

        @Test
        @Override
        public void deletion() {
            List<SvFrequency> result = instance.parseLine("1\t2775802\t.\tGAAGGAGAGGAAGAAAGGAAGGGAGGCAAGAAGGAAGGAAGCAGGGAGGAAGGAAGGAGGAAGGAGGAAGGAAGGAAGA\tG\t.\tPASS\tAC=660;AF=0.4558011049723757;AN=1448;DGV_ID=esv3561426;DGV_PUBMED=23714750;DGV_SOURCE=Boomsma_et_al_2014;DISCOVERY=CLEVER,PINDEL;GENOTYPING=MATE-CLEVER;KNOWN=DGV,GONLr5,CHM1;SVLEN=-78;SVTYPE=DEL\n");
            assertThat(result, equalTo(List.of(new SvFrequency(1, 2775802, 2775880, -78, VariantType.DEL, "esv3561426", "GONL", "", 660, 1448))));
        }

        @Test
        @Override
        public void insertion() {
            // n.b. this is hypothetical - there are no precise insertions in the dataset
            List<SvFrequency> result = instance.parseLine("1\t2775802\t.\tG\tGAAGGAGAGGAAGAAAGGAAGGGAGGCAAGAAGGAAGGAAGCAGGGAGGAAGGAAGGAGGAAGGAGGAAGGAAGGAAGA\t.\tPASS\tAC=660;AF=0.4558011049723757;AN=1448;DGV_ID=esv3561426;DGV_PUBMED=23714750;DGV_SOURCE=Boomsma_et_al_2014;DISCOVERY=CLEVER,PINDEL;GENOTYPING=MATE-CLEVER;KNOWN=DGV,GONLr5,CHM1;SVLEN=78;SVTYPE=INS\n");
            assertThat(result, equalTo(List.of(new SvFrequency(1, 2775802, 2775802, 78, VariantType.INS, "esv3561426", "GONL", "", 660, 1448))));
        }

        @Test
        @Override
        public void duplication() {
            // not present in dataset
            assertTrue(true);
        }

        @Test
        @Override
        public void inversion() {
            // not present in dataset
            assertTrue(true);
        }

        @Test
        @Override
        public void cnvGain() {
            // not present in dataset
            assertTrue(true);
        }

        @Test
        @Override
        public void cnvLoss() {
            // not present in dataset
            assertTrue(true);
        }

        @Test
        @Override
        public void breakend() {
            // not present in dataset
            assertTrue(true);
        }
    }

}