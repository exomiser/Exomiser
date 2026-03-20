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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspHg38AlleleParserTest extends AbstractAlleleParserTester<EspHg38AlleleParser> {

    @Override
    public EspHg38AlleleParser newInstance() {
        return new EspHg38AlleleParser();
    }

    @Test
    public void parseHeaderLine() throws Exception {
        String line = "##fileformat=VCFv4.1";
        assertParseLineEquals(line, List.of());
    }

    @Test
    public void parseUnmappableHg38PositionLine() throws Exception {
        //##INFO=<ID=GRCh38_POSITION,Number=.,Type=String,Description="GRCh38 chromosomal postion liftover from the original GRCh37 chromosomal position. A value of -1 means the GRCh37 position can not be mapped to the GRCh38 build.">
        String line = "1\t12345\t.\tT\tA\t.\tPASS\tGRCh38_POSITION=-1";

        assertParseLineEquals(line, List.of());
    }

    @Test
    public void parseAlternateScaffoldHg38PositionLine() throws Exception {
        //##INFO=<ID=GRCh38_POSITION,Number=.,Type=String,Description="GRCh38 chromosomal postion liftover from the original GRCh37 chromosomal position. A value of -1 means the GRCh37 position can not be mapped to the GRCh38 build.">
        String line = "1\t12345\t.\tT\tA\t.\tPASS\tGRCh38_POSITION=7_KI270803v1_alt:466346";

        assertParseLineEquals(line, List.of());
    }

    @Test
    public void parseHg38PositionLine() throws Exception {
        //##INFO=<ID=GRCh38_POSITION,Number=.,Type=String,Description="GRCh38 chromosomal postion liftover from the original GRCh37 chromosomal position. A value of -1 means the GRCh37 position can not be mapped to the GRCh38 build.">
        String line = "17\t5994\trs375149461\tG\tA\t.\tPASS\tDBSNP=dbSNP_138;EA_AC=20,3162;AA_AC=1,1383;TAC=21,4545;MAF=0.6285,0.0723,0.4599;GTS=AA,AG,GG;EA_GTC=1,18,1572;AA_GTC=0,1,691;GTC=1,19,2263;DP=58;GL=.;CP=0.0;CG=-1.0;AA=A;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=near-gene-3;HGVS_CDNA_VAR=.;HGVS_PROTEIN_VAR=.;CDS_SIZES=.;GS=.;PH=.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=17:156203";

        Allele expected = new Allele(17, 156203, "G", "A");
        expected.setRsId("rs375149461");
        expected.addFrequency(AlleleData.frequencyOf(ESP_EA, 20, 3182));
        expected.addFrequency(AlleleData.frequencyOf(ESP_AA, 1, 1384));
        expected.addFrequency(AlleleData.frequencyOf(ESP_ALL, 21, 4566));

        assertParseLineEquals(line, List.of(expected));
    }
}