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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantAnnotations;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.InvalidCoordinatesException;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Produces Variants from VCF files.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class VariantFactory {

    private static final Logger logger = LoggerFactory.getLogger(VariantFactory.class);

    private final VariantContextAnnotator variantAnnotator;

    //in cases where a variant cannot be positioned on a chromosome we're going to use 0 in order to fulfil the
    //requirement of a variant having an integer chromosome
    private static final int UNKNOWN_CHROMOSOME = 0;

    @Autowired
    public VariantFactory(JannovarData jannovarData) {
        this.variantAnnotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
    }

    public Stream<VariantContext> streamVariantContexts(Path vcfPath) {
        logger.info("Streaming variants from file {}", vcfPath);
        try (VCFFileReader vcfReader = new VCFFileReader(vcfPath.toFile(), false)) {
            return vcfReader.iterator().stream();
        }
    }

    public Stream<VariantEvaluation> streamVariantEvaluations(Path vcfPath) {
        //note - VariantContexts with with unknown references will not create a Variant.
        final AtomicInteger variantRecords = new AtomicInteger(0);
        final AtomicInteger unannotatedVariants = new AtomicInteger(0);
        final AtomicInteger annotatedVariants = new AtomicInteger(0);

        Stream<VariantEvaluation> variantEvaluationStream = streamVariantContexts(vcfPath)
                .map(logVariantContextCount(variantRecords))
                .flatMap(streamVariantEvaluations())
                .map(logAnnotatedVariantCount(unannotatedVariants, annotatedVariants));

        logger.info("Annotating variant records, trimming sequences and normalising positions...");
        return variantEvaluationStream
                .onClose(() -> {
                    if (unannotatedVariants.get() > 0) {
                        logger.info("Processed {} variant records into {} single allele variants, {} are missing annotations, most likely due to non-numeric chromosome designations", variantRecords.get(), annotatedVariants.get(), unannotatedVariants.get());
                    } else {
                        logger.info("Processed {} variant records into {} single allele variants", variantRecords.get(), annotatedVariants.get());
                    }
                });
    }

    public Stream<VariantEvaluation> streamVariantEvaluations(Stream<VariantContext> variantContextStream) {
        return variantContextStream.flatMap(streamVariantEvaluations());
    }

    private Function<VariantContext, VariantContext> logVariantContextCount(AtomicInteger variantRecords) {
        return variantContext -> {
            variantRecords.incrementAndGet();
            return variantContext;
        };
    }

    private Function<VariantEvaluation, VariantEvaluation> logAnnotatedVariantCount(AtomicInteger unannotatedVariants, AtomicInteger annotatedVariants) {
        return variantEvaluation -> {
            if (variantEvaluation.hasAnnotations()) {
                annotatedVariants.incrementAndGet();
            } else {
                unannotatedVariants.incrementAndGet();
            }
            return variantEvaluation;
        };
    }

    /**
     * An Exomiser VariantEvaluation is a single-allele variant whereas the VariantContext can have multiple alleles.
     * This means that a multi allele Variant record in a VCF can result in several VariantEvaluations - one for each
     * alternate allele.
     */
    private Function<VariantContext, Stream<VariantEvaluation>> streamVariantEvaluations() {
        return variantContext -> {
            final List<VariantAnnotations> variantAlleleAnnotations = buildVariantAnnotations(variantContext);
//            logger.info("Making variantEvaluations for alternate alleles {}:{} {} {}", variantContext.getContig(), variantContext.getStart(), variantContext.getAlleles(), variantContext.getGenotypes());
            return variantContext.getAlternateAlleles().stream()
                    .map(buildAlleleVariantEvaluation(variantContext, variantAlleleAnnotations))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        };
    }

    /**
     * Returns a list of variants of known reference. If a VariantContext has no
     * known reference on the genome an empty list will be returned.
     *
     * @param variantContext {@link VariantContext} to get {@link Variant} objects for
     * @return one {@link Variant} object for each alternative allele in vc.
     */
    public List<VariantAnnotations> buildVariantAnnotations(VariantContext variantContext) {
        try {
            //builds one annotation list for each alternative allele
            //beware - this needs synchronisation in jannovar versions 0.16 and below
            synchronized (this) {
                return variantAnnotator.buildAnnotations(variantContext);
            }
        } catch (InvalidCoordinatesException ex) {
            //Not all genes can be assigned to a chromosome, so these will fail here.
            //Should we report these? They will not be used in the analysis or appear in the output anywhere.
            logger.trace("Cannot build annotations for VariantContext {} {} {}: {}", variantContext.getContig(), variantContext.getStart(), variantContext.getAlleles(), ex);
        }
        return Collections.emptyList();
    }

    private Function<Allele, Optional<VariantEvaluation>> buildAlleleVariantEvaluation(VariantContext variantContext, List<VariantAnnotations> variantAlleleAnnotations) {
        return allele -> {
            //alternate Alleles are always after the reference allele, which is 0
            int altAlleleId = variantContext.getAlleleIndex(allele) - 1;
            //loggers are commented out as even using debug this adds considerable overhead when checking millions of variants
//            logger.info("checking allele {} altAlleleId={}", allele, altAlleleId);
            if (alleleIsObservedInGenotypes(allele, variantContext)) {
//                logger.info("Alt allele {} observed in samples", variantContext.getAlternateAllele(altAlleleId));
                if (variantAlleleAnnotations.isEmpty()) {
                    return Optional.of(buildUnknownVariantEvaluation(variantContext, altAlleleId));
                } else {
                    VariantAnnotations variantAnnotations = variantAlleleAnnotations.get(altAlleleId);
                    return Optional.of(buildAnnotatedVariantEvaluation(variantContext, altAlleleId, variantAnnotations));
                }
            }
            return Optional.empty();
        };
    }

    private boolean alleleIsObservedInGenotypes(Allele allele, VariantContext variantContext) {
        return variantContext.getGenotypes().stream().anyMatch(alleleObservedInGenotype(allele));
    }

    private Predicate<Genotype> alleleObservedInGenotype(Allele allele) {
        return genotype -> genotype.getAlleles().stream().anyMatch(allele::equals);
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
        final String chromosomeName = variantContext.getContig();
        final String ref = variantContext.getReference().getBaseString();
        final String alt = variantContext.getAlternateAllele(altAlleleId).getBaseString();
        final int pos = variantContext.getStart();

        logger.trace("Building unannotated variant for {} {} {} {} - assigning to chromosome {}", chromosomeName, pos, ref, alt, UNKNOWN_CHROMOSOME);
        return new VariantEvaluation.Builder(UNKNOWN_CHROMOSOME, pos, ref, alt)
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
    VariantEvaluation buildAnnotatedVariantEvaluation(VariantContext variantContext, int altAlleleId, VariantAnnotations variantAnnotations) {
        int chr = variantAnnotations.getChr();
        int pos = buildPos(variantAnnotations);
        String ref = buildRef(variantAnnotations);
        String alt = buildAlt(variantAnnotations);

        VariantEffect variantEffect = variantAnnotations.getHighestImpactEffect();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();

        List<TranscriptAnnotation> annotations = buildTranscriptAnnotations(variantAnnotations.getAnnotations());

        return new VariantEvaluation.Builder(chr, pos, ref, alt)
                //HTSJDK derived data are only used for writing out the
                //HTML (VariantEffectCounter) VCF/TSV-VARIANT formatted files
                //can be removed from InheritanceModeAnalyser as Jannovar 0.18+ is not reliant on the VariantContext
                //need most/all of the info in order to write it all out again.
                //TODO: remove this direct dependency without it the RAM usage can be halved such that a SPARSE analysis of the POMP sample can be held comfortably in 8GB RAM
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
                .annotations(annotations)
                .build();
    }

    private List<TranscriptAnnotation> buildTranscriptAnnotations(List<Annotation> annotations) {
        List<TranscriptAnnotation> transcriptAnnotations = new ArrayList<>(annotations.size());
        for (Annotation annotation : annotations) {
            transcriptAnnotations.add(toTranscriptAnnotation(annotation));
        }
        return transcriptAnnotations;
    }

    private TranscriptAnnotation toTranscriptAnnotation(Annotation annotation) {
         return TranscriptAnnotation.builder()
                .variantEffect(annotation.getMostPathogenicVarType())
                .accession(getTranscriptAccession(annotation))
                .geneSymbol(buildGeneSymbol(annotation))
                .hgvsCdna(annotation.getCDSNTChangeStr())
                .hgvsProtein(annotation.getProteinChangeStr())
                .distanceFromNearestGene(getDistFromNearestGene(annotation))
                .build();
    }

    private String getTranscriptAccession(Annotation annotation) {
        TranscriptModel transcriptModel = annotation.getTranscript();
         if (transcriptModel == null) {
             return "";
         }
        return transcriptModel.getAccession();
    }

    private int getDistFromNearestGene(Annotation annotation) {

        TranscriptModel tm = annotation.getTranscript();
        if (tm == null) {
            return Integer.MIN_VALUE;
        }
        GenomeVariant change = annotation.getGenomeVariant();
        Set<VariantEffect> effects = annotation.getEffects();
        if (effects.contains(VariantEffect.INTERGENIC_VARIANT) || effects.contains(VariantEffect.UPSTREAM_GENE_VARIANT) || effects.contains(VariantEffect.DOWNSTREAM_GENE_VARIANT)) {
            if (change.getGenomeInterval().isLeftOf(tm.getTXRegion().getGenomeBeginPos()))
                return tm.getTXRegion().getGenomeBeginPos().differenceTo(change.getGenomeInterval().getGenomeEndPos());
            else
                return change.getGenomeInterval().getGenomeBeginPos().differenceTo(tm.getTXRegion().getGenomeEndPos());
        }

        return Integer.MIN_VALUE;
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
        if (transcriptModel == null) {
            return -1;
        }
        Map<String, String> altGeneIds = transcriptModel.getAltGeneIDs();
        String entrezId = altGeneIds.getOrDefault("ENTREZ_ID", "");
        if (entrezId.isEmpty()) {
            return -1;
        }
        // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
        // for UCSC. At this point, there is a hard dependency on using the UCSC database.
        return Integer.parseInt(entrezId);
    }

    private String buildGeneSymbol(Annotation annotation) {
        if (annotation == null || annotation.getGeneSymbol() == null) {
            return ".";
        } else {
            return annotation.getGeneSymbol();
        }
    }
}
