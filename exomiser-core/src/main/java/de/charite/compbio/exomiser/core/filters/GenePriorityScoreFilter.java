/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GenePriorityScoreFilter implements GeneFilter {

    private static final FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;

    private final float minPriorityScore;
    
    private final FilterResult passResult = new GenericFilterResult(filterType, 1.0f, FilterResultStatus.PASS);
    private final FilterResult failResult = new GenericFilterResult(filterType, 0.0f, FilterResultStatus.FAIL);

    public GenePriorityScoreFilter(float minPriorityScore) {
        this.minPriorityScore = minPriorityScore;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Fails all Genes with a priority score below the set threshold. Note that
     * un-prioritised genes will have a score of 0 and will therefore fail this
     * filter by default.
     *
     * @param gene
     * @return
     */
    @Override
    public FilterResult runFilter(Gene gene) {
        if (gene.getPriorityScore() >= minPriorityScore) {
            return passResult;
        }
        return failResult;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Float.floatToIntBits(this.minPriorityScore);
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
        final GenePriorityScoreFilter other = (GenePriorityScoreFilter) obj;
        if (Float.floatToIntBits(this.minPriorityScore) != Float.floatToIntBits(other.minPriorityScore)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return filterType + " filter: minPriorityScore=" + minPriorityScore;
    }

}

