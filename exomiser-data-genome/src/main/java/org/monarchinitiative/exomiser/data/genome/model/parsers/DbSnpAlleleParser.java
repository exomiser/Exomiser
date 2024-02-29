/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @link https://www.ncbi.nlm.nih.gov/variation/docs/human_variation_vcf/
 */
public class DbSnpAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(DbSnpAlleleParser.class);

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        System.out.println(info);
        Map<AlleleProto.FrequencySource, List<String>> minorAlleleFrequencies = parseMinorAlleleFrequencies(info);

        for (Map.Entry<AlleleProto.FrequencySource, List<String>> entry : minorAlleleFrequencies.entrySet()) {
            AlleleProto.FrequencySource alleleProperty = entry.getKey();
            List<String> alleleMafs = entry.getValue();
            for (int i = 0; i < alleleMafs.size(); i++) {
                String maf = alleleMafs.get(i);
                if (!maf.equals(".")) {
                    float freq = 100f * Float.parseFloat(maf);
                    Allele allele = alleles.get(i);
                    allele.addFrequency(AlleleData.frequencyOf(alleleProperty, freq));
                }
            }
        }
        return alleles.stream().filter(allele -> !allele.getFrequencies().isEmpty()).toList();
    }

    // ##INFO=<ID=CAF,Number=.,Type=String,Description="An ordered, comma delimited list of allele frequencies based on 1000Genomes, starting with the reference allele followed by alternate alleles as ordered in the ALT column. Where a 1000Genomes alternate allele is not in the dbSNPs alternate allele set, the allele is added to the ALT column.  The minor allele is the second largest value in the list, and was previuosly reported in VCF as the GMAF.  This is the GMAF reported on the RefSNP and EntrezSNP pages and VariationReporter">
    // also in b151
    // ##INFO=<ID=TOPMED,Number=.,Type=String,Description="An ordered, comma delimited list of allele frequencies based on TOPMed, starting with the reference allele followed by alternate alleles as ordered in the ALT column. The TOPMed minor allele is the second largest value in the list.">
    private Map<AlleleProto.FrequencySource, List<String>> parseMinorAlleleFrequencies(String info) {
        EnumMap<AlleleProto.FrequencySource, List<String>> mafMap = new EnumMap<>(AlleleProto.FrequencySource.class);
        String[] infoFields = info.split(";");
        for (String infoField : infoFields) {
            if (infoField.startsWith("CAF=")) {
                String frequencyValues = getFrequencyValues(infoField);
                mafMap.put(AlleleProto.FrequencySource.KG, parseFreqField(frequencyValues));
            }
            if (infoField.startsWith("TOPMED=")) {
                String frequencyValues = getFrequencyValues(infoField);
                mafMap.put(AlleleProto.FrequencySource.TOPMED, parseFreqField(frequencyValues));
            }
            // newer b152+ format has all the frequency data in the FREQ field which requires further parsing
            if (infoField.startsWith("FREQ=")) {
                String[] sources = infoField.substring(infoField.indexOf('=') + 1).split("\\|");
                for (String source : sources) {
                    int colonPos = source.indexOf(':');
                    String sourceId = source.substring(0, colonPos);
                    String frequencyValues = source.substring(colonPos + 1);
                    switch (sourceId) {
                        case "1000Genomes":
                            mafMap.put(AlleleProto.FrequencySource.KG, parseFreqField(frequencyValues));
                        case "TOPMED":
                            mafMap.put(AlleleProto.FrequencySource.TOPMED, parseFreqField(frequencyValues));
//                        case "TWINSUK":
//                            // https://twinsuk.ac.uk/about-us/what-is-twinsuk/
//                            mafMap.put(AlleleProperty.TWINSUK, parseFreqField(frequencyValues));
//                        case "ALSPAC":
//                            // http://www.bristol.ac.uk/alspac/researchers/cohort-profile/
//                            mafMap.put(AlleleProperty.ALSPAC, parseFreqField(frequencyValues));
                        default: // do nothing
                    }
                }
            }
        }
        return mafMap;
    }

    private String getFrequencyValues(String infoField) {
        return infoField.substring(infoField.indexOf('=') + 1);
    }

    private List<String> parseFreqField(String infoField) {
        //allele freq data format is:
        // CAF=0.9812,.,0.01882
        // and / or
        // TOPMED=0.999725,0.000274744,.
        // where major allele is 1st followed by minor alleles in order of alt line
        List<String> minorFreqs = new ArrayList<>();
        String[] freqs = infoField.split(",");
        //note we're taking the minor freqs, so the loop starts at int i = 1
        for (int i = 1; i < freqs.length; i++) {
            minorFreqs.add(freqs[i]);
        }
        return minorFreqs;
    }

}
