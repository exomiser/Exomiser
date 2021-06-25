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
import org.monarchinitiative.exomiser.core.model.SvMetaType;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.monarchinitiative.svart.util.VcfConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class VariantContextConverter {

    private static final Logger logger = LoggerFactory.getLogger(VariantContextConverter.class);

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
    public Variant convertToVariant(@Nonnull VariantContext variantContext, @Nonnull Allele altAllele) {
        Contig contig = vcfConverter.parseContig(variantContext.getContig());
        if (contig.isUnknown()) {
            logger.debug("Unknown contig for {} unable to convert to variant", variantContext);
            return null;
        }
        String id = variantContext.getID();
        int start = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        // Symbolic variants are 'symbolic' in that they have no reported bases and/or contain non-base characters '<>[].'
        String alt = parseAlt(variantContext, altAllele);

        VariantType variantType = VariantType.parseType(ref, alt);

        int altAlleleId = variantContext.getAlleleIndex(altAllele) - 1;
        // TODO: re-enable once moved from VariantFactoryImpl
//        Map<String, SampleGenotype> sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, altAlleleId);

        if (VariantType.isBreakend(alt) || variantType == VariantType.BND || variantType == VariantType.TRA) {
//            ConfidenceInterval startCi = parseConfidenceInterval(variantContext, "CIPOS");
//            ConfidenceInterval endCi = parseConfidenceInterval(variantContext, "CIEND");
//
//            String mateId = variantContext.getAttributeAsString("MATEID", "");
//            String eventId = parseEventId(variantContext);
//
//            return vcfConverter.convertBreakend(contig, id, Position.of(start, startCi), ref, alt, endCi, mateId, eventId);
            // completely disable breakends for the time being as these cause a number of issues.
            logger.debug("Breakend variant {} - skipping conversion", variantContext);
            return null;
        } else if (VariantType.isLargeSymbolic(alt)) {
            int end = variantContext.getCommonInfo().getAttributeAsInt("END", variantContext.getEnd());
            int changeLength = parseChangeLength(variantContext, start, variantType, end);

            Position startPos = Position.of(start, parseConfidenceInterval(variantContext, "CIPOS"));
            Position endPos = Position.of(end, parseConfidenceInterval(variantContext, "CIEND"));

            try {
                // due to the general imprecision and lack of definition about symbolic variants skip any which svart
                // has issues with as svart can be annoyingly precise and inflexible for these types.
                return vcfConverter.convertSymbolic(VariantEvaluation.builder(), contig, id, startPos, endPos, ref, alt, changeLength)
                        .variantContext(variantContext)
                        .altAlleleId(altAlleleId)
                        .id((".".equals(variantContext.getID())) ? "" : variantContext.getID())
//                        .sampleGenotypes(sampleGenotypes)
                        .quality(variantContext.getPhredScaledQual())
                        .build();
            } catch (Exception e) {
                logger.warn("Skipping variant {}-{}-{}-{}-{} due to {}: {}", contig.id(), start, end, ref, alt, e.getClass().getName(), e.getMessage());
            }
            return null;
        }
        return vcfConverter.convert(VariantEvaluation.builder(), contig, id, start, ref, alt)
                .variantContext(variantContext)
                .altAlleleId(altAlleleId)
                .id((".".equals(variantContext.getID())) ? "" : variantContext.getID())
//                .sampleGenotypes(sampleGenotypes)
                .quality(variantContext.getPhredScaledQual())
                .build();
    }

    private String parseAlt(VariantContext variantContext, Allele altAllele) {
        String alt = altAllele.isSymbolic() ? altAllele.getDisplayString() : altAllele.getBaseString();
        if (alt.startsWith("<CN")) {
            if (variantContext.getGenotypes().size() == 1) {
                logger.debug("Single-sample CNV - setting type based on sample CN");
            }
            // CNV, CNV:GAIN, CNV:LOSS, CN0, CN1, CN2...
            // figure out gain or loss from CN, return DEL or DUP
            return "<CNV>";
        }
        return alt;
    }

    private int parseChangeLength(VariantContext variantContext, int start, VariantType variantType, int end) {
        // remember these are 1-based coordinates so need to 'open' the start coordinate
        int changeLength = variantContext.getAttributeAsInt("SVLEN", (start - 1) - end);
        // Manta-specific insertion length - this can be present in other types too so only return it for INS
        if (variantContext.hasAttribute("SVINSLEN") && variantType.baseType() == VariantType.INS) {
            return variantContext.getAttributeAsInt("SVINSLEN", changeLength);
        }
        if (changeLength <= 0 && (SvMetaType.isGain(variantType) || variantType == VariantType.CNV)) {
            // gain should have a positive change length.
            // also return a positive length for CNV types as this is the copy-number over a region, at least as far as Canvas defines it.
            return Math.max(Math.abs(changeLength), 1);
        } else if (changeLength >= 0 && SvMetaType.isLoss(variantType)) {
            // loss should have a negative change length.
            return Math.min(Math.negateExact(changeLength), -1);
        }
        return changeLength;
    }

    private String parseEventId(@Nonnull VariantContext variantContext) {
        if (variantContext.hasAttribute("EVENT")) {
            // Manta-specific event id
            return variantContext.getAttributeAsString("EVENT", "");
        }
        return variantContext.getAttributeAsString("EVENTID", "");
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
