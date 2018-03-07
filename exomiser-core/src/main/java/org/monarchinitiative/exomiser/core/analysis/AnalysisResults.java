/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * The results of an Exomiser Analysis run.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisResults {

    private final String probandSampleName;

    @JsonIgnore
    private final List<String> sampleNames;

    private final List<Gene> genes;
    @JsonIgnore
    private final List<VariantEvaluation> variantEvaluations;

    public AnalysisResults(Builder builder) {
        this.probandSampleName = builder.probandSampleName;

        this.sampleNames = builder.sampleNames;

        this.genes = builder.genes;
        this.variantEvaluations = builder.variantEvaluations;
    }

    public String getProbandSampleName() {
        return probandSampleName;
    }

    /**
     * @return List of Strings representing the sample names in the VCF file.
     */
    public List<String> getSampleNames() {
        return sampleNames;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public List<VariantEvaluation> getVariantEvaluations() {
        return variantEvaluations;
    }

    @JsonIgnore
    public List<VariantEvaluation> getUnAnnotatedVariantEvaluations() {
        return variantEvaluations.stream().filter(varEval -> !varEval.hasTranscriptAnnotations()).collect(toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(probandSampleName, sampleNames, variantEvaluations, genes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResults that = (AnalysisResults) o;
        return Objects.equals(probandSampleName, that.probandSampleName) &&
                Objects.equals(sampleNames, that.sampleNames) &&
                Objects.equals(variantEvaluations, that.variantEvaluations) &&
                Objects.equals(genes, that.genes);
    }

    @Override
    public String toString() {
        return "AnalysisResults{" +
                "probandSampleName='" + probandSampleName + '\'' +
                ", sampleNames=" + sampleNames +
                ", genes=" + genes.size() +
                ", variantEvaluations=" + variantEvaluations.size() +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String probandSampleName = "";

        private List<String> sampleNames = Collections.emptyList();

        private List<VariantEvaluation> variantEvaluations = Collections.emptyList();
        private List<Gene> genes = Collections.emptyList();

        public Builder probandSampleName(String probandSampleName) {
            this.probandSampleName = probandSampleName;
            return this;
        }

        public Builder sampleNames(List<String> sampleNames) {
            this.sampleNames = sampleNames;
            return this;
        }

        public Builder variantEvaluations(List<VariantEvaluation> variantList) {
            this.variantEvaluations = variantList;
            return this;
        }

        public Builder genes(List<Gene> geneList) {
            this.genes = geneList;
            return this;
        }

        public AnalysisResults build() {
            return new AnalysisResults(this);
        }

    }
}
