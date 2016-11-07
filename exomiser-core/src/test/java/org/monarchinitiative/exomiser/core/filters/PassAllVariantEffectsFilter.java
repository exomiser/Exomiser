/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.EnumSet;

/**
 * Mock variant effect filter which will always return a PASS FilterResult with a score of 1.0.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassAllVariantEffectsFilter extends VariantEffectFilter {

    public PassAllVariantEffectsFilter() {
        super(EnumSet.noneOf(VariantEffect.class));
    }
    
    @Override
    public FilterType getFilterType() {
        return FilterType.VARIANT_EFFECT_FILTER;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        return new PassFilterResult(FilterType.VARIANT_EFFECT_FILTER);
    }
    
}
