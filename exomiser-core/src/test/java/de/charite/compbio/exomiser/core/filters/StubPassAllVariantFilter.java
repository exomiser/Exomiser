/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.Objects;

/**
 * Stub class passes all VariantEvaluations through a Filter of the declared 
 * FilterType.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class StubPassAllVariantFilter implements VariantFilter {
    
    private final FilterType mockFilterType;
    
    public StubPassAllVariantFilter(FilterType mockFilterType) {
        this.mockFilterType = mockFilterType;
    }
     
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        return new PassFilterResult(mockFilterType);
    }

    @Override
    public FilterType getFilterType() {
        return mockFilterType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.mockFilterType);
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
        final StubPassAllVariantFilter other = (StubPassAllVariantFilter) obj;
        if (this.mockFilterType != other.mockFilterType) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StubVariantFilter{" + "mockFilterType=" + mockFilterType + '}';
    }
    
}
