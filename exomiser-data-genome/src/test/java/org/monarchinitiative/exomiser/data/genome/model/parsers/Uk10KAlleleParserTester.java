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
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Uk10KAlleleParserTester extends AbstractAlleleParserTester<Uk10kAlleleParser> {

    @Override
    public Uk10kAlleleParser newInstance() {
        return new Uk10kAlleleParser();
    }

    @Test
    public void testOne() throws Exception {
        String line = "1	28590	.	T	TTGG	999	PASS	DP=12080;VQSLOD=0.7589;AN=7562;AC=7226;AF=0.955567;AN_TWINSUK=3708;AC_TWINSUK=3571;AF_TWINSUK=0.963053;AN_ALSPAC=3854;AC_ALSPAC=3655;AF_ALSPAC=0.948365;CSQ=ENSR00000528774:-:regulatory_region_variant+ENST00000423562:WASH7P:intron_variant,nc_transcript_variant,feature_elongation+ENST00000438504:WASH7P:intron_variant,nc_transcript_variant,feature_elongation+ENST00000469289:MIR1302-10:upstream_gene_variant+ENST00000473358:MIR1302-10:upstream_gene_variant+ENST00000488147:WASH7P:intron_variant,nc_transcript_variant,feature_elongation+ENST00000538476:WASH7P:intron_variant,nc_transcript_variant,feature_elongation+ENST00000541675:WASH7P:upstream_gene_variant+ENST00000607096:MIR1302-10:upstream_gene_variant;AC_TWINSUK_NODUP=3443;AN_TWINSUK_NODUP=3574;AF_TWINSUK_NODUP=0.963346";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.addFrequency(AlleleData.frequencyOf(AlleleProto.FrequencySource.UK10K, 7226, 7562));

        assertParseLineEquals(line, List.of(expected));
    }


    @Test
    public void testOneAllAlleles() throws Exception {
        String line = "1\t866920\trs2341361\tA\tG\t999\tPASS\tDP=24068;VQSLOD=3.8238;AN=7562;AC=7562;AF=1;AN_TWINSUK=3708;AC_TWINSUK=3708;AF_TWINSUK=1;AN_ALSPAC=3854;AC_ALSPAC=3854;AF_ALSPAC=1;AF_AFR=1;AF_AMR=1;AF_ASN=1;AF_EUR=1;AF_MAX=1;CSQ=ENST00000341065:SAMD11:intron_variant+ENST00000342066:SAMD11:intron_variant+ENST00000420190:SAMD11:intron_variant+ENST00000437963:SAMD11:intron_variant+ENST00000598827:AL645608.1:upstream_gene_variant+GERP,-6.67;AC_TWINSUK_NODUP=3574;AN_TWINSUK_NODUP=3574;AF_TWINSUK_NODUP=1";
        Allele expected = new Allele(1, 866920, "A", "G");
        expected.setRsId("rs2341361");
        expected.addFrequency(AlleleData.frequencyOf(AlleleProto.FrequencySource.UK10K, 7562, 7562));

        assertParseLineEquals(line, List.of(expected));
    }
}