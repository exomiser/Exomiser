/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PriorityScoreFilter implements GeneFilter {

    private static final FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;

    private final float minPriorityScore;

    private final PriorityType priorityType;

    private final FilterResult passesFilter = new PassFilterResult(filterType);
    private final FilterResult failsFilter = new FailFilterResult(filterType);

    public PriorityScoreFilter(PriorityType priorityType, float minPriorityScore) {
        this.minPriorityScore = minPriorityScore;
        this.priorityType = priorityType;
    }

    public PriorityType getPriorityType() {
        return priorityType;
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
        PriorityResult priorityResult = gene.getPriorityResult(priorityType);
        if (priorityResult == null) {
            return failsFilter;
        }
        if (priorityResult.getScore() >= minPriorityScore) {
            return passesFilter;
        }
        return failsFilter;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Float.floatToIntBits(this.minPriorityScore);
        hash = 17 * hash + Objects.hashCode(this.priorityType);
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
        return this.priorityType == other.priorityType;
    }

    @Override
    public String toString() {
        return "PriorityScoreFilter{" + "priorityType=" + priorityType + ", minPriorityScore=" + minPriorityScore + '}';
    }

}

