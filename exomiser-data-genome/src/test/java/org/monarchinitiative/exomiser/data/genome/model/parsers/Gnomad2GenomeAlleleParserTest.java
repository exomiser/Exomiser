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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Gnomad2GenomeAlleleParserTest {

    @Nested
    class GnomadV2_0_FormatTest extends AbstractAlleleParserTester<GnomadAlleleParser> {

        @Override
        public GnomadAlleleParser newInstance() {
            return new GnomadParser(GnomadPopulationKey.GNOMAD_V2_0_GENOMES);
        }

        private static class GnomadParser extends GnomadAlleleParser {

            public GnomadParser(List<GnomadPopulationKey> populationKeys) {
                super(populationKeys);
            }
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
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_FIN, 1, 1990));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_NFE, 1, 9406));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_OTH, 1, 670));

            assertParseLineEquals(line, List.of(expected));
        }

        @Test
        public void parseLinePassesMultipleFailSnpWithOverrideValues() {
            String line = "1\t51459\trs77426779\tG\tA\t3097.33\tRF;InbreedingCoeff\tAC=3;AF=1.38530e-04;AN=21656;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=1;AC_NFE=1;AC_OTH=1;AC_Male=1;AC_Female=2;AN_AFR=7226;AN_AMR=630;AN_ASJ=166;AN_EAS=1568;AN_FIN=1990;AN_NFE=9406;AN_OTH=670;AN_Male=12140;AN_Female=9516;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=5.02513e-04;AF_NFE=1.06315e-04;AF_OTH=1.49254e-03;AF_Male=8.23723e-05;AF_Female=2.10172e-04;GC_AFR=3613,0,0;GC_AMR=315,0,0;GC_ASJ=83,0,0;GC_EAS=784,0,0;GC_FIN=994,1,0;GC_NFE=4702,1,0;GC_OTH=334,1,0;GC_Male=6069,1,0;GC_Female=4756,2,0;AC_raw=123;AN_raw=24640;AF_raw=4.99188e-03;GC_raw=12235,47,38;GC=10825,3,0;AC_POPMAX=1;AN_POPMAX=1990;AF_POPMAX=5.02513e-04";

            Allele expected = new Allele(1, 51459, "G", "A");
            expected.setRsId("rs77426779");
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_FIN, 1, 1990));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_NFE, 1, 9406));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_OTH, 1, 670));

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
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_NFE, 1, 8998));

            assertParseLineEquals(line, List.of(expected));
        }

        @Test
        public void parseCommonSnp() {
            String line = "1\t96594\trs551871856\tC\tT\t65932.1\tPASS\tAC=148;AF=6.41748e-03;AN=23062;AC_AFR=4;AC_AMR=1;AC_ASJ=0;AC_EAS=2;AC_FIN=11;AC_NFE=124;AC_OTH=6;AC_Male=82;AC_Female=66;AN_AFR=7762;AN_AMR=682;AN_ASJ=186;AN_EAS=1606;AN_FIN=2118;AN_NFE=10000;AN_OTH=708;AN_Male=12890;AN_Female=10172;AF_AFR=5.15331e-04;AF_AMR=1.46628e-03;AF_ASJ=0.00000e+00;AF_EAS=1.24533e-03;AF_FIN=5.19358e-03;AF_NFE=1.24000e-02;AF_OTH=8.47458e-03;AF_Male=6.36152e-03;AF_Female=6.48840e-03;GC_AFR=3877,4,0;GC_AMR=340,1,0;GC_ASJ=93,0,0;GC_EAS=801,2,0;GC_FIN=1048,11,0;GC_NFE=4901,74,25;GC_OTH=349,4,1;GC_Male=6378,52,15;GC_Female=5031,44,11;AC_raw=171;AN_raw=25478;AF_raw=6.71167e-03;GC_raw=12602,103,34;GC=11409,96,26;AC_POPMAX=124;AN_POPMAX=10000;AF_POPMAX=1.24000e-02";

            Allele expected = new Allele(1, 96594, "C", "T");
            expected.setRsId("rs551871856");
//        AC_AFR=4;AC_AMR=1;AC_ASJ=0;AC_EAS=2;AC_FIN=11;AC_NFE=124;AC_OTH=6
//        AN_AFR=7762;AN_AMR=682;AN_ASJ=186;AN_EAS=1606;AN_FIN=2118;AN_NFE=10000;AN_OTH=708
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_AFR, 4, 7762));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_AMR, 1, 682));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_EAS, 2, 1606));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_FIN, 11, 2118));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_NFE, 124, 10000));
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_OTH, 6, 708));

            assertParseLineEquals(line, List.of(expected));
        }
    }

    @Nested
    class Gnomad_V2_1_FormatTest extends AbstractAlleleParserTester<Gnomad2GenomeAlleleParser> {

        @Override
        public Gnomad2GenomeAlleleParser newInstance() {
            return new Gnomad2GenomeAlleleParser();
        }

        @Test
        void testPassLine() {
            String line = "chrY\t2781554\t.\tG\tA\t.\tPASS\tAC=1;AN=10643;AF=9.39585e-05;popmax=nfe;faf95_popmax=0.00000;AC_non_neuro_nfe=1;AN_non_neuro_nfe=5088;AF_non_neuro_nfe=0.000196541;nhomalt_non_neuro_n" +
                    "fe=0;AC_non_neuro_afr_XY=0;AN_non_neuro_afr_XY=1897;AF_non_neuro_afr_XY=0.00000;nhomalt_non_neuro_afr_XY=0;AC_non_neuro_nfe_XY=1;AN_non_neuro_nfe_XY=5088;AF_non_neuro_nfe_XY=0.000196541;nhomalt_non_neuro_nf" +
                    "e_XY=0;AC_controls_and_biobanks_eas_XY=0;AN_controls_and_biobanks_eas_XY=190;AF_controls_and_biobanks_eas_XY=0.00000;nhomalt_controls_and_biobanks_eas_XY=0;AC_non_v2=1;AN_non_v2=8012;AF_non_v2=0.000124813;n" +
                    "homalt_non_v2=0;AC_non_v2_mid=0;AN_non_v2_mid=25;AF_non_v2_mid=0.00000;nhomalt_non_v2_mid=0;AC_non_topmed_sas=0;AN_non_topmed_sas=335;AF_non_topmed_sas=0.00000;nhomalt_non_topmed_sas=0;AC_amr_XY=0;AN_amr_XY" +
                    "=957;AF_amr_XY=0.00000;nhomalt_amr_XY=0;AC_controls_and_biobanks_XY=1;AN_controls_and_biobanks_XY=2157;AF_controls_and_biobanks_XY=0.000463607;nhomalt_controls_and_biobanks_XY=0;AC_non_neuro_asj_XY=0;AN_non" +
                    "_neuro_asj_XY=334;AF_non_neuro_asj_XY=0.00000;nhomalt_non_neuro_asj_XY=0;AC_oth=0;AN_oth=140;AF_oth=0.00000;nhomalt_oth=0;AC_non_topmed_mid_XY=0;AN_non_topmed_mid_XY=21;AF_non_topmed_mid_XY=0.00000;nhomalt_" +
                    "non_topmed_mid_XY=0;AC_sas_XY=0;AN_sas_XY=340;AF_sas_XY=0.00000;nhomalt_sas_XY=0;AC_non_neuro_fin=0;AN_non_neuro_fin=401;AF_non_neuro_fin=0.00000;nhomalt_non_neuro_fin=0;AC_non_topmed_amr_XY=0;AN_non_topmed" +
                    "_amr_XY=795;AF_non_topmed_amr_XY=0.00000;nhomalt_non_topmed_amr_XY=0;AC_non_v2_raw=4;AN_non_v2_raw=24811;AF_non_v2_raw=0.000161219;nhomalt_non_v2_raw=0;AC_non_v2_asj=0;AN_non_v2_asj=311;AF_non_v2_asj=0.0000" +
                    "0;nhomalt_non_v2_asj=0;AC_controls_and_biobanks_raw=2;AN_controls_and_biobanks_raw=8608;AF_controls_and_biobanks_raw=0.000232342;nhomalt_controls_and_biobanks_raw=0;AC_controls_and_biobanks_ami=0;AN_control" +
                    "s_and_biobanks_ami=6;AF_controls_and_biobanks_ami=0.00000;nhomalt_controls_and_biobanks_ami=0;AC_non_topmed_eas=0;AN_non_topmed_eas=287;AF_non_topmed_eas=0.00000;nhomalt_non_topmed_eas=0;AC_non_v2_amr=0;AN_" +
                    "non_v2_amr=850;AF_non_v2_amr=0.00000;nhomalt_non_v2_amr=0;AC_non_neuro_sas=0;AN_non_neuro_sas=339;AF_non_neuro_sas=0.00000;nhomalt_non_neuro_sas=0;AC_non_cancer_fin_XY=0;AN_non_cancer_fin_XY=570;AF_non_canc" +
                    "er_fin_XY=0.00000;nhomalt_non_cancer_fin_XY=0;AC_non_cancer_nfe_XY=1;AN_non_cancer_nfe_XY=4827;AF_non_cancer_nfe_XY=0.000207168;nhomalt_non_cancer_nfe_XY=0;AC_non_v2_oth=0;AN_non_v2_oth=119;AF_non_v2_oth=0." +
                    "00000;nhomalt_non_v2_oth=0;AC_ami=0;AN_ami=91;AF_ami=0.00000;nhomalt_ami=0;AC_non_cancer_XY=1;AN_non_cancer_XY=9969;AF_non_cancer_XY=0.000100311;nhomalt_non_cancer_XY=0;AC_non_v2_sas=0;AN_non_v2_sas=246;AF_" +
                    "non_v2_sas=0.00000;nhomalt_non_v2_sas=0;AC_sas=0;AN_sas=340;AF_sas=0.00000;nhomalt_sas=0;AC_ami_XY=0;AN_ami_XY=91;AF_ami_XY=0.00000;nhomalt_ami_XY=0;AC_non_cancer_eas=0;AN_non_cancer_eas=356;AF_non_cancer_e" +
                    "as=0.00000;nhomalt_non_cancer_eas=0;AC_non_topmed_XY=0;AN_non_topmed_XY=5324;AF_non_topmed_XY=0.00000;nhomalt_non_topmed_XY=0;AC_non_v2_ami=0;AN_non_v2_ami=91;AF_non_v2_ami=0.00000;nhomalt_non_v2_ami=0;AC_n" +
                    "on_neuro=1;AN_non_neuro=9629;AF_non_neuro=0.000103853;nhomalt_non_neuro=0;AC_controls_and_biobanks_nfe_XY=1;AN_controls_and_biobanks_nfe_XY=590;AF_controls_and_biobanks_nfe_XY=0.00169492;nhomalt_controls_an" +
                    "d_biobanks_nfe_XY=0;AC_controls_and_biobanks_eas=0;AN_controls_and_biobanks_eas=190;AF_controls_and_biobanks_eas=0.00000;nhomalt_controls_and_biobanks_eas=0;AC_non_cancer_oth_XY=0;AN_non_cancer_oth_XY=124;A" +
                    "F_non_cancer_oth_XY=0.00000;nhomalt_non_cancer_oth_XY=0;AC_non_v2_XY=1;AN_non_v2_XY=8012;AF_non_v2_XY=0.000124813;nhomalt_non_v2_XY=0;AC_fin=0;AN_fin=570;AF_fin=0.00000;nhomalt_fin=0;AC_controls_and_biobank" +
                    "s_afr=0;AN_controls_and_biobanks_afr=454;AF_controls_and_biobanks_afr=0.00000;nhomalt_controls_and_biobanks_afr=0;AC_non_topmed_mid=0;AN_non_topmed_mid=21;AF_non_topmed_mid=0.00000;nhomalt_non_topmed_mid=0;" +
                    "AC_non_cancer_sas_XY=0;AN_non_cancer_sas_XY=335;AF_non_cancer_sas_XY=0.00000;nhomalt_non_cancer_sas_XY=0;AC_non_topmed=0;AN_non_topmed=5324;AF_non_topmed=0.00000;nhomalt_non_topmed=0;AC_non_neuro_ami_XY=0;A" +
                    "N_non_neuro_ami_XY=89;AF_non_neuro_ami_XY=0.00000;nhomalt_non_neuro_ami_XY=0;AC_controls_and_biobanks_afr_XY=0;AN_controls_and_biobanks_afr_XY=454;AF_controls_and_biobanks_afr_XY=0.00000;nhomalt_controls_an" +
                    "d_biobanks_afr_XY=0;AC_non_topmed_amr=0;AN_non_topmed_amr=795;AF_non_topmed_amr=0.00000;nhomalt_non_topmed_amr=0;AC_controls_and_biobanks_amr=0;AN_controls_and_biobanks_amr=302;AF_controls_and_biobanks_amr=" +
                    "0.00000;nhomalt_controls_and_biobanks_amr=0;AC_non_cancer_raw=4;AN_non_cancer_raw=33333;AF_non_cancer_raw=0.000120001;nhomalt_non_cancer_raw=0;AC_non_neuro_mid=0;AN_non_neuro_mid=26;AF_non_neuro_mid=0.00000" +
                    ";nhomalt_non_neuro_mid=0;AC_non_v2_asj_XY=0;AN_non_v2_asj_XY=311;AF_non_v2_asj_XY=0.00000;nhomalt_non_v2_asj_XY=0;AC_non_v2_afr=0;AN_non_v2_afr=1661;AF_non_v2_afr=0.00000;nhomalt_non_v2_afr=0;AC_non_neuro_f" +
                    "in_XY=0;AN_non_neuro_fin_XY=401;AF_non_neuro_fin_XY=0.00000;nhomalt_non_neuro_fin_XY=0;AC_non_cancer_afr=0;AN_non_cancer_afr=2393;AF_non_cancer_afr=0.00000;nhomalt_non_cancer_afr=0;AC_non_topmed_sas_XY=0;AN" +
                    "_non_topmed_sas_XY=335;AF_non_topmed_sas_XY=0.00000;nhomalt_non_topmed_sas_XY=0;AC_mid_XY=0;AN_mid_XY=26;AF_mid_XY=0.00000;nhomalt_mid_XY=0;AC_non_v2_oth_XY=0;AN_non_v2_oth_XY=119;AF_non_v2_oth_XY=0.00000;n" +
                    "homalt_non_v2_oth_XY=0;AC_controls_and_biobanks_fin=0;AN_controls_and_biobanks_fin=293;AF_controls_and_biobanks_fin=0.00000;nhomalt_controls_and_biobanks_fin=0;AC_non_neuro_eas_XY=0;AN_non_neuro_eas_XY=392;" +
                    "AF_non_neuro_eas_XY=0.00000;nhomalt_non_neuro_eas_XY=0;AC_non_cancer_ami=0;AN_non_cancer_ami=91;AF_non_cancer_ami=0.00000;nhomalt_non_cancer_ami=0;AC_XY=1;AN_XY=10643;AF_XY=9.39585e-05;nhomalt_XY=0;" +
                    "AC_non_topmed_eas_XY=0;AN_non_topmed_eas_XY=287;AF_non_topmed_eas_XY=0.00000;nhomalt_non_topmed_eas_XY=0;AC_non_v2_eas_XY=0;AN_non_v2_eas_XY=191;AF_non_v2_eas_XY=0.00000;nhomalt_non_v2_eas_XY=0;AC_eas=0;" +
                    "AN_eas=392;AF_eas=0.00000;nhomalt_eas=0;AC_asj_XY=0;AN_asj_XY=339;AF_asj_XY=0.00000;nhomalt_asj_XY=0;AC_controls_and_biobanks_mid_XY=0;AN_controls_and_biobanks_mid_XY=18;AF_controls_and_biobanks_mid_XY=0.00000;" +
                    "nhomalt_controls_and_biobanks_mid_XY=0;AC_fin_XY=0;AN_fin_XY=570;AF_fin_XY=0.00000;nhomalt_fin_XY=0;AC_non_topmed_nfe=0;AN_non_topmed_nfe=1829;AF_non_topmed_nfe=0.00000;nhomalt_non_topmed_nfe=0;AC_amr=0;AN_amr=957;" +
                    "AF_amr=0.00000;nhomalt_amr=0;AC_non_neuro_ami=0;AN_non_neuro_ami=89;AF_non_neuro_ami=0.00000;nhomalt_non_neuro_ami=0;AC_non_cancer_mid=0;AN_non_cancer_mid=24;AF_non_cancer_mid=0.00000;nhomalt_non_cancer_mid=0;" +
                    "AC_non_v2_mid_XY=0;AN_non_v2_mid_XY=25;AF_non_v2_mid_XY=0.00000;nhomalt_non_v2_mid_XY=0;AC_controls_and_biobanks_amr_XY=0;AN_controls_and_biobanks_amr_XY=302;AF_controls_and_biobanks_amr_XY=0.00000;" +
                    "nhomalt_controls_and_biobanks_amr_XY=0;AC_non_cancer_ami_XY=0;AN_non_cancer_ami_XY=91;AF_non_cancer_ami_XY=0.00000;nhomalt_non_cancer_ami_XY=0;AC_afr=0;AN_afr=2423;AF_afr=0.00000;nhomalt_afr=0;AC_non_cancer_sas=0;" +
                    "AN_non_cancer_sas=335;AF_non_cancer_sas=0.00000;nhomalt_non_cancer_sas=0;AC_non_topmed_fin=0;AN_non_topmed_fin=565;AF_non_topmed_fin=0.00000;nhomalt_non_topmed_fin=0;AC_non_cancer_asj_XY=0;AN_non_cancer_asj_XY=325;" +
                    "AF_non_cancer_asj_XY=0.00000;nhomalt_non_cancer_asj_XY=0;AC_non_cancer_mid_XY=0;AN_non_cancer_mid_XY=24;AF_non_cancer_mid_XY=0.00000;nhomalt_non_cancer_mid_XY=0;AC_raw=4;AN_raw=34526;AF_raw=0.000115855;nhomalt_raw=0;" +
                    "AC_eas_XY=0;AN_eas_XY=392;AF_eas_XY=0.00000;nhomalt_eas_XY=0;AC_controls_and_biobanks_mid=0;AN_controls_and_biobanks_mid=18;AF_controls_and_biobanks_mid=0.00000;nhomalt_controls_and_biobanks_mid=0;AC_non_v2_nfe_XY=1;" +
                    "AN_non_v2_nfe_XY=4058;AF_non_v2_nfe_XY=0.000246427;nhomalt_non_v2_nfe_XY=0;AC_controls_and_biobanks_sas=0;AN_controls_and_biobanks_sas=250;AF_controls_and_biobanks_sas=0.00000;nhomalt_controls_and_biobanks_sas=0;" +
                    "AC_non_v2_eas=0;AN_non_v2_eas=191;AF_non_v2_eas=0.00000;nhomalt_non_v2_eas=0;AC_mid=0;AN_mid=26;AF_mid=0.00000;nhomalt_mid=0;AC_oth_XY=0;AN_oth_XY=140;AF_oth_XY=0.00000;nhomalt_oth_XY=0;AC_non_cancer_nfe=1;" +
                    "AN_non_cancer_nfe=4827;AF_non_cancer_nfe=0.000207168;nhomalt_non_cancer_nfe=0;AC_non_neuro_sas_XY=0;AN_non_neuro_sas_XY=339;AF_non_neuro_sas_XY=0.00000;nhomalt_non_neuro_sas_XY=0;AC_non_topmed_asj=0;" +
                    "AN_non_topmed_asj=176;AF_non_topmed_asj=0.00000;nhomalt_non_topmed_asj=0;nhomalt=0;AC_non_v2_amr_XY=0;AN_non_v2_amr_XY=850;AF_non_v2_amr_XY=0.00000;nhomalt_non_v2_amr_XY=0;AC_asj=0;AN_asj=339;AF_asj=0.00000;" +
                    "nhomalt_asj=0;AC_non_topmed_asj_XY=0;AN_non_topmed_asj_XY=176;AF_non_topmed_asj_XY=0.00000;nhomalt_non_topmed_asj_XY=0;AC_non_topmed_asj=0;AN_non_topmed_asj=176;AF_non_topmed_asj=0.00000;nhomalt_non_topmed_asj=0;" +
                    "nhomalt=0;AC_non_v2_amr_XY=0;AN_non_v2_amr_XY=850;AF_non_v2_amr_XY=0.00000;nhomalt_non_v2_amr_XY=0;AC_asj=0;AN_asj=339;AF_asj=0.00000;nhomalt_asj=0;AC_non_topmed_asj_XY=0;AN_non_topmed_asj_XY=176;" +
                    "AF_non_topmed_asj_XY=0.00000;nhomalt_non_topmed_asj_XY=0;AC_non_topmed_ami=0;AN_non_topmed_ami=8;AF_non_topmed_ami=0.00000;nhomalt_non_topmed_ami=0;AC_non_topmed_raw=3;AN_non_topmed_raw=21595;AF_non_topmed_raw=0.000138921;" +
                    "nhomalt_non_topmed_raw=0;AC_non_cancer_eas_XY=0;AN_non_cancer_eas_XY=356;AF_non_cancer_eas_XY=0.00000;nhomalt_non_cancer_eas_XY=0;AC_non_cancer=1;AN_non_cancer=9969;AF_non_cancer=0.000100311;nhomalt_non_cancer=0;" +
                    "AC_controls_and_biobanks_ami_XY=0;AN_controls_and_biobanks_ami_XY=6;AF_controls_and_biobanks_ami_XY=0.00000;nhomalt_controls_and_biobanks_ami_XY=0;AC_non_v2_afr_XY=0;AN_non_v2_afr_XY=1661;AF_non_v2_afr_XY=0.00000;" +
                    "nhomalt_non_v2_afr_XY=0;AC_non_v2_sas_XY=0;AN_non_v2_sas_XY=246;AF_non_v2_sas_XY=0.00000;nhomalt_non_v2_sas_XY=0;AC_non_v2_fin=0;AN_non_v2_fin=460;AF_non_v2_fin=0.00000;nhomalt_non_v2_fin=0;AC_non_neuro_oth=0;" +
                    "AN_non_neuro_oth=132;AF_non_neuro_oth=0.00000;nhomalt_non_neuro_oth=0;AC_non_neuro_asj=0;AN_non_neuro_asj=334;AF_non_neuro_asj=0.00000;nhomalt_non_neuro_asj=0;AC_non_topmed_afr=0;AN_non_topmed_afr=1208;" +
                    "AF_non_topmed_afr=0.00000;nhomalt_non_topmed_afr=0;AC_non_topmed_afr_XY=0;AN_non_topmed_afr_XY=1208;AF_non_topmed_afr_XY=0.00000;nhomalt_non_topmed_afr_XY=0;AC_non_neuro_eas=0;AN_non_neuro_eas=392;" +
                    "AF_non_neuro_eas=0.00000;nhomalt_non_neuro_eas=0;AC_non_neuro_mid_XY=0;AN_non_neuro_mid_XY=26;AF_non_neuro_mid_XY=0.00000;nhomalt_non_neuro_mid_XY=0;AC_non_cancer_amr=0;AN_non_cancer_amr=924;AF_non_cancer_amr=0.00000;" +
                    "nhomalt_non_cancer_amr=0;AC_afr_XY=0;AN_afr_XY=2423;AF_afr_XY=0.00000;nhomalt_afr_XY=0;AC_non_topmed_fin_XY=0;AN_non_topmed_fin_XY=565;AF_non_topmed_fin_XY=0.00000;nhomalt_non_topmed_fin_XY=0;AC_non_neuro_amr_XY=0;" +
                    "AN_non_neuro_amr_XY=931;AF_non_neuro_amr_XY=0.00000;nhomalt_non_neuro_amr_XY=0;AC_controls_and_biobanks_asj_XY=0;AN_controls_and_biobanks_asj_XY=8;AF_controls_and_biobanks_asj_XY=0.00000;nhomalt_controls_and_biobanks_asj_XY=0;" +
                    "AC_non_v2_fin_XY=0;AN_non_v2_fin_XY=460;AF_non_v2_fin_XY=0.00000;nhomalt_non_v2_fin_XY=0;AC_non_cancer_amr_XY=0;AN_non_cancer_amr_XY=924;AF_non_cancer_amr_XY=0.00000;nhomalt_non_cancer_amr_XY=0;AC_controls_and_biobanks=1;" +
                    "AN_controls_and_biobanks=2157;AF_controls_and_biobanks=0.000463607;nhomalt_controls_and_biobanks=0;AC_controls_and_biobanks_oth=0;AN_controls_and_biobanks_oth=46;AF_controls_and_biobanks_oth=0.00000;" +
                    "nhomalt_controls_and_biobanks_oth=0;AC_nfe_XY=1;AN_nfe_XY=5365;AF_nfe_XY=0.000186393;nhomalt_nfe_XY=0;AC_controls_and_biobanks_sas_XY=0;AN_controls_and_biobanks_sas_XY=250;AF_controls_and_biobanks_sas_XY=0.00000;" +
                    "nhomalt_controls_and_biobanks_sas_XY=0;AC_non_cancer_oth=0;AN_non_cancer_oth=124;AF_non_cancer_oth=0.00000;nhomalt_non_cancer_oth=0;AC_non_topmed_oth=0;AN_non_topmed_oth=100;AF_non_topmed_oth=0.00000;" +
                    "nhomalt_non_topmed_oth=0;AC_non_topmed_nfe_XY=0;AN_non_topmed_nfe_XY=1829;AF_non_topmed_nfe_XY=0.00000;nhomalt_non_topmed_nfe_XY=0;AC_non_v2_nfe=1;AN_non_v2_nfe=4058;AF_non_v2_nfe=0.000246427;nhomalt_non_v2_nfe=0;" +
                    "AC_controls_and_biobanks_nfe=1;AN_controls_and_biobanks_nfe=590;AF_controls_and_biobanks_nfe=0.00169492;nhomalt_controls_and_biobanks_nfe=0;AC_controls_and_biobanks_oth_XY=0;AN_controls_and_biobanks_oth_XY=46;" +
                    "AF_controls_and_biobanks_oth_XY=0.00000;nhomalt_controls_and_biobanks_oth_XY=0;AC_controls_and_biobanks_fin_XY=0;AN_controls_and_biobanks_fin_XY=293;AF_controls_and_biobanks_fin_XY=0.00000;" +
                    "nhomalt_controls_and_biobanks_fin_XY=0;AC_non_cancer_asj=0;AN_non_cancer_asj=325;AF_non_cancer_asj=0.00000;nhomalt_non_cancer_asj=0;AC_non_neuro_amr=0;AN_non_neuro_amr=931;AF_non_neuro_amr=0.00000;nhomalt_non_neuro_amr=0;" +
                    "AC_non_v2_ami_XY=0;AN_non_v2_ami_XY=91;AF_non_v2_ami_XY=0.00000;nhomalt_non_v2_ami_XY=0;AC_non_neuro_raw=4;AN_non_neuro_raw=29772;AF_non_neuro_raw=0.000134354;nhomalt_non_neuro_raw=0;AC_non_neuro_afr=0;" +
                    "AN_non_neuro_afr=1897;AF_non_neuro_afr=0.00000;nhomalt_non_neuro_afr=0;AC_non_topmed_ami_XY=0;AN_non_topmed_ami_XY=8;AF_non_topmed_ami_XY=0.00000;nhomalt_non_topmed_ami_XY=0;AC_non_neuro_oth_XY=0;" +
                    "AN_non_neuro_oth_XY=132;AF_non_neuro_oth_XY=0.00000;nhomalt_non_neuro_oth_XY=0;AC_non_cancer_afr_XY=0;AN_non_cancer_afr_XY=2393;AF_non_cancer_afr_XY=0.00000;nhomalt_non_cancer_afr_XY=0;AC_non_cancer_fin=0;" +
                    "AN_non_cancer_fin=570;AF_non_cancer_fin=0.00000;nhomalt_non_cancer_fin=0;AC_controls_and_biobanks_asj=0;AN_controls_and_biobanks_asj=8;AF_controls_and_biobanks_asj=0.00000;nhomalt_controls_and_biobanks_asj=0;" +
                    "AC_non_topmed_oth_XY=0;AN_non_topmed_oth_XY=100;AF_non_topmed_oth_XY=0.00000;nhomalt_non_topmed_oth_XY=0;AC_non_neuro_XY=1;AN_non_neuro_XY=9629;AF_non_neuro_XY=0.000103853;nhomalt_non_neuro_XY=0;AC_nfe=1;AN_nfe=5365;" +
                    "AF_nfe=0.000186393;nhomalt_nfe=0;AC_popmax=1;AN_popmax=5365;AF_popmax=0.000186393;nhomalt_popmax=0;faf95_amr_XY=0.00000;faf99_amr_XY=0.00000;faf95_sas_XY=0.00000;faf99_sas_XY=0.00000;faf95_sas=0.00000;faf99_sas=0.00000;" +
                    "faf95_XY=0.00000;faf99_XY=0.00000;faf95_eas=0.00000;faf99_eas=0.00000;faf95_amr=0.00000;faf99_amr=0.00000;faf95_afr=0.00000;faf99_afr=0.00000;faf95_eas_XY=0.00000;faf99_eas_XY=0.00000;faf95=0.00000;faf99=0.00000;" +
                    "faf95_afr_XY=0.00000;faf99_afr_XY=0.00000;faf95_nfe_XY=0.00000;faf99_nfe_XY=0.00000;faf95_nfe=0.00000;faf99_nfe=0.00000;age_hist_het_bin_freq=0|0|0|0|0|0|0|0|0|0;age_hist_het_n_smaller=0;age_hist_het_n_larger=0;" +
                    "age_hist_hom_bin_freq=0|0|0|0|1|0|0|0|0|0;age_hist_hom_n_smaller=0;age_hist_hom_n_larger=0;FS=0.00000;MQ=52.6140;MQRankSum=0.158000;QUALapprox=3467;QD=30.9554;ReadPosRankSum=-0.474000;VarDP=112;AS_FS=0.00000;" +
                    "AS_MQ=52.6140;AS_MQRankSum=0.158000;AS_QUALapprox=3467;AS_QD=30.9554;AS_ReadPosRankSum=-0.474000;AS_SB_TABLE=1,0|92,19;AS_SOR=1.85718;InbreedingCoeff=-5.80549e-05;AS_culprit=AS_QD;AS_VQSLOD=-2.05150;NEGATIVE_TRAIN_SITE;" +
                    "allele_type=snv;n_alt_alleles=1;variant_type=snv;nonpar;gq_hist_alt_bin_freq=0|0|0|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0;gq_hist_all_bin_freq=0|0|0|0|8904|993|612|111|15|7|1|0|0|0|0|0|0|0|0|0;" +
                    "dp_hist_alt_bin_freq=0|0|1|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0;dp_hist_alt_n_larger=0;dp_hist_all_bin_freq=0|0|8664|1849|107|22|1|0|0|0|0|0|0|0|0|0|0|0|0|0;dp_hist_all_n_larger=0;" +
                    "ab_hist_alt_bin_freq=0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0;cadd_raw_score=0.230252;cadd_phred=3.47300;" +
                    "vep=A|upstream_gene_variant|MODIFIER|RNU6-1334P|ENSG00000251841|Transcript|ENST00000516032|snRNA||||||||||1|3195|1|SNV||HGNC|HGNC:48297|YES||||||||||||||||||||," +
                    "A|intron_variant&non_coding_transcript_variant|MODIFIER|XGY2|ENSG00000288621|Transcript|ENST00000674986|lncRNA||3/7|ENST00000674986.1:n.106+6815G>A|||||||1||1|SNV||HGNC|HGNC:34022|||||||||||||||||||||," +
                    "A|intron_variant&non_coding_transcript_variant|MODIFIER|XGY2|ENSG00000288621|Transcript|ENST00000675255|lncRNA||3/4|ENST00000675255.1:n.106+6815G>A|||||||1||1|SNV||HGNC|HGNC:34022|||||||||||||||||||||," +
                    "A|intron_variant&non_coding_transcript_variant|MODIFIER|XGY2|ENSG00000288621|Transcript|ENST00000675396|lncRNA||3/3|ENST00000675396.1:n.319+6815G>A|||||||1||1|SNV||HGNC|HGNC:34022|YES||||||||||||||||||||," +
                    "A|intron_variant&non_coding_transcript_variant|MODIFIER|XGY2|ENSG00000288621|Transcript|ENST00000675500|lncRNA||3/3|ENST00000675500.1:n.107-5441G>A|||||||1||1|SNV||HGNC|HGNC:34022|||||||||||||||||||||," +
                    "A|intron_variant&non_coding_transcript_variant|MODIFIER|XGY2|ENSG00000288621|Transcript|ENST00000675941|lncRNA||3/6|ENST00000675941.1:n.106+6815G>A|||||||1||1|SNV||HGNC|HGNC:34022|||||||||||||||||||||," +
                    "A|intron_variant&non_coding_transcript_variant|MODIFIER|XGY2|ENSG00000288621|Transcript|ENST00000676270|lncRNA||3/4|ENST00000676270.1:n.107-5441G>A|||||||1||1|SNV||HGNC|HGNC:34022|||||||||||||||||||||";

            //AC_nfe=1;AN_nfe=5365;F_nfe=0.000186393;nhomalt_nfe=0;
            Allele expected = new Allele(24, 2781554, "G", "A");
            expected.addFrequency(AlleleData.frequencyOf(GNOMAD_G_NFE, 1, 5365));

            assertParseLineEquals(line, List.of(expected));
        }
    }
}