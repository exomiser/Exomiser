/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies the given {@code Filter} to the {@code VariantEvaluation}. This can
 * be done in a 'non-destructive' manner such that every
 * {@code VariantEvaluation} is passed through each and every filter, or in a
 * 'destructive' manner where only {@code VariantEvaluation} which pass through
 * all the desired filters are returned at the end.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFilterer {

    private static final Logger logger = LoggerFactory.getLogger(VariantFilterer.class);
    
    public static List<VariantEvaluation> useDestructiveFiltering(List<Filter> filters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using DESTRUCTIVE filtering", variantEvaluations.size());
        List<VariantEvaluation> filteredList = new ArrayList<>();
        
        if (filters.isEmpty()) {
            logger.info("Unable to filter variants against empty Filter list - returning all variants");
            return variantEvaluations;
        }
        
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            for (Filter filter : filters) {
                if (! filter.filterVariant(variantEvaluation)) {
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

    public static List<VariantEvaluation> useNonDestructiveFiltering(List<Filter> filters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using NON-DESTRUCTIVE filtering", variantEvaluations.size());
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            for (Filter filter : filters) {
                filter.filterVariant(variantEvaluation);
            }
        }
        return variantEvaluations;
    }
    
}
