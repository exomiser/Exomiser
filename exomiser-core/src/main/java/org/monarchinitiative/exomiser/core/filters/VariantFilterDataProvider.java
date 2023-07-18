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

package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

/**
 * Decorator interface to provide data for variants just in time for the filter 
 * which requires it.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantFilterDataProvider extends VariantFilter {

    /**
     * @return the decorated filter which the DataProvider is providing data for.
     */
    VariantFilter getDecoratedFilter();

    /**
     * Provides the variantEvaluation with the implementation-specific data.
     * @param variantEvaluation
     */
    void provideVariantData(VariantEvaluation variantEvaluation);

    @Override
    default FilterResult runFilter(VariantEvaluation variantEvaluation) {
        provideVariantData(variantEvaluation);
        return getDecoratedFilter().runFilter(variantEvaluation);
    }

    @Override
    default FilterType getFilterType() {
        return getDecoratedFilter().getFilterType();
    }

}
