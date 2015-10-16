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

package de.charite.compbio.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;

/**
 * Interface to enable Filters and Prioritisers to be grouped together and provide some default utility methods.
 *
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface AnalysisStep {

    enum AnalysisStepType {VARIANT_FILTER, GENE_ONLY_DEPENDENT, INHERITANCE_MODE_DEPENDENT};

    @JsonIgnore
    default boolean isInheritanceModeDependent() {
        return InheritanceFilter.class.isInstance(this) || OMIMPriority.class.isInstance(this);
    }

    @JsonIgnore
    default boolean isVariantFilter() {
        return VariantFilter.class.isInstance(this);
    }

    @JsonIgnore
    default boolean isOnlyGeneDependent() {
        if (isInheritanceModeDependent()) {
            //note that both InheritanceFilter and OMIMPriority operate solely on genes, yet have a dependence on filtered variants
            //hence their exclusion here
            return false;
        }
        return Prioritiser.class.isInstance(this) || PriorityScoreFilter.class.isInstance(this);
    }

    @JsonIgnore
    default AnalysisStepType getType() {
        if (isInheritanceModeDependent()) {
            return AnalysisStepType.INHERITANCE_MODE_DEPENDENT;
        }
        if (isVariantFilter()) {
            return AnalysisStepType.VARIANT_FILTER;
        }
        return AnalysisStepType.GENE_ONLY_DEPENDENT;
    }
}
