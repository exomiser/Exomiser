/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RawScoreGeneScorerTest {

    private static final FilterResult PASS_FREQUENCY = FilterResult.pass(FilterType.FREQUENCY_FILTER);
    private static final FilterResult FAIL_FREQUENCY = FilterResult.fail(FilterType.FREQUENCY_FILTER);
    private static final FilterResult PASS_PATHOGENICITY = FilterResult.pass(FilterType.PATHOGENICITY_FILTER);
    private static final FilterResult FAIL_PATHOGENICITY = FilterResult.fail(FilterType.PATHOGENICITY_FILTER);

    private Gene newGene(VariantEvaluation... variantEvaluations) {
        Gene gene = new Gene("TEST1", 1234);
        Arrays.stream(variantEvaluations).forEach(gene::addVariant);
        return gene;
    }

    private VariantEvaluation failFreq() {
        return VariantEvaluation.builder(1, 1, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(FAIL_FREQUENCY)
                .build();
    }

    private VariantEvaluation passAllFrameShift() {
        return VariantEvaluation.builder(1, 2, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .build();
    }

    private VariantEvaluation passAllFrameShiftWithSampleGenotype(Map<String, SampleGenotype> sampleGenotypes) {
        return VariantEvaluation.builder(1, 2, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(sampleGenotypes)
                .build();
    }

    private VariantEvaluation passAllMissense() {
        return VariantEvaluation.builder(1, 3, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .build();
    }

    private VariantEvaluation passAllMissenseWithSampleGenotype(Map<String, SampleGenotype> sampleGenotypes) {
        return VariantEvaluation.builder(1, 3, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(sampleGenotypes)
                .build();
    }

    private VariantEvaluation passAllSynonymous() {
        return VariantEvaluation.builder(1, 4, "A", "T")
                .variantEffect(VariantEffect.SYNONYMOUS_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .build();
    }

    private List<GeneScore> scoreGene(Gene gene, ModeOfInheritance modeOfInheritance) {
        return scoreGene(gene, modeOfInheritance, SampleIdentifier.of("sample", 0), Pedigree.justProband("sample"));
    }

    private List<GeneScore> scoreGene(Gene gene, ModeOfInheritance modeOfInheritance, Sex sex) {
        return scoreGene(gene, modeOfInheritance, SampleIdentifier.of("sample", 0), Pedigree.justProband("sample", sex), sex);
    }

    private List<GeneScore> scoreGene(Gene gene, ModeOfInheritance modeOfInheritance, SampleIdentifier probandSample, Pedigree pedigree) {
        return scoreGene(gene, modeOfInheritance, probandSample, pedigree, Sex.UNKNOWN);
    }

    private List<GeneScore> scoreGene(Gene gene, ModeOfInheritance modeOfInheritance, SampleIdentifier probandSample, Pedigree pedigree, Sex sex) {
        InheritanceModeAnnotator inheritanceModeAnnotator = new InheritanceModeAnnotator(pedigree, InheritanceModeOptions
                .defaultForModes(modeOfInheritance));
        RawScoreGeneScorer instance = new RawScoreGeneScorer(probandSample, sex, inheritanceModeAnnotator);
        return instance.scoreGene().apply(gene);
    }

    private List<GeneScore> scoreGene(Gene gene, InheritanceModeOptions inheritanceModeOptions) {
        return scoreGene(gene, inheritanceModeOptions, SampleIdentifier.of("sample", 0), Pedigree.justProband("sample"));
    }

    private List<GeneScore> scoreGene(Gene gene, InheritanceModeOptions inheritanceModeOptions, SampleIdentifier probandSample) {
        return scoreGene(gene, inheritanceModeOptions, probandSample, Pedigree.justProband("sample"));
    }

    private List<GeneScore> scoreGene(Gene gene, InheritanceModeOptions inheritanceModeOptions, SampleIdentifier probandSample, Pedigree pedigree) {
        return getInstance(inheritanceModeOptions, probandSample, pedigree).scoreGene().apply(gene);
    }

    private RawScoreGeneScorer getInstance(InheritanceModeOptions inheritanceModeOptions, SampleIdentifier probandSample, Pedigree pedigree) {
        InheritanceModeAnnotator inheritanceModeAnnotator = new InheritanceModeAnnotator(pedigree, inheritanceModeOptions);
        return new RawScoreGeneScorer(probandSample, Sex.UNKNOWN, inheritanceModeAnnotator);
    }

    private RawScoreGeneScorer getInstance(InheritanceModeOptions inheritanceModeOptions, Sex sex, SampleIdentifier probandSample, Pedigree pedigree) {
        InheritanceModeAnnotator inheritanceModeAnnotator = new InheritanceModeAnnotator(pedigree, inheritanceModeOptions);
        return new RawScoreGeneScorer(probandSample, sex, inheritanceModeAnnotator);
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariantsUninitialised() {
        Gene gene = newGene();
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariantsAutosomalDominant() {
        Gene gene = newGene();
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(Collections.emptyList())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariantsAutosomalRecessive() {
        Gene gene = newGene();
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(Collections.emptyList())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariantUninitialised() {
        Gene gene = newGene(failFreq());
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(Collections.emptyList())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariantAutosomalDominant() {
        Gene gene = newGene(failFreq());
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(Collections.emptyList())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariantAutosomalRecessive() {
        Gene gene = newGene(failFreq());
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(Collections.emptyList())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariantUninitialised() {
        VariantEvaluation passAllFrameshift = passAllFrameShift();
        Gene gene = newGene(passAllFrameshift);
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        float variantScore = passAllFrameshift.getVariantScore();

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariantAutosomalDominant() {
        VariantEvaluation passAllFrameshift = passAllFrameShift();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        Gene gene = newGene(passAllFrameshift);
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);

        float variantScore = passAllFrameshift.getVariantScore();

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariantAutosomalRecessiveHomAlt() {
        List<Allele> alleles = buildAlleles("A", "T");

        //Classical recessive inheritance mode
        Genotype proband = buildUnPhasedSampleGenotype("Cain", alleles.get(1), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HOM_VAR));

        Genotype mother = buildUnPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        Genotype father = buildUnPhasedSampleGenotype("Adam", alleles.get(1), alleles.get(0));
        assertThat(father.getType(), equalTo(GenotypeType.HET));

        VariantContext variantContext = buildVariantContext(1, 12345, alleles, proband, mother, father);
        System.out.println("Built variant context " + variantContext);
        System.out.println("Proband sample 0 has genotype " + variantContext.getGenotype(0).getGenotypeString());

        Individual probandIndividual = Individual.builder().id("Cain").fatherId("Adam").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").fatherId("").motherId("").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();
        Individual fatherIndividual = Individual.builder().id("Adam").fatherId("").motherId("").sex(Sex.MALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, fatherIndividual);

        VariantEvaluation probandHomAlt = filteredVariant(1, 12345, "A", "T", FilterResult.pass(FilterType.FREQUENCY_FILTER), variantContext, VariantEffect.MISSENSE_VARIANT);
        probandHomAlt.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        Gene gene = newGene(probandHomAlt);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE, SampleIdentifier.of(probandIndividual.getId(), 0), pedigree);

        float variantScore = probandHomAlt.getVariantScore();

        assertThat(probandHomAlt.contributesToGeneScore(), is(true));

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(probandHomAlt))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariantAutosomalRecessiveHet() {
        VariantEvaluation passAllFrameShift = passAllFrameShift();
        Gene gene = newGene(passAllFrameShift);
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        //A single het allele can't be compatible with AR
        assertThat(passAllFrameShift.contributesToGeneScore(), is(false));

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(Collections.emptyList())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithSinglePassedAndSingleFailedVariantOnlyPassedVariantIsConsidered() {
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllFrameshift, failFreq());
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariantsUninitialisedInheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllFrameshift, passAllMissense);
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariantsAutosomalDominantInheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        VariantEvaluation passAllFrameshift = passAllFrameShift();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        Gene gene = newGene(passAllFrameshift, passAllMissense);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));
        assertThat(passAllFrameshift.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariantsXDominantInheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT));

        VariantEvaluation passAllFrameshift = passAllFrameShift();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT));

        Gene gene = newGene(passAllFrameshift, passAllMissense);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.X_DOMINANT);

        float variantScore = passAllFrameshift.getVariantScore();

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.X_DOMINANT)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                //REALLY? This isn't X-linked
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneXRecessiveModeForFemale() {
        VariantEvaluation passAllMissense = passAllMissense();
        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT));

        VariantEvaluation passAllFrameshift = passAllFrameShiftWithSampleGenotype(ImmutableMap.of("sample", SampleGenotype
                .homAlt()));
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        Gene gene = newGene(passAllFrameshift, passAllMissense);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.X_RECESSIVE, Sex.FEMALE);

        float variantScore = passAllFrameshift.getVariantScore();

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.X_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneXRecessiveCompHetModeForFemale() {
        // X chromosome is chr 23
        VariantEvaluation passAllMissense = VariantEvaluation.builder(23, 3, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(ImmutableMap.of("sample", SampleGenotype.het()))
                .build();

        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        VariantEvaluation passAllFrameshift = VariantEvaluation.builder(23, 2, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(ImmutableMap.of("sample", SampleGenotype.het()))
                .build();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        Gene gene = newGene(passAllFrameshift, passAllMissense);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.X_RECESSIVE, Sex.FEMALE);

        float variantScore = (passAllMissense.getVariantScore() + passAllFrameshift.getVariantScore()) / 2;

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.X_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift, passAllMissense))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneXRecessiveModeForMale() {
        // X chromosome is chr 23
        VariantEvaluation passAllMissense = VariantEvaluation.builder(23, 3, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(ImmutableMap.of("sample", SampleGenotype.het()))
                .build();

        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        VariantEvaluation passAllFrameshift = VariantEvaluation.builder(23, 2, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(ImmutableMap.of("sample", SampleGenotype.het()))
                .build();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        Gene gene = newGene(passAllFrameshift, passAllMissense);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.X_RECESSIVE, Sex.MALE);

        float variantScore = passAllFrameshift.getVariantScore();
        // Given the X-linked MOI here it is a dominant pattern for a male, even under the X-recessive model.
        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.X_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneXRecessiveCompHetUnderNonXModeFemale() {
        // X chromosome is chr 23
        VariantEvaluation passAllMissense = VariantEvaluation.builder(23, 3, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(ImmutableMap.of("sample", SampleGenotype.het()))
                .build();

        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        VariantEvaluation passAllFrameshift = VariantEvaluation.builder(23, 2, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(ImmutableMap.of("sample", SampleGenotype.het()))
                .build();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE));

        Gene gene = newGene(passAllFrameshift, passAllMissense);

        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT, Sex.FEMALE);

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .variantScore(0f)
                .phenotypeScore(0f)
                .combinedScore(0f)
                .contributingVariants(ImmutableList.of())
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariantsAutosomalRecessiveInheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        VariantEvaluation passAllFrameshift = passAllFrameShift();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        Gene gene = newGene(passAllMissense, passAllFrameshift);
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        float variantScore = (passAllFrameshift.getVariantScore() + passAllMissense.getVariantScore()) / 2f;

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllMissense, passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithThreePassedVariantsAutosomalRecessiveInheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        VariantEvaluation passAllSynonymous = passAllSynonymous();
        passAllSynonymous.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        VariantEvaluation passAllFrameshift = passAllFrameShift();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        Gene gene = newGene(passAllMissense, passAllSynonymous, passAllFrameshift);
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        float variantScore = (passAllFrameshift.getVariantScore() + passAllMissense.getVariantScore()) / 2f;
        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));
        assertThat(passAllFrameshift.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        assertThat(passAllMissense.contributesToGeneScore(), is(true));
        assertThat(passAllMissense.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        assertThat(passAllSynonymous.contributesToGeneScore(), is(false));
        assertThat(passAllSynonymous.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));


        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllMissense, passAllFrameshift))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithThreePassedVariantsAutosomalRecessiveAndCompHetInheritance() {
        // Here we're testing that a homozygous alt missense allele is ranked above a comp-het pair
        VariantEvaluation passAllMissense = VariantEvaluation.builder(1, 3, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .sampleGenotypes(Map.of("sample", SampleGenotype.homAlt()))
                .build();
        passAllMissense.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        VariantEvaluation passAllSynonymous = passAllSynonymous();
        passAllSynonymous.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        VariantEvaluation passAllFrameshift = passAllFrameShift();
        passAllFrameshift.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        Gene gene = newGene(passAllMissense, passAllSynonymous, passAllFrameshift);
        List<GeneScore> geneScores = scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        float variantScore = passAllMissense.getVariantScore();
        assertThat(passAllMissense.contributesToGeneScore(), is(true));
        assertThat(passAllMissense.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));

        assertThat(passAllFrameshift.contributesToGeneScore(), is(false));
        assertThat(passAllFrameshift.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));

        assertThat(passAllSynonymous.contributesToGeneScore(), is(false));
        assertThat(passAllSynonymous.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(false));


        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .variantScore(variantScore)
                .phenotypeScore(0f)
                .combinedScore(variantScore / 2)
                .contributingVariants(ImmutableList.of(passAllMissense))
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testGenesAreRankedAccordingToScore() {
        Gene first = new Gene("FIRST", 1111);
        first.addVariant(passAllFrameShift());
        first.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, first.getEntrezGeneID(), first.getGeneSymbol(), 1d));

        Gene middle = new Gene("MIDDLE", 2222);
        middle.addVariant(passAllMissense());
        middle.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, middle.getEntrezGeneID(), middle.getGeneSymbol(), 1d));

        Gene last = new Gene("LAST", 3333);
        last.addVariant(passAllSynonymous());
        last.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, last.getEntrezGeneID(), last.getGeneSymbol(), 1d));

        List<Gene> genes = Lists.newArrayList(last, first, middle);
        Collections.shuffle(genes);

        SampleIdentifier sampleIdentifier = SampleIdentifier.of("sample", 0);
        InheritanceModeAnnotator inheritanceModeAnnotator = new InheritanceModeAnnotator(Pedigree.justProband("Nemo"), InheritanceModeOptions
                .empty());
        RawScoreGeneScorer instance = new RawScoreGeneScorer(sampleIdentifier, Sex.UNKNOWN, inheritanceModeAnnotator);
        instance.scoreGenes(genes);

        genes.forEach(System.out::println);

        assertThat(genes.indexOf(first), equalTo(0));
        assertThat(genes.indexOf(middle), equalTo(1));
        assertThat(genes.indexOf(last), equalTo(2));
    }

    ///Priority and Combined score tests
    @Test
    public void testCalculateCombinedScoreFromUnoptimisedPrioritiser() {
        Gene gene = newGene();
        gene.addPriorityResult(new MockPriorityResult(PriorityType.OMIM_PRIORITY, gene.getEntrezGeneID(), gene.getGeneSymbol(), 1d));

        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        GeneScore expected = GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.ANY)
                .variantScore(0f)
                .phenotypeScore(1f)
                .combinedScore(0.5f)
                .build();

        assertThat(geneScores, equalTo(ImmutableList.of(expected)));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariantsAllInheritanceModes() {
        Gene gene = newGene();
        EnumSet<ModeOfInheritance> inheritanceModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE, ModeOfInheritance.X_DOMINANT, ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.MITOCHONDRIAL);
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.defaults());

        List<GeneScore> expected = inheritanceModes.stream()
                .map(mode ->  GeneScore.builder()
                    .geneIdentifier(gene.getGeneIdentifier())
                    .modeOfInheritance(mode)
                    .variantScore(0f)
                    .phenotypeScore(0f)
                    .combinedScore(0f)
                    .contributingVariants(Collections.emptyList())
                    .build())
                .collect(toList());

        assertThat(geneScores, equalTo(expected));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariantsOrInheritanceModes() {
        Gene gene = newGene();
        List<GeneScore> geneScores = scoreGene(gene, InheritanceModeOptions.empty());

        List<GeneScore> expected = ImmutableList.of(
                GeneScore.builder()
                        .geneIdentifier(gene.getGeneIdentifier())
                        .modeOfInheritance(ModeOfInheritance.ANY)
                        .variantScore(0f)
                        .phenotypeScore(0f)
                        .combinedScore(0f)
                        .contributingVariants(Collections.emptyList())
                        .build()
        );

        assertThat(geneScores, equalTo(expected));
    }

}
