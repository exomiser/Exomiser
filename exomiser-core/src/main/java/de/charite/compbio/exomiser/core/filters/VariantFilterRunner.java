/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantFilterRunner extends FilterRunner<VariantFilter, VariantEvaluation>{

    @Override
    public List<VariantEvaluation> run(VariantFilter variantFilter, List<VariantEvaluation> variantEvaluations);

    @Override
    public List<VariantEvaluation> run(List<VariantFilter> variantFilters, List<VariantEvaluation> variantEvaluations);

    public FilterResult run(Filter filter, VariantEvaluation variantEvaluation);
    
}
