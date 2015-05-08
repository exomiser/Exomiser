/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces Variants from VCF files.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactory {

    private static final Logger logger = LoggerFactory.getLogger(VariantFactory.class);

    private final VariantAnnotationsFactory variantAnnotator;
    /*in cases where a variant cannot be positioned on a chromosome we're going 
     * to use 0 in order to fulfil the requirement of a variant having an integer chromosome 
     */
    private final int UNKNOWN_CHROMOSOME = 0;

    public VariantFactory(VariantAnnotationsFactory variantAnnotator) {
        this.variantAnnotator = variantAnnotator;
    }

    public List<VariantContext> createVariantContexts(VCFFileReader vcfReader) {
        logger.info("Loading variants from VCF...");
        List<VariantContext> records = new ArrayList<>();
        for (VariantContext vc : vcfReader) {
            records.add(vc);
        }
        vcfReader.close();
        logger.info("Created {} variant records from VCF", records.size());
        return records;
    }

    public List<VariantEvaluation> createVariantEvaluations(VCFFileReader vcfFileReader) {
        List<VariantContext> variantContexts = createVariantContexts(vcfFileReader);

        //note - VariantContexts with with unknown references will not create a Variant.
        logger.info("Annotating variant records, trimming sequences and normalising positions...");
        List<VariantEvaluation> variants = new ArrayList<>(variantContexts.size());
        // build Variant objects from VariantContexts
        int unannotatedVariants = 0;
        for (VariantContext variantContext : variantContexts) {
            List<VariantAnnotations> variantAlleleAnnotations = variantAnnotator.buildVariantAnnotations(variantContext);
            //What about missing annotations? How should these be handled???
            if (variantAlleleAnnotations.isEmpty()) {
                for (int altAlleleId = 0; altAlleleId < variantContext.getAlternateAlleles().size(); ++altAlleleId) {
                    unannotatedVariants++;
                    variants.add(buildUnAnnotatedVariantEvaluation(variantContext, altAlleleId));
                }
            } else {
                //an Exomiser Variant is a single-allele variant the VariantContext can have multiple alleles
                for (int altAlleleId = 0; altAlleleId < variantContext.getAlternateAlleles().size(); ++altAlleleId) {
                    VariantAnnotations variantAnnotations = variantAlleleAnnotations.get(altAlleleId);
                    variants.add(buildAnnotatedVariantEvaluation(variantContext, altAlleleId, variantAnnotations));
                }
            }
        }
        logger.info("Created {} single allele variants from {} variant records - {} are missing annotations, most likely due to non-numeric chromosome designations", variants.size(), variantContexts.size(), unannotatedVariants);
        return variants;
    }

    /**
     * A basic VariantEvaluation for an alternative allele described by the
     * VariantContext. These positions will not be trimmed or annotated by
     * Jannovar. This method is only provided for completeness so that users can
     * have a list of variants which were not used in any analyses.
     *
     * @param variantContext
     * @param altAlleleId
     * @return
     */
    private VariantEvaluation buildUnAnnotatedVariantEvaluation(VariantContext variantContext, int altAlleleId) {
        // Build the GenomeChange object.
        final String chromosomeName = variantContext.getChr();
        final String ref = variantContext.getReference().getBaseString();
        final String alt = variantContext.getAlternateAllele(altAlleleId).getBaseString();
        final int pos = variantContext.getStart();

        logger.info("Building unannotated variant for {} {} {} {} - assigning to chromosome {}", chromosomeName, pos, ref, alt, UNKNOWN_CHROMOSOME);
        return new VariantEvaluation.VariantBuilder(UNKNOWN_CHROMOSOME, pos, ref, alt)
                .variantContext(variantContext)
                .altAlleleId(altAlleleId)
                .numIndividuals(variantContext.getNSamples())
                //quality is the only value from the VCF file directly required for analysis
                .quality(variantContext.getPhredScaledQual())
                .chromosomeName(chromosomeName)
                .build();
    }

    /**
     * Creates a VariantEvaluation made from all the relevant bits of the
     * VariantContext and VariantAnnotations for a given alternative allele.
     *
     * @param variantContext
     * @param altAlleleId
     * @param variantAnnotations
     * @return
     */
    public VariantEvaluation buildAnnotatedVariantEvaluation(VariantContext variantContext, int altAlleleId, VariantAnnotations variantAnnotations) {
        int chr = variantAnnotations.getChr();
        int pos = buildPos(variantAnnotations);
        String ref = buildRef(variantAnnotations);
        String alt = buildAlt(variantAnnotations);

        VariantEffect variantEffect = variantAnnotations.getHighestImpactEffect();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();

        return new VariantEvaluation.VariantBuilder(chr, pos, ref, alt)
                //HTSJDK derived data are only used for writing out the
                //VCF/TSV-VARIANT formatted files 
                .variantContext(variantContext)
                .altAlleleId(altAlleleId)
                .numIndividuals(variantContext.getNSamples())
                //quality is the only value from the VCF file directly required for analysis
                .quality(variantContext.getPhredScaledQual())
                //jannovar derived data
                .chromosomeName(genomeVariant.getChrName())
                .isOffExome(variantEffect.isOffExome())
                .geneSymbol(buildGeneSymbol(highestImpactAnnotation))
                .geneId(buildGeneId(highestImpactAnnotation))
                .variantEffect(variantEffect)
                .annotations(variantAnnotations.getAnnotations())
                .build();
    }

    /**
     * Jannovar uses a zero-based coordinate system but currently exomiser uses
     * a one-based system which matches what is seen in VCF files. We'll keep
     * using a one-based system until zero-based becomes the norm.
     *
     * @param variantAnnotations
     * @return
     */
    private int buildPos(VariantAnnotations variantAnnotations) {
        if (variantAnnotations.getRef().equals("")) {
            return variantAnnotations.getPos();
        } else {
            return variantAnnotations.getPos() + 1;
        }
    }

    private String buildRef(VariantAnnotations variantAnnotations) {
        if (variantAnnotations.getRef().equals("")) {
            return "-";
        } else {
            return variantAnnotations.getRef();
        }
    }

    private String buildAlt(VariantAnnotations variantAnnotations) {
        if (variantAnnotations.getAlt().equals("")) {
            return "-";
        } else {
            return variantAnnotations.getAlt();
        }
    }

    private int buildGeneId(Annotation annotation) {
        if (annotation == null) {
            return -1;
        }

        final TranscriptModel transcriptModel = annotation.getTranscript();
        if (transcriptModel == null || transcriptModel.getGeneID() == null || transcriptModel.getGeneID().equals("null")) {
            return -1;
        }
        // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
        // for UCSC. At this point, there is a hard dependency on using the UCSC database.
        return Integer.parseInt(transcriptModel.getGeneID().substring("ENTREZ".length()));
    }

    private String buildGeneSymbol(Annotation annotation) {
        if (annotation == null || annotation.getGeneSymbol() == null) {
            return ".";
        } else {
            return annotation.getGeneSymbol();
        }
    }
}
