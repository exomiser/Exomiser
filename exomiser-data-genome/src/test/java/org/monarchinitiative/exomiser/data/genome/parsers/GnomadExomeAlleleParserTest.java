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
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.Collections;

import static org.monarchinitiative.exomiser.data.genome.model.AlleleProperty.GNOMAD_E_SAS;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GnomadExomeAlleleParserTest extends AbstractAlleleParserTester<GnomadExomeAlleleParser> {

    @Override
    public GnomadExomeAlleleParser newInstance() {
        return new GnomadExomeAlleleParser();
    }

    @Test
    public void parseLineFailAcSnp() throws Exception {
        String line = "1\t139019\t.\tG\tC\t2965.40\tAC0\tAC=0;AF=0.00000e+00;AN=132342;BaseQRankSum=-1.98000e-01;ClippingRankSum=9.22000e-01;DP=7253058;FS=0.00000e+00;InbreedingCoeff=-8.30000e-03;MQ=4.63500e+01;MQRankSum=1.98000e-01;QD=3.63000e+00;ReadPosRankSum=3.58000e-01;SOR=6.43000e-01;VQSLOD=-1.10200e-01;VQSR_culprit=QD;VQSR_NEGATIVE_TRAIN_SITE;GQ_HIST_ALT=0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|1;DP_HIST_ALT=0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0;AB_HIST_ALT=0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0;GQ_HIST_ALL=3189|1641|421|585|445|187|319|296|143|243|191|92|306|26|193|63|208|24|205|63825;DP_HIST_ALL=5211|1205|757|527|486|345|5343|21021|10849|3044|1126|884|949|938|1030|1068|1029|1069|1088|1328;AB_HIST_ALL=0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=0;AC_NFE=0;AC_OTH=0;AC_SAS=0;AC_Male=0;AC_Female=0;AN_AFR=6184;AN_AMR=23686;AN_ASJ=7998;AN_EAS=9934;AN_FIN=6008;AN_NFE=52508;AN_OTH=3638;AN_SAS=22386;AN_Male=72368;AN_Female=59974;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=0.00000e+00;AF_NFE=0.00000e+00;AF_OTH=0.00000e+00;AF_SAS=0.00000e+00;AF_Male=0.00000e+00;AF_Female=0.00000e+00;GC_AFR=3092,0,0;GC_AMR=11843,0,0;GC_ASJ=3999,0,0;GC_EAS=4967,0,0;GC_FIN=3004,0,0;GC_NFE=26254,0,0;GC_OTH=1819,0,0;GC_SAS=11193,0,0;GC_Male=36184,0,0;GC_Female=29987,0,0;AC_raw=1;AN_raw=145204;AF_raw=6.88686e-06;GC_raw=72601,1,0;GC=66171,0,0;Hom_AFR=0;Hom_AMR=0;Hom_ASJ=0;Hom_EAS=0;Hom_FIN=0;Hom_NFE=0;Hom_OTH=0;Hom_SAS=0;Hom_Male=0;Hom_Female=0;Hom_raw=0;Hom=0;POPMAX=.;AC_POPMAX=.;AN_POPMAX=.;AF_POPMAX=.;DP_MEDIAN=710;DREF_MEDIAN=6.30957e-174;GQ_MEDIAN=99;AB_MEDIAN=1.67606e-01;AS_RF=1.54603e-01;AS_FilterStatus=AC0";

        assertParseLineEquals(line, Collections.emptyList());
    }

    @Test
    public void parseLinePassSnp() throws Exception {
        String line = "1\t139020\t.\tT\tC\t1976.24\tPASS\tAC=1;AF=7.55504e-06;AN=132362;BaseQRankSum=3.81000e+00;ClippingRankSum=8.56000e-01;DP=7254150;FS=2.19900e+00;InbreedingCoeff=-7.80000e-03;MQ=4.70800e+01;MQRankSum=1.38000e+00;QD=7.32000e+00;ReadPosRankSum=1.52000e+00;SOR=8.48000e-01;VQSLOD=-8.14000e-01;VQSR_culprit=QD;VQSR_NEGATIVE_TRAIN_SITE;GQ_HIST_ALT=0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|1;DP_HIST_ALT=0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0;AB_HIST_ALT=0|0|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0|0;GQ_HIST_ALL=3203|1636|438|584|447|186|305|298|142|231|199|103|305|32|193|59|205|27|208|63837;DP_HIST_ALL=5227|1208|754|526|484|345|5354|21034|10848|3044|1125|884|949|938|1030|1068|1029|1069|1088|1328;AB_HIST_ALL=0|0|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0|0;AC_AFR=0;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=0;AC_NFE=0;AC_OTH=0;AC_SAS=1;AC_Male=1;AC_Female=0;AN_AFR=6182;AN_AMR=23682;AN_ASJ=7998;AN_EAS=9936;AN_FIN=6012;AN_NFE=52528;AN_OTH=3638;AN_SAS=22386;AN_Male=72380;AN_Female=59982;AF_AFR=0.00000e+00;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=0.00000e+00;AF_NFE=0.00000e+00;AF_OTH=0.00000e+00;AF_SAS=4.46708e-05;AF_Male=1.38160e-05;AF_Female=0.00000e+00;GC_AFR=3091,0,0;GC_AMR=11841,0,0;GC_ASJ=3999,0,0;GC_EAS=4968,0,0;GC_FIN=3006,0,0;GC_NFE=26264,0,0;GC_OTH=1819,0,0;GC_SAS=11192,1,0;GC_Male=36189,1,0;GC_Female=29991,0,0;AC_raw=1;AN_raw=145276;AF_raw=6.88345e-06;GC_raw=72637,1,0;GC=66180,1,0;Hom_AFR=0;Hom_AMR=0;Hom_ASJ=0;Hom_EAS=0;Hom_FIN=0;Hom_NFE=0;Hom_OTH=0;Hom_SAS=0;Hom_Male=0;Hom_Female=0;Hom_raw=0;Hom=0;POPMAX=SAS;AC_POPMAX=1;AN_POPMAX=22386;AF_POPMAX=4.46708e-05;DP_MEDIAN=264;DREF_MEDIAN=6.30957e-205;GQ_MEDIAN=99;AB_MEDIAN=3.10606e-01;AS_RF=2.26895e-01;AS_FilterStatus=PASS";

        Allele expected = new Allele(1, 139020, "T", "C");
        // AC_AFR=0;AN_AFR=6182;
        // AC_AMR=0;AN_AMR=23682;
        // AC_ASJ=0;AN_ASJ=7998;
        // AC_EAS=0;AN_EAS=9936;
        // AC_FIN=0;AN_FIN=6012;
        // AC_NFE=0;AN_NFE=52528;
        // AC_OTH=0;AN_OTH=3638;
        // AC_SAS=1;AN_SAS=22386
        expected.addValue(GNOMAD_E_SAS, 0.0044670776378093f);

        assertParseLineEquals(line, Collections.singletonList(expected));
    }

}