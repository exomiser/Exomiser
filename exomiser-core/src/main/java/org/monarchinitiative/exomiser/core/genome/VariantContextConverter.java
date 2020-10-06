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

package org.monarchinitiative.exomiser.core.genome;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFConstants;
import org.monarchinitiative.exomiser.core.model.ConfidenceInterval;
import org.monarchinitiative.exomiser.core.model.VariantAllele;
import org.monarchinitiative.exomiser.core.model.VariantType;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class VariantContextConverter {

    private VariantContextConverter() {
    }

    public static VariantAllele toVariantAllele(VariantContext variantContext, Allele altAllele) {
        String contig = variantContext.getContig();
        int start = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        // Symbolic variants are 'symbolic' in that they have no reported bases and/or contain non-base characters '<>[].'
        String alt = (altAllele.isSymbolic()) ? altAllele.getDisplayString() : altAllele.getBaseString();

        if (altAllele.isSymbolic()) {
            VariantType variantType = parseAlleleVariantType(variantContext, altAllele);
            String endContig = variantContext.getCommonInfo().getAttributeAsString("CHR2", contig);
            int end = variantContext.getCommonInfo().getAttributeAsInt("END", variantContext.getEnd());
            ConfidenceInterval startCi = parseConfidenceInterval(variantContext, "CIPOS");
            ConfidenceInterval endCi = parseConfidenceInterval(variantContext, "CIEND");
            int length = variantContext.getAttributeAsInt("SVLEN", end - start);
//            logger.info("Annotating contig={}: start={} ref={} alt={} variantType={} length={} startCi={} endContig={} end={} endCi={}", contig, start, ref, alt, variantType, length, startCi, endContig, end, endCi);
            return VariantAllele.of(contig, start, end, ref, alt, length, variantType, endContig, startCi, endCi);
        }
        // What about 1 ATGC CGTA SVTYPE=INV or T TTTT SYVTYP=DUP ?
        // According to HGVS, which has a much more useful and well described set of rules for determining variant type:
        // When a description is possible according to several types, the preferred description is:
        //   (1) deletion, (2) inversion, (3) duplication, (4) conversion, (5) insertion.
        // - When a variant can be described as a duplication or an insertion, prioritisation determines it should be
        //   described as a duplication.

        return VariantAllele.of(contig, start, ref, alt);
    }

    private static VariantType parseAlleleVariantType(VariantContext variantContext, Allele altAllele) {
        // WARNING! variantContext.getStructuralVariantType() IS NOT SAFE! It throws the following exception:
        //  java.lang.IllegalArgumentException: No enum constant htsjdk.variant.variantcontext.StructuralVariantType.SVA
        //  for the line
        //  22   16918023    esv3647185  C   <INS:ME:SVA>    100 PASS    SVLEN=1312;SVTYPE=SVA;TSD=AAAAATACAAAAATTTGC;VT=SV   GT  0|1
        if (altAllele.isSymbolic()) {
            String svTypeString = variantContext.getAttributeAsString(VCFConstants.SVTYPE, "");
            // SV types should not be SMALL so try parsing the alt allele if the SVTYPE field isn't recognised (as in the case of ALU, LINE, SVA from 1000 genomes)
            VariantType parseValue = VariantType.parseValue(altAllele.getDisplayString());
            return parseValue == VariantType.SYMBOLIC ? VariantType.parseValue(svTypeString) : parseValue;
        }

        return nonSymbolicVariantType(variantContext.getReference(), altAllele);
    }

    private static VariantType nonSymbolicVariantType(Allele refAllele, Allele altAllele) {
        int refLength = refAllele.length();
        int altLength = altAllele.length();
        if (refLength == altLength) {
            if (altLength == 1) {
                return VariantType.SNV;
            }
            return VariantType.MNV;
        }
        return refLength > altLength ? VariantType.DEL : VariantType.INS;
    }

    private static ConfidenceInterval parseConfidenceInterval(VariantContext variantContext, String ciKey) {
        //CIPOS=-56,20 or CIEND=-10,62
        List<String> ciList = variantContext.getCommonInfo().getAttributeAsStringList(ciKey, "");
        if (ciList.isEmpty()) {
            return ConfidenceInterval.precise();
        }
        return ConfidenceInterval.of(Integer.parseInt(ciList.get(0)), Integer.parseInt(ciList.get(1)));
    }
}
