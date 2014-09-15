/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.List;

/**
 * 
 * @author jj8
 */
public interface VariantFilterer {
    
    public List<VariantEvaluation> filterVariants(List<Filter> filters, List<VariantEvaluation> variantEvaluations);
}
