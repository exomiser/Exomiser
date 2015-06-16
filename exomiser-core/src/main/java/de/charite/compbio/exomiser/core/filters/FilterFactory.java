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

        List<FilterType> filtersRequired = determineFilterTypesToRun(settings);

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case ENTREZ_GENE_ID_FILTER:
                    variantFilters.add(new EntrezGeneIdFilter(settings.getGenesToKeep()));
                    break;
                case TARGET_FILTER:
                    variantFilters.add(new TargetFilter());
                    break;
                case FREQUENCY_FILTER:
                    variantFilters.add(new FrequencyFilter(settings.getMaximumFrequency(), settings.removeKnownVariants()));
                    break;
                case QUALITY_FILTER:
                    variantFilters.add(new QualityFilter(settings.getMinimumQuality()));
                    break;
                case PATHOGENICITY_FILTER:
                    // if keeping off-target variants need to remove the pathogenicity cutoff to ensure that these variants always
                    // pass the pathogenicity filter and still get scored for pathogenicity
                    variantFilters.add(new PathogenicityFilter(settings.removePathFilterCutOff()));
                    break;
                case INTERVAL_FILTER:
                    variantFilters.add(new IntervalFilter(settings.getGeneticInterval()));
                    break;
                case INHERITANCE_FILTER:
                    //this isn't run as a VariantFilter - it's actually a Gene runFilter - currently it's a bastard orphan sitting in Exomiser
                    break;
                default:
                //do nothing
            }
        }

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

        List<FilterType> filtersRequired = determineFilterTypesToRun(settings);

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case INHERITANCE_FILTER:
                    geneFilters.add(new InheritanceFilter(settings.getModeOfInheritance()));
                    break;
                default:
                //do nothing
            }
        }

        return geneFilters;
    }

    /**
     * Determines the required {@code FilterType} to be run from the given
     * {@code FilterSettings}.
     *
     * @param filterSettings
     * @return
     */
    protected List<FilterType> determineFilterTypesToRun(FilterSettings filterSettings) {
        List<FilterType> filtersToRun = new ArrayList<>();

        if (!filterSettings.getGenesToKeep().isEmpty()) {
            filtersToRun.add(FilterType.ENTREZ_GENE_ID_FILTER);
        }

        //this would make more sense to be called 'removeOffTargetVariants'
        if (!filterSettings.keepOffTargetVariants()) {
            filtersToRun.add(FilterType.TARGET_FILTER);
        }
        filtersToRun.add(FilterType.FREQUENCY_FILTER);

        if (filterSettings.getMinimumQuality() != 0) {
            filtersToRun.add(FilterType.QUALITY_FILTER);
        }

        filtersToRun.add(FilterType.PATHOGENICITY_FILTER);

        if (filterSettings.getGeneticInterval() != null) {
            filtersToRun.add(FilterType.INTERVAL_FILTER);
        }
        if (filterSettings.getModeOfInheritance() != ModeOfInheritance.UNINITIALIZED) {
            filtersToRun.add(FilterType.INHERITANCE_FILTER);
        }

        return filtersToRun;
    }

}
