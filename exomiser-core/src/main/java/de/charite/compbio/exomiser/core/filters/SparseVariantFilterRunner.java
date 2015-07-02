/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class SparseVariantFilterRunner implements FilterRunner<VariantEvaluation, VariantFilter> {

    private static final Logger logger = LoggerFactory.getLogger(SparseVariantFilterRunner.class);

    @Autowired
    private VariantDataService variantDataService;

    /**
     *
     * @param filters
     * @param variantEvaluations
     * @return
     */
    @Override
    public List<VariantEvaluation> run(List<VariantFilter> filters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using sparse filtering...", variantEvaluations.size());

        if (ifThereAreNoFiltersToRun(filters)) {
            return variantEvaluations;
        }

        List<VariantEvaluation> filteredVariantEvaluations = new ArrayList<>();
        int count = 0;
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            count++;
            if (count % 10000 == 0){
                logger.info("{} of {} variants have passed filtering so far...", filteredVariantEvaluations.size(), count);
            }
            runFiltersOverVariantEvaluationUntilFailure(filters, variantEvaluation);
            if (variantEvaluation.passedFilters()) {
                filteredVariantEvaluations.add(variantEvaluation);
            }
        }

        int numRemoved = variantEvaluations.size() - filteredVariantEvaluations.size();
        logger.info("Filtering for {} removed {} of {} variants - returning {} filtered variants.", getFilterTypes(filters), numRemoved, variantEvaluations.size(), filteredVariantEvaluations.size());
        return filteredVariantEvaluations;
    }

    private boolean ifThereAreNoFiltersToRun(List<VariantFilter> filters) {
        if (filters.isEmpty()) {
            logger.info("Unable to filter variants against empty Filter list - returning all variants");
            return true;
        }
        return false;
    }

    private void runFiltersOverVariantEvaluationUntilFailure(List<VariantFilter> filters, VariantEvaluation variantEvaluation) {
        for (Filter filter : filters) {
            fetchMissingFrequencyAndPathogenicityData(filter.getFilterType(), variantEvaluation);
            FilterResult filterResult = runFilterAndAddResult(filter, variantEvaluation);
            //we want to know which filter the variant failed and then don't run any more
            //this can be an expensive operation when looking up frequency and pathogenicity info from the database
            if (variantFailedTheFilter(filterResult)) {
                break;
            }
        }
    }

    private void fetchMissingFrequencyAndPathogenicityData(FilterType filterType, VariantEvaluation variantEvaluation) {
        switch (filterType) {
            case FREQUENCY_FILTER:
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

    private FilterResult runFilterAndAddResult(Filter filter, VariantEvaluation variantEvaluation) {
        FilterResult filterResult = filter.runFilter(variantEvaluation);
        variantEvaluation.addFilterResult(filterResult);
        return filterResult;
    }

    private static boolean variantFailedTheFilter(FilterResult filterResult) {
        return filterResult.getResultStatus() == FilterResultStatus.FAIL;
    }

    private Set<FilterType> getFilterTypes(List<VariantFilter> filters) {
        Set<FilterType> filtersRun = new LinkedHashSet<>();
        for (Filter filter : filters) {
            filtersRun.add(filter.getFilterType());
        }
        return filtersRun;
    }
}
