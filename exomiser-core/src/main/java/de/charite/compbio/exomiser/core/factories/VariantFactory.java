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
        for (VariantContext variantContext : variantContexts) {
            List<VariantAnnotations> variantAlleleAnnotations = variantAnnotator.buildVariantAnnotations(variantContext);
            //What about missing annotations? How should these be handled???
            if (!variantAlleleAnnotations.isEmpty()) {
                //an Exomiser Variant is a single-allele variant the VariantContext can have multiple alleles
                for (int altAlleleId = 0; altAlleleId < variantContext.getAlternateAlleles().size(); ++altAlleleId) {
                    VariantAnnotations variantAnnotations = variantAlleleAnnotations.get(altAlleleId);
                    if (variantAnnotations.hasAnnotation() && variantAnnotations.getGenomeVariant() != null) {
                        //this shouldn't happen, as it should have been dealt with by buildAlleleAnnotations(), but just in case...
                        variants.add(buildVariantEvaluation(variantContext, altAlleleId, variantAnnotations));
                    }
                }
            }
        }
        logger.info("Created {} single allele variants from variant records", variants.size());
        return variants;
    }

    /**
     * @param variantContext
     * @param altAlleleId
     * @param variantAnnotations
     * @return a VariantEvaluation made from all the relevant bits of the
     * VariantContext and VariantAnnotations for a given alternative allele.
     */
    public VariantEvaluation buildVariantEvaluation(VariantContext variantContext, int altAlleleId, VariantAnnotations variantAnnotations) {
        int chr = variantAnnotations.getChr();
        int pos = buildPos(variantAnnotations);
        String ref = buildRef(variantAnnotations);
        String alt = buildAlt(variantAnnotations);

        VariantEffect variantEffect = variantAnnotations.getHighestImpactEffect();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();

        return new VariantEvaluation.VariantBuilder(chr, pos, ref, alt)
                    //HTSJDK derived data
                    .variantContext(variantContext)
                    .altAlleleId(altAlleleId)
                    .quality(variantContext.getPhredScaledQual())
                    .numIndividuals(variantContext.getNSamples())
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
