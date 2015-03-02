/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import java.util.List;

import de.charite.compbio.jannovar.annotation.VariantEffect;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEffectCount {
    private final VariantEffect variantEffect;
    private final List<Integer> sampleVariantEffectCounts;

    public VariantEffectCount(VariantEffect variantType, List<Integer> sampleVariantTypeCounts) {
        this.variantEffect = variantType;
        this.sampleVariantEffectCounts = sampleVariantTypeCounts;
    }
    
    public VariantEffect getVariantType() {
        return variantEffect;
    }

    public List<Integer> getSampleVariantTypeCounts() {
        return sampleVariantEffectCounts;
    }

    @Override
    public String toString() {
        return variantEffect + "=" + sampleVariantEffectCounts;
    }
}
