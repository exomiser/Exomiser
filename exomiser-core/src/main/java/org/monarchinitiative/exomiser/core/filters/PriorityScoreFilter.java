/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import com.fasterxml.jackson.annotation.JsonRootName;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.Objects;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonRootName("priorityScoreFilter")
public class PriorityScoreFilter implements GeneFilter {

    private static final FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    private final float minPriorityScore;

    private final PriorityType priorityType;

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
            return FAIL;
        }       
        if (priorityResult.getScore() >= minPriorityScore) {
            return addFilterResultToVariants(PASS, gene);
        }
        return addFilterResultToVariants(FAIL, gene);
    }

    private FilterResult addFilterResultToVariants(FilterResult filterResult, Gene gene) {
        for (VariantEvaluation variant : gene.getVariantEvaluations()) {
            variant.addFilterResult(filterResult);
        }
        return filterResult;
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
