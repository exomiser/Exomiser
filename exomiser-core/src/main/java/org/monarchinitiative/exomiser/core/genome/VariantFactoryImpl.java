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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.SampleGenotypes;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

    private final GenomeAssembly genomeAssembly;
    private final VariantAnnotator variantAnnotator;
    private final VariantContextConverter variantContextConverter;

    private final VcfReader vcfReader;

    public VariantFactoryImpl(VariantAnnotator variantAnnotator, Path vcfPath) {
        this(variantAnnotator, new VcfFileReader(vcfPath));
    }

    public VariantFactoryImpl(VariantAnnotator variantAnnotator, VcfReader vcfReader) {
        Objects.requireNonNull(variantAnnotator);
        Objects.requireNonNull(vcfReader);
        this.variantAnnotator = variantAnnotator;
        this.genomeAssembly = variantAnnotator.genomeAssembly();
        this.variantContextConverter = VariantContextConverter.of(genomeAssembly.genomicAssembly(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        this.vcfReader = vcfReader;
    }

    @Override
    public Stream<VariantEvaluation> createVariantEvaluations() {
        logger.info("Annotating variant records, trimming sequences and normalising positions...");
        VariantCounter counter = new VariantCounter();
        return vcfReader.readVariantContexts()
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
                .map(buildAlleleVariantEvaluations(variantContext))
                .flatMap(Collection::stream);
    }

    private Function<Allele, List<VariantEvaluation>> buildAlleleVariantEvaluations(VariantContext variantContext) {
        return altAllele -> {
            // Itererating by alleleId here this is less clean, but faster
            // alternate Alleles are always after the reference allele, which is 0
            int altAlleleId = variantContext.getAlleleIndex(altAllele) - 1;
            GenotypesContext genotypes = variantContext.getGenotypes();
            // n.b. samples with no genotypes (e.g. ./. or .) will return no variants
            if (alleleIsObservedInGenotypes(altAllele, genotypes) || hasCopyNumber(altAllele, genotypes)) {
                return buildVariantEvaluations(variantContext, altAlleleId, altAllele);
            }
            logger.debug("Skipping allele {} - has no genotype or copy-number", altAllele);
            return List.of();
        };
    }

    // this is required in case of incorrectly merged multi-sample VCF files to remove alleles not represented in the sample genotypes
    // however sometimes genotyping isn't run...
    private synchronized boolean alleleIsObservedInGenotypes(Allele allele, GenotypesContext genotypesContext) {
        for (Genotype genotype : genotypesContext) {
            List<Allele> genotypeAlleles = genotype.getAlleles();
            if (genotypeAlleles.contains(allele)) {
                logger.trace("Allele {} found in genotype {}", allele, genotype);
                return true;
            }
        }
        logger.debug("Allele {} has no genotype {}", allele, genotypesContext);
        return false;
    }

    // .. but if there is a CN (copy number) attribute, we might be able to make a guess
    private boolean hasCopyNumber(Allele allele, GenotypesContext genotypesContext) {
        for (Genotype genotype : genotypesContext) {
            if (genotype.hasExtendedAttribute("CN")) {
                logger.trace("Found copy number variant {}", genotype);
                return true;
            }
        }
        logger.debug("Allele {} has no reported copy number", allele);
        return false;
    }

    /**
     * Creates a VariantEvaluation made from all the relevant bits of the
     * VariantContext and VariantAnnotations for a given alternative allele.
     */
    private List<VariantEvaluation> buildVariantEvaluations(VariantContext variantContext, int altAlleleId, Allele altAllele) {
        // It is possible for a variant to overlap two or more genes (see issue https://github.com/exomiser/Exomiser/issues/294)
        // so we're expecting a single gene per variant annotation which might have different variant consequences and different
        // phenotypes for each gene
        // TODO: Plan for Breakends...
        //  Return List<VariantAnnotation> from VariantAnnotator (done)
        //  Make VariantAnnotation a simple data class NOT extending AbstractVariant class (done)
        //  REMOVE AbstractVariant
        //  Go straight to creating a VariantEvaluation.
        //  REASON: Creating a Variant from a VariantContext can result in a BreakendVariant being created which
        //  throws an exception when being used with the VariantEvaluation.with constructor. In fact all Exomiser Variant
        //  classes extend BaseVariant which means breakends *cannot* currently be analysed.

        //  VariantContext - (convert) -> GenomicVariant - (annotate) -> VariantAnnotations - (build)-> VariantEvaluation
        GenomicVariant variant = variantContextConverter.convertToVariant(variantContext, altAllele);
        logger.trace("Converted variant context {} alt allele {} to {}", variantContext, altAlleleId, variant);
        if (variant == null) {
            return List.of();
        }
        VariantEvaluation.Builder variantBuilder = createVariantBuilder(variantContext, altAlleleId, variant);
        List<VariantAnnotation> variantAnnotations = variantAnnotator.annotate(variant);
        // now we have a list of variant annotations - one per gene including affected transcripts and regulatory regions
        List<VariantEvaluation> variantEvaluations = new ArrayList<>(variantAnnotations.size());
        for (VariantAnnotation variantAnnotation : variantAnnotations) {
            VariantEvaluation variantEvaluation = addAnnotations(variantBuilder, variantAnnotation).build();
            variantEvaluations.add(variantEvaluation);
        }
        return variantEvaluations;
    }

    private VariantEvaluation.Builder createVariantBuilder(VariantContext variantContext, int altAlleleId, GenomicVariant variant) {
        SampleGenotypes sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, altAlleleId);

        return VariantEvaluation.builder()
                .variant(variant)
                .genomeAssembly(genomeAssembly)
                .variantContext(variantContext)
                .altAlleleId(altAlleleId)
                .sampleGenotypes(sampleGenotypes)
                //quality is the only value from the VCF file directly required for analysis
                .quality(variantContext.getPhredScaledQual());
    }

    private VariantEvaluation.Builder addAnnotations(VariantEvaluation.Builder variantBuilder, VariantAnnotation variantAnnotation) {
        return variantBuilder
                .geneId(variantAnnotation.getGeneId())
                .geneSymbol(variantAnnotation.getGeneSymbol())
                .variantEffect(variantAnnotation.getVariantEffect())
                .annotations(variantAnnotation.getTranscriptAnnotations());
    }

    /**
     * Data class for tracking number of annotated variants
     */
    private static class VariantCounter {
        final AtomicInteger variantRecords = new AtomicInteger(0);
        final AtomicInteger structuralVariants = new AtomicInteger(0);
        final AtomicInteger unannotatedVariants = new AtomicInteger(0);
        final AtomicInteger annotatedVariants = new AtomicInteger(0);
        final Instant start = Instant.now();

        Consumer<VariantContext> countVariantContext() {
            return variantContext -> variantRecords.incrementAndGet();
        }

        Consumer<VariantEvaluation> countAnnotatedVariant() {
            return variantEvaluation -> {
                // This does add a few seconds overhead over 4 mill variants
                if (variantEvaluation.isSymbolic()) {
                    structuralVariants.incrementAndGet();
                }
                if (variantEvaluation.hasTranscriptAnnotations()) {
                    annotatedVariants.incrementAndGet();
                } else {
                    unannotatedVariants.incrementAndGet();
                }
            };
        }

        void logCount() {
            if (unannotatedVariants.get() > 0) {
                logger.info("Processed {} variant records into {} single allele variants (including {} structural variants), {} are missing annotations, most likely due to non-numeric chromosome designations",
                        variantRecords.get(), annotatedVariants.get(), structuralVariants.get(), unannotatedVariants.get());
            } else {
                logger.info("Processed {} variant records into {} single allele variants (including {} structural variants)",
                        variantRecords.get(), annotatedVariants.get(), structuralVariants.get());
            }
            Duration duration = Duration.between(start, Instant.now());
            int hoursPart = duration.toHoursPart();
            if (hoursPart > 0) {
                logger.info("Variant annotation finished in {}h {}m {}s {}ms ({} ms)", hoursPart, duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart(), duration.toMillis());
            }
            logger.info("Variant annotation finished in {}m {}s {}ms ({} ms)", duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart(), duration.toMillis());
        }
    }
}
