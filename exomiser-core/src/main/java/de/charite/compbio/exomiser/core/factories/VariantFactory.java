/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * Produces Variants from VCF files.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactory {

    private static final Logger logger = LoggerFactory.getLogger(VariantFactory.class);

    private final VariantAnnotator variantAnnotator;

    //in cases where a variant cannot be positioned on a chromosome we're going to use 0 in order to fulfil the
    //requirement of a variant having an integer chromosome
    private final int UNKNOWN_CHROMOSOME = 0;

    public VariantFactory(VariantAnnotator variantAnnotator) {
        this.variantAnnotator = variantAnnotator;
    }

    public List<VariantContext> createVariantContexts(Path vcfPath) {
        logger.info("Loading variants...");
        try (Stream<VariantContext> variantContextStream = streamVariantContexts(vcfPath)) {
            List<VariantContext> records = variantContextStream.collect(toList());
            logger.info("Created {} variant records from file {}", records.size(), vcfPath);
            return records;
        }
    }

    public Stream<VariantContext> streamVariantContexts(Path vcfPath) {
        logger.info("Streaming variants from file {}", vcfPath);
        VCFFileReader vcfReader = new VCFFileReader(vcfPath.toFile(), false); // false => do not require index
        Iterable<VariantContext> variantIterable = () -> vcfReader.iterator();
        boolean runParallel = false;
        return StreamSupport.stream(variantIterable.spliterator(), runParallel);
    }

    public List<VariantEvaluation> createVariantEvaluations(Path vcfPath) {
        Stream<VariantEvaluation> variantEvaluationStream = streamVariantEvaluations(vcfPath);
        List<VariantEvaluation> variantEvaluations = variantEvaluationStream.collect(toList());
        variantEvaluationStream.close();
        return variantEvaluations;
    }

    public Stream<VariantEvaluation> streamVariantEvaluations(Path vcfPath) {
        //note - VariantContexts with with unknown references will not create a Variant.
        int[] variantRecords = {0};
        int[] unannotatedVariants = {0};
        int[] annotatedVariants = {0};

        Stream<VariantEvaluation> variantEvaluationStream = streamVariantContexts(vcfPath).flatMap(variantContext -> {
            variantRecords[0]++;
            List<VariantEvaluation> variantEvaluations = new ArrayList<>();
            //TODO: this looks like a stateful stream - can't this bit return a stream of variantEvaluations?
            List<VariantAnnotations> variantAlleleAnnotations = variantAnnotator.buildVariantAnnotations(variantContext);
            if (variantAlleleAnnotations.isEmpty()) {
                for (int altAlleleId = 0; altAlleleId < variantContext.getAlternateAlleles().size(); ++altAlleleId) {
                    unannotatedVariants[0]++;
                    variantEvaluations.add(buildUnknownVariantEvaluation(variantContext, altAlleleId));
                }
            } else {
                //an Exomiser Variant is a single-allele variant the VariantContext can have multiple alleles
                for (int altAlleleId = 0; altAlleleId < variantContext.getAlternateAlleles().size(); ++altAlleleId) {
                    annotatedVariants[0]++;
                    VariantAnnotations variantAnnotations = variantAlleleAnnotations.get(altAlleleId);
                    variantEvaluations.add(buildAnnotatedVariantEvaluation(variantContext, altAlleleId, variantAnnotations));
                }
            }
            return variantEvaluations.stream();
        });
        logger.info("Annotating variant records, trimming sequences and normalising positions...");
        return variantEvaluationStream
                .onClose(() -> {
                    if (unannotatedVariants[0] > 0) {
                        logger.info("Processed {} variant records into {} single allele variants, {} are missing annotations, most likely due to non-numeric chromosome designations", variantRecords[0], annotatedVariants[0], unannotatedVariants[0]);
                    } else {
                        logger.info("Processed {} variant records into {} single allele variants", variantRecords[0], annotatedVariants[0]);
                    }
                });
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
    private VariantEvaluation buildUnknownVariantEvaluation(VariantContext variantContext, int altAlleleId) {
        // Build the GenomeChange object.
        final String chromosomeName = variantContext.getChr();
        final String ref = variantContext.getReference().getBaseString();
        final String alt = variantContext.getAlternateAllele(altAlleleId).getBaseString();
        final int pos = variantContext.getStart();

        logger.trace("Building unannotated variant for {} {} {} {} - assigning to chromosome {}", chromosomeName, pos, ref, alt, UNKNOWN_CHROMOSOME);
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
    protected VariantEvaluation buildAnnotatedVariantEvaluation(VariantContext variantContext, int altAlleleId, VariantAnnotations variantAnnotations) {
        int chr = variantAnnotations.getChr();
        int pos = buildPos(variantAnnotations);
        String ref = buildRef(variantAnnotations);
        String alt = buildAlt(variantAnnotations);

        VariantEffect variantEffect = getVariantEffect(variantAnnotations, chr, pos);
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

    private VariantEffect getVariantEffect(VariantAnnotations variantAnnotations, int chr, int pos) {
        return variantAnnotations.getHighestImpactEffect();
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
