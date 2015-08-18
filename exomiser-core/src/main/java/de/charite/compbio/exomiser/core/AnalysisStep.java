/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.filters.GeneFilter;
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

    default boolean isInheritanceModeDependent() {
        return InheritanceFilter.class.isInstance(this) || OMIMPriority.class.isInstance(this);
    }

    default boolean isVariantFilter() {
        return VariantFilter.class.isInstance(this);
    }

    default boolean isGeneFilter() {
        return GeneFilter.class.isInstance(this);
    }

    default boolean isPrioritiser() {
        return Prioritiser.class.isInstance(this);
    }

    default boolean onlyRequiresGenes() {
        //could also return !isInheritanceModeDependent() &! isVariantFilter() but this might not always hold true if new classes are added
        if (OMIMPriority.class.isInstance(this)) {
            return false;
        }
        return Prioritiser.class.isInstance(this) || PriorityScoreFilter.class.isInstance(this);
    }
}
