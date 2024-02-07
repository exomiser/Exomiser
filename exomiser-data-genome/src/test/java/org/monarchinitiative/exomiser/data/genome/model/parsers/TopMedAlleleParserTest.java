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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.TOPMED;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TopMedAlleleParserTest extends AbstractAlleleParserTester<TopMedAlleleParser> {

    @Override
    public TopMedAlleleParser newInstance() {
        return new TopMedAlleleParser();
    }

    @Test
    public void parseOne() throws Exception {
        String line = "1\t1669523\trs185801647\tC\tT\t.\t.\tTOPMED=0.000206058";

        Allele expected = new Allele(1, 1669523, "C", "T");
        expected.setRsId("rs185801647");
        expected.addFrequency(AlleleData.frequencyOf(TOPMED, 0.0206058f));

        List<Allele> alleles = parseLine(line);

        assertThat(alleles.size(), equalTo(1));

        Allele allele = alleles.get(0);
        assertThat(allele, equalTo(expected));
        assertThat(allele.getValues(), equalTo(expected.getValues()));
    }

    @Test
    public void parseMultiAllele() throws Exception {
        String line = "1\t1669549\trs904496209\tG\tA,C\t.\t.\tTOPMED=3.4343e-005,0.000103029";

        Allele expected1 = new Allele(1, 1669549, "G", "A");
        expected1.setRsId("rs904496209");
        expected1.addFrequency(AlleleData.frequencyOf(TOPMED, 3.4343e-003f));

        Allele expected2 = new Allele(1, 1669549, "G", "C");
        expected2.setRsId("rs904496209");
        expected2.addFrequency(AlleleData.frequencyOf(TOPMED, 0.0103029f));

        List<Allele> expectedAlleles = Arrays.asList(expected1, expected2);

        assertParseLineEquals(line, expectedAlleles);
    }


    @Test
    public void parseMultiAlleleOneMissingFreqDot() throws Exception {
        String line = "1\t1668890\trs367918436\tC\tG,T\t.\t.\tTOPMED=3.43643e-005,.";

        Allele expected1 = new Allele(1, 1668890, "C", "G");
        expected1.setRsId("rs367918436");
        expected1.addFrequency(AlleleData.frequencyOf(TOPMED, 0.0034364301f));

        Allele expected2 = new Allele(1, 1668890, "C", "T");
        expected2.setRsId("rs367918436");

        List<Allele> expectedAlleles = Arrays.asList(expected1, expected2);

        assertParseLineEquals(line, expectedAlleles);
    }

    @Test
    public void parseMultiAlleleOneMissingFreqWithMissingDot() throws Exception {
//        2       202593315       rs587777132     G       A,C,T   .       .       TOPMED=,6.8686e-005,.
        String line = "2\t202593315\trs587777132\tG\tA,C,T\t.\t.\tTOPMED=,6.8686e-005,.";
        Allele expected1 = new Allele(2, 202593315, "G", "A");
        expected1.setRsId("rs587777132");

        Allele expected2 = new Allele(2, 202593315, "G", "C");
        expected2.setRsId("rs587777132");
        expected2.addFrequency(AlleleData.frequencyOf(TOPMED, 6.8686e-003f));

        Allele expected3 = new Allele(2, 202593315, "G", "T");
        expected3.setRsId("rs587777132");

        List<Allele> expectedAlleles = Arrays.asList(expected1, expected2, expected3);

        assertParseLineEquals(line, expectedAlleles);
    }

    @Test
    public void onlyEmptyOrMissingFreqs() throws Exception {
//        17      10599057        rs587776629     CTC     C,CAG,CGA       .       .       TOPMED=.,.,
        String line = "17\t10599057\trs587776629\tCTC\tC,CAG,CGA\t.\t.\tTOPMED=.,.,";

        Allele expected1 = new Allele(17, 10599057, "CTC", "C");
        expected1.setRsId("rs587776629");
        Allele expected2 = new Allele(17, 10599058, "TC", "AG");
        expected2.setRsId("rs587776629");
        Allele expected3 = new Allele(17, 10599058, "TC", "GA");
        expected3.setRsId("rs587776629");

        List<Allele> expectedAlleles = Arrays.asList(expected1, expected2, expected3);

        assertParseLineEquals(line, expectedAlleles);
    }

    @Test
    public void finalFreqMissingValue() throws Exception {
//        13      86928760        rs201161694     AGA     A,AA    .       .       TOPMED=0.990109,
        String line = "13\t86928760\trs201161694\tAGA\tA,AA\t.\t.\tTOPMED=0.990109,";

        Allele expected1 = new Allele(13, 86928760, "AGA", "A");
        expected1.setRsId("rs201161694");
        expected1.addFrequency(AlleleData.frequencyOf(TOPMED, 99.0109f));

        Allele expected2 = new Allele(13, 86928760, "AG", "A");
        expected2.setRsId("rs201161694");

        List<Allele> expectedAlleles = Arrays.asList(expected1, expected2);

        assertParseLineEquals(line, expectedAlleles);
    }

    @Test
    public void parseOneMissingFreq() throws Exception {
        String line = "1\t2045245\trs1038362304\tCAGGTGGGTGGT\tC\t.\t.\tTOPMED=";

        Allele expected = new Allele(1, 2045245, "CAGGTGGGTGGT", "C");
        expected.setRsId("rs1038362304");

        assertParseLineEquals(line, Collections.singletonList(expected));
    }

    @Test
    public void parseOneIncorrectValue() throws Exception {
        String line = "1\t2045245\trs1038362304\tCAGGTGGGTGGT\tC\t.\t.\tTOPMED=wibble";

        Allele expected = new Allele(1, 2045245, "CAGGTGGGTGGT", "C");
        expected.setRsId("rs1038362304");

        assertParseLineEquals(line, Collections.singletonList(expected));
    }

}
