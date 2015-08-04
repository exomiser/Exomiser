/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
abstract class AbstractFilterResult implements FilterResult {
    
    private final FilterType filterType;
    private final FilterResultStatus filterResultStatus;

    AbstractFilterResult(FilterType filterType, FilterResultStatus filterResultStatus) {
        this.filterType = filterType;
        this.filterResultStatus = filterResultStatus;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResultStatus getResultStatus() {
        return filterResultStatus;
    }

    @Override
    public boolean passedFilter() {
        return filterResultStatus == FilterResultStatus.PASS;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.filterType);
        hash = 59 * hash + Objects.hashCode(this.filterResultStatus);
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
        final AbstractFilterResult other = (AbstractFilterResult) obj;
        if (this.filterType != other.filterType) {
            return false;
        }
        return this.filterResultStatus == other.filterResultStatus;
    }
        
    @Override
    public String toString() {
        return String.format(Locale.UK, "Filter=%s status=%s",filterType, filterResultStatus);
    }
    
}
