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

import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Uk10kAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(Uk10kAlleleParser.class);

    /**
     * Parses the AF value form the INFO line and adds it to the {@link Allele}. The AF is pre-calculated as the AC/AN.
     * AC and AN are the aggregated values of the ALSPAC and TWINSUK cohorts. These are non-rare disease cohorts so should
     * be suitable for rare disease analysis.
     * <p>
     * http://www.uk10k.org/studies/cohorts.html
     * <p>
     * ##INFO=<ID=DP,Number=1,Type=Integer,Description="Raw read depth">
     * ##INFO=<ID=VQSLOD,Number=1,Type=Float,Description="Log odds ratio of being a true variant versus being false under the trained gaussian mixture model (GATK)">
     * ##INFO=<ID=AC,Number=A,Type=Integer,Description="Allele count in called genotypes">
     * ##INFO=<ID=AN,Number=1,Type=Integer,Description="Total number of alleles in called genotypes">
     * ##INFO=<ID=AF,Number=A,Type=Float,Description="Allele frequency in called genotypes">
     * ##INFO=<ID=AC_TWINSUK,Number=A,Type=Integer,Description="Allele count in called genotypes in TWINSUK cohort">
     * ##INFO=<ID=AN_TWINSUK,Number=1,Type=Integer,Description="Total number of alleles in called genotypes in TWINSUK cohort">
     * ##INFO=<ID=AF_TWINSUK,Number=A,Type=Float,Description="Allele frequency in called genotypes in TWINSUK cohort">
     * ##INFO=<ID=AC_ALSPAC,Number=A,Type=Integer,Description="Allele count in called genotypes in ALSPAC cohort">
     * ##INFO=<ID=AF_ALSPAC,Number=A,Type=Float,Description="Allele frequency in called genotypes in ASLPAC cohort">
     * ##INFO=<ID=AF_AFR,Number=1,Type=Float,Description="1000 Genomes Phase 1 Allele Frequency in African population (YRI,LWK,ASW)">
     * ##INFO=<ID=AN_ALSPAC,Number=1,Type=Integer,Description="Total number of alleles in called genotypes in ALSPAC cohort">
     * ##INFO=<ID=AF_AMR,Number=1,Type=Float,Description="1000 Genomes Phase 1 Allele Frequency in American population (MXL,CLM,PUR)">
     * ##INFO=<ID=AF_ASN,Number=1,Type=Float,Description="1000 Genomes Phase 1 Allele Frequency in Asian population (CHB,CHS,JPT)">
     * ##INFO=<ID=AF_EUR,Number=1,Type=Float,Description="1000 Genomes Phase 1 Allele Frequency in European population (CEU,TSI,FIN,GBR,IBS)">
     * ##INFO=<ID=AF_MAX,Number=1,Type=Float,Description="1000 Genomes Phase 1 Maximum Allele Frequency">
     * ##INFO=<ID=ESP_MAF,Number=3,Type=Float,Description="Minor allele frequecy in percent for European American, African American and All populations in the NHLBI Exome Sequencing Project (ESP)">
     * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence of the ALT alleles from Ensembl 75 VEP v75, format transcriptId:geneName:consequence[:codingSeqPosition:proteinPosition:proteinAlleles:proteinPredictions]+...[+gerpScore]">
     * ##INFO=<ID=AC_TWINSUK_NODUP,Number=A,Type=Integer,Description="Allele count in called genotypes in TWINSUK cohort excluding 67 samples where a monozygotic or dyzygotic twin was included in the release">
     * ##INFO=<ID=AN_TWINSUK_NODUP,Number=1,Type=Integer,Description="Total number of alleles in called genotypes in TWINSUK cohort excluding 67 samples where a monozygotic or dyzygotic twin was included in the release">
     * ##INFO=<ID=AF_TWINSUK_NODUP,Number=A,Type=Float,Description="Allele frequency in called genotypes in TWINSUK cohort excluding 67 samples where a monozygotic or dyzygotic twin was included in the release">
     *
     * @param alleles
     * @param info
     * @return
     */
    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        String[] infoFields = info.split(";");
        int an = 0;
        String[] acValues = new String[0];

        for (String infoField : infoFields) {
            if (infoField.startsWith("AN=")) {
                String anValue = infoField.substring(3);
                an = Integer.parseInt(anValue);
            }
            if (infoField.startsWith("AC=")) {
                acValues = infoField.substring(3).split(",");
            }
        }
        if (acValues.length != alleles.size()) {
            logger.warn("Incorrect number of alleles present: {}", info);
            return alleles;
        }

        // CAUTION! This file appears to have duplicated sites with slightly different counts.
        // $ tabix UK10K_COHORT.20160215.sites.vcf.gz 1:11957369-11957369
        // 1	11957369	rs36041052	G	GAC	999	PASS	DP=22894;VQSLOD=7.4972;AN=7562;AC=350;AF=0.046284;AN_TWINSUK=3708;AC_TWINSUK=177;AF_TWINSUK=0.047735;AN_ALSPAC=3854;AC_ALSPAC=173;AF_ALSPAC=0.044888;CSQ=-:-:intergenic_variant;AC_TWINSUK_NODUP=173;AN_TWINSUK_NODUP=3574;AF_TWINSUK_NODUP=0.0484051
        // 1	11957369	rs36041052	G	GAC	999	PASS	DP=22373;VQSLOD=7.3524;AN=7562;AC=352;AF=0.046549;AN_TWINSUK=3708;AC_TWINSUK=178;AF_TWINSUK=0.048004;AN_ALSPAC=3854;AC_ALSPAC=174;AF_ALSPAC=0.045148;CSQ=-:-:intergenic_variant;AC_TWINSUK_NODUP=173;AN_TWINSUK_NODUP=3574;AF_TWINSUK_NODUP=0.0484051

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            var frequency = AlleleData.frequencyOf(AlleleProto.FrequencySource.UK10K, Integer.parseInt(acValues[i]), an);
            allele.addFrequency(frequency);
        }
        return alleles;
    }
}
