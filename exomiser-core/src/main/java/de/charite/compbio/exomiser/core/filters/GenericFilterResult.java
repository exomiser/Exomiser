/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GenericFilterResult implements FilterResult {
    
    private final FilterType filterType;
    private final float score;
    private final FilterResultStatus filterResultStatus;

    protected GenericFilterResult(FilterType filterType, float score, FilterResultStatus filterResultStatus) {
        this.filterType = filterType;
        this.score = score;
        this.filterResultStatus = filterResultStatus;
    }

    @Override
    public float getScore() {
        return score;
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
        hash = 59 * hash + Float.floatToIntBits(this.score);
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
        final GenericFilterResult other = (GenericFilterResult) obj;
        if (this.filterType != other.filterType) {
            return false;
        }
        if (Float.floatToIntBits(this.score) != Float.floatToIntBits(other.score)) {
            return false;
        }
        return this.filterResultStatus == other.filterResultStatus;
    }
        
    @Override
    public String toString() {
        return String.format("Filter=%s score=%.3f status=%s",filterType, score, filterResultStatus);
    }
    
}
