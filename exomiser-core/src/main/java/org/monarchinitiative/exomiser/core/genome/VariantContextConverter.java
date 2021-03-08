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

package org.monarchinitiative.exomiser.core.genome;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFConstants;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.monarchinitiative.svart.util.VcfConverter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class VariantContextConverter {

    private final VcfConverter vcfConverter;

    private VariantContextConverter(GenomicAssembly genomicAssembly, VariantTrimmer variantTrimmer) {
        vcfConverter = new VcfConverter(genomicAssembly, variantTrimmer);
    }

    public static VariantContextConverter of(GenomicAssembly genomicAssembly, VariantTrimmer variantTrimmer) {
        Objects.requireNonNull(genomicAssembly);
        Objects.requireNonNull(variantTrimmer);
        return new VariantContextConverter(genomicAssembly, variantTrimmer);
    }

    @Nullable
    public Variant convertToVariant(VariantContext variantContext, Allele altAllele) {
        Contig contig = vcfConverter.parseContig(variantContext.getContig());
        if (contig.isUnknown()) {
            return null;
        }
        String id = variantContext.getID();
        int start = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        // Symbolic variants are 'symbolic' in that they have no reported bases and/or contain non-base characters '<>[].'
        String alt = altAllele.isSymbolic() ? altAllele.getDisplayString() : altAllele.getBaseString();

        VariantType variantType = VariantType.parseType(ref, alt);

        if (VariantType.isBreakend(alt) || variantType == VariantType.BND || variantType == VariantType.TRA) {

            ConfidenceInterval startCi = parseConfidenceInterval(variantContext, "CIPOS");
            ConfidenceInterval endCi = parseConfidenceInterval(variantContext, "CIEND");

            String mateId = variantContext.getAttributeAsString("MATEID", "");
            String eventId = variantContext.getAttributeAsString("EVENTID", "");

            return vcfConverter.convertBreakend(contig, id, Position.of(start, startCi), ref, alt, endCi, mateId, eventId);
        } else if (VariantType.isLargeSymbolic(alt)) {
            int end = variantContext.getCommonInfo().getAttributeAsInt("END", variantContext.getEnd());
            int changeLength = variantContext.getAttributeAsInt("SVLEN", start - end);
            if (changeLength == 0 && variantType.baseType() == VariantType.INS) {
                changeLength = 1;
            }

            Position startPos = Position.of(start, parseConfidenceInterval(variantContext, "CIPOS"));
            Position endPos = Position.of(end, parseConfidenceInterval(variantContext, "CIEND"));

            return vcfConverter.convertSymbolic(contig, id, startPos, endPos, ref, alt, changeLength);
        }
        return vcfConverter.convert(contig, id, start, ref, alt);
    }

    private static VariantType parseAlleleVariantType(VariantContext variantContext, Allele altAllele) {
        // WARNING! variantContext.getStructuralVariantType() IS NOT SAFE! It throws the following exception:
        //  java.lang.IllegalArgumentException: No enum constant htsjdk.variant.variantcontext.StructuralVariantType.SVA
        //  for the line
        //  22   16918023    esv3647185  C   <INS:ME:SVA>    100 PASS    SVLEN=1312;SVTYPE=SVA;TSD=AAAAATACAAAAATTTGC;VT=SV   GT  0|1
        if (altAllele.isSymbolic()) {
            String svTypeString = variantContext.getAttributeAsString(VCFConstants.SVTYPE, "");
            // SV types should not be SMALL so try parsing the alt allele if the SVTYPE field isn't recognised (as in the case of ALU, LINE, SVA from 1000 genomes)
            VariantType parseValue = VariantType.parseType(altAllele.getDisplayString());
            return parseValue == VariantType.SYMBOLIC ? VariantType.parseType(svTypeString) : parseValue;
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
