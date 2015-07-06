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
public class PriorityScoreFilter implements GeneFilter {

    private static final FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;

    private final float minPriorityScore;
    
    private final FilterResult passResult = new PassFilterResult(filterType, 1.0f);
    private final FilterResult failResult = new FailFilterResult(filterType, 0.0f);

    public PriorityScoreFilter(float minPriorityScore) {
        this.minPriorityScore = minPriorityScore;
    }

    public float getMinPriorityScore() {
        return minPriorityScore;
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
        final PriorityScoreFilter other = (PriorityScoreFilter) obj;
        if (Float.floatToIntBits(this.minPriorityScore) != Float.floatToIntBits(other.minPriorityScore)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PriorityScoreFilter{" + "minPriorityScore=" + minPriorityScore + '}';
    }

}

