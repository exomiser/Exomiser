/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.SimpleGeneFilterRunner;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class PassOnlyAnalysisRunner extends AbstractAnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(PassOnlyAnalysisRunner.class);
    
    PassOnlyAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService) {
        super(sampleDataFactory, variantDataService, new SparseVariantFilterRunner(), new SimpleGeneFilterRunner());
    }

    @Override
    protected Predicate<VariantEvaluation> isInKnownGene(Map<String, Gene> genes) {
        return variantEvaluation -> {
            //Only load the variant if the gene has passed the other filters
            //this should drastically reduce the number of collected variants
            if(genes.containsKey(variantEvaluation.getGeneSymbol())) {
                Gene gene = genes.get(variantEvaluation.getGeneSymbol());
                return gene.passedFilters();
            }
            return false;
        };
    }

    @Override
    protected Predicate<VariantEvaluation> runVariantFilters(List<VariantFilter> variantFilters) {
        return variantEvaluation -> {
            //loop through the filters and only run if the variantEvaluation has passed all prior filters
            variantFilters.stream()
                    .filter(filter -> variantEvaluation.passedFilters())
                    .forEach(filter -> variantFilterRunner.run(filter, variantEvaluation));

            return variantEvaluation.passedFilters();
        };
    }

    @Override
    protected List<Gene> getFinalGeneList(Map<String, Gene> allGenes) {
        return allGenes.values()
                .stream()
                .filter(gene -> !gene.getVariantEvaluations().isEmpty())
                .filter(Gene::passedFilters)
                .map(gene -> {
                    removeFailedVariants(gene);
                    return gene;
                })
                .collect(toList());
    }

    private void removeFailedVariants(Gene gene) {
        Iterator<VariantEvaluation> variantIterator = gene.getVariantEvaluations().iterator();
        while (variantIterator.hasNext()) {
            VariantEvaluation variant = variantIterator.next();
            if (failedFilters(variant)) {
                variantIterator.remove();
            }
        }
    }

    private static boolean failedFilters(VariantEvaluation variant) {
        return !variant.passedFilters();
    }
    
    @Override
    protected List<VariantEvaluation> getFinalVariantList(List<VariantEvaluation> variants) {
        return variants
                .stream()
                .filter(VariantEvaluation::passedFilters)
                .collect(toList());
    }
}
