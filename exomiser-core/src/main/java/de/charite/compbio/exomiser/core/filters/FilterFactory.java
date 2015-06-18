/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for handling creation of Filter objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FilterFactory.class);

    /**
     * Utility method for wrapping-up how the {@code VariantFilter} classes are
     * created using an ExomiserSettings.
     *
     * @param settings
     * @return A list of {@code VariantFilter} objects
     */
    public List<VariantFilter> makeVariantFilters(FilterSettings settings) {
        List<VariantFilter> variantFilters = new ArrayList<>();

        //IMPORTANT: These are ordered by increasing computational difficulty and
        //the number of variants they will remove.
        //Don't change them as this will negatively effect performance.
        
        //GENE_ID
        if (!settings.getGenesToKeep().isEmpty()) {
            variantFilters.add(new EntrezGeneIdFilter(settings.getGenesToKeep()));
        }
        //INTERVAL
        if (settings.getGeneticInterval() != null) {
            variantFilters.add(new IntervalFilter(settings.getGeneticInterval()));
        }
        //TARGET
        //this would make more sense to be called 'removeOffTargetVariants'
        if (!settings.keepOffTargetVariants()) {
            variantFilters.add(new TargetFilter());
        }
        //QUALITY
        if (settings.getMinimumQuality() != 0) {
            variantFilters.add(new QualityFilter(settings.getMinimumQuality()));
        }
        //FREQUENCY
        variantFilters.add(new FrequencyFilter(settings.getMaximumFrequency(), settings.removeKnownVariants()));
        //PATHOGENICITY
        // if keeping off-target variants need to remove the pathogenicity cutoff to ensure that these variants always
        // pass the pathogenicity filter and still get scored for pathogenicity
        variantFilters.add(new PathogenicityFilter(settings.removePathFilterCutOff()));

        return variantFilters;
    }

    /**
     * Makes a list of {@code GeneFilter}
     *
     * @param settings
     * @return GeneFilters to run
     */
    public List<GeneFilter> makeGeneFilters(FilterSettings settings) {
        List<GeneFilter> geneFilters = new ArrayList<>();

        if (settings.getModeOfInheritance() != ModeOfInheritance.UNINITIALIZED) {
            geneFilters.add(new InheritanceFilter(settings.getModeOfInheritance()));
        }

        return geneFilters;
    }

}
