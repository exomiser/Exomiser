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

package org.monarchinitiative.exomiser.core.analysis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.monarchinitiative.exomiser.core.filters.InheritanceFilter;
import org.monarchinitiative.exomiser.core.filters.PriorityScoreFilter;
import org.monarchinitiative.exomiser.core.filters.VariantFilter;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriority;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;

/**
 * Interface to enable Filters and Prioritisers to be grouped together and provide some default utility methods.
 *
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface AnalysisStep {

    enum AnalysisStepType {VARIANT_FILTER, GENE_ONLY_DEPENDENT, INHERITANCE_MODE_DEPENDENT}

    @JsonIgnore
    default boolean isInheritanceModeDependent() {
        return this instanceof InheritanceFilter || this instanceof OmimPriority;
    }

    @JsonIgnore
    default boolean isVariantFilter() {
        return this instanceof VariantFilter;
    }

    @JsonIgnore
    default boolean isOnlyGeneDependent() {
        if (isInheritanceModeDependent()) {
            //note that both InheritanceFilter and OMIMPriority operate solely on genes, yet have a dependence on filtered variants
            //hence their exclusion here
            return false;
        }
        return this instanceof Prioritiser || this instanceof PriorityScoreFilter;
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
