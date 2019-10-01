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
        return altAllele -> {
            //alternate Alleles are always after the reference allele, which is 0
            int altAlleleId = variantContext.getAlleleIndex(altAllele) - 1;
            if (alleleIsObservedInGenotypes(altAllele, variantContext.getGenotypes())) {
                return Optional.of(buildVariantEvaluation(variantContext, altAlleleId, altAllele));
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
    VariantEvaluation buildVariantEvaluation(VariantContext variantContext, int altAlleleId, Allele altAllele) {
        VariantAnnotation variantAnnotation = annotateVariantAllele(variantContext, altAllele);
        // symbolic alleles are reported as VariantEffect.STRUCTURAL_VARIANT
        // but have a default pathogenicity score of zero
        // will need to have an end and/or length (sigInt) and SVTYPE (DEL, INS, DUP, INV, CNV, BND)
        // lookup in Jannovar to find effected genes?

        // find length and type
        // https://github.com/Illumina/ExpansionHunter format for STR - this isn't part of the standard VCF spec
        // also consider <STR27> RU=CAG expands to (CAG)*27 STR = Short Tandem Repeats RU = Repeat Unit
        // link to https://panelapp.genomicsengland.co.uk/panels/20/str/PPP2R2B_CAG/
        // https://panelapp.genomicsengland.co.uk/WebServices/get_panel/20/?format=json
        return buildVariantEvaluation(variantContext, altAlleleId, variantAnnotation);
    }

    private VariantAnnotation annotateVariantAllele(VariantContext variantContext, Allele altAllele) {
        String contig = variantContext.getContig();
        int pos = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        // Structural variants are 'symbolic' in that they have no actual reported bases
        String alt = (altAllele.isSymbolic()) ? altAllele.getDisplayString() : altAllele.getBaseString();
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
                .id((".".equals(variantContext.getID())) ? "" : variantContext.getID())
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
