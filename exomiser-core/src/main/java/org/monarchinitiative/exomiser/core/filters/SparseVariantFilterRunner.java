/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an order-dependent filter which will only run a filter if an input variant has passed all previous filters.
 * The overall outcome (pass/fail) for a variant being run through a set of filters will be the same as for the
 * {@link SimpleVariantFilterRunner}, only this {@link VariantFilterRunner} implementation will not exhaustively run all
 * filters.
 *
 * For example consider three variants 0, 1 and 2 being run through three filters A, B, and C. The result is the same for
 * each filtered variant, but unless the full results are required using this implementation could be computationally
 * much less expensive, if the filters are optimally ordered. Assuming the computational cost of running a filter is
 * A = 1, B = 2, C = 3 then the cost of running the filters through this filter runner will be:
 *
 * FULL(ABC):      SPARSE(ABC):    SPARSE(BAC):    SPARSE(ACB):    SPARSE(BCA):    SPARSE(CBA):    SPARSE(CAB):
 *   A B C Result    A B C Result    B A C Result    A C B Result    B C A Result    C B A Result    C A B Result
 * 0 X + X FAIL    0 X     FAIL    0 + X   FAIL    0 X     FAIL    0 + X   FAIL    0 X     FAIL    0 X     FAIL
 * 1 + + + PASS    1 + + + PASS    1 + + + PASS    1 + + + PASS    1 + + + PASS    1 + + + PASS    1 + + + PASS
 * 2 + X + FAIL    2 + X   FAIL    2 X     FAIL    2 + + X FAIL    2 X     FAIL    2 + X   FAIL    2 + + X FAIL
 * Cost: 18        Cost: 10        Cost: 11        Cost: 13        Cost: 13        Cost: 14        Cost: 15
 *
 * key: X = failed filter, + = passed filter, blank = filter not run.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SparseVariantFilterRunner implements VariantFilterRunner {

    private final FilterResultsCounter filterResultsCounter;

    public SparseVariantFilterRunner() {
        this.filterResultsCounter = new FilterResultsCounter();
    }

    @Override
    public List<VariantEvaluation> run(VariantFilter filter, List<VariantEvaluation> variantEvaluations) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.passedFilters()) {
                run(filter, variantEvaluation);
            }
        }
        return passedFilteredVariants(variantEvaluations);
    }

    private List<VariantEvaluation> passedFilteredVariants(List<VariantEvaluation> variantEvaluations) {
        List<VariantEvaluation> passedVariantEvaluations = new ArrayList<>();
        for (VariantEvaluation variant : variantEvaluations) {
            if (variant.passedFilters()) {
                passedVariantEvaluations.add(variant);
            }
        }
        return passedVariantEvaluations;
    }

    @Override
    public FilterResult logFilterResult(FilterResult filterResult) {
        filterResultsCounter.logResult(filterResult);
        return filterResult;
    }

    @Override
    public List<FilterResultCount> filterCounts() {
        return filterResultsCounter.filterResultCounts();
    }
}
