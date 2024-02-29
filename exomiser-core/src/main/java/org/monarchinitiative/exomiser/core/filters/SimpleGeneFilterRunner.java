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

import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleGeneFilterRunner implements GeneFilterRunner {

    private final FilterResultsCounter filterResultsCounter;

    public SimpleGeneFilterRunner() {
        filterResultsCounter = new FilterResultsCounter();
    }

    @Override
    public List<Gene> run(GeneFilter filter, List<Gene> genes) {
        for (Gene gene : genes) {
            if (gene.passedFilters()) {
                runFilterAndAddResult(filter, gene);
            }
        }
        return genes;
    }

    private void runFilterAndAddResult(GeneFilter filter, Gene gene) {
        FilterResult filterResult = filter.runFilter(gene);
        if (filterResult.wasRun()) {
            gene.addFilterResult(filterResult);
            filterResultsCounter.logResultsForFilter(filter, gene);
        }
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
