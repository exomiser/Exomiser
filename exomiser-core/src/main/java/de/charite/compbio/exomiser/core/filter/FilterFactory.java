/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import jannovar.common.ModeOfInheritance;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for handling creation of VariantFilter objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FilterFactory.class);

    public FilterFactory() {
    }

    /**
     * Utility method for wrapping-up how the {@code VariantFilter} classes are
     * created using an ExomiserSettings.
     *
     * @param settings
     * @return A list of {@code VariantFilter} objects
     */
    public List<Filter> makeVariantFilters(ExomiserSettings settings) {
        List<Filter> variantFilters = new ArrayList<>();

        List<FilterType> filtersRequired = determineFilterTypesToRun(settings);

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case TARGET_FILTER:
                    variantFilters.add(getTargetFilter());
                    break;
                case FREQUENCY_FILTER:
                    variantFilters.add(getFrequencyFilter(settings.getMaximumFrequency(), settings.removeDbSnp()));
                    break;
                case QUALITY_FILTER:
                    variantFilters.add(getQualityFilter(settings.getMinimumQuality()));
                    break;
                case PATHOGENICITY_FILTER:
                    variantFilters.add(getPathogenicityFilter(settings.removePathFilterCutOff()));
                    break;
                case INTERVAL_FILTER:
                    variantFilters.add(getIntervalFilter(settings.getGeneticInterval()));
                    break;
                case INHERITANCE_FILTER:
                    //this isn't run as a VariantFilter - it's actually a Gene runFilter - currently it's a bastard orphan sitting in Exomiser
                    break;
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
    public List<Filter> makeGeneFilters(ExomiserSettings settings) {
        List<Filter> geneFilters = new ArrayList<>();

        List<FilterType> filtersRequired = determineFilterTypesToRun(settings);

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case INHERITANCE_FILTER:
                    geneFilters.add(getInheritanceFilter(settings.getModeOfInheritance()));
                    break;
            }
        }

        return geneFilters;
    }

    /**
     * Determines the required {@code FilterType} to be run from the given
     * {@code ExomiserSettings}.
     *
     * @param settings
     * @return
     */
    public static List<FilterType> determineFilterTypesToRun(ExomiserSettings settings) {
        List<FilterType> filtersToRun = new ArrayList<>();

        if (settings.removeOffTargetVariants()) {
            filtersToRun.add(FilterType.TARGET_FILTER);
        }
        filtersToRun.add(FilterType.FREQUENCY_FILTER);

        if (settings.getMinimumQuality() != 0) {
            filtersToRun.add(FilterType.QUALITY_FILTER);
        }

        filtersToRun.add(FilterType.PATHOGENICITY_FILTER);

        if (settings.getGeneticInterval() != null) {
            filtersToRun.add(FilterType.INTERVAL_FILTER);
        }
        if (settings.getModeOfInheritance() != ModeOfInheritance.UNINITIALIZED) {
            filtersToRun.add(FilterType.INHERITANCE_FILTER);
        }

        return filtersToRun;
    }

    /**
     * VariantFilter on variant type that is expected potential pathogenic
     * (Missense, Intergenic etc and not off target (INTERGENIC, UPSTREAM,
     * DOWNSTREAM).
     *
     * @return
     */
    public VariantFilter getTargetFilter() {
        VariantFilter targetFilter = new TargetFilter();
        logger.info("Made new: {}", targetFilter);
        return targetFilter;
    }

    /**
     * Add a frequency runFilter. There are several options. If the argument
     * filterOutAllDbsnp is true, then all dbSNP entries are removed
     * (dangerous). Else if the frequency is set to some value, we set this as
     * the maximum MAF. else we set the frequency runFilter to 100%, i.e., no
     * filtering.
     *
     * @param maxFrequency
     * @param filterOutAllDbsnp
     * @return
     */
    public VariantFilter getFrequencyFilter(float maxFrequency, boolean filterOutAllDbsnp) {

        VariantFilter frequencyFilter = new FrequencyFilter(maxFrequency, filterOutAllDbsnp);

        logger.info("Made new: {}", frequencyFilter);
        return frequencyFilter;
    }

    public VariantFilter getQualityFilter(float quality_threshold) {
        VariantFilter filter = new QualityFilter(quality_threshold);

        logger.info("Made new Quality Filter: {}", filter);
        return filter;
    }

    public VariantFilter getPathogenicityFilter(boolean removePathFilterCutOff) {
        // if keeping off-target variants need to remove the pathogenicity cutoff to ensure that these variants always 
        // pass the pathogenicity filter and still get scored for pathogenicity
        PathogenicityFilter filter = new PathogenicityFilter(removePathFilterCutOff);
        logger.info("Made new: {}", filter);
        return filter;
    }

    public VariantFilter getIntervalFilter(GeneticInterval interval) {

        VariantFilter filter = new IntervalFilter(interval);

        logger.info("Made new: {}", filter);
        return filter;
    }

    public VariantFilter getBedFilter(Set<String> commalist) {
        VariantFilter filter = new BedFilter(commalist);
        logger.info("Made new: {}", filter);
        return filter;
    }

    public GeneFilter getInheritanceFilter(ModeOfInheritance modeOfInheritance) {
        GeneFilter filter = new InheritanceFilter(modeOfInheritance);
        logger.info("Made new: {}", filter);
        return filter;
    }
}
