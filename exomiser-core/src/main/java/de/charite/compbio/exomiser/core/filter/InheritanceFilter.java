/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.ModeOfInheritance;
import java.util.List;
import java.util.Objects;

/**
 * A Gene filter for filtering against a particular inheritance mode.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceFilter implements GeneFilter {

    private final FilterType filterType = FilterType.INHERITANCE_FILTER;
    
    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterScore passedScore = new InheritanceFilterScore(1f);
    private final FilterScore failedScore = new InheritanceFilterScore(0f);

    private final ModeOfInheritance modeOfInheritance;
    
    public InheritanceFilter(ModeOfInheritance modeOfInheritance) {
        this.modeOfInheritance = modeOfInheritance;
    }
        
    @Override
    public void filter(List<Gene> genes) {
        for (Gene gene : genes) {
            filter(gene);
        }
    }

    @Override
    public boolean filter(Gene gene) {
        if (modeOfInheritance == ModeOfInheritance.UNINITIALIZED) {
            //if ModeOfInheritance.UNINITIALIZED pass the filter - ideally it shouldn't be applied in the first place.
            return true;
        }
        
        //set the filter scores for the variant evaluations
        if (gene.isConsistentWith(modeOfInheritance)) {
            //yay we're compatible!
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                variantEvaluation.addPassedFilter(FilterType.INHERITANCE_FILTER, passedScore);
            }
            return true;
        } else {
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                variantEvaluation.addFailedFilter(FilterType.INHERITANCE_FILTER, failedScore);
            }
            return false;
        }
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.modeOfInheritance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InheritanceFilter other = (InheritanceFilter) obj;
        return this.modeOfInheritance == other.modeOfInheritance;
    }

    @Override
    public String toString() {
        return filterType + " filter: ModeOfInheritance=" + modeOfInheritance;
    }
    
}
