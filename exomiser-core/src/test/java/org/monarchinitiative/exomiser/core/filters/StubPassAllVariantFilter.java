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
 * Stub class passes all VariantEvaluations through a Filter of the declared 
 * FilterType.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class StubPassAllVariantFilter implements VariantFilter {

    private final FilterType mockFilterType;

    public StubPassAllVariantFilter(FilterType mockFilterType) {
        this.mockFilterType = mockFilterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        return new PassFilterResult(mockFilterType);
    }

    @Override
    public FilterType getFilterType() {
        return mockFilterType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.mockFilterType);
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
        final StubPassAllVariantFilter other = (StubPassAllVariantFilter) obj;
        return this.mockFilterType == other.mockFilterType;
    }

    @Override
    public String toString() {
        return "StubVariantFilter{" + "mockFilterType=" + mockFilterType + '}';
    }

}
