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
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.monarchinitiative.exomiser.core.model.AllelePosition;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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

    private final JannovarVariantAnnotator variantAnnotator;

    //in cases where a variant cannot be positioned on a chromosome we're going to use 0 in order to fulfil the
    //requirement of a variant having an integer chromosome
    private static final int UNKNOWN_CHROMOSOME = 0;

    @Autowired
    public VariantFactory(JannovarData jannovarData) {
        this.variantAnnotator = new JannovarVariantAnnotator(jannovarData);
    }

    public Stream<VariantEvaluation> streamVariantEvaluations(Path vcfPath) {
        return streamVariantEvaluations(streamVariantContexts(vcfPath));
    }

    public Stream<VariantEvaluation> streamVariantEvaluations(Stream<VariantContext> variantContextStream) {
        logger.info("Annotating variant records, trimming sequences and normalising positions...");
        VariantCounter counter = new VariantCounter();
        return variantContextStream
                .peek(counter.countVariantContext())
                .flatMap(toVariantEvaluations())
                .peek(counter.countAnnotatedVariant())
                .onClose(counter::logCount);
    }

    public Stream<VariantContext> streamVariantContexts(Path vcfPath) {
        logger.info("Streaming variants from file {}", vcfPath);
        try (VCFFileReader vcfReader = new VCFFileReader(vcfPath.toFile(), false)) {
            return vcfReader.iterator().stream();
        }
    }

    /**
     * An Exomiser VariantEvaluation is a single-allele variant whereas the VariantContext can have multiple alleles.
     * This means that a multi allele Variant record in a VCF can result in several VariantEvaluations - one for each
     * alternate allele.
     */
    private Function<VariantContext, Stream<VariantEvaluation>> toVariantEvaluations() {
        return variantContext -> variantContext.getAlternateAlleles().stream()
                .map(buildAlleleVariantEvaluation(variantContext))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
    }

    private Function<Allele, Optional<VariantEvaluation>> buildAlleleVariantEvaluation(VariantContext variantContext) {
        return allele -> {
            //alternate Alleles are always after the reference allele, which is 0
            int altAlleleId = variantContext.getAlleleIndex(allele) - 1;
            if (alleleIsObservedInGenotypes(allele, variantContext)) {
                return Optional.of(buildVariantEvaluation(variantContext, altAlleleId));
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
     * Creates a VariantEvaluation made from all the relevant bits of the
     * VariantContext and VariantAnnotations for a given alternative allele.
     *
     * @param variantContext
     * @param altAlleleId
     * @return
     */
    VariantEvaluation buildVariantEvaluation(VariantContext variantContext, int altAlleleId) {
        AllelePosition trimmedAllele = trimVcfAllele(variantContext, altAlleleId);
        VariantAnnotations variantAnnotations = getVariantAnnotations(variantContext, trimmedAllele);
        if (variantAnnotations.hasAnnotation()) {
            return annotatedVariantEvaluation(variantContext, altAlleleId, trimmedAllele, variantAnnotations);
        } else return unAnnotatedVariantEvaluation(variantContext, altAlleleId, trimmedAllele);
    }

    private AllelePosition trimVcfAllele(VariantContext variantContext, int altAlleleId) {
        int vcfPos = variantContext.getStart();
        String vcfRef = variantContext.getReference().getBaseString();
        String vcfAlt = variantContext.getAlternateAllele(altAlleleId).getBaseString();
        return AllelePosition.trim(vcfPos, vcfRef, vcfAlt);
    }

    private VariantAnnotations getVariantAnnotations(VariantContext variantContext, AllelePosition allelePosition) {
        String contig = variantContext.getContig();
        return variantAnnotator.getVariantAnnotations(contig, allelePosition);
    }

    private VariantEvaluation annotatedVariantEvaluation(VariantContext variantContext, int altAlleleId, AllelePosition allelePosition, VariantAnnotations variantAnnotations) {
        int pos = allelePosition.getPos();
        String ref = allelePosition.getRef();
        String alt = allelePosition.getAlt();

        int chr = variantAnnotations.getChr();
        VariantEffect variantEffect = variantAnnotations.getHighestImpactEffect();
        GenomeVariant genomeVariant = variantAnnotations.getGenomeVariant();
        //Attention! highestImpactAnnotation can be null
        Annotation highestImpactAnnotation = variantAnnotations.getHighestImpactAnnotation();
        List<TranscriptAnnotation> annotations = buildTranscriptAnnotations(variantAnnotations.getAnnotations());

        return VariantEvaluation.builder(chr, pos, ref, alt)
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
    private VariantEvaluation unAnnotatedVariantEvaluation(VariantContext variantContext, int altAlleleId, AllelePosition allelePosition) {

        int pos = allelePosition.getPos();
        String ref = allelePosition.getRef();
        String alt = allelePosition.getAlt();

        String chromosomeName = variantContext.getContig();
        logger.trace("Building unannotated variant for {} {} {} {} - assigning to chromosome {}", chromosomeName, pos, ref, alt, UNKNOWN_CHROMOSOME);
        return VariantEvaluation.builder(UNKNOWN_CHROMOSOME, pos, ref, alt)
                .variantContext(variantContext)
                .altAlleleId(altAlleleId)
                .numIndividuals(variantContext.getNSamples())
                //quality is the only value from the VCF file directly required for analysis
                .quality(variantContext.getPhredScaledQual())
                .chromosomeName(chromosomeName)
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
//                .hgvsGenomic(annotation.getGenomicNTChangeStr())
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

    /**
     * Data class for tracking number of annotated variants
     */
    private class VariantCounter {
        final AtomicInteger variantRecords = new AtomicInteger(0);
        final AtomicInteger unannotatedVariants = new AtomicInteger(0);
        final AtomicInteger annotatedVariants = new AtomicInteger(0);
        final Instant start = Instant.now();

        Consumer<VariantContext> countVariantContext() {
            return variantContext -> variantRecords.incrementAndGet();
        }

        Consumer<VariantEvaluation> countAnnotatedVariant() {
            return variantEvaluation -> {
                if (variantEvaluation.hasAnnotations()) {
                    annotatedVariants.incrementAndGet();
                } else {
                    unannotatedVariants.incrementAndGet();
                }
            };
        }

        void logCount() {
            if (unannotatedVariants.get() > 0) {
                logger.info("Processed {} variant records into {} single allele variants, {} are missing annotations, most likely due to non-numeric chromosome designations", variantRecords
                        .get(), annotatedVariants.get(), unannotatedVariants.get());
            } else {
                logger.info("Processed {} variant records into {} single allele variants", variantRecords.get(), annotatedVariants
                        .get());
            }
            Duration duration = Duration.between(start, Instant.now());
            long ms = duration.toMillis();
            logger.info("Variant annotation finished in {}m {}s {}ms ({} ms)", (ms / 1000) / 60 % 60, ms / 1000 % 60, ms % 1000, ms);
        }
    }
}
