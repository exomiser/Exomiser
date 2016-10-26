/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantFilterRunner extends FilterRunner<VariantFilter, VariantEvaluation>{

    @Override
    List<VariantEvaluation> run(VariantFilter variantFilter, List<VariantEvaluation> variantEvaluations);

    @Override
    List<VariantEvaluation> run(List<VariantFilter> variantFilters, List<VariantEvaluation> variantEvaluations);

    FilterResult run(Filter filter, VariantEvaluation variantEvaluation);
    
}
