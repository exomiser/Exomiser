/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies the given {@code VariantFilter} to the {@code VariantEvaluation}.
 * This can be done in a 'non-destructive' manner such that every
 * {@code VariantEvaluation} is passed through each and every run, or in a
 * 'destructive' manner where only {@code VariantEvaluation} which pass through
 * all the desired filters are returned at the end.
 *
 * This simple implementation of the {@code VariantFilter} assumes that all the
 * necessary data has been applied to the {@code VariantEvaluation} being
 * filtered beforehand. If it hasn't then the results will be wrong
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantFilterRunner implements FilterRunner<VariantEvaluation, VariantFilter> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleVariantFilterRunner.class);

    public static class VariantFilterRunner {

        private List<VariantFilter> variantFilters = new ArrayList<>();
        private List<VariantEvaluation> variantEvaluations = new ArrayList<>();

        public VariantFilterRunner run(VariantFilter frequencyFilter) {
            variantFilters.add(frequencyFilter);
            return this;
        }

        public VariantFilterRunner run(List<VariantFilter> variantFilters) {
            this.variantFilters = variantFilters;
            return this;
        }

        public VariantFilterRunner over(List<VariantEvaluation> variantEvaluations) {
            this.variantEvaluations = variantEvaluations;
            return this;
        }

        public List<VariantEvaluation> usingSimpleFiltering() {
            return new SimpleVariantFilterRunner().run(variantFilters, variantEvaluations);
        }
    }

    @Override
    public List<VariantEvaluation> run(List<VariantFilter> variantFilters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using non-destructive simple filtering...", variantEvaluations.size());
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            runAllFiltersOverVariantEvaluation(variantFilters, variantEvaluation);
        }
        logger.info("Ran {} filters over {} variants using non-destructive simple filtering.", getFilterTypes(variantFilters), variantEvaluations.size());
        return variantEvaluations;
    }
    
    private void runAllFiltersOverVariantEvaluation(List<VariantFilter> variantFilters, VariantEvaluation variantEvaluation) {
        for (VariantFilter filter : variantFilters) {
            FilterResult filterResult = filter.runFilter(variantEvaluation);
            variantEvaluation.addFilterResult(filterResult);
        }
    }

    private Set<FilterType> getFilterTypes(List<VariantFilter> filters) {
        Set<FilterType> filtersRun = new LinkedHashSet<>();
        for (Filter filter : filters) {
            filtersRun.add(filter.getFilterType());
        }
        return filtersRun;
    }
    
}
