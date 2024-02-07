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
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.TOPMED;

/**
 * Parser for TOPMED VCF files downloaded from
 * http://ftp.ensembl.org/pub/data_files/homo_sapiens/GRCh37/variation_genotype/TOPMED*.vcf
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TopMedAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(TopMedAlleleParser.class);

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        String alleleValues = extractFrequencyValues(info);
        if (alleleValues.isEmpty()) {
            return alleles;
        }
        List<String> alleleFrequencyValues = parseAlleleFrequencies(alleleValues);

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            //once case of this:
            //17      10599057        rs587776629     CTC     C,CAG,CGA       .       .       TOPMED=.,.,
            //which will throw an IndexOutOfBoundsException
            if (i <= alleleFrequencyValues.size() - 1) {
                String freqValue = alleleFrequencyValues.get(i);
                if (!freqValue.isEmpty() && !".".equals(freqValue)) {
                    try {
                        float freq = 100f * Float.parseFloat(freqValue);
                        allele.addFrequency(AlleleData.frequencyOf(TOPMED, freq));
                    } catch (NumberFormatException ex) {
                        // swallow these
                    }
                }
            }
        }

        return alleles;
    }

    private String extractFrequencyValues(String info) {
        String[] values = info.split("=");
        if (values.length != 2) {
            return "";
        }
        return values[1];
    }

    private List<String> parseAlleleFrequencies(String info) {
        //##INFO=<ID=TOPMED,Number=.,Type=String,Description="An ordered, comma delimited list of allele frequencies based on TOPMed, starting with the reference allele followed by alternate alleles as ordered in the ALT column. The TOPMed minor allele is the second largest value in the list.">
        String[] infoFields = info.split(",");
        return Arrays.asList(infoFields);
    }
}
