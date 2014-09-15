/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
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
 * This simple implementation of the {@code VariantFilter} assumes that all the
 * necessary data has been applied to the {@code VariantEvaluation} being
 * filtered beforehand. If it hasn't then the results will be wrong 
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantFilterer implements VariantFilterer {

    private static final Logger logger = LoggerFactory.getLogger(SimpleVariantFilterer.class);

    @Override
    public List<VariantEvaluation> filterVariants(List<Filter> filters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using NON-DESTRUCTIVE simple filtering", variantEvaluations.size());
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            for (Filter filter : filters) {
                filter.filterVariant(variantEvaluation);
            }
        }
        return variantEvaluations;
    }

}
