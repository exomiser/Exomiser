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

import com.google.common.collect.ImmutableList;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFConstants;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.StructuralType;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
        return variantContext ->  variantContext.getAlternateAlleles().stream()
                    .map(buildAlleleVariantEvaluations(variantContext))
                    .flatMap(Collection::stream);
            // TODO: is this easier to use if we have streams all the way down rather than dealing with lists?
    }

    private Function<Allele, List<VariantEvaluation>> buildAlleleVariantEvaluations(VariantContext variantContext) {
        return altAllele -> {
            // Itererating by alleleId here this is less clean, but faster
            // alternate Alleles are always after the reference allele, which is 0
            int altAlleleId = variantContext.getAlleleIndex(altAllele) - 1;
            // n.b. samples with no genotypes (e.g. ./.) will return no variants
            if (alleleIsObservedInGenotypes(altAllele, variantContext.getGenotypes())) {
                return buildVariantEvaluations(variantContext, altAlleleId, altAllele);
            }
            return ImmutableList.of();
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
     */
    private List<VariantEvaluation> buildVariantEvaluations(VariantContext variantContext, int altAlleleId, Allele altAllele) {
        List<VariantAnnotation> variantAnnotations = annotateVariantAllele(variantContext, altAllele);

        // symbolic alleles are reported as VariantEffect.STRUCTURAL_VARIANT

        // https://github.com/Illumina/ExpansionHunter format for STR - this isn't part of the standard VCF spec
        // also consider <STR27> RU=CAG expands to (CAG)*27 STR = Short Tandem Repeats RU = Repeat Unit
        // link to https://panelapp.genomicsengland.co.uk/panels/20/str/PPP2R2B_CAG/
        // https://panelapp.genomicsengland.co.uk/WebServices/get_panel/20/?format=json
        ImmutableList.Builder<VariantEvaluation> variantEvaluations = new ImmutableList.Builder<>();
        for (VariantAnnotation variantAnnotation : variantAnnotations) {
            VariantEvaluation variantEvaluation = buildVariantEvaluation(variantContext, altAlleleId, altAllele, variantAnnotation);
            variantEvaluations.add(variantEvaluation);
        }
        return variantEvaluations.build();
    }

    // It is possible for a variant to overlap two or more genes (see issue https://github.com/exomiser/Exomiser/issues/294)
    // so we're expecting a single gene per variant annotation which might have different variant consequences and different
    // phenotypes for each gene
    private List<VariantAnnotation> annotateVariantAllele(VariantContext variantContext, Allele altAllele) {
        String contig = variantContext.getContig();
        int start = variantContext.getStart();
        String ref = variantContext.getReference().getBaseString();
        // Structural variants are 'symbolic' in that they have no actual reported bases
        String alt = (altAllele.isSymbolic()) ? altAllele.getDisplayString() : altAllele.getBaseString();

        StructuralType structuralType = detectAlleleVariantType(variantContext, altAllele);
        if (structuralType.isStructural()) {
            String endContig = variantContext.getCommonInfo().getAttributeAsString("CHR2", contig);
            int end = variantContext.getCommonInfo().getAttributeAsInt("END", variantContext.getEnd());
            List<Integer> startCi = getCiListOrDefault(variantContext, "CIPOS", ImmutableList.of(0, 0));
            List<Integer> endCi = getCiListOrDefault(variantContext, "CIEND", ImmutableList.of(0, 0));
            int length = Math.abs(variantContext.getAttributeAsInt("SVLEN", end - start));
//            logger.info("Annotating {}: {} {} {} {} {} {} {} {} {}", structuralType, ref, alt, contig, start, startCi, endContig, end, endCi, length);
            return variantAnnotator.annotateStructuralVariant(structuralType, ref, alt, contig, start, startCi, endContig, end, endCi, length);
        }

        return variantAnnotator.annotate(contig, start, ref, alt);
    }

    private StructuralType detectAlleleVariantType(VariantContext variantContext, Allele altAllele) {
        // WARNING! variantContext.getStructuralVariantType() IS NOT SAFE! It throws the following exception:
        //  java.lang.IllegalArgumentException: No enum constant htsjdk.variant.variantcontext.StructuralVariantType.SVA
        //  for the line
        //  22   16918023    esv3647185  C   <INS:ME:SVA>    100 PASS    SVLEN=1312;SVTYPE=SVA;TSD=AAAAATACAAAAATTTGC;VT=SV   GT  0|1
        if (altAllele.isSymbolic()){
            String svTypeString = variantContext.getAttributeAsString(VCFConstants.SVTYPE, null);
            // SV types should not be SMALL so try parsing the alt allele if the SVTYPE field isn't recognised (as in the case of ALU, LINE, SVA from 1000 genomes)
            StructuralType parseValue = StructuralType.parseValue(altAllele.getDisplayString());
            return parseValue == StructuralType.UNKNOWN ? StructuralType.parseValue(svTypeString) : parseValue;
        }
        return StructuralType.NON_STRUCTURAL;
    }

    private List<Integer> getCiListOrDefault(VariantContext variantContext, String cipos, List<Integer> defaultValue) {
        List<Integer> ciList = variantContext.getCommonInfo().getAttributeAsIntList(cipos, -1);
        return ciList.isEmpty() ? defaultValue : ciList;
    }

    private VariantEvaluation buildVariantEvaluation(VariantContext variantContext, int altAlleleId, Allele altAllele, VariantAnnotation variantAnnotation) {
        // See also notes in InheritanceModeAnnotator.
        Map<String, SampleGenotype> sampleGenotypes = VariantContextSampleGenotypeConverter.createAlleleSampleGenotypes(variantContext, altAlleleId);
        // all the variantAnnotation methods are present on the Variant interface so this should be a VariantEvaluation.copy(Variant)
        return VariantEvaluation.copy(variantAnnotation)
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
                .build();
    }

    /**
     * Data class for tracking number of annotated variants
     */
    private class VariantCounter {
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
                if (variantEvaluation.isStructuralVariant()) {
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
            long ms = duration.toMillis();
            logger.info("Variant annotation finished in {}m {}s {}ms ({} ms)", (ms / 1000) / 60 % 60, ms / 1000 % 60, ms % 1000, ms);
        }
    }
}
