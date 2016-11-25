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
package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RawScoreGeneScorerTest {

    private static final FilterResult PASS_FREQUENCY = FilterResult.pass(FilterType.FREQUENCY_FILTER);
    private static final FilterResult FAIL_FREQUENCY = FilterResult.fail(FilterType.FREQUENCY_FILTER);
    private static final FilterResult PASS_PATHOGENICITY = FilterResult.pass(FilterType.PATHOGENICITY_FILTER);
    private static final FilterResult FAIL_PATHOGENICITY = FilterResult.fail(FilterType.PATHOGENICITY_FILTER);

    private RawScoreGeneScorer instance;


    @Before
    public void setUp() {
        instance = new RawScoreGeneScorer();
    }

    private Gene newGene(VariantEvaluation... variantEvaluations) {
        Gene gene = new Gene("TEST1", 1234);
        Arrays.stream(variantEvaluations).forEach(gene::addVariant);
        return gene;
    }

    private VariantEvaluation failFreq() {
        return new VariantEvaluation.Builder(1, 1, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(FAIL_FREQUENCY)
                .build();
    }

    private VariantEvaluation passAllFrameShift() {
        return new VariantEvaluation.Builder(1, 1, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .build();
    }

    VariantEvaluation passAllMissense() {
        return new VariantEvaluation.Builder(1, 1, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .build();
    }

    VariantEvaluation passAllSynonymous() {
        return new VariantEvaluation.Builder(1, 1, "A", "T")
                .variantEffect(VariantEffect.SYNONYMOUS_VARIANT)
                .filterResults(PASS_FREQUENCY, PASS_PATHOGENICITY)
                .build();
    }

    private void scoreGene(Gene gene, ModeOfInheritance modeOfInheritance, int sampleId) {
        instance.scoreGene(modeOfInheritance, sampleId).accept(gene);
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariants_UNINITIALIZED() {
        Gene gene = newGene();
        scoreGene(gene, ModeOfInheritance.ANY, 0);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariants_AUTOSOMAL_DOMINANT() {
        Gene gene = newGene();
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT, 0);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariants_AUTOSOMAL_RECESSIVE() {
        Gene gene = newGene();
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariant_UNINITIALIZED() {
        Gene gene = newGene(failFreq());
        scoreGene(gene, ModeOfInheritance.ANY, 0);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariant_AUTOSOMAL_DOMINANT() {
        Gene gene = newGene(failFreq());
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT, 0);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariant_AUTOSOMAL_RECESSIVE() {
        Gene gene = newGene(failFreq());
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariant_UNINITIALIZED() {
        VariantEvaluation passAllFrameshift = passAllFrameShift();
        Gene gene = newGene(passAllFrameshift);
        scoreGene(gene, ModeOfInheritance.ANY, 0);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariant_AUTOSOMAL_DOMINANT() {
        VariantEvaluation passAllFrameshift = passAllFrameShift();
        Gene gene = newGene(passAllFrameshift);
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT, 0);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariant_AUTOSOMAL_RECESSIVE() {
        VariantEvaluation passAllFrameShift = passAllFrameShift();
        Gene gene = newGene(passAllFrameShift);
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0);

        float variantScore = passAllFrameShift.getVariantScore();

        assertThat(passAllFrameShift.contributesToGeneScore(), is(true));

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithSinglePassedAndSingleFailedVariantOnlyPassedVariantIsConsidered() {
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllFrameshift, failFreq());
        scoreGene(gene, ModeOfInheritance.ANY, 0);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_UNINITIALIZED_inheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllFrameshift, passAllMissense);
        scoreGene(gene, ModeOfInheritance.ANY, 0);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_AUTOSOMAL_DOMINANT_inheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllFrameshift, passAllMissense);
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT, 0);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_X_DOMINANT_inheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllFrameshift, passAllMissense);
        scoreGene(gene, ModeOfInheritance.X_DOMINANT, 0);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_AUTOSOMAL_RECESSIVE_inheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllMissense, passAllFrameshift);
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0);

        float variantScore = (passAllFrameshift.getVariantScore() + passAllMissense.getVariantScore()) / 2f;
        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithThreePassedVariants_AUTOSOMAL_RECESSIVE_inheritance() {
        VariantEvaluation passAllMissense = passAllMissense();
        VariantEvaluation passAllSynonymous = passAllSynonymous();
        VariantEvaluation passAllFrameshift = passAllFrameShift();

        Gene gene = newGene(passAllMissense, passAllSynonymous, passAllFrameshift);
        scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0);

        float variantScore = (passAllFrameshift.getVariantScore() + passAllMissense.getVariantScore()) / 2f;
        assertThat(passAllFrameshift.contributesToGeneScore(), is(true));
        assertThat(passAllMissense.contributesToGeneScore(), is(true));
        assertThat(passAllSynonymous.contributesToGeneScore(), is(false));

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
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

        instance.scoreGenes(genes, ModeOfInheritance.ANY, 0);

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

        scoreGene(gene, ModeOfInheritance.ANY, 0);

        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(1f));
        assertThat(gene.getCombinedScore(), equalTo(0.5f));
    }


}
