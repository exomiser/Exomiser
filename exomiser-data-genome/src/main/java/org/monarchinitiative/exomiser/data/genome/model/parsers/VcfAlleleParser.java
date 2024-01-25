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

import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class VcfAlleleParser implements AlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(VcfAlleleParser.class);
    private final VariantTrimmer variantTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

    protected Set<String> allowedFilterValues = Set.of(".", "PASS");

    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            return List.of();
        }
        String[] fields = line.split("\t");
        List<Allele> alleles = parseAlleles(fields);

        if (hasNoInfoField(fields) || alleles.isEmpty()) {
            return alleles;
        }
        String info = fields[7];

        try {
            return parseInfoField(alleles, info);
        } catch (Exception e) {
            logger.error("Unable to parse info field in line '{}'", line, e);
        }

        return alleles;
    }

    private boolean hasNoInfoField(String[] fields) {
        return fields.length <= 7;
    }

    abstract List<Allele> parseInfoField(List<Allele> alleles, String info);

    private List<Allele> parseAlleles(String[] fields) {

        int chr = Contigs.parseId(fields[0]);
        if (chr == 0 || !unfilteredOrPassed(fields[6])) {
            return List.of();
        }

        int pos = Integer.parseInt(fields[1]);
        //A dbSNP rsID such as rs101432848. In rare cases may be multiple e.g., rs200118651;rs202059104
        String rsId = RsIdParser.parseRsId(fields[2]);
        //Uppercasing shouldn't be necessary acccording to the VCF standard,
        //but occasionally one sees VCF files with lower case for part of the
        //sequences, e.g., to show indels.
        String ref = fields[3].toUpperCase();

        //dbSNP has introduced the concept of multiple minor alleles on the
        //same VCF line with their frequencies reported in same order in the
        //INFO field in the CAF section Because of this had to introduce a loop
        //and move the dbSNP freq parsing to here. Not ideal as ESP processing
        //also goes through this method but does not use the CAF field so
        //should be skipped
        String[] alts = fields[4].toUpperCase().split(",");

        List<Allele> alleles = new ArrayList<>(alts.length);
        for (int i = 0; i < alts.length; i++) {
            Allele allele = makeAllele(chr, pos, ref, alts[i]);
            allele.setRsId(rsId);
            alleles.add(allele);
        }
        return alleles;
    }

    private boolean unfilteredOrPassed(String passField) {
        if (allowedFilterValues.isEmpty()) {
            // if we're ignoring the filters carry on
            return true;
        }

        String[] filters = passField.split(";");
        for (String filter : filters) {
            if (!allowedFilterValues.contains(filter)) {
                return false;
            }
        }
        return true;
    }

    private Allele makeAllele(int chr, int pos, String ref, String alt) {
        VariantTrimmer.VariantPosition variantPosition = variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
        return new Allele(chr, variantPosition.start(), variantPosition.ref(), variantPosition.alt());
    }

}
