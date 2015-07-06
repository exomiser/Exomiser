/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.Filterable;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
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
public class SimpleVariantFilterRunner implements VariantFilterRunner {

    private static final Logger logger = LoggerFactory.getLogger(SimpleVariantFilterRunner.class);

    private final VariantDataService variantDataService;

    public SimpleVariantFilterRunner(VariantDataService variantDataService) {
        this.variantDataService = variantDataService;
    }
   
    @Override
    public List<VariantEvaluation> run(List<VariantFilter> variantFilters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using non-destructive simple filtering...", variantEvaluations.size());
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            run(variantFilters, variantEvaluation);
        }
        logger.info("Ran {} filters over {} variants using non-destructive simple filtering.", getFilterTypes(variantFilters), variantEvaluations.size());
        return variantEvaluations;
    }

    @Override
    public List<VariantEvaluation> run(VariantFilter filter, List<VariantEvaluation> filterables) {
        for (VariantEvaluation variantEvaluation : filterables) {
            addMissingDataAndRunFilter(filter, variantEvaluation);
        }
        return filterables;
    }

    private void run(List<VariantFilter> variantFilters, VariantEvaluation variantEvaluation) {
        for (VariantFilter filter : variantFilters) {
            addMissingDataAndRunFilter(filter, variantEvaluation);
        }
    }

    protected void addMissingDataAndRunFilter(Filter filter, VariantEvaluation variantEvaluation) {
        addMissingFrequencyAndPathogenicityData(filter.getFilterType(), variantEvaluation);
        FilterResult result = runFilterAndAddResult(filter, variantEvaluation);
    }

    protected void addMissingFrequencyAndPathogenicityData(FilterType filterType, VariantEvaluation variantEvaluation) {
        switch (filterType) {
            case FREQUENCY_FILTER:
            case KNOWN_VARIANT_FILTER:
                variantDataService.setVariantFrequencyData(variantEvaluation);
                break;
            case PATHOGENICITY_FILTER:
                variantDataService.setVariantPathogenicityData(variantEvaluation);
                break;
            case CADD_FILTER:
                variantDataService.setVariantCADDData(variantEvaluation);
                break;    
            case REGULATORY_FEATURE_FILTER:
                variantDataService.setVariantRegulatoryFeatureData(variantEvaluation);
                break;
        }
    }

    protected FilterResult runFilterAndAddResult(Filter filter, Filterable filterable) {
        FilterResult filterResult = filter.runFilter(filterable);
        filterable.addFilterResult(filterResult);
        return filterResult;
    }

    protected Set<FilterType> getFilterTypes(List<VariantFilter> filters) {
        Set<FilterType> filtersRun = new LinkedHashSet<>();
        for (Filter filter : filters) {
            filtersRun.add(filter.getFilterType());
        }
        return filtersRun;
    }

}
