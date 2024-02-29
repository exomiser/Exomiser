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
package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.FilterResultCount;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisResultsTest {

    @Test
    public void defaultConstructorInitialisesGenesVariantEvalations() {
        AnalysisResults instance = AnalysisResults.builder().build();
        assertThat(instance.getSample(), equalTo(Sample.builder().build()));
        assertThat(instance.getAnalysis(), equalTo(Analysis.builder().build()));
        assertThat(instance.getGenes(), equalTo(Collections.emptyList()));
        assertThat(instance.getVariantEvaluations(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testSample() {
        Sample sample = Sample.builder().probandSampleName("Slartibartfast").build();

        AnalysisResults instance = AnalysisResults.builder()
                .sample(sample)
                .build();

        assertThat(instance.getProbandSampleName(), equalTo(sample.getProbandSampleName()));
        assertThat(instance.getSample(), equalTo(sample));
    }

    @Test
    public void testCanSetAndGetSampleNames() {
        List<String> sampleNames = List.of("David");

        AnalysisResults instance = AnalysisResults.builder()
                .sampleNames(sampleNames)
                .build();

        assertThat(instance.getSampleNames(), equalTo(sampleNames));
    }

    @Test
    public void testCanSetAndGetVariantEvaluations() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        AnalysisResults instance = AnalysisResults.builder()
                .variantEvaluations(variantEvaluations)
                .build();
        assertThat(instance.getVariantEvaluations(), equalTo(variantEvaluations));
    }

    @Test
    public void testCanSetAndGetGenes() {
        List<Gene> genes = new ArrayList<>();
        AnalysisResults instance = AnalysisResults.builder()
                .genes(genes)
                .build();
        assertThat(instance.getGenes(), equalTo(genes));
    }

    @Test
    public void testGetGeneScoresReturnsEmptyListWithNoResults() {
        AnalysisResults empty = AnalysisResults.builder().build();
        assertThat(empty.getGeneScores(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testGetGeneScores() {
        //
        Gene fgfr2Gene = TestFactory.newGeneFGFR2();
        VariantEvaluation top = TestFactory.variantBuilder(10, 23456, "G", "T").build();
        GeneScore first = GeneScore.builder()
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .combinedScore(1f)
                .contributingVariants(List.of(top))
                .build();

        VariantEvaluation bottom = TestFactory.variantBuilder(10, 23456, "A", "T").build();
        GeneScore third = GeneScore.builder()
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .combinedScore(0.50f)
                .contributingVariants(List.of(bottom))
                .build();

        fgfr2Gene.addGeneScore(first);
        fgfr2Gene.addGeneScore(third);

        Gene rbm8aGene = TestFactory.newGeneRBM8A();
        VariantEvaluation middle = TestFactory.variantBuilder(7, 456889, "C", "A").build();
        GeneScore second = GeneScore.builder()
                .geneIdentifier(rbm8aGene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .combinedScore(0.75f)
                .contributingVariants(List.of(middle))
                .build();
        rbm8aGene.addGeneScore(second);

        //no gene score for SHH
        Gene shhGene = TestFactory.newGeneSHH();

        List<Gene> genes = List.of(fgfr2Gene, rbm8aGene, shhGene);

        AnalysisResults instance = AnalysisResults.builder()
                .genes(genes)
                .build();

        List<GeneScore> expected = List.of(first, second, third);
        assertThat(instance.getGeneScores(), equalTo(expected));
    }

    @Test
    public void testGetGeneScoresForMode() {
        Gene fgfr2Gene = TestFactory.newGeneFGFR2();
        VariantEvaluation top = TestFactory.variantBuilder(10, 23456, "G", "T").build();
        GeneScore firstAD = GeneScore.builder()
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .combinedScore(1f)
                .contributingVariants(List.of(top))
                .build();

        VariantEvaluation bottom = TestFactory.variantBuilder(10, 23456, "A", "T").build();
        GeneScore thirdAR = GeneScore.builder()
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_RECESSIVE)
                .combinedScore(0.50f)
                .contributingVariants(List.of(bottom))
                .build();

        fgfr2Gene.addGeneScore(firstAD);
        fgfr2Gene.addGeneScore(thirdAR);

        Gene rbm8aGene = TestFactory.newGeneRBM8A();
        VariantEvaluation middle = TestFactory.variantBuilder(7, 456889, "C", "A").build();
        GeneScore secondAD = GeneScore.builder()
                .geneIdentifier(rbm8aGene.getGeneIdentifier())
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .combinedScore(0.75f)
                .contributingVariants(List.of(middle))
                .build();
        rbm8aGene.addGeneScore(secondAD);

        //no gene score for SHH
        Gene shhGene = TestFactory.newGeneSHH();

        List<Gene> genes = List.of(fgfr2Gene, rbm8aGene, shhGene);

        AnalysisResults instance = AnalysisResults.builder()
                .genes(genes)
                .build();

        assertThat(instance.getGeneScoresForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(List.of(firstAD, secondAD)));
        assertThat(instance.getGeneScoresForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(List.of(thirdAR)));
    }

    @Test
    public void testCanReturnUnannotatedVariantEvaluations() {
        VariantEvaluation annotatedVariantEvaluation = TestFactory.variantBuilder(10, 123353297, "G", "C")
                .annotations(Collections.singletonList(TranscriptAnnotation.empty()))
                .build();

        VariantEvaluation unAnnotatedVariantEvaluation = TestFactory.variantBuilder(7, 155604800, "C", "CTT").build();

        List<VariantEvaluation> allVariantEvaluations = List.of(annotatedVariantEvaluation, unAnnotatedVariantEvaluation);

        AnalysisResults instance = AnalysisResults.builder()
                .variantEvaluations(allVariantEvaluations)
                .build();

        List<VariantEvaluation> unAnnotatedVariantEvaluations = Collections.singletonList(unAnnotatedVariantEvaluation);

        assertThat(instance.getUnAnnotatedVariantEvaluations(), equalTo(unAnnotatedVariantEvaluations));
    }

    @Test
    void noFilterCounts() {
        AnalysisResults instance = AnalysisResults.builder()
                .build();
        assertThat(instance.getFilterCounts(), equalTo(Collections.emptyList()));
        assertThat(instance.getFilterCount(FilterType.FREQUENCY_FILTER), equalTo(new FilterResultCount(FilterType.FREQUENCY_FILTER, 0, 0)));
    }

    @Test
    void filterCounts() {
        FilterResultCount freqFilterResultCount = new FilterResultCount(FilterType.FREQUENCY_FILTER, 1000, 2000);
        FilterResultCount pathFilterResultCount = new FilterResultCount(FilterType.PATHOGENICITY_FILTER, 3000, 0);
        List<FilterResultCount> filterResultCounts = List.of(
                freqFilterResultCount,
                pathFilterResultCount
        );
        AnalysisResults instance = AnalysisResults.builder()
                .filterCounts(filterResultCounts)
                .build();

        assertThat(instance.getFilterCounts(), equalTo(filterResultCounts));
        assertThat(instance.getFilterCount(FilterType.FREQUENCY_FILTER), equalTo(freqFilterResultCount));
        assertThat(instance.getFilterCount(FilterType.PATHOGENICITY_FILTER), equalTo(pathFilterResultCount));
        assertThat(instance.getFilterCount(FilterType.VARIANT_EFFECT_FILTER), equalTo(new FilterResultCount(FilterType.VARIANT_EFFECT_FILTER, 0, 0)));
    }

    @Test
    public void testHashCode() {
        AnalysisResults instance = AnalysisResults.builder().build();
        AnalysisResults other = AnalysisResults.builder().build();
        assertThat(instance.hashCode(), equalTo(other.hashCode()));
    }

    @Test
    public void testEquals() {
        Sample sample = Sample.builder().probandSampleName("wibble").build();
        AnalysisResults instance = AnalysisResults.builder().sample(sample).build();
        AnalysisResults other = AnalysisResults.builder().sample(sample).build();
        assertThat(instance, equalTo(other));
    }

    @Test
    public void testNotEquals() {
        Sample sample = Sample.builder().probandSampleName("wibble").build();
        AnalysisResults instance = AnalysisResults.builder().sample(sample).build();
        AnalysisResults other = AnalysisResults.builder()
                .sample(sample)
                .sampleNames(List.of("Fred", "Wilma"))
                .build();
        assertThat(instance, not(equalTo(other)));
    }

    @Test
    public void testString() {
        Sample sample = Sample.builder().probandSampleName("Zaphod_Beeblebrox").build();
        AnalysisResults instance = AnalysisResults.builder().sample(sample).build();
    }
}
