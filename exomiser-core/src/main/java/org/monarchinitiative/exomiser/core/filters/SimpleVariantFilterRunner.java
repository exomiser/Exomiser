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

import java.util.List;

/**
 * Applies the given {@code VariantFilter} to the {@code VariantEvaluation}.
 * This can be done in a 'non-destructive' manner such that every
 * {@code VariantEvaluation} is passed through each and every run, or in a
 * 'destructive' manner where only {@code VariantEvaluation} which pass through
 * all the desired filters are returned at the end.
 *
 * This simple implementation of the {@code VariantFilter} assumes that all the
 * necessary data has been applied to the {@code VariantEvaluation} being
 * filtered beforehand. If it hasn't then the results will be wrong
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantFilterRunner implements VariantFilterRunner {

    private final FilterResultsCounter filterResultsCounter;

    public SimpleVariantFilterRunner() {
        filterResultsCounter = new FilterResultsCounter();
    }

    @Override
    public List<VariantEvaluation> run(VariantFilter filter, List<VariantEvaluation> variantEvaluations) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            run(filter, variantEvaluation);
        }
        return variantEvaluations;
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
