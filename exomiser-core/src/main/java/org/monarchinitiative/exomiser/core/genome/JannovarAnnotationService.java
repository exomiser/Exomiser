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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.SVAnnotations;
import de.charite.compbio.jannovar.annotation.SVAnnotator;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantAnnotator;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.*;
import org.monarchinitiative.exomiser.core.model.StructuralType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper to build Jannovar annotations for variants. CAUTION! This class returns native Jannovar objects which use zero-based
 * coordinates.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarAnnotationService {

    private static final Logger logger = LoggerFactory.getLogger(JannovarAnnotationService.class);

    // Regular expression pattern for matching breakends in VCF.
    private static final Pattern BND_PATTERN = Pattern.compile(
            "^(?<leadingBases>\\w*)(?<firstBracket>[\\[\\]])(?<targetChrom>[^:]+):(?<targetPos>\\w+)(?<secondBracket>[\\[\\]])(?<trailingBases>\\w*)$");

    //in cases where a variant cannot be positioned on a chromosome we're going to use 0 in order to fulfil the
    //requirement of a variant having an integer chromosome
    private static final int UNKNOWN_CHROMOSOME = 0;

    private final ReferenceDictionary referenceDictionary;
    private final VariantAnnotator variantAnnotator;
    private final SVAnnotator structuralVariantAnnotator;

    public JannovarAnnotationService(JannovarData jannovarData) {
        this.referenceDictionary = jannovarData.getRefDict();
        this.variantAnnotator = new VariantAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes(), new AnnotationBuilderOptions());
        this.structuralVariantAnnotator = new SVAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
    }

    /**
     * Takes VCF (forward-strand, one-based) style variants and returns a set of Jannovar {@link VariantAnnotations}.
     *
     * @param contig
     * @param pos
     * @param ref
     * @param alt
     * @return a set of {@link VariantAnnotations} for the given variant coordinates. CAUTION! THE RETURNED ANNOTATIONS
     * WILL USE ZERO-BASED COORDINATES AND WILL BE TRIMMED LEFT SIDE FIRST, ie. RIGHT SHIFTED. This is counter to VCF
     * conventions.
     */
    public VariantAnnotations annotateVariant(String contig, int pos, String ref, String alt) {
        GenomePosition genomePosition = buildGenomePosition(contig, pos);
        GenomeVariant genomeVariant = new GenomeVariant(genomePosition, ref, alt);
        return annotateGenomeVariant(genomeVariant);
    }

    private GenomePosition buildGenomePosition(String contig, int pos) {
        int chr = getIntValueOfChromosomeOrZero(contig);
        return new GenomePosition(referenceDictionary, Strand.FWD, chr, pos, PositionType.ONE_BASED);
    }

    private Integer getIntValueOfChromosomeOrZero(String contig) {
        return referenceDictionary.getContigNameToID().getOrDefault(contig, UNKNOWN_CHROMOSOME);
    }

    private VariantAnnotations annotateGenomeVariant(GenomeVariant genomeVariant) {
        if (genomeVariant.getChr() == UNKNOWN_CHROMOSOME) {
            //Need to check this here and return otherwise the variantAnnotator will throw a NPE.
            return VariantAnnotations.buildEmptyList(genomeVariant);
        }
        try {
            return variantAnnotator.buildAnnotations(genomeVariant);
        } catch (Exception e) {
            logger.debug("Unable to annotate variant {}-{}-{}-{}",
                    genomeVariant.getChrName(),
                    genomeVariant.getPos(),
                    genomeVariant.getRef(),
                    genomeVariant.getAlt(),
                    e);
        }
        return VariantAnnotations.buildEmptyList(genomeVariant);
    }

    public SVAnnotations annotateStructuralVariant(StructuralType structuralType, String alt, String startContig, int startPos, List<Integer> startCiIntervals, String endContig, int endPos, List<Integer> endCiIntervals) {
        GenomePosition start = buildGenomePosition(startContig, startPos);
        GenomePosition end = buildGenomePosition(endContig, endPos);

        SVGenomeVariant svGenomeVariant = buildSvGenomeVariant(structuralType, alt, start, startCiIntervals, end, endCiIntervals);
        // Unsupported types
        if (structuralType == StructuralType.NON_STRUCTURAL) {
            logger.info("{} is not a supported structural type", structuralType);
            return SVAnnotations.buildEmptyList(svGenomeVariant);
        }

        try {
            return structuralVariantAnnotator.buildAnnotations(svGenomeVariant);
        } catch (Exception e) {
            logger.debug("Unable to annotate variant {}-{}-{}",
                    svGenomeVariant.getChrName(),
                    svGenomeVariant.getPos(),
                    svGenomeVariant.getPos2(),
                    e);
        }
        return SVAnnotations.buildEmptyList(svGenomeVariant);
    }

    private SVGenomeVariant buildSvGenomeVariant(StructuralType structuralType, String alt, GenomePosition start, List<Integer> startCiIntervals, GenomePosition end, List<Integer> endCiIntervals) {

        int lowerCiStart = startCiIntervals.get(0);
        int upperCiStart = startCiIntervals.get(1);

        int lowerCiEnd = endCiIntervals.get(0);
        int upperCiEnd = endCiIntervals.get(1);

        StructuralType svSubType = structuralType.getSubType();
        switch (svSubType) {
            case DEL:
                return new SVDeletion(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            case DEL_ME:
                return new SVMobileElementDeletion(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            case DUP:
                return new SVDuplication(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            case DUP_TANDEM:
                return new SVTandemDuplication(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            case INS:
                return new SVInsertion(start, lowerCiStart, upperCiStart);
            case INS_ME:
                return new SVMobileElementInsertion(start, lowerCiStart, upperCiStart);
            case INV:
                return new SVInversion(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            case CNV:
                return new SVCopyNumberVariant(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            case BND:
                return buildBreakendVariant(alt, start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
            default:
                return new SVUnknown(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
        }
    }

    private SVGenomeVariant buildBreakendVariant(String alt, GenomePosition start, GenomePosition end, int lowerCiStart, int upperCiStart, int lowerCiEnd, int upperCiEnd) {
        Matcher matcher = BND_PATTERN.matcher(alt);
        if (matcher.matches()) {
            String firstBracket = matcher.group("firstBracket");
            String secondBracket = matcher.group("secondBracket");
            if (firstBracket.equals(secondBracket)) {
                String contig2 = matcher.group("targetChrom");
                int pos2 = Integer.parseInt(matcher.group("targetPos"));
                GenomePosition gBNDPos2 = buildGenomePosition(contig2, pos2);

                String leadingBases = matcher.group("leadingBases");
                String trailingBases = matcher.group("trailingBases");

                return new SVBreakend(
                        start, gBNDPos2, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd,
                        leadingBases, trailingBases,
                        "]" .equals(firstBracket) ? SVBreakend.Side.LEFT_END : SVBreakend.Side.RIGHT_END);
            }
        }
        logger.error("Invalid BND alternative allele: {}", alt);
        return new SVUnknown(start, end, lowerCiStart, upperCiStart, lowerCiEnd, upperCiEnd);
    }
}
