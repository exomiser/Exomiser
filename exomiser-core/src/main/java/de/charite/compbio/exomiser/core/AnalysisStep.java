/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;

/**
 * Interface to enable Filters and Prioritisers to be grouped together and provide some default utility methods.
 * 
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
