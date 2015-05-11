package de.charite.compbio.exomiser.core.factories;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

import java.util.Arrays;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.InvalidGenomeChange;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderDispatcher;
import de.charite.compbio.jannovar.annotation.builders.AnnotationBuilderOptions;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Helper class for constructing {@link Variant} objects for tests.
 *
 * The construction of {@link Variant} objects is quite complex but for tests,
 * we would ideally have them for testing our data sets. This class helps us
 * with the construction.
 */
public class TestVariantFactory {

    private final ReferenceDictionary refDict;
    private final VariantFactory variantFactory;

    public TestVariantFactory() {
        JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
        this.refDict = testJannovarData.getRefDict();
        this.variantFactory = new VariantFactory(new VariantAnnotationsFactory(testJannovarData));
    }

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
        // build annotation list (for the one transcript we have below only)
        final GenomePosition gPos = new GenomePosition(refDict, Strand.FWD, chrom, pos, PositionType.ZERO_BASED);
        final GenomeVariant change = new GenomeVariant(gPos, ref, alt);
        final AnnotationBuilderDispatcher dispatcher;
        final TranscriptModel tmFGFR2 = TestTranscriptModelFactory.buildTMForFGFR2();
        final TranscriptModel tmSHH = TestTranscriptModelFactory.buildTMForSHH();
        if (tmFGFR2.getTXRegion().contains(gPos)) {
            dispatcher = new AnnotationBuilderDispatcher(tmFGFR2, change, new AnnotationBuilderOptions());
        } else if (tmSHH.getTXRegion().contains(gPos)) {
            dispatcher = new AnnotationBuilderDispatcher(tmSHH, change, new AnnotationBuilderOptions());
        } else {
            dispatcher = new AnnotationBuilderDispatcher(null, change, new AnnotationBuilderOptions());
        }
        final VariantAnnotations annotations;
        try {
            Annotation anno = dispatcher.build();
            if (anno != null) {
                annotations = new VariantAnnotations(change, Arrays.asList(anno));
            } else {
                annotations = new VariantAnnotations(change, Arrays.<Annotation>asList());
            }
        } catch (InvalidGenomeChange e) {
            throw new RuntimeException("Problem building annotation", e);
        }
                
        VariantContext variantContext = constructVariantContext(chrom, pos, ref, alt, gt, readDepth, qual);
        
        return variantFactory.buildAnnotatedVariantEvaluation(variantContext, altAlleleID, annotations);
    }

    public Variant constructVariant(int chrom, int pos, String ref, String alt, Genotype gt, int rd, int altAlleleID) {
        return constructVariant(chrom, pos, ref, alt, gt, rd, altAlleleID, 20.0);
    }

    public VariantContext constructVariantContext(int chrom, int pos, String ref, String alt, Genotype gt, int readDepth) {
        return constructVariantContext(chrom, pos, ref, alt, gt, readDepth, 20.0);
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
