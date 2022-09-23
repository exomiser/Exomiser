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

package org.monarchinitiative.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The results of an Exomiser Analysis run.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 8.0.0
 */
public class AnalysisResults {

    private final Sample sample;
    private final Analysis analysis;

    @JsonIgnore
    private final List<String> sampleNames;

    private final List<Gene> genes;
    @JsonIgnore
    private final List<VariantEvaluation> variantEvaluations;

    public AnalysisResults(Builder builder) {
        this.sample = builder.sample;
        this.analysis = builder.analysis;

        this.sampleNames = builder.sampleNames;

        this.genes = builder.genes;
        this.variantEvaluations = builder.variantEvaluations;
    }

    /**
     * The sample name of the proband as found in the input VCF file.
     *
     * @return the proband sample name.
     */
    public String getProbandSampleName() {
        return sample.getProbandSampleName();
    }

    /**
     * @return List of Strings representing the sample names in the VCF file.
     */
    public List<String> getSampleNames() {
        return sampleNames;
    }

    /**
     * @return The {@link Sample} which was run through the {@link Analysis} to generate the {@link AnalysisResults}.
     * @since 13.0.0
     */
    public Sample getSample() {
        return sample;
    }

    /**
     * @return The {@link Analysis} through which the {@link Sample} was run in order to generate the {@link AnalysisResults}.
     * @since 13.0.0
     */
    public Analysis getAnalysis() {
        return analysis;
    }

    /**
     * A list of {@link Gene} objects resulting from an {@link Analysis}.
     * <p>
     * IMPORTANT: A {@link Gene} could have several {@link GeneScore}, with different overall ranks depending on the
     * {@link ModeOfInheritance} and the scoring from the OmimPrioritiser with the inheritance mode compatibility.
     * For this reason directly iterating through the genes and their scores in order will result in incorrect overall
     * rankings. To get the correct variant rankings use the {@code getGeneScores()} or {@code getGeneScoresForMode()}
     * methods.
     *
     * @return a list of {@link Gene} objects resulting from an {@link Analysis}.
     */
    public List<Gene> getGenes() {
        return genes;
    }

    /**
     * A list of {@link VariantEvaluation} objects resulting from an {@link Analysis}.
     * <p>
     * IMPORTANT: A {@link VariantEvaluation} could be compatible with several {@link ModeOfInheritance} and may or may
     * not contribute to the overall {@link GeneScore}. For this reason directly iterating through the {@link VariantEvaluation}
     * and their scores in order will result in incorrect overall rankings. To get the correct contributing variant rankings
     * use the {@code getContributingVariants()} or {@code getContributingVariantsForMode()} methods.
     *
     * @return a list of {@link VariantEvaluation} objects resulting from an {@link Analysis}.
     */
    public List<VariantEvaluation> getVariantEvaluations() {
        return variantEvaluations;
    }

    /**
     * Returns a list of {@link GeneScore} objects computed from the gene results. These {@link GeneScore} will be ranked
     * by the combined score and will contain the results for all {@link ModeOfInheritance}. The {@link GeneScore} objects
     * will all contain contributing variants.
     * <p>
     * If you require a ranked list of {@link GeneScore} and their contributing variants, use this method.
     *
     * @return a ranked list of {@link GeneScore} and their contributing variants.
     * @since 10.1.0
     */
    @JsonIgnore
    public List<GeneScore> getGeneScores() {
        return sortedGeneScoresWithContributingVariants().toList();
    }

    /**
     * Returns a list of {@link GeneScore} objects computed from the gene results for the given {@link ModeOfInheritance}.
     * These {@link GeneScore} will be ranked  by the combined score and will only contain the results for the stated
     * {@link ModeOfInheritance}. The {@link GeneScore} objects will all contain contributing variants.
     * <p>
     * If you require a ranked list of {@link GeneScore} and their contributing variants for a specific
     * {@link ModeOfInheritance}, use this method.
     *
     * @return a ranked list of {@link GeneScore} and their contributing variants for the {@link ModeOfInheritance} argument.
     * @since 10.1.0
     */
    @JsonIgnore
    public List<GeneScore> getGeneScoresForMode(ModeOfInheritance modeOfInheritance) {
        return sortedGeneScoresWithContributingVariantsForMode(modeOfInheritance).toList();
    }

    /**
     * @return the ranked variants contributing to the gene score under all modes of inheritance
     * @since 10.1.0
     */
    @JsonIgnore
    public List<VariantEvaluation> getContributingVariants() {
        return sortedGeneScoresWithContributingVariants()
                .map(GeneScore::getContributingVariants)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * @return
     * @since 10.1.0
     */
    @JsonIgnore
    public List<VariantEvaluation> getContributingVariantsForMode(ModeOfInheritance modeOfInheritance) {
        return sortedGeneScoresWithContributingVariantsForMode(modeOfInheritance)
                .map(GeneScore::getContributingVariants)
                .flatMap(Collection::stream)
                .sorted()
                .toList();
    }

    private Stream<GeneScore> sortedGeneScoresWithContributingVariants() {
        return genes.stream()
                .map(Gene::getGeneScores)
                .flatMap(Collection::stream)
                // A geneScore can have no contributing variants, so want to skip these
                .filter(GeneScore::hasContributingVariants)
                .sorted();
    }

    private Stream<GeneScore> sortedGeneScoresWithContributingVariantsForMode(ModeOfInheritance modeOfInheritance) {
        return genes.stream()
                .map(gene -> gene.getGeneScoreForMode(modeOfInheritance))
                // A geneScore can have no contributing variants, so want to skip these
                .filter(GeneScore::hasContributingVariants)
                .sorted();
    }

    @JsonIgnore
    public List<VariantEvaluation> getUnAnnotatedVariantEvaluations() {
        return variantEvaluations.stream().filter(varEval -> !varEval.hasTranscriptAnnotations()).toList();
    }

    @Override
    public int hashCode() {
        return Objects.hash(sample, sampleNames, variantEvaluations, genes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResults that = (AnalysisResults) o;
        return Objects.equals(sample, that.sample) &&
                Objects.equals(sampleNames, that.sampleNames) &&
                Objects.equals(variantEvaluations, that.variantEvaluations) &&
                Objects.equals(genes, that.genes);
    }

    @Override
    public String toString() {
        return "AnalysisResults{" +
                "sample='" + sample + '\'' +
                ", sampleNames=" + sampleNames +
                ", genes=" + genes.size() +
                ", variantEvaluations=" + variantEvaluations.size() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Sample sample = Sample.builder().build();
        private Analysis analysis = Analysis.builder().build();

        private List<String> sampleNames = List.of();

        private List<VariantEvaluation> variantEvaluations = List.of();
        private List<Gene> genes = List.of();


        public Builder sample(Sample sample) {
            this.sample = Objects.requireNonNull(sample);
            return this;
        }

        public Builder analysis(Analysis analysis) {
            this.analysis = Objects.requireNonNull(analysis);
            return this;
        }

        public Builder sampleNames(List<String> sampleNames) {
            this.sampleNames = Objects.requireNonNull(sampleNames);
            return this;
        }

        public Builder variantEvaluations(List<VariantEvaluation> variantList) {
            this.variantEvaluations = Objects.requireNonNull(variantList);
            return this;
        }

        public Builder genes(List<Gene> geneList) {
            this.genes = Objects.requireNonNull(geneList);
            return this;
        }

        public AnalysisResults build() {
            return new AnalysisResults(this);
        }

    }
}
