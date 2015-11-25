/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.filters;

import java.lang.*;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.annotation.Annotation;


import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters variants according to their {@link VariantEffect}. The filter will mark variants as failed if they are contained
 * in the set of specified {@link VariantEffect}.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEffectFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(VariantEffectFilter.class);

    private static final FilterType filterType = FilterType.VARIANT_EFFECT_FILTER;

    private final FilterResult passesFilter = new PassFilterResult(filterType);
    private final FilterResult failsFilter = new FailFilterResult(filterType);

    private final Set<VariantEffect> offTargetVariantTypes;
    
    public VariantEffectFilter(Set<VariantEffect> notWanted) {
        offTargetVariantTypes = notWanted;
    }

    public Set<VariantEffect> getOffTargetVariantTypes() {
        return offTargetVariantTypes;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation filterable) {
        VariantEffect effect = filterable.getVariantEffect();
        if (offTargetVariantTypes.contains(effect)) {
            return failsFilter;
        }
        return passesFilter;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(VariantEffectFilter.filterType);
        hash = 37 * hash + Objects.hashCode(this.offTargetVariantTypes);
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
        final VariantEffectFilter other = (VariantEffectFilter) obj;
        return Objects.equals(this.offTargetVariantTypes, other.offTargetVariantTypes);
    }

    @Override
    public String toString() {
        return "VariantEffectFilter{" + "offTargetVariantTypes=" + offTargetVariantTypes + '}';
    }

}
