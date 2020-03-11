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
import org.monarchinitiative.exomiser.core.model.StructuralType;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;

import java.util.List;

/**
 * Handles Annotation of {@link VariantContext} objects to produce {@link VariantAnnotation} objects. This
 * implementation will handle both small and structural variants.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class VariantContextAnnotator {

    protected final VariantAnnotator variantAnnotator;

    public VariantContextAnnotator(VariantAnnotator variantAnnotator) {
        this.variantAnnotator = variantAnnotator;
    }

    public List<VariantAnnotation> annotateAllele(VariantContext variantContext, Allele altAllele) {
        String contig = variantContext.getContig();
        int start = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        // Structural variants are 'symbolic' in that they have no actual reported bases
        String alt = (altAllele.isSymbolic()) ? altAllele.getDisplayString() : altAllele.getBaseString();

        StructuralType structuralType = detectAlleleVariantType(variantContext, altAllele);
        if (structuralType.isStructural()) {
            String endContig = variantContext.getCommonInfo().getAttributeAsString("CHR2", contig);
            int end = variantContext.getCommonInfo().getAttributeAsInt("END", variantContext.getEnd());
            ConfidenceInterval startCi = getConfidenceInterval(variantContext, "CIPOS");
            ConfidenceInterval endCi = getConfidenceInterval(variantContext, "CIEND");
            int length = Math.abs(variantContext.getAttributeAsInt("SVLEN", end - start));
//            logger.info("Annotating {}: {} {} {} {} {} {} {} {} {}", contig, start, ref, alt, structuralType, length, startCi, endContig, end, endCi);
            return variantAnnotator.annotate(contig, start, ref, alt, structuralType, length, startCi, endContig, end, endCi);
        }

        return variantAnnotator.annotate(contig, start, ref, alt);
    }

    private StructuralType detectAlleleVariantType(VariantContext variantContext, Allele altAllele) {
        // WARNING! variantContext.getStructuralVariantType() IS NOT SAFE! It throws the following exception:
        //  java.lang.IllegalArgumentException: No enum constant htsjdk.variant.variantcontext.StructuralVariantType.SVA
        //  for the line
        //  22   16918023    esv3647185  C   <INS:ME:SVA>    100 PASS    SVLEN=1312;SVTYPE=SVA;TSD=AAAAATACAAAAATTTGC;VT=SV   GT  0|1
        if (altAllele.isSymbolic()) {
            String svTypeString = variantContext.getAttributeAsString(VCFConstants.SVTYPE, null);
            // SV types should not be SMALL so try parsing the alt allele if the SVTYPE field isn't recognised (as in the case of ALU, LINE, SVA from 1000 genomes)
            StructuralType parseValue = StructuralType.parseValue(altAllele.getDisplayString());
            return parseValue == StructuralType.UNKNOWN ? StructuralType.parseValue(svTypeString) : parseValue;
        }
        return StructuralType.NON_STRUCTURAL;
    }

    private ConfidenceInterval getConfidenceInterval(VariantContext variantContext, String ciKey) {
        //CIPOS=-56,20 or CIEND=-10,62
        List<String> ciList = variantContext.getCommonInfo().getAttributeAsStringList(ciKey, "");
        if (ciList.isEmpty()) {
            return ConfidenceInterval.empty();
        }
        return ConfidenceInterval.of(Integer.parseInt(ciList.get(0)), Integer.parseInt(ciList.get(1)));
    }
}
