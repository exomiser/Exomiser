/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Produces Variants from VCF files.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactoryImpl implements VariantFactory {

    private static final Logger logger = LoggerFactory.getLogger(VariantFactoryImpl.class);

    private final VariantAnnotator variantAnnotator;

    public VariantFactoryImpl(VariantAnnotator variantAnnotator) {
        this.variantAnnotator = variantAnnotator;
    }

    @Override
    public Stream<VariantEvaluation> createVariantEvaluations(Path vcfPath) {
        return createVariantEvaluations(VcfFiles.readVariantContexts(vcfPath));
    }

    @Override
    public Stream<VariantEvaluation> createVariantEvaluations(Stream<VariantContext> variantContextStream) {
        logger.info("Annotating variant records, trimming sequences and normalising positions...");
        VariantCounter counter = new VariantCounter();
        return variantContextStream
                .peek(counter.countVariantContext())
                .flatMap(toVariantEvaluations())
                .peek(counter.countAnnotatedVariant())
                .onClose(counter::logCount);
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
            if (alleleIsObservedInGenotypes(allele, variantContext.getGenotypes())) {
                return Optional.of(buildVariantEvaluation(variantContext, altAlleleId));
            }
            return Optional.empty();
        };
    }

    // this is required in case of incorrectly merged multi-sample VCF files to remove alleles not represented in the sample genotypes
    private synchronized boolean alleleIsObservedInGenotypes(Allele allele, GenotypesContext genotypesContext) {
        return genotypesContext.stream()
                .map(Genotype::getAlleles)
                .anyMatch(genotypeAlleles -> genotypeAlleles.contains(allele));
    }

    /**
     * Creates a VariantEvaluation made from all the relevant bits of the
     * VariantContext and VariantAnnotations for a given alternative allele.
     *
     * @param variantContext
     * @param altAlleleId
     * @return
     */
    //This is package-private as it is used by the TestVariantFactory
    VariantEvaluation buildVariantEvaluation(VariantContext variantContext, int altAlleleId) {
        VariantAnnotation variantAnnotation = annotateVariantAllele(variantContext, altAlleleId);
        return buildVariantEvaluation(variantContext, altAlleleId, variantAnnotation);
    }

    private VariantAnnotation annotateVariantAllele(VariantContext variantContext, int altAlleleId) {
        String contig = variantContext.getContig();
        int pos = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        String alt = variantContext.getAlternateAllele(altAlleleId).getBaseString();
        return variantAnnotator.annotate(contig, pos, ref, alt);
    }

    private VariantEvaluation buildVariantEvaluation(VariantContext variantContext, int altAlleleId, VariantAnnotation variantAnnotation) {

        //See also notes in InheritanceModeAnnotator.
        Map<String, SampleGenotype> sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, altAlleleId);

        GenomeAssembly genomeAssembly = variantAnnotation.getGenomeAssembly();
        int chr = variantAnnotation.getChromosome();
        String chromosomeName = variantAnnotation.getChromosomeName();
        int pos = variantAnnotation.getPosition();
        String ref = variantAnnotation.getRef();
        String alt = variantAnnotation.getAlt();

        String geneSymbol = variantAnnotation.getGeneSymbol();
        String geneId = variantAnnotation.getGeneId();
        VariantEffect variantEffect = variantAnnotation.getVariantEffect();
        List<TranscriptAnnotation> annotations = variantAnnotation.getTranscriptAnnotations();

        return VariantEvaluation.builder(chr, pos, ref, alt)
                .genomeAssembly(genomeAssembly)
                //HTSJDK derived data are used for writing out the
                //HTML (VariantEffectCounter) VCF/TSV-VARIANT formatted files
                //can be removed from InheritanceModeAnalyser as Jannovar 0.18+ is not reliant on the VariantContext
                //need most/all of the info in order to write it all out again.
                //If we could remove this direct dependency the RAM usage can be halved such that a SPARSE analysis of the POMP sample can be held comfortably in 8GB RAM
                //To do this we could just store the string value here - it can be re-hydrated later. See TestVcfParser
                .variantContext(variantContext)
                .altAlleleId(altAlleleId)
                .sampleGenotypes(sampleGenotypes)
                //quality is the only value from the VCF file directly required for analysis
                .quality(variantContext.getPhredScaledQual())
                //jannovar derived data
                .chromosomeName(chromosomeName)
                .geneSymbol(geneSymbol)
                //This used to be an ENTREZ gene identifier, but could now be anything.
                .geneId(geneId)
                .variantEffect(variantEffect)
                .annotations(annotations)
                .build();
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
                if (variantEvaluation.hasTranscriptAnnotations()) {
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
