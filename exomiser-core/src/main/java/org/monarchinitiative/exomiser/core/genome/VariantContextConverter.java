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
import org.monarchinitiative.exomiser.core.model.SvMetaType;
import org.monarchinitiative.svart.*;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
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

    private static final String A = "A";
    private static final String T = "T";
    private static final String G = "G";
    private static final String C = "C";
    private static final String NO_CALL = ".";
    private static final String N = "N";

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
    public GenomicVariant convertToVariant(@Nonnull VariantContext variantContext, @Nonnull Allele altAllele) {
        Contig contig = vcfConverter.parseContig(variantContext.getContig());
        if (contig.isUnknown()) {
            logger.debug("Unknown contig for {} unable to convert to variant", variantContext);
            return null;
        }
        String id = variantContext.hasID() ? variantContext.getID() : "";
        int start = variantContext.getStart();
        String ref = getBaseString(variantContext.getReference().getBases());
        // Symbolic variants are 'symbolic' in that they have no reported bases and/or contain non-base characters '<>[].'
        String alt = parseAlt(variantContext, altAllele);

        VariantType variantType = VariantType.parseType(ref, alt);

        if (VariantType.isBreakend(alt) || variantType == VariantType.BND || variantType == VariantType.TRA) {
            // TODO: enable breakend conversion and annotation - need full end to end test for intergenic and coding sequence BND
//            ConfidenceInterval startCi = parseConfidenceInterval(variantContext, "CIPOS");
//            ConfidenceInterval endCi = parseConfidenceInterval(variantContext, "CIEND");
//
//            String mateId = variantContext.getAttributeAsString("MATEID", "");
//            String eventId = parseEventId(variantContext);
//
//            try {
//                // use convertSymbolic as this will be copied to a GenomicVariant - convert to BreakendVariant when annotating
//                return vcfConverter.convertSymbolic(contig, id, start, startCi, ref, alt, endCi, mateId, eventId);
//            } catch (Exception e) {
//                logger.warn("Skipping variant {}-{}-{}-{} due to {}: {}", contig.id(), start, ref, alt, e.getClass().getName(), e.getMessage());
//            }
            // completely disable breakends for the time being as these cause a number of issues.
            logger.debug("Breakend variant {} - skipping conversion", variantContext);
            return null;
        } else if (VariantType.isLargeSymbolic(alt)) {
            int end = variantContext.getCommonInfo().getAttributeAsInt("END", variantContext.getEnd());
            int changeLength = parseChangeLength(variantContext, start, variantType, end);

            ConfidenceInterval startCi = parseConfidenceInterval(variantContext, "CIPOS");
            ConfidenceInterval endCi = parseConfidenceInterval(variantContext, "CIEND");

            try {
                // due to the general imprecision and lack of definition about symbolic variants skip any which svart
                // has issues with as svart can be annoyingly precise and inflexible for these types.
                return vcfConverter.convertSymbolic(contig, id, start, startCi, end, endCi, ref, alt, changeLength);
            } catch (Exception e) {
                logger.warn("Skipping variant {}-{}-{}-{}-{} due to {}: {}", contig.id(), start, end, ref, alt, e.getClass().getName(), e.getMessage());
            }
            return null;
        }
        return vcfConverter.convert(contig, id, start, ref, alt);
    }

    private String parseAlt(VariantContext variantContext, Allele altAllele) {
        String alt = altAllele.isSymbolic() ? altAllele.getDisplayString() : getBaseString(altAllele.getBases());
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

    private String getBaseString(byte[] bases) {
        // seems petty, but this can save ~200MB RAM and tens of thousands of object allocations
        // on the 4.5 million variant POMP sample.
        if (bases.length == 1) {
            return switch (bases[0]) {
                case 'A' -> A;
                case 'T' -> T;
                case 'G' -> G;
                case 'C' -> C;
                case '.' -> NO_CALL;
                case 'N' -> N;
                default -> N;
            };
        }
        return new String(bases);
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

    private static ConfidenceInterval parseConfidenceInterval(VariantContext variantContext, String ciKey) {
        //CIPOS=-56,20 or CIEND=-10,62
        List<String> ciList = variantContext.getCommonInfo().getAttributeAsStringList(ciKey, "");
        if (ciList.isEmpty()) {
            return ConfidenceInterval.precise();
        }
        return ConfidenceInterval.of(Integer.parseInt(ciList.get(0)), Integer.parseInt(ciList.get(1)));
    }
}
