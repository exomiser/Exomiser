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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactoryTest {

    private final VariantFactory instance;

    public VariantFactoryTest() {
        JannovarData jannovarData = TestFactory.buildDefaultJannovarData();
        instance = new VariantFactory(jannovarData);
    }

    private Consumer<VariantEvaluation> printVariant() {
        return variant -> {
            GenotypesContext genotypes = variant.getVariantContext().getGenotypes();
            List<GenotypeType> genotypeTypes = genotypes.stream().map(Genotype::getType).collect(toList());
            System.out.printf("%s %s %s %s %s %s %s offExome=%s gene=%s %s%n", variant.getChromosome(), variant.getPosition(), variant
                    .getRef(), variant.getAlt(), variant.getGenotypeString(), genotypes, genotypeTypes, variant.isOffExome(), variant
                    .getGeneSymbol(), variant.getVariantContext());
        };
    }

    @Test
    public void alternateConstructor() {
        VariantFactory alternateFactory = new VariantFactory(TestFactory.buildDefaultJannovarData());
        assertThat(alternateFactory, notNullValue());
    }

    @Test(expected = TribbleException.class)
    public void testCreateVariantContexts_NonExistentFile() {
        Path vcfPath = Paths.get("src/test/resources/wibble.vcf");
        instance.streamVariantContexts(vcfPath);
    }

    @Test
    public void testCreateVariantContexts_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        long numVariants;
        try (Stream<VariantContext> variantStream = instance.streamVariantContexts(vcfPath)) {
            numVariants = variantStream.count();
        }
        assertThat(numVariants, equalTo(3L));
    }

    @Test
    public void testStreamVariantContexts_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantContext> variants = instance.streamVariantContexts(vcfPath)
                .filter(variantContext -> (variantContext.getContig().equals("1")))
                .collect(toList());

        assertThat(variants.size(), equalTo(3));
    }

    @Test
    public void testStreamCreateVariants_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        long numVariants;
        try (Stream<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath)) {
            numVariants = variants
                    .peek(printVariant())
                    .count();
        }
        assertThat(numVariants, equalTo(3L));
    }

    @Test
    public void testCreateVariantContexts_MultipleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        List<VariantContext> variants = instance.streamVariantContexts(vcfPath).collect(toList());
        assertThat(variants.size(), equalTo(1));
    }

    @Test
    public void testCreateVariantContexts_MultipleAlleles_DiferentSingleSampleGenotypes() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        variants.forEach(printVariant());
        assertThat(variants.size(), equalTo(11));
    }

    @Test
    public void testCreateVariants_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        variants.forEach(printVariant());
        assertThat(variants.size(), equalTo(3));

    }

    @Test
    public void testCreateVariants_MultipleAllelesProduceOneVariantPerAllele() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        variants.forEach(printVariant());
        assertThat(variants.size(), equalTo(2));
    }

    @Test
    public void testCreateVariants_MultipleAlleles_SingleSampleGenotypesShouldOnlyReturnRepresentedVariationFromGenotype() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        variants.forEach(printVariant());
        assertThat(variants.size(), equalTo(11));
    }

    @Test
    public void testCreateVariants_NoVariantAnnotationsProduceVariantEvaluationsWithNoAnnotations() {
        Path vcfPath = Paths.get("src/test/resources/noAnnotations.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        assertThat(variants.size(), equalTo(2));

        for (VariantEvaluation variant : variants) {
            System.out.println(variant.getChromosomeName() + " " + variant);
            assertThat(variant.hasAnnotations(), is(false));
        }
    }

    @Test
    public void testStreamVariantEvaluations_MultipleAlleles_DifferentSingleSampleGenotypes() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        assertThat(variants.size(), equalTo(11));
    }

    @Test
    public void testKnownSingleSampleSnp() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample")
                .parse("10\t123256215\t.\tT\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        System.out.println(variantEvaluation);
        assertThat(variantEvaluation.getChromosome(), equalTo(10));
        assertThat(variantEvaluation.getChromosomeName(), equalTo("10"));
        assertThat(variantEvaluation.getPosition(), equalTo(123256215));
        assertThat(variantEvaluation.getRef(), equalTo("T"));
        assertThat(variantEvaluation.getAlt(), equalTo("G"));
        assertThat(variantEvaluation.hasAnnotations(), is(true));
        System.out.println(variantEvaluation.getAnnotations());
        assertThat(variantEvaluation.getEntrezGeneId(), equalTo(2263));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    public void testUnKnownSingleSampleSnp() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample")
                .parse("UNKNOWN\t12345\t.\tT\tC\t0\tPASS\t.\tGT:DP\t0/1:21");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        System.out.println(variantEvaluation);
        assertThat(variantEvaluation.getChromosome(), equalTo(0));
        assertThat(variantEvaluation.getChromosomeName(), equalTo("UNKNOWN"));
        assertThat(variantEvaluation.getPosition(), equalTo(12345));
        assertThat(variantEvaluation.getRef(), equalTo("T"));
        assertThat(variantEvaluation.getAlt(), equalTo("C"));
        assertThat(variantEvaluation.hasAnnotations(), is(false));
        System.out.println(variantEvaluation.getAnnotations());
        assertThat(variantEvaluation.getEntrezGeneId(), equalTo(-1));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("."));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
    }

    /**
     * See https://github.com/exomiser/Exomiser/issues/207#issuecomment-310123621
     */
    @Test
    public void testSingleSampleDeletion() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample")
                .parse("1\t123256213\t.\tCA\tC\t100.15\tPASS\tGENE=RBM8A\tGT:DP\t0/1:33");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        System.out.println(variantEvaluation);
        assertThat(variantEvaluation.getChromosome(), equalTo(1));
        assertThat(variantEvaluation.getChromosomeName(), equalTo("1"));
        assertThat(variantEvaluation.getPosition(), equalTo(123256213));
        assertThat(variantEvaluation.getRef(), equalTo("CA"));
        assertThat(variantEvaluation.getAlt(), equalTo("C"));
        assertThat(variantEvaluation.hasAnnotations(), is(true));
        System.out.println(variantEvaluation.getAnnotations());
        assertThat(variantEvaluation.getEntrezGeneId(), equalTo(9939));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
    }

    @Test
    public void testSnpWithNoGenotypeReturnsNothing() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples().parse("UNKNOWN\t12345\t.\tT\tC\t0\tPASS\t.");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.isEmpty(), is(true));
    }

    @Test
    public void testSingleSampleHomVar() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample")
                .parse("1\t120612040\t.\tT\tTCCGCCG\t258.62\tPASS\t.\tGT\t1/1");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
    }

    @Test
    public void testSingleSampleMultiPosition() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample")
                .parse("1\t120612040\t.\tT\tTCCGCCG,TCCTCCGCCG\t258.62\tPASS\t.\tGT\t1/2");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        //TODO: Should this really be 1? Genotype is [Sample TCCGCCG/TCCTCCGCCG]
        assertThat(variants.size(), equalTo(2));
    }

    @Test
    public void testMultiSampleMultiPositionAlleleIsSplitIntoAlternateAlleles() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample1", "Sample2")
                .parse("1\t120612040\t.\tT\tTCCGCCG,TCCTCCGCCG\t258.62\tPASS\t.\tGT\t0/1\t0/2");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.size(), equalTo(2));
        VariantEvaluation firstAllele = variants.get(0);
        System.out.println(firstAllele);
        assertThat(firstAllele.getChromosome(), equalTo(1));
        assertThat(firstAllele.getChromosomeName(), equalTo("1"));
        assertThat(firstAllele.getPosition(), equalTo(120612040));
        assertThat(firstAllele.getRef(), equalTo("T"));
        assertThat(firstAllele.getAlt(), equalTo("TCCGCCG"));
        assertThat(firstAllele.hasAnnotations(), is(true));
        System.out.println(firstAllele.getAnnotations());
        assertThat(firstAllele.getEntrezGeneId(), equalTo(9939));
        assertThat(firstAllele.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(firstAllele.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));


        VariantEvaluation secondAllele = variants.get(1);
        System.out.println(secondAllele);
        assertThat(secondAllele.getChromosome(), equalTo(1));
        assertThat(secondAllele.getChromosomeName(), equalTo("1"));
        assertThat(secondAllele.getPosition(), equalTo(120612040));
        assertThat(secondAllele.getRef(), equalTo("T"));
        assertThat(secondAllele.getAlt(), equalTo("TCCTCCGCCG"));
        assertThat(secondAllele.hasAnnotations(), is(true));
        System.out.println(secondAllele.getAnnotations());
        assertThat(secondAllele.getEntrezGeneId(), equalTo(9939));
        assertThat(secondAllele.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(secondAllele.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
    }

    @Test
    public void testMultiSampleMultiPositionOnlyOneAltAlleleIsPresentInSamplesProducesOneVariantEvaluation() {
        Stream<VariantContext> variantContexts = VcfParser.forSamples("Sample1", "Sample2")
                .parse("1\t120612040\t.\tT\tTCCGCCG,TCCTCCGCCG\t258.62\tPASS\t.\tGT\t0/1\t0/1");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(variantContexts)
                .peek(printVariant())
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        System.out.println(variantEvaluation);
        assertThat(variantEvaluation.getChromosome(), equalTo(1));
        assertThat(variantEvaluation.getChromosomeName(), equalTo("1"));
        assertThat(variantEvaluation.getPosition(), equalTo(120612040));
        assertThat(variantEvaluation.getRef(), equalTo("T"));
        assertThat(variantEvaluation.getAlt(), equalTo("TCCGCCG"));
        assertThat(variantEvaluation.hasAnnotations(), is(true));
        System.out.println(variantEvaluation.getAnnotations());
        assertThat(variantEvaluation.getEntrezGeneId(), equalTo(9939));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
    }

    private static class VcfParser {

        private final VCFCodec vcfCodec;

        static VcfParser forSamples(String... sampleNames) {
            return new VcfParser(sampleNames);
        }

        private VcfParser(String... sampleNames) {
            vcfCodec = getVcfCodecForSamples(sampleNames);
        }

        private VCFCodec getVcfCodecForSamples(String... sampleNames) {
            VCFCodec vcfCodec = new VCFCodec();
            vcfCodec.setVCFHeader(new VCFHeader(Collections.emptySet(), Arrays.asList(sampleNames)), VCFHeaderVersion.VCF4_2);
            return vcfCodec;
        }

        Stream<VariantContext> parse(String... lines) {
            return Stream.of(lines).map(vcfCodec::decode);
        }

        Stream<VariantContext> parse(String line) {
            return Stream.of(line).map(vcfCodec::decode);
        }
    }
}
