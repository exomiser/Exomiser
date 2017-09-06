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

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Objects;

/**
 * Filter for removing variants which have been characterised in a database.
 * This includes having an RSID assigned or having any frequency data.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class KnownVariantFilter implements VariantFilter {

    private static final FilterType KNOWN_VARIANT_FILTER_TYPE = FilterType.KNOWN_VARIANT_FILTER;

    private static final FilterResult PASS = FilterResult.pass(KNOWN_VARIANT_FILTER_TYPE);
    private static final FilterResult FAIL = FilterResult.fail(KNOWN_VARIANT_FILTER_TYPE);

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (notRepresentedInDatabase(variantEvaluation)) {
            return PASS;
        }
        return FAIL;
    }

    private boolean notRepresentedInDatabase(VariantEvaluation variantEvaluation) {
        return !variantEvaluation.getFrequencyData().isRepresentedInDatabase();
    }

    @Override
    public FilterType getFilterType() {
        return KNOWN_VARIANT_FILTER_TYPE;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(KNOWN_VARIANT_FILTER_TYPE);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "KnownVariantFilter{}";
    }
}
