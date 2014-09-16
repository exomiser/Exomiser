/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.factories.VariantEvaluationDataFactory;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jj8
 */
@Component
public class SparseVariantFilterRunner implements FilterRunner<VariantEvaluation, VariantFilter> {

    private static final Logger logger = LoggerFactory.getLogger(SparseVariantFilterRunner.class);

    @Autowired
    private VariantEvaluationDataFactory variantEvaluationFactory;

    /**
     *
     * @param filters
     * @param variantEvaluations
     * @return
     */
    @Override
    public List<VariantEvaluation> run(List<VariantFilter> filters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using sparse filtering", variantEvaluations.size());
        List<VariantEvaluation> filteredList = new ArrayList<>();

        if (filters.isEmpty()) {
            logger.info("Unable to filter variants against empty Filter list - returning all variants");
            return variantEvaluations;
        }

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            for (Filter filter : filters) {
                if (filter.getFilterType() == FilterType.FREQUENCY_FILTER) {
                    variantEvaluationFactory.addFrequencyData(variantEvaluation);
                }
                if (filter.getFilterType() == FilterType.PATHOGENICITY_FILTER) {
                    variantEvaluationFactory.addPathogenicityData(variantEvaluation);
                }
                if (!filter.filter(variantEvaluation)) {
                    break;
                }
            }
            if (variantEvaluation.passesFilters()) {
                filteredList.add(variantEvaluation);
            }
        }
        int removed = variantEvaluations.size() - filteredList.size();
        logger.info("Filtering removed {} variants. Returning {} filtered variants from initial list of {}", removed, filteredList.size(), variantEvaluations.size());
        return filteredList;
    }

}
