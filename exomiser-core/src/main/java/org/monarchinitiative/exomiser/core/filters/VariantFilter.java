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
 * This interface is implemented by classes that perform filtering of the
 * <b>variants</b> in the VCF file according to various criteria. A
 * {@code FilterResult} gets returned for each {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Peter N Robinson
 * @version 0.07 (April 28, 2013).
 */
public interface VariantFilter extends Filter<VariantEvaluation> {

    /**
     * Returns a {@code FilterResult} indicating whether the
     * {@code VariantEvaluation} passed or failed the {@code Filter}.
     *
     * @param variantEvaluation to be filtered
     */
    @Override
    FilterResult runFilter(VariantEvaluation variantEvaluation);

}
