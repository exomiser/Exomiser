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

import java.util.*;

/**
 * Parser for the ExAC/GnomAD data sets. The data we want from these are almost identical, with variation only in the
 * output values and slight differences in the population fields. These differences are encoded in the {@link ExacPopulationKey}
 * class.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class ExacAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(ExacAlleleParser.class);

    private final List<ExacPopulationKey> populationKeys;

    public ExacAlleleParser(List<ExacPopulationKey> populationKeys, Set<String> allowedFilterValues) {
        this.populationKeys = populationKeys;
        this.allowedFilterValues = allowedFilterValues;
    }

    public List<ExacPopulationKey> getPopulationKeys() {
        return populationKeys;
    }

    //;AC_AFR=1;AC_AMR=0;AC_ASJ=0;AC_EAS=0;AC_FIN=0;AC_NFE=0;AC_OTH=0;AC_SAS=0;AC_Male=1;AC_Female=0;AN_AFR=2596;AN_AMR=2182;AN_ASJ=264;AN_EAS=2126;AN_FIN=170;AN_NFE=5270;AN_OTH=438;AN_SAS=2534;AN_Male=8378;AN_Female=7202;AF_AFR=3.85208e-04;AF_AMR=0.00000e+00;AF_ASJ=0.00000e+00;AF_EAS=0.00000e+00;AF_FIN=0.00000e+00;AF_NFE=0.00000e+00;AF_OTH=0.00000e+00;AF_SAS=0.00000e+00;AC_raw=1;AN_raw=51438;AF_raw=1.94409e-05;GC_raw=25718,1,0;GC=7789,1,0;Hom_AFR=0;Hom_AMR=0;Hom_ASJ=0;Hom_EAS=0;Hom_FIN=0;Hom_NFE=0;Hom_OTH=0;Hom_SAS=0;Hom_Male=0;Hom_Female=0;Hom_raw=0;Hom=0;POPMAX=AFR;AC_POPMAX=1;AN_POPMAX=2596;AF_POPMAX=3.85208e-04;
    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<String, String> alleleCounts = getAlleleCountsFromInfoField(info);

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            //AC = AlleleCount, AN = AlleleNumber, freq as percentage = (AC/AN) * 100
            var frequencies = parseAllelePopulationFrequencies(alleleCounts, i);
            allele.addAllFrequencies(frequencies);
        }
        return alleles;
    }

    //this is the slowest part of this.
    private Map<String, String> getAlleleCountsFromInfoField(String info) {
        Map<String, String> exACFreqs = new HashMap<>();
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            // freq data for each population e.g. AC_FIN=0,0;AN_FIN=6600;AC_EAS=0,1;AN_EAS=8540 etc...
            if (infoField.startsWith(ExacPopulationKey.ALLELE_COUNT_PREFIX) || infoField.startsWith(ExacPopulationKey.ALLELE_NUMBER_PREFIX) || infoField.startsWith("Hom")) {
                String[] exACData = infoField.split("=");
                exACFreqs.put(exACData[0], exACData[1]);
            }
        }
        return exACFreqs;
    }

    private List<AlleleProto.Frequency> parseAllelePopulationFrequencies(Map<String, String> alleleCounts, int i) {
        List<AlleleProto.Frequency> frequencies = new ArrayList<>();
        for (ExacPopulationKey population : populationKeys) {
            int alleleCount = parseAlleleCount(alleleCounts.get(population.acPop()), i);
            if (alleleCount != 0) {
                int alleleNumber = Integer.parseInt(alleleCounts.get(population.anPop()));
                int homozygotes = parseAlleleCount(alleleCounts.get(population.homPop()), i);

                var frequency = AlleleData.frequencyOf(population.frequencySource(), alleleCount, alleleNumber, homozygotes);
                frequencies.add(frequency);
            }
        }
        return frequencies;
    }

    private int parseAlleleCount(String alleleCountValue, int altAllelePos) {
        if (alleleCountValue == null) {
            return 0;
        }
        String alleleCount = alleleCountValue.split(",")[altAllelePos];
        return Integer.parseInt(alleleCount);
    }

    private float frequencyAsPercentage(int alleleCount, int alleleNumber) {
        return 100f * alleleCount / alleleNumber;
    }

}
