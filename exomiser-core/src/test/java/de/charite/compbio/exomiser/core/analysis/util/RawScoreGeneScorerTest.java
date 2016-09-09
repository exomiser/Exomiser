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
package de.charite.compbio.exomiser.core.analysis.util;

import com.google.common.collect.Lists;
import de.charite.compbio.exomiser.core.filters.FailFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.PassFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.MockPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RawScoreGeneScorerTest {

    private static final PassFilterResult PASS_FREQUENCY = new PassFilterResult(FilterType.FREQUENCY_FILTER);
    private static final FailFilterResult FAIL_FREQUENCY = new FailFilterResult(FilterType.FREQUENCY_FILTER);
    private static final PassFilterResult PASS_PATHOGENICITY = new PassFilterResult(FilterType.PATHOGENICITY_FILTER);
    private static final FailFilterResult FAIL_PATHOGENICITY = new FailFilterResult(FilterType.PATHOGENICITY_FILTER);

    private RawScoreGeneScorer instance;
    
    private VariantEvaluation failFreq;
    private VariantEvaluation failPath;
    private VariantEvaluation failFreqPassPath;
    private VariantEvaluation passAllFrameshift;
    private VariantEvaluation passAllMissense;
    private VariantEvaluation passAllSynonymous;

    @Before
    public void setUp() {
        instance = new RawScoreGeneScorer();
        
        failFreq = new VariantEvaluation
                .VariantBuilder(1, 1, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        failFreq.addFilterResult(FAIL_FREQUENCY);

        failPath = new VariantEvaluation
                .VariantBuilder(1, 1, "A", "T")
                .variantEffect(VariantEffect.CODING_SEQUENCE_VARIANT)
                .build();
        failPath.addFilterResult(FAIL_PATHOGENICITY);

        failFreqPassPath = new VariantEvaluation
                .VariantBuilder(1, 1, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        failFreqPassPath.addFilterResult(FAIL_FREQUENCY);
        failFreqPassPath.addFilterResult(PASS_PATHOGENICITY);
        
        // Scoring should only includes variants which have actually passed all the filters
        passAllFrameshift = new VariantEvaluation
                .VariantBuilder(1, 1, "A", "T")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .build();
        passAllFrameshift.addFilterResult(PASS_FREQUENCY);
        passAllFrameshift.addFilterResult(PASS_PATHOGENICITY);

        passAllMissense = new VariantEvaluation
                .VariantBuilder(1, 1, "A", "T")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        passAllFrameshift.addFilterResult(PASS_FREQUENCY);
        passAllFrameshift.addFilterResult(PASS_PATHOGENICITY);

        passAllSynonymous = new VariantEvaluation
                .VariantBuilder(1, 1, "A", "T")
                .variantEffect(VariantEffect.SYNONYMOUS_VARIANT)
                .build();
        passAllSynonymous.addFilterResult(PASS_FREQUENCY);
        passAllSynonymous.addFilterResult(PASS_PATHOGENICITY);
    }

    private Gene newGene(VariantEvaluation... variantEvaluations) {
        Gene gene = new Gene("TEST1", 1234);
        Arrays.stream(variantEvaluations).forEach(gene::addVariant);
        return gene;
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariants_UNINITIALIZED() {
        Gene gene = newGene();
        instance.scoreGene(gene, ModeOfInheritance.UNINITIALIZED);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithoutPriorityResultsOrVariants_AUTOSOMAL_RECESSIVE() {
        Gene gene = newGene();
        instance.scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariant_UNINITIALIZED() {
        Gene gene = newGene(failFreq);
        instance.scoreGene(gene, ModeOfInheritance.UNINITIALIZED);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSingleFailedVariant_AUTOSOMAL_RECESSIVE() {
        Gene gene = newGene(failFreq);
        instance.scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(0f));
    }

    @Test
    public void testScoreGeneWithSinglePassedVariant() {
        Gene gene = newGene(passAllFrameshift);
        instance.scoreGene(gene, ModeOfInheritance.UNINITIALIZED);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithSinglePassedAndSingleFailedVariantOnlyPassedVariantIsConsidered() {
        Gene gene = newGene(passAllFrameshift, failFreq);
        instance.scoreGene(gene, ModeOfInheritance.UNINITIALIZED);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_UNINITIALIZED_inheritance() {
        Gene gene = newGene(passAllFrameshift, passAllMissense);
        instance.scoreGene(gene, ModeOfInheritance.UNINITIALIZED);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_AUTOSOMAL_DOMINANT_inheritance() {
        Gene gene = newGene(passAllFrameshift, passAllMissense);
        instance.scoreGene(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_X_DOMINANT_inheritance() {
        Gene gene = newGene(passAllFrameshift, passAllMissense);
        instance.scoreGene(gene, ModeOfInheritance.X_DOMINANT);

        float variantScore = passAllFrameshift.getVariantScore();

        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithTwoPassedVariants_AUTOSOMAL_RECESSIVE_inheritance() {
        Gene gene = newGene(passAllMissense, passAllFrameshift);
        instance.scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        float variantScore = (passAllFrameshift.getVariantScore() + passAllMissense.getVariantScore()) / 2f;
        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testScoreGeneWithThreePassedVariants_AUTOSOMAL_RECESSIVE_inheritance() {
        Gene gene = newGene(passAllMissense, passAllSynonymous, passAllFrameshift);
        instance.scoreGene(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);

        float variantScore = (passAllFrameshift.getVariantScore() + passAllMissense.getVariantScore()) / 2f;
        assertThat(gene.getFilterScore(), equalTo(variantScore));
        assertThat(gene.getPriorityScore(), equalTo(0f));
        assertThat(gene.getCombinedScore(), equalTo(variantScore / 2));
    }

    @Test
    public void testGenesAreRankedAccordingToScore() {
        Gene first = new Gene("FIRST", 1111);
        first.addVariant(passAllFrameshift);
        first.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, first.getEntrezGeneID(), first.getGeneSymbol(), 1d));

        Gene middle = new Gene("MIDDLE", 2222);
        middle.addVariant(passAllMissense);
        middle.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, middle.getEntrezGeneID(), middle.getGeneSymbol(), 1d));

        Gene last = new Gene("LAST", 3333);
        last.addVariant(passAllSynonymous);
        last.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, last.getEntrezGeneID(), last.getGeneSymbol(), 1d));

        List<Gene> genes = Lists.newArrayList(last, first, middle);
        Collections.shuffle(genes);

        instance.scoreGenes(genes, ModeOfInheritance.UNINITIALIZED);

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

        instance.scoreGene(gene, ModeOfInheritance.UNINITIALIZED);

        assertThat(gene.getFilterScore(), equalTo(0f));
        assertThat(gene.getPriorityScore(), equalTo(1f));
        assertThat(gene.getCombinedScore(), equalTo(0.5f));
    }


}
