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
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GnomadGenomeAlleleParserTest extends AbstractAlleleParserTester<GnomadGenomeAlleleParser> {

    @Override
    public GnomadGenomeAlleleParser newInstance() {
        return new GnomadGenomeAlleleParser();
    }

    @Test
    public void parseLineFailAc0Snp() throws Exception {
        String line = "1\t51459\trs77426779\tG\tA\t3097.33\tAC0\tAC=3;AF=1.38530e-04;AN=21656;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=1;AC_NFE=1;AC_OTH=1;AC_Male=1;AC_Female=2;AN_AFR=7226;AN_AMR=630;AN_ASJ=166;AN_EAS=1568;AN_FIN=1990;AN_NFE=9406;AN_OTH=670;AN_Male=12140;AN_Female=9516;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=5.02513e-04;AF_NFE=1.06315e-04;AF_OTH=1.49254e-03;AF_Male=8.23723e-05;AF_Female=2.10172e-04;GC_AFR=3613,0,0;GC_AMR=315,0,0;GC_ASJ=83,0,0;GC_EAS=784,0,0;GC_FIN=994,1,0;GC_NFE=4702,1,0;GC_OTH=334,1,0;GC_Male=6069,1,0;GC_Female=4756,2,0;AC_raw=123;AN_raw=24640;AF_raw=4.99188e-03;GC_raw=12235,47,38;GC=10825,3,0;AC_POPMAX=1;AN_POPMAX=1990;AF_POPMAX=5.02513e-04";

        assertParseLineEquals(line, List.of());
    }


    @Test
    public void parseLinePassesRfFailSnpWithOverrideValues() throws Exception {
        String line = "1\t51459\trs77426779\tG\tA\t3097.33\tRF\tAC=3;AF=1.38530e-04;AN=21656;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=1;AC_NFE=1;AC_OTH=1;AC_Male=1;AC_Female=2;AN_AFR=7226;AN_AMR=630;AN_ASJ=166;AN_EAS=1568;AN_FIN=1990;AN_NFE=9406;AN_OTH=670;AN_Male=12140;AN_Female=9516;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=5.02513e-04;AF_NFE=1.06315e-04;AF_OTH=1.49254e-03;AF_Male=8.23723e-05;AF_Female=2.10172e-04;GC_AFR=3613,0,0;GC_AMR=315,0,0;GC_ASJ=83,0,0;GC_EAS=784,0,0;GC_FIN=994,1,0;GC_NFE=4702,1,0;GC_OTH=334,1,0;GC_Male=6069,1,0;GC_Female=4756,2,0;AC_raw=123;AN_raw=24640;AF_raw=4.99188e-03;GC_raw=12235,47,38;GC=10825,3,0;AC_POPMAX=1;AN_POPMAX=1990;AF_POPMAX=5.02513e-04";

        Allele expected = new Allele(1, 51459, "G", "A");
        expected.setRsId("rs77426779");
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_FIN, 1, 1990));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_NFE, 1, 9406));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_OTH, 1, 670));

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void parseLinePassesMultipleFailSnpWithOverrideValues() {
        String line = "1\t51459\trs77426779\tG\tA\t3097.33\tRF;InbreedingCoeff\tAC=3;AF=1.38530e-04;AN=21656;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=1;AC_NFE=1;AC_OTH=1;AC_Male=1;AC_Female=2;AN_AFR=7226;AN_AMR=630;AN_ASJ=166;AN_EAS=1568;AN_FIN=1990;AN_NFE=9406;AN_OTH=670;AN_Male=12140;AN_Female=9516;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=5.02513e-04;AF_NFE=1.06315e-04;AF_OTH=1.49254e-03;AF_Male=8.23723e-05;AF_Female=2.10172e-04;GC_AFR=3613,0,0;GC_AMR=315,0,0;GC_ASJ=83,0,0;GC_EAS=784,0,0;GC_FIN=994,1,0;GC_NFE=4702,1,0;GC_OTH=334,1,0;GC_Male=6069,1,0;GC_Female=4756,2,0;AC_raw=123;AN_raw=24640;AF_raw=4.99188e-03;GC_raw=12235,47,38;GC=10825,3,0;AC_POPMAX=1;AN_POPMAX=1990;AF_POPMAX=5.02513e-04";

        Allele expected = new Allele(1, 51459, "G", "A");
        expected.setRsId("rs77426779");
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_FIN, 1, 1990));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_NFE, 1, 9406));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_OTH, 1, 670));

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void parseLineFailsMultipleFailSnpWithOverrideValues() {
        String line = "1\t51459\trs77426779\tG\tA\t3097.33\tRF;InbreedingCoeff;AC0\tAC=3;AF=1.38530e-04;AN=21656;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=1;AC_NFE=1;AC_OTH=1;AC_Male=1;AC_Female=2;AN_AFR=7226;AN_AMR=630;AN_ASJ=166;AN_EAS=1568;AN_FIN=1990;AN_NFE=9406;AN_OTH=670;AN_Male=12140;AN_Female=9516;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=5.02513e-04;AF_NFE=1.06315e-04;AF_OTH=1.49254e-03;AF_Male=8.23723e-05;AF_Female=2.10172e-04;GC_AFR=3613,0,0;GC_AMR=315,0,0;GC_ASJ=83,0,0;GC_EAS=784,0,0;GC_FIN=994,1,0;GC_NFE=4702,1,0;GC_OTH=334,1,0;GC_Male=6069,1,0;GC_Female=4756,2,0;AC_raw=123;AN_raw=24640;AF_raw=4.99188e-03;GC_raw=12235,47,38;GC=10825,3,0;AC_POPMAX=1;AN_POPMAX=1990;AF_POPMAX=5.02513e-04";

        assertParseLineEquals(line, List.of());
    }

    @Test
    public void parseLinePassSnp() throws Exception {
        String line = "1\t51420\t.\tC\tG\t235.66\tPASS\tAC=1;AF=4.78286e-05;AN=20908;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=0;AC_NFE=1;AC_OTH=0;AC_Male=0;AC_Female=1;AN_AFR=7080;AN_AMR=588;AN_ASJ=158;AN_EAS=1570;AN_FIN=1888;AN_NFE=8998;AN_OTH=626;AN_Male=11720;AN_Female=9188;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=0.00000e+00;AF_NFE=1.11136e-04;AF_OTH=0.00000e+00;AF_Male=0.00000e+00;AF_Female=1.08838e-04;GC_AFR=3540,0,0;GC_AMR=294,0,0;GC_ASJ=79,0,0;GC_EAS=785,0,0;GC_FIN=944,0,0;GC_NFE=4498,1,0;GC_OTH=313,0,0;GC_Male=5860,0,0;GC_Female=4593,1,0;AC_raw=1;AN_raw=25880;AF_raw=3.86399e-05;GC_raw=12939,1,0;GC=10453,1,0;AC_POPMAX=1;AN_POPMAX=8998;AF_POPMAX=1.11136e-04";

        Allele expected = new Allele(1, 51420, "C", "G");
        //AC_NFE=1/AN_NFE=8998 *100
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_NFE, 1, 8998));

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void parseCommonSnp() {
        String line = "1\t96594\trs551871856\tC\tT\t65932.1\tPASS\tAC=148;AF=6.41748e-03;AN=23062;AC_AFR=4;AC_AMR=1;AC_ASJ=0;AC_EAS=2;AC_FIN=11;AC_NFE=124;AC_OTH=6;AC_Male=82;AC_Female=66;AN_AFR=7762;AN_AMR=682;AN_ASJ=186;AN_EAS=1606;AN_FIN=2118;AN_NFE=10000;AN_OTH=708;AN_Male=12890;AN_Female=10172;AF_AFR=5.15331e-04;AF_AMR=1.46628e-03;AF_ASJ=0.00000e+00;AF_EAS=1.24533e-03;AF_FIN=5.19358e-03;AF_NFE=1.24000e-02;AF_OTH=8.47458e-03;AF_Male=6.36152e-03;AF_Female=6.48840e-03;GC_AFR=3877,4,0;GC_AMR=340,1,0;GC_ASJ=93,0,0;GC_EAS=801,2,0;GC_FIN=1048,11,0;GC_NFE=4901,74,25;GC_OTH=349,4,1;GC_Male=6378,52,15;GC_Female=5031,44,11;AC_raw=171;AN_raw=25478;AF_raw=6.71167e-03;GC_raw=12602,103,34;GC=11409,96,26;AC_POPMAX=124;AN_POPMAX=10000;AF_POPMAX=1.24000e-02";

        Allele expected = new Allele(1, 96594, "C", "T");
        expected.setRsId("rs551871856");
//        AC_AFR=4;AC_AMR=1;AC_ASJ=0;AC_EAS=2;AC_FIN=11;AC_NFE=124;AC_OTH=6
//        AN_AFR=7762;AN_AMR=682;AN_ASJ=186;AN_EAS=1606;AN_FIN=2118;AN_NFE=10000;AN_OTH=708
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_AFR, 4, 7762));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_AMR, 1, 682));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_EAS, 2, 1606));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_FIN, 11, 2118));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_NFE, 124, 10000));
        expected.addFrequency(Allele.buildFrequency(GNOMAD_G_OTH, 6, 708));

        assertParseLineEquals(line, List.of(expected));
    }

}