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

package org.monarchinitiative.exomiser.core.analysis;

import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.SimpleGeneFilterRunner;
import org.monarchinitiative.exomiser.core.filters.SparseVariantFilterRunner;
import org.monarchinitiative.exomiser.core.filters.VariantFilter;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class PassOnlyAnalysisRunner extends AbstractAnalysisRunner {

    PassOnlyAnalysisRunner(GenomeAnalysisService genomeAnalysisService) {
        super(genomeAnalysisService, new SparseVariantFilterRunner(), new SimpleGeneFilterRunner());
    }

    @Override
    protected Predicate<VariantEvaluation> isAssociatedWithKnownGene(Map<String, Gene> genes) {
        return variantEvaluation -> {
            //Only load the variant if the gene has passed the other filters
            //this should drastically reduce the number of collected variants
            Gene gene = genes.get(variantEvaluation.getGeneSymbol());
            return gene != null && gene.passedFilters();
        };
    }

    @Override
    protected Predicate<VariantEvaluation> runVariantFilters(List<VariantFilter> variantFilters, FilterStats filterStats) {
        return variantEvaluation -> {
            //loop through the filters and only run if the variantEvaluation has passed all prior filters
            for (VariantFilter filter : variantFilters) {
                if (variantEvaluation.passedFilters()) {
                    FilterResult result = variantFilterRunner.run(filter, variantEvaluation);
                    filterStats.addResult(result);
                }
            }
            return variantEvaluation.passedFilters();
        };
    }

    @Override
    protected Stream<Gene> getGenesWithVariants(Map<String, Gene> allGenes) {
        return allGenes.values()
                .stream()
                .filter(Gene::hasVariants)
                .filter(Gene::passedFilters)
                .map(removeFailedVariants());
    }

    private Function<Gene, Gene> removeFailedVariants() {
        return gene -> {
            gene.getVariantEvaluations().removeIf(variantFailedFilters());
            return gene;
        };
    }

    private Predicate<VariantEvaluation> variantFailedFilters() {
        return variantEvaluation -> !variantEvaluation.passedFilters();
    }
    
    @Override
    protected List<VariantEvaluation> getFinalVariantList(List<VariantEvaluation> variants) {
        return variants.stream()
                .filter(VariantEvaluation::passedFilters)
                .collect(toList());
    }
}
