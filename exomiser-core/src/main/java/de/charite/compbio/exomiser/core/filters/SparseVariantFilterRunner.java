/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SparseVariantFilterRunner extends SimpleVariantFilterRunner {

    private static final Logger logger = LoggerFactory.getLogger(SparseVariantFilterRunner.class);

    public SparseVariantFilterRunner(VariantDataService variantDataService) {
        super(variantDataService);
    }

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

        //TODO: actually performance test this using an entire genome - it's not appreciably slower 
        //(perhaps about 0.5 secs) using runFilters() for a few tens of thousand variants. 
        //old way- this should be the most performant way as this loops through the longest list once running the shortest list for each of the elements in the longest list.
//        List<VariantEvaluation> filteredVariantEvaluations = runFiltersPerVariant(filters, variantEvaluations);

        //new way - this should be the least performant as it is running the longest list through each of the shortest lists.
        List<VariantEvaluation> filteredVariantEvaluations = runFilters(filters, variantEvaluations);

        int numRemoved = variantEvaluations.size() - filteredVariantEvaluations.size();
        logger.info("Filtering for {} removed {} of {} variants - returning {} filtered variants.", getFilterTypes(filters), numRemoved, variantEvaluations.size(), filteredVariantEvaluations.size());
        return filteredVariantEvaluations;
    }

    @Override
    public List<VariantEvaluation> run(VariantFilter filter, List<VariantEvaluation> variantEvaluations) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.passedFilters()) {
                addMissingDataAndRunFilter(filter, variantEvaluation);
            }
        }
        List<VariantEvaluation> filteredVariantEvaluations = makeListofFilteredVariants(variantEvaluations);

        return filteredVariantEvaluations;
    }
    
    private boolean ifThereAreNoFiltersToRun(List<VariantFilter> filters) {
        if (filters.isEmpty()) {
            logger.info("Unable to filter variants against empty Filter list - returning all variants");
            return true;
        }
        return false;
    }

    private List<VariantEvaluation> runFiltersPerVariant(List<VariantFilter> filters, List<VariantEvaluation> variantEvaluations) {
        List<VariantEvaluation> filteredVariantEvaluations = new ArrayList<>();

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            runFiltersOverVariantUntilFailure(filters, variantEvaluation);
            addFilteredVariantToFilteredList(variantEvaluation, filteredVariantEvaluations);
        }

        return filteredVariantEvaluations;
    }

    private List<VariantEvaluation> runFilters(List<VariantFilter> filters, List<VariantEvaluation> variantEvaluations) {

        for (Filter filter : filters) {
            for (VariantEvaluation variantEvaluation : variantEvaluations) {
                //the only difference between sparse and full filtering is this if clause here...
                if (variantEvaluation.passedFilters()) {
                    addMissingDataAndRunFilter(filter, variantEvaluation);
                }
            }
        }

        return makeListofFilteredVariants(variantEvaluations);
    }

    private List<VariantEvaluation> makeListofFilteredVariants(List<VariantEvaluation> variantEvaluations) {
        List<VariantEvaluation> filteredVariantEvaluations = new ArrayList<>();
        for (VariantEvaluation variant : variantEvaluations) {
            addFilteredVariantToFilteredList(variant, filteredVariantEvaluations);
        }
        return filteredVariantEvaluations;
    }

    private void runFiltersOverVariantUntilFailure(List<VariantFilter> filters, VariantEvaluation variantEvaluation) {
        for (Filter filter : filters) {
            addMissingDataAndRunFilter(filter, variantEvaluation);
            //we want to know which filter the variant failed and then don't run any more
            //this can be an expensive operation when looking up frequency and pathogenicity info from the database
            if (variantFailedTheFilter(variantEvaluation)) {
                break;
            }
        }
    }

    private void addFilteredVariantToFilteredList(VariantEvaluation variant, List<VariantEvaluation> filteredVariantEvaluations) {
        if (variant.passedFilters()) {
            filteredVariantEvaluations.add(variant);
        }
    }

    private boolean variantFailedTheFilter(VariantEvaluation variantEvaluation) {
        return !variantEvaluation.passedFilters();
    }

}
