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

import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

/**
 * Provides the base functionality for VariantFilterDataProviders.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AbstractFilterDataProvider implements VariantFilterDataProvider {

    protected final VariantDataService variantDataService;
    private final VariantFilter variantFilter;

    /**
     * Provides the base functionality for all VariantFilterDataProviders.
     *
     * @param variantDataService
     * @param variantFilter
     */
    AbstractFilterDataProvider(VariantDataService variantDataService, VariantFilter variantFilter) {
        this.variantDataService = variantDataService;
        this.variantFilter = variantFilter;
    }

    @Override
    public VariantFilter getDecoratedFilter() {
        return variantFilter;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        provideVariantData(variantEvaluation);
        return variantFilter.runFilter(variantEvaluation);
    }

    @Override
    public FilterType getFilterType() {
        return variantFilter.getFilterType();
    }

    //TODO: is this a good idea to make this class 'invisible' like this?
    @Override
    public boolean equals(Object o) {
        return variantFilter.equals(o);
    }

    @Override
    public int hashCode() {
        return variantFilter.hashCode();
    }

    @Override
    public String toString() {
        return variantFilter.toString();
    }

}
