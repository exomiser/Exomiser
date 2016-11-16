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

package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.vcf.VCFHeader;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.nio.file.Path;
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

    private final Path vcfPath;
    private final Path pedPath;
    private final String probandSampleName;

    private final VCFHeader vcfHeader;
    private final List<String> sampleNames;
    private final Pedigree pedigree;

    private final List<Gene> genes;
    private final List<VariantEvaluation> variantEvaluations;

    public AnalysisResults(Builder builder) {
        this.vcfPath = builder.vcfPath;
        this.pedPath = builder.pedPath;
        this.probandSampleName = builder.probandSampleName;

        this.vcfHeader = builder.vcfHeader;
        this.sampleNames = builder.sampleNames;
        this.pedigree = builder.pedigree;

        this.genes = builder.genes;
        this.variantEvaluations = builder.variantEvaluations;
    }
    
    public Path getVcfPath() {
        return vcfPath;
    }

    public Path getPedPath() {
        return pedPath;
    }

    public String getProbandSampleName() {
        return probandSampleName;
    }

    public VCFHeader getVcfHeader() {
        return vcfHeader;
    }

    /**
     * @return List of Strings representing the sample names in the VCF file.
     */
    public List<String> getSampleNames() {
        return sampleNames;
    }

    public Pedigree getPedigree() {
        return pedigree;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public List<VariantEvaluation> getVariantEvaluations() {
        return variantEvaluations;
    }

    public List<VariantEvaluation> getUnAnnotatedVariantEvaluations() {
        return variantEvaluations.stream().filter(varEval -> !varEval.hasAnnotations()).collect(toList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vcfPath, pedPath, probandSampleName, sampleNames, variantEvaluations, genes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResults that = (AnalysisResults) o;
        return Objects.equals(vcfPath, that.vcfPath) &&
                Objects.equals(pedPath, that.pedPath) &&
                Objects.equals(probandSampleName, that.probandSampleName) &&
                Objects.equals(sampleNames, that.sampleNames) &&
                Objects.equals(variantEvaluations, that.variantEvaluations) &&
                Objects.equals(genes, that.genes);
    }

    @Override
    public String toString() {
        return "AnalysisResults{" + "vcfPath=" + vcfPath + ", pedPath=" + pedPath + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Path vcfPath;
        private Path pedPath = null;
        private String probandSampleName = "";

        private VCFHeader vcfHeader = new VCFHeader();
        private List<String> sampleNames = Collections.emptyList();
        private Pedigree pedigree = Pedigree.constructSingleSamplePedigree("unknown sample");

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
        public Builder vcfPath(Path vcfPath) {
            this.vcfPath = vcfPath;
            return this;
        }

        public Builder pedPath(Path pedPath) {
            this.pedPath = pedPath;
            return this;
        }

        public Builder vcfHeader(VCFHeader vcfHeader) {
            this.vcfHeader = vcfHeader;
            return this;
        }

        public Builder variantEvaluations(List<VariantEvaluation> variantList) {
            this.variantEvaluations = variantList;
            return this;
        }

        public Builder pedigree(Pedigree pedigree) {
            this.pedigree = pedigree;
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
