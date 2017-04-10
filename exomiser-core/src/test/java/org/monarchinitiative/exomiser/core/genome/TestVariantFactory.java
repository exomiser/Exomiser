/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.InvalidGenomeVariant;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderDispatcher;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.reference.*;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Arrays;

/**
 * Helper class for constructing {@link Variant} objects for tests.
 *
 * The construction of {@link Variant} objects is quite complex but for tests,
 * we would ideally have them for testing our data sets. This class helps us
 * with the construction.
 */
public class TestVariantFactory {

    private final ReferenceDictionary refDict = TestFactory.getDefaultRefDict();
    private final VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory();

    private final TranscriptModel tmFGFR2 = TestTranscriptModelFactory.buildTMForFGFR2();
    private final TranscriptModel tmSHH = TestTranscriptModelFactory.buildTMForSHH();

    /**
     * Construct a new {@link Variant} object with the given values.
     *
     * @param chrom numeric chromosome id
     * @param pos zero-based position of the variant
     * @param ref reference string
     * @param alt alt string
     * @param gt the Genotype to use
     * @param readDepth depth the read depth to use
     * @param altAlleleID alternative allele ID
     * @param qual phred-scale quality
     * @return {@link Variant} with the setting
     */
    public VariantEvaluation constructVariant(int chrom, int pos, String ref, String alt, Genotype gt, int readDepth, int altAlleleID, double qual) {
        VariantContext variantContext = constructVariantContext(chrom, pos, ref, alt, gt, readDepth, qual);
        VariantAnnotations annotations = buildVariantAnnotations(chrom, pos, ref, alt);

        return variantFactory.buildAnnotatedVariantEvaluation(variantContext, altAlleleID, annotations);
    }

    private VariantAnnotations buildVariantAnnotations(int chrom, int pos, String ref, String alt) {
        // build annotation list (for the one transcript we have below only)
        final GenomePosition genomePosition = new GenomePosition(refDict, Strand.FWD, chrom, pos, PositionType.ZERO_BASED);
        final GenomeVariant genomeVariant = new GenomeVariant(genomePosition, ref, alt);
        final AnnotationBuilderDispatcher dispatcher;
        if (tmFGFR2.getTXRegion().contains(genomePosition)) {
            dispatcher = new AnnotationBuilderDispatcher(tmFGFR2, genomeVariant, new AnnotationBuilderOptions());
        } else if (tmSHH.getTXRegion().contains(genomePosition)) {
            dispatcher = new AnnotationBuilderDispatcher(tmSHH, genomeVariant, new AnnotationBuilderOptions());
        } else {
            dispatcher = new AnnotationBuilderDispatcher(null, genomeVariant, new AnnotationBuilderOptions());
        }
        final VariantAnnotations annotations;
        try {
            Annotation anno = dispatcher.build();
            if (anno != null) {
                annotations = new VariantAnnotations(genomeVariant, Arrays.asList(anno));
            } else {
                annotations = new VariantAnnotations(genomeVariant, Arrays.asList());
            }
        } catch (InvalidGenomeVariant e) {
            throw new RuntimeException("Problem building annotation", e);
        }
        return annotations;
    }

    public VariantContext constructVariantContext(int chrom, int pos, String ref, String alt, Genotype gt, int readDepth, double qual) {
        Allele refAllele = Allele.create(ref, true);
        Allele altAllele = Allele.create(alt);
        VariantContextBuilder vcBuilder = new VariantContextBuilder();

        // build Genotype
        GenotypeBuilder gtBuilder = new GenotypeBuilder("sample");
        setGenotype(gtBuilder, refAllele, altAllele, gt);
        gtBuilder.attribute("RD", readDepth);
        // System.err.println(gtBuilder.make().toString());

        // build VariantContext
        vcBuilder.loc("chr" + chrom, pos + 1, pos + ref.length());
        vcBuilder.alleles(Arrays.asList(refAllele, altAllele));
        vcBuilder.genotypes(gtBuilder.make());
        vcBuilder.attribute("RD", readDepth);
        vcBuilder.log10PError(-0.1 * qual);
        // System.err.println(vcBuilder.make().toString());

        return vcBuilder.make();
    }

    private void setGenotype(GenotypeBuilder gtb, Allele refAllele, Allele altAllele, Genotype gt) {
        switch (gt) {
            case HOMOZYGOUS_ALT:
                gtb.alleles(Arrays.asList(altAllele, altAllele));
                break;
            case HOMOZYGOUS_REF:
                gtb.alleles(Arrays.asList(refAllele, refAllele));
                break;
            case HETEROZYGOUS:
                gtb.alleles(Arrays.asList(refAllele, altAllele));
                break;
            default:
                break;
        }
    }

}
