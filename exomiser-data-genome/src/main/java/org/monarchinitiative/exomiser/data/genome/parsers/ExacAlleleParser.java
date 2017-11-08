/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExacAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(ExacAlleleParser.class);

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<String, String> alleleCounts = getAlleleCountsFromInfoField(info);

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            //AC = AlleleCount, AN = AlleleNumber, freq as percentage = (AC/AN) * 100

            Map<AlleleProperty, Float> allelePopFreqs = calculateAllelePopulationFrequencies(alleleCounts, i);
            allele.getValues().putAll(allelePopFreqs);
        }
        return alleles;
    }

    private Map<String, String> getAlleleCountsFromInfoField(String info) {
        Map<String, String> exACFreqs = new HashMap<>();
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            // freq data for each population e.g. AC_FIN=0,0;AN_FIN=6600;AC_EAS=0,1;AN_EAS=8540 etc...
            if (infoField.startsWith(EXAC_FIELD.ALLELE_COUNT_PREFIX) || infoField.startsWith(EXAC_FIELD.ALLELE_NUMBER_PREFIX)) {
                String[] exACData = infoField.split("=");
                exACFreqs.put(exACData[0], exACData[1]);
            }
        }
        return exACFreqs;
    }

    private Map<AlleleProperty, Float> calculateAllelePopulationFrequencies(Map<String, String> alleleCounts, int i) {
        Map<AlleleProperty, Float> allelePopFreqs = new EnumMap<>(AlleleProperty.class);
        for (EXAC_FIELD field : EXAC_FIELD.values()) {
            int alleleCount = parseAlleleCount(alleleCounts.get(field.AC), i);
            if (alleleCount != 0) {
                int alleleNumber = Integer.parseInt(alleleCounts.get(field.AN));
                float minorAlleleFrequency = frequencyAsPercentage(alleleCount, alleleNumber);
                allelePopFreqs.put(AlleleProperty.valueOf(field.name()), minorAlleleFrequency);
            }
        }
        return allelePopFreqs;
    }

    private int parseAlleleCount(String alleleCountValue, int altAllelePos) {
        String alleleCount = alleleCountValue.split(",")[altAllelePos];
        return Integer.parseInt(alleleCount);
    }

    private float frequencyAsPercentage(int alleleCount, int alleleNumber) {
        return 100f * alleleCount / alleleNumber;
    }

    public enum EXAC_FIELD {
        EXAC_AFR("AN_AFR", "AC_AFR"),
        EXAC_AMR("AN_AMR", "AC_AMR"),
        EXAC_EAS("AN_EAS", "AC_EAS"),
        EXAC_FIN("AN_FIN", "AC_FIN"),
        EXAC_NFE("AN_NFE", "AC_NFE"),
        EXAC_OTH("AN_OTH", "AC_OTH"),
        EXAC_SAS("AN_SAS", "AC_SAS");

        public static final String ALLELE_COUNT_PREFIX = "AC";
        public static final String ALLELE_NUMBER_PREFIX = "AN";

        private final String AN;
        private final String AC;

        EXAC_FIELD(String alleleNumField, String alleleCountField) {
            this.AN = alleleNumField;
            this.AC = alleleCountField;
        }

    }

}
