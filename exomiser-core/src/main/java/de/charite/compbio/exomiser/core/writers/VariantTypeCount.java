/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import jannovar.common.VariantType;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantTypeCount {
    private final VariantType variantType;
    private final List<Integer> sampleVariantTypeCounts;

    public VariantTypeCount(VariantType variantType, List<Integer> sampleVariantTypeCounts) {
        this.variantType = variantType;
        this.sampleVariantTypeCounts = sampleVariantTypeCounts;
    }
    
    public VariantType getVariantType() {
        return variantType;
    }

    public List<Integer> getSampleVariantTypeCounts() {
        return sampleVariantTypeCounts;
    }

    @Override
    public String toString() {
        return variantType + "=" + sampleVariantTypeCounts;
    }
}
