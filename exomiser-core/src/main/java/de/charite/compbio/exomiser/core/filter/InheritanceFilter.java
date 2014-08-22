/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.ModeOfInheritance;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jj8
 */
public class InheritanceFilter implements Filter {

    private static final FilterType filterType = FilterType.INHERITANCE_FILTER;
    
    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterScore passedScore = new InheritanceFilterScore(1f);        
    private final FilterScore failedScore = new InheritanceFilterScore(0f);
    
    private final ModeOfInheritance modeOfInheritance;
    
    
    public InheritanceFilter(ModeOfInheritance modeOfInheritance) {
        this.modeOfInheritance = modeOfInheritance;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }
    
    @Override
    public void filterVariants(List<VariantEvaluation> variantEvaluations) {
        for (VariantEvaluation ve : variantEvaluations) {
            filterVariant(ve);
        }
    }

    @Override
    public boolean filterVariant(VariantEvaluation variantEvaluation) {

        //we're not interested in this filter if this is the case so everything passes
        if (modeOfInheritance == ModeOfInheritance.UNINITIALIZED) {
            return true;
        }
        //OK so we are interested in the inheritance mode after all. 
        //double-check that the variant has been constructed properly
        Set<ModeOfInheritance> variantInheritanceModes = variantEvaluation.getInheritanceModes();
        if (variantInheritanceModes == null || variantInheritanceModes.isEmpty()) {
            return true;
        }
        //right lets check the modes of inheritance are compatible!
//        boolean compatibleWithModeOfInheritance = true;

        if (variantInheritanceModes.contains(modeOfInheritance)){
            variantEvaluation.addPassedFilter(filterType, passedScore);
            return true;
        }
        //nope, it really, really isn't compatible.
        variantEvaluation.addFailedFilter(filterType, failedScore);
        return false;                
    }   

    @Override
    public String toString() {
        return "InheritanceFilter{" + "modeOfInheritance=" + modeOfInheritance + '}';
    }
    
}
