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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.GenotypesContext;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.svart.ConfidenceInterval;
import org.monarchinitiative.svart.Coordinates;
import org.monarchinitiative.svart.VariantType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactoryImplTest {

    private String sample = "Sample";
    private String sample1 = "Sample1";

    private VariantFactoryImpl newInstance(Path vcfPath) {
        VariantAnnotator variantAnnotator = TestFactory.buildDefaultVariantAnnotator();
        return new VariantFactoryImpl(variantAnnotator, vcfPath);
    }

    private VariantFactoryImpl newInstance(VcfReader vcfReader) {
        VariantAnnotator variantAnnotator = TestFactory.buildDefaultVariantAnnotator();
        return new VariantFactoryImpl(variantAnnotator, vcfReader);
    }

    private Consumer<VariantEvaluation> printVariant() {
        return variant -> {
            GenotypesContext genotypes = variant.getVariantContext().getGenotypes();
            List<GenotypeType> genotypeTypes = genotypes.stream().map(Genotype::getType).collect(toList());
            System.out.printf("%s %s %s %s %s %s %s gene={%s %s} %s%n", variant.contigId(), variant.start(), variant
                            .ref(), variant.alt(), variant.getGenotypeString(), genotypes, genotypeTypes,
                    variant.getGeneSymbol(), variant.getGeneId(), variant.getVariantContext());
        };
    }

    @Test
    public void testStreamCreateVariantsSingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        long numVariants;
        try (Stream<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations()) {
            numVariants = variants
                    .count();
        }
        assertThat(numVariants, equalTo(3L));
    }

    @Test
    public void testCreateVariantContextsMultipleAllelesDiferentSingleSampleGenotypes() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        List<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations().collect(toList());
        assertThat(variants.size(), equalTo(11));
    }

    @Test
    public void testCreateVariantsSingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations().collect(toList());
        assertThat(variants.size(), equalTo(3));

    }

    @Test
    public void testCreateVariantsMultipleAllelesProduceOneVariantPerAllele() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        List<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations().collect(toList());
        assertThat(variants.size(), equalTo(2));
    }

    @Test
    public void testCreateVariantsMultipleAllelesSingleSampleGenotypesShouldOnlyReturnRepresentedVariationFromGenotype() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        List<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations().collect(toList());
        assertThat(variants.size(), equalTo(11));
    }

    @Test
    public void testCreateVariantsNoVariantAnnotationsProduceVariantEvaluationsWithNoAnnotations() {
        Path vcfPath = Paths.get("src/test/resources/noAnnotations.vcf");
        List<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations().collect(toList());
        assertThat(variants.size(), equalTo(2));

        for (VariantEvaluation variant : variants) {
            assertThat(variant.hasTranscriptAnnotations(), is(false));
        }
    }

    @Test
    public void testStreamVariantEvaluationsMultipleAllelesDifferentSingleSampleGenotypes() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        List<VariantEvaluation> variants = newInstance(vcfPath).createVariantEvaluations().collect(toList());
        assertThat(variants.size(), equalTo(11));
    }

    @Test
    public void testKnownSingleSampleSnp() {
        VcfReader vcfReader = TestVcfReader.builder().samples(sample)
                .vcfLines("10\t123256215\t.\tT\tG\t100\tPASS\tGENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1|0")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);

        assertThat(variantEvaluation.contigId(), equalTo(10));
        assertThat(variantEvaluation.contigName(), equalTo("10"));
        assertThat(variantEvaluation.start(), equalTo(123256215));
        assertThat(variantEvaluation.end(), equalTo(123256215));
        assertThat(variantEvaluation.length(), equalTo(1));
        assertThat(variantEvaluation.ref(), equalTo("T"));
        assertThat(variantEvaluation.alt(), equalTo("G"));
        assertThat(variantEvaluation.hasTranscriptAnnotations(), is(true));

        assertThat(variantEvaluation.getGeneId(), equalTo("2263"));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("FGFR2"));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.phased(AlleleCall.ALT, AlleleCall.REF))));
    }

    @Test
    public void testUnKnownSingleSampleSnp() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample")
                .vcfLines("UNKNOWN\t12345\t.\tT\tC\t0\tPASS\t.\tGT:DP\t0/1:21")
                .build();

        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(0));
//        VariantEvaluation variantEvaluation = variants.get(0);
//
//        assertThat(variantEvaluation.contigId(), equalTo(0));
//        assertThat(variantEvaluation.contigName(), equalTo("na"));
//        assertThat(variantEvaluation.start(), equalTo(12345));
//        assertThat(variantEvaluation.ref(), equalTo("T"));
//        assertThat(variantEvaluation.alt(), equalTo("C"));
//        assertThat(variantEvaluation.hasTranscriptAnnotations(), is(false));
//
//        assertThat(variantEvaluation.getGeneId(), equalTo(""));
//        assertThat(variantEvaluation.getGeneSymbol(), equalTo("."));
//        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
//        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(ImmutableMap.of("Sample", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT))));
    }

    /**
     * See https://github.com/exomiser/Exomiser/issues/207#issuecomment-310123621
     */
    @Test
    public void testSingleSampleDeletion() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample")
                .vcfLines("1\t123256213\t.\tCA\tC\t100.15\tPASS\tGENE=RBM8A\tGT:DP\t0/1:33")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);

        assertThat(variantEvaluation.contigId(), equalTo(1));
        assertThat(variantEvaluation.contigName(), equalTo("1"));
        assertThat(variantEvaluation.start(), equalTo(123256213));
        assertThat(variantEvaluation.ref(), equalTo("CA"));
        assertThat(variantEvaluation.alt(), equalTo("C"));
        assertThat(variantEvaluation.hasTranscriptAnnotations(), is(true));

        assertThat(variantEvaluation.getGeneId(), equalTo("9939"));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.het())));
    }

    @Test
    public void testSnpWithNoGenotypeReturnsNothing() {
        VcfReader vcfReader = TestVcfReader.builder().samples()
                .vcfLines("UNKNOWN\t12345\t.\tT\tC\t0\tPASS\t.")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.isEmpty(), is(true));
    }

    @Test
    public void testSingleSampleHomVar() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample")
                .vcfLines("1\t120612040\t.\tT\tTCCGCCG\t258.62\tPASS\t.\tGT\t1/1")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variant = variants.get(0);
        assertThat(variant.getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.homAlt())));
    }

    @Test
    public void testSingleSampleMultiPosition() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample")
                .vcfLines("1\t120612040\t.\tT\tTCCGCCG,TCCTCCGCCG\t258.62\tPASS\t.\tGT\t1/2")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(2));
        assertThat(variants.get(0).getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.ALT))));
        assertThat(variants.get(1).getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.of(AlleleCall.OTHER_ALT, AlleleCall.ALT))));
    }

    @Test
    public void testMultiSampleMultiPositionAlleleIsSplitIntoAlternateAlleles() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample", "Sample1")
                .vcfLines("1\t120612040\t.\tT\tTCCGCCG,TCCTCCGCCG\t258.62\tPASS\t.\tGT\t0/1\t0/2")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(2));
        VariantEvaluation firstAllele = variants.get(0);

        assertThat(firstAllele.contigId(), equalTo(1));
        assertThat(firstAllele.contigName(), equalTo("1"));
        assertThat(firstAllele.start(), equalTo(120612040));
        assertThat(firstAllele.end(), equalTo(120612040));
        assertThat(firstAllele.changeLength(), equalTo(6));
        assertThat(firstAllele.ref(), equalTo("T"));
        assertThat(firstAllele.alt(), equalTo("TCCGCCG"));
        assertThat(firstAllele.hasTranscriptAnnotations(), is(true));

        assertThat(firstAllele.getGeneId(), equalTo("9939"));
        assertThat(firstAllele.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(firstAllele.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(firstAllele.getSampleGenotypes(), equalTo(
                SampleGenotypes.of(
                        sample, SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                        sample1, SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT)
                ))
        );

        VariantEvaluation secondAllele = variants.get(1);

        assertThat(secondAllele.contigId(), equalTo(1));
        assertThat(secondAllele.contigName(), equalTo("1"));
        assertThat(secondAllele.start(), equalTo(120612040));
        assertThat(secondAllele.end(), equalTo(120612040));
        assertThat(secondAllele.changeLength(), equalTo(9));
        assertThat(secondAllele.ref(), equalTo("T"));
        assertThat(secondAllele.alt(), equalTo("TCCTCCGCCG"));
        assertThat(secondAllele.hasTranscriptAnnotations(), is(true));

        assertThat(secondAllele.getGeneId(), equalTo("9939"));
        assertThat(secondAllele.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(secondAllele.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(secondAllele.getSampleGenotypes(), equalTo(
                SampleGenotypes.of(
                        sample, SampleGenotype.of(AlleleCall.REF, AlleleCall.OTHER_ALT),
                        sample1, SampleGenotype.het()
                ))
        );
    }

    @Test
    public void testMultiSampleMultiPositionOnlyOneAltAlleleIsPresentInSamplesProducesOneVariantEvaluation() {
        VcfReader vcfReader = TestVcfReader.builder().samples(sample, sample1)
                .vcfLines("1\t120612040\t.\tT\tTCCGCCG,TCCTCCGCCG\t258.62\tPASS\t.\tGT\t0/1\t0/1")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);

        assertThat(variantEvaluation.contigId(), equalTo(1));
        assertThat(variantEvaluation.contigName(), equalTo("1"));
        assertThat(variantEvaluation.start(), equalTo(120612040));
        assertThat(variantEvaluation.ref(), equalTo("T"));
        assertThat(variantEvaluation.alt(), equalTo("TCCGCCG"));
        assertThat(variantEvaluation.hasTranscriptAnnotations(), is(true));

        assertThat(variantEvaluation.getGeneId(), equalTo("9939"));
        assertThat(variantEvaluation.getGeneSymbol(), equalTo("RBM8A"));
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(
                SampleGenotypes.of(
                        sample, SampleGenotype.het(),
                        sample1, SampleGenotype.het()
                ))
        );
    }

    @Test
    void testTranscriptsOverlappingTwoGenes() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample1")
                //145510184
                .vcfLines("16 89935214 . G A 6 PASS GENE=TUBB3,AC092143.1 GT:GQ 0/1:12")
                .build();

        GeneIdentifier AC092143 = GeneIdentifier.builder().geneSymbol("AC092143.1").build();
        TranscriptModel ENST00000556922 = TestTranscriptModelFactory.builder()
                .geneIdentifier(AC092143)
                .knownGeneLine("ENST00000556922.1\tchr16\t+\t89919164\t89936092\t89919258\t89935804\t5\t89919164,89920589,89932570,89933467,89934728,\t89920208,89920737,89932679,89933578,89936092,\tA0A0B4J269\tuc002fpf.3")
                .mRnaSequence("GGCAGCACCATGAACTAAGCAGGACACCTGGAGGGGAAGAACTGTGGGGACCTGGAGGCCTCCAACGACTCCTTCCTGCTTCCTGGACAGGACTATGGCTGTGCAGGGATCCCAGAGAAGACTTCTGGGCTCCCTCAACTCCACCCCCACAGCCATCCCCCAGCTGGGGCTGGCTGCCAACCAGACAGGAGCCCGGTGCCTGGAGGTGTCCATCTCTGACGGGCTCTTCCTCAGCCTGGGGCTGGTGAGCTTGGTGGAGAACGCGCTGGTGGTGGCCACCATCGCCAAGAACCGGAACCTGCACTCACCCATGTACTGCTTCATCTGCTGCCTGGCCTTGTCGGACCTGCTGGTGAGCGGGAGCAACGTGCTGGAGACGGCCGTCATCCTCCTGCTGGAGGCCGGTGCACTGGTGGCCCGGGCTGCGGTGCTGCAGCAGCTGGACAATGTCATTGACGTGATCACCTGCAGCTCCATGCTGTCCAGCCTCTGCTTCCTGGGCGCCATCGCCGTGGACCGCTACATCTCCATCTTCTACGCACTGCGCTACCACAGCATCGTGACCCTGCCGCGGGCGCGGCGAGCCGTTGCGGCCATCTGGGTGGCCAGTGTCGTCTTCAGCACGCTCTTCATCGCCTACTACGACCACGTGGCCGTCCTGCTGTGCCTCGTGGTCTTCTTCCTGGCTATGCTGGTGCTCATGGCCGTGCTGTACGTCCACATGCTGGCCCGGGCCTGCCAGCACGCCCAGGGCATCGCCCGGCTCCACAAGAGGCAGCGCCCGGTCCACCAGGGCTTTGGCCTTAAAGGCGCTGTCACCCTCACCATCCTGCTGGGCATTTTCTTCCTCTGCTGGGGCCCCTTCTTCCTGCATCTCACACTCATCGTCCTCTGCCCCGAGCACCCCACGTGCGGCTGCATCTTCAAGAACTTCAACCTCTTTCTCGCCCTCATCATCTGCAATGCCATCATCGACCCCCTCATCTACGCCTTCCACAGCCAGGAGCTCCGCAGGACGCTCAAGGAGGTGCTGACATGCTCCTGCTCTCAGGACCGTGCCCTCGTCAGCTGGGATGTGAAGTCTCTGGGTGGAAGTGTGTGCCAAGAGCTACTCCCACAGCAGCCCCAGGAGAAGGGGCTTTGTGACCAGAAAGCTTCATCCACAGCCTTGCAGCGGCTCCTGCAAAAGGAGTTCTGGGAAGTCATCAGTGATGAGCATGGCATCGACCCCAGCGGCAACTACGTGGGCGACTCGGACTTGCAGCTGGAGCGGATCAGCGTCTACTACAACGAGGCCTCTTCTCACAAGTACGTGCCTCGAGCCATTCTGGTGGACCTGGAACCCGGAACCATGGACAGTGTCCGCTCAGGGGCCTTTGGACATCTCTTCAGGCCTGACAATTTCATCTTTGGTCAGAGTGGGGCCGGCAACAACTGGGCCAAGGGTCACTACACGGAGGGGGCGGAGCTGGTGGATTCGGTCCTGGATGTGGTGCGGAAGGAGTGTGAAAACTGCGACTGCCTGCAGGGCTTCCAGCTGACCCACTCGCTGGGGGGCGGCACGGGCTCCGGCATGGGCACGTTGCTCATCAGCAAGGTGCGTGAGGAGTATCCCGACCGCATCATGAACACCTTCAGCGTCGTGCCCTCACCCAAGGTGTCAGACACGGTGGTGGAGCCCTACAACGCCACGCTGTCCATCCACCAGCTGGTGGAGAACACGGATGAGACCTACTGCATCGACAACGAGGCGCTCTACGACATCTGCTTCCGCACCCTCAAGCTGGCCACGCCCACCTACGGGGACCTCAACCACCTGGTATCGGCCACCATGAGCGGAGTCACCACCTCCTTGCGCTTCCCGGGCCAGCTCAACGCTGACCTGCGCAAGCTGGCCGTCAACATGGTGCCCTTCCCGCGCCTGCACTTCTTCATGCCCGGCTTCGCCCCCCTCACAGCCCGGGGCAGCCAGCAGTACCGGGCCCTGACCGTGCCCGAGCTCACCCAGCAGATGTTCGATGCCAAGAACATGATGGCCGCCTGCGACCCGCGCCACGGCCGCTACCTGACGGTGGCCACCGTGTTCCGGGGCCGCATGTCCATGAAGGAGGTGGACGAGCAGATGCTGGCCATCCAGAGCAAGAACAGCAGCTACTTCGTGGAGTGGATCCCCAACAACGTGAAGGTGGCCGTGTGTGACATCCCGCCCCGCGGCCTCAAGATGTCCTCCACCTTCATCGGGAACAGCACGGCCATCCAGGAGCTGTTCAAGCGCATCTCCGAGCAGTTCACGGCCATGTTCCGGCGCAAGGCCTTCCTGCACTGGTACACGGGCGAGGGCATGGACGAGATGGAGTTCACCGAGGCCGAGAGCAACATGAACGACCTGGTGTCCGAGTACCAGCAGTACCAGGACGCCACGGCCGAGGAAGAGGGCGAGATGTACGAAGACGACGAGGAGGAGTCGGAGGCCCAGGGCCCCAAGTGAAGCTGCTCGCAGCTGGAGTGAGAGGCAGGTGGCGGCCGGGGCCGAAGCCAGCAGTGTCTAAACCCCCGGAGCCATCTTGCTGCCGACACCCTGCTTTCCCCTCGCCCTAGGGCTCCCTTGCCGCCCTCCTGCAGTATTTATGGCCTCGTCCTCCCCACCTAGGCCACGTGTGAGCTGCTCCTGTCTCTGTCTTATTGCAGCTCCAGGCCTGACGTTTTACGGTTTTGTTTTTTACTGGTTTGTGTTTATATTTTCGGGGATACTTAATAAATCTATTGCTGTCAGATA")
                .build();

        GeneIdentifier tubb3 = GeneIdentifier.builder().geneSymbol("TUBB3").build();
        TranscriptModel ENST00000315491 = TestTranscriptModelFactory.builder()
                .geneIdentifier(tubb3)
                .knownGeneLine("ENST00000315491.11\tchr16\t+\t89923278\t89936097\t89923401\t89935804\t4\t89923278,89932570,89933467,89934728,\t89923458,89932679,89933578,89936097,\tQ13509\tuc002fph.2")
                .mRnaSequence("GACATCAGCCGATGCGAAGGGCGGGGCCGCGGCTATAAGAGCGCGCGGCCGCGGTCCCCGACCCTCAGCAGCCAGCCCGGCCCGCCCGCGCCCGTCCGCAGCCGCCCGCCAGACGCGCCCAGTATGAGGGAGATCGTGCACATCCAGGCCGGCCAGTGCGGCAACCAGATCGGGGCCAAGTTCTGGGAAGTCATCAGTGATGAGCATGGCATCGACCCCAGCGGCAACTACGTGGGCGACTCGGACTTGCAGCTGGAGCGGATCAGCGTCTACTACAACGAGGCCTCTTCTCACAAGTACGTGCCTCGAGCCATTCTGGTGGACCTGGAACCCGGAACCATGGACAGTGTCCGCTCAGGGGCCTTTGGACATCTCTTCAGGCCTGACAATTTCATCTTTGGTCAGAGTGGGGCCGGCAACAACTGGGCCAAGGGTCACTACACGGAGGGGGCGGAGCTGGTGGATTCGGTCCTGGATGTGGTGCGGAAGGAGTGTGAAAACTGCGACTGCCTGCAGGGCTTCCAGCTGACCCACTCGCTGGGGGGCGGCACGGGCTCCGGCATGGGCACGTTGCTCATCAGCAAGGTGCGTGAGGAGTATCCCGACCGCATCATGAACACCTTCAGCGTCGTGCCCTCACCCAAGGTGTCAGACACGGTGGTGGAGCCCTACAACGCCACGCTGTCCATCCACCAGCTGGTGGAGAACACGGATGAGACCTACTGCATCGACAACGAGGCGCTCTACGACATCTGCTTCCGCACCCTCAAGCTGGCCACGCCCACCTACGGGGACCTCAACCACCTGGTATCGGCCACCATGAGCGGAGTCACCACCTCCTTGCGCTTCCCGGGCCAGCTCAACGCTGACCTGCGCAAGCTGGCCGTCAACATGGTGCCCTTCCCGCGCCTGCACTTCTTCATGCCCGGCTTCGCCCCCCTCACAGCCCGGGGCAGCCAGCAGTACCGGGCCCTGACCGTGCCCGAGCTCACCCAGCAGATGTTCGATGCCAAGAACATGATGGCCGCCTGCGACCCGCGCCACGGCCGCTACCTGACGGTGGCCACCGTGTTCCGGGGCCGCATGTCCATGAAGGAGGTGGACGAGCAGATGCTGGCCATCCAGAGCAAGAACAGCAGCTACTTCGTGGAGTGGATCCCCAACAACGTGAAGGTGGCCGTGTGTGACATCCCGCCCCGCGGCCTCAAGATGTCCTCCACCTTCATCGGGAACAGCACGGCCATCCAGGAGCTGTTCAAGCGCATCTCCGAGCAGTTCACGGCCATGTTCCGGCGCAAGGCCTTCCTGCACTGGTACACGGGCGAGGGCATGGACGAGATGGAGTTCACCGAGGCCGAGAGCAACATGAACGACCTGGTGTCCGAGTACCAGCAGTACCAGGACGCCACGGCCGAGGAAGAGGGCGAGATGTACGAAGACGACGAGGAGGAGTCGGAGGCCCAGGGCCCCAAGTGAAGCTGCTCGCAGCTGGAGTGAGAGGCAGGTGGCGGCCGGGGCCGAAGCCAGCAGTGTCTAAACCCCCGGAGCCATCTTGCTGCCGACACCCTGCTTTCCCCTCGCCCTAGGGCTCCCTTGCCGCCCTCCTGCAGTATTTATGGCCTCGTCCTCCCCACCTAGGCCACGTGTGAGCTGCTCCTGTCTCTGTCTTATTGCAGCTCCAGGCCTGACGTTTTACGGTTTTGTTTTTTACTGGTTTGTGTTTATATTTTCGGGGATACTTAATAAATCTATTGCTGTCAGATACCCTT")
                .build();

        JannovarData jannovarData = TestFactory.buildJannovarData(ENST00000556922, ENST00000315491);
        JannovarVariantAnnotator variantAnnotator = new JannovarVariantAnnotator(GenomeAssembly.HG38, jannovarData, ChromosomalRegionIndex.empty());
        VariantFactory variantFactory = new VariantFactoryImpl(variantAnnotator, vcfReader);

        List<VariantEvaluation> variants = variantFactory.createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(2));

        Map<String, List<VariantEvaluation>> variantsByGeneSymbol = variants.stream()
                .collect(groupingBy(VariantEvaluation::getGeneSymbol));

        variantsByGeneSymbol.get("TUBB3")
                .forEach(variantEvaluation -> {
                    assertThat(variantEvaluation.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
                    assertThat(variantEvaluation.contigId(), equalTo(16));
                    assertThat(variantEvaluation.start(), equalTo(89935214));
                    assertThat(variantEvaluation.ref(), equalTo("G"));
                    assertThat(variantEvaluation.alt(), equalTo("A"));
                    assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
                    variantEvaluation.getTranscriptAnnotations().forEach(
                            transcriptAnnotation -> {
                                assertThat(transcriptAnnotation.getAccession(), equalTo(ENST00000315491.getAccession()));
                                assertThat(transcriptAnnotation.getGeneSymbol(), equalTo(ENST00000315491.getGeneSymbol()));
                            }
                    );});
        variantsByGeneSymbol.get("AC092143.1")
                .forEach(variantEvaluation -> {
                    assertThat(variantEvaluation.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
                    assertThat(variantEvaluation.contigId(), equalTo(16));
                    assertThat(variantEvaluation.start(), equalTo(89935214));
                    assertThat(variantEvaluation.ref(), equalTo("G"));
                    assertThat(variantEvaluation.alt(), equalTo("A"));
                    assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
                    variantEvaluation.getTranscriptAnnotations().forEach(
                            transcriptAnnotation -> {
                                assertThat(transcriptAnnotation.getAccession(), equalTo(ENST00000556922.getAccession()));
                                assertThat(transcriptAnnotation.getGeneSymbol(), equalTo(ENST00000556922.getGeneSymbol()));
                            }
                    );
                });
    }

    @Test
    public void testStructuralVariant() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample")
                .vcfLines("10 123256215 . T <DEL> 6 PASS SVTYPE=DEL;END=123256420;SVLEN=-205;CIPOS=-56,20;CIEND=-10,62 GT:GQ 0/1:12")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.FRAMESHIFT_TRUNCATION));
        assertThat(variantEvaluation.variantType(), equalTo(VariantType.DEL));

        assertThat(variantEvaluation.start(), equalTo(123256215));
        assertThat(variantEvaluation.startConfidenceInterval(), equalTo(ConfidenceInterval.of(-56, 20)));

        assertThat(variantEvaluation.end(), equalTo(123256420));
        assertThat(variantEvaluation.endConfidenceInterval(), equalTo(ConfidenceInterval.of(-10, 62)));
        assertThat(variantEvaluation.changeLength(), equalTo(-205));

        assertThat(variantEvaluation.ref(), equalTo("T"));
        assertThat(variantEvaluation.alt(), equalTo("<DEL>"));

        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.het())));
    }

    @Test
    void testStructuralVariantNoLength() {
        VcfReader vcfReader = TestVcfReader.builder().samples(sample)
                .vcfLines("1 212471179 esv3588749 T <DEL> 100 PASS CIEND=0,444;CIPOS=-471,0;END=212472619;SVTYPE=DEL;VT=SV GT 0|1")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        // note that the VariantEffect will be 'INTERGENIC_VARIANT' unless the variant overlaps with a gene. In this case there
        // are no transcript models covering this region loaded in the test data, so 'INTERGENIC_VARIANT' is correct
        // functionality here.
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantEvaluation.variantType(), equalTo(VariantType.DEL));

        assertThat(variantEvaluation.coordinates(), equalTo(Coordinates.oneBased(212471179, ConfidenceInterval.of(-471, 0), 212472619, ConfidenceInterval.of(0, 444))));
//        assertThat(variantEvaluation.startPosition(), equalTo(Position.of(212471179, ConfidenceInterval.of(-471, 0))));
//        assertThat(variantEvaluation.endPosition(), equalTo(Position.of(212472619, ConfidenceInterval.of(0, 444))));
        assertThat(variantEvaluation.changeLength(), equalTo(-1441));

        assertThat(variantEvaluation.ref(), equalTo("T"));
        assertThat(variantEvaluation.alt(), equalTo("<DEL>"));

        SampleGenotypes sampleGenotypes = SampleGenotypes.of(sample, SampleGenotype.phased(AlleleCall.REF, AlleleCall.ALT));
        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(sampleGenotypes));
    }

    @Test
    void testStructuralVariantNoEnd() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample")
                .vcfLines("1 112992009 esv3587212 T <INS:ME:ALU> 100 PASS SVLEN=280;SVTYPE=ALU;VT=SV GT 1/0")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.INTERGENIC_VARIANT));
        assertThat(variantEvaluation.variantType(), equalTo(VariantType.INS_ME_ALU));

        assertThat(variantEvaluation.start(), equalTo(112992009));
        assertThat(variantEvaluation.startMax(), equalTo(112992009));
        assertThat(variantEvaluation.startMin(), equalTo(112992009));

        assertThat(variantEvaluation.end(), equalTo(112992009));
        assertThat(variantEvaluation.endMin(), equalTo(112992009));
        assertThat(variantEvaluation.endMax(), equalTo(112992009));

        assertThat(variantEvaluation.changeLength(), equalTo(280));

        assertThat(variantEvaluation.ref(), equalTo("T"));
        assertThat(variantEvaluation.alt(), equalTo("<INS:ME:ALU>"));

        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(SampleGenotypes.of(sample, SampleGenotype.het())));
    }

    @Test
    void testVariantContextIdIsCaptured() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample1")
                .vcfLines("1 112992009 esv3587212 T A 100 PASS . GT 1|0")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        assertThat(variantEvaluation.id(), equalTo("esv3587212"));
    }

    @Test
    void testVariantContextIdIsEmptyWhenDotInVcf() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample1")
                .vcfLines("1 112992009 . T A 100 PASS . GT 1|0")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        assertThat(variantEvaluation.id(), equalTo(""));
    }

    @Test
    void testRetainsCopyNumberVariantWithoutGenotype() {
        VcfReader vcfReader = TestVcfReader.builder().samples("Sample1", "Sample2")
                .vcfLines("1 112992009 Canvas:COMPLEX T <CNV> 100 PASS END=112993000 CN 3 0")
                .build();
        List<VariantEvaluation> variants = newInstance(vcfReader).createVariantEvaluations()
                .collect(toList());
        assertThat(variants.size(), equalTo(1));
        VariantEvaluation variantEvaluation = variants.get(0);
        assertThat(variantEvaluation.getSampleGenotype("Sample1"), equalTo(SampleGenotype.het()));
        assertThat(variantEvaluation.getSampleGenotype("Sample2"), equalTo(SampleGenotype.homAlt()));
    }

}
