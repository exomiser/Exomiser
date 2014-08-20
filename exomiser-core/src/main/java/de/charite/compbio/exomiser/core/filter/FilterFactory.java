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
 * Factory class for handling creation of Filter objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FilterFactory.class);

    public FilterFactory() {
    }

    /**
     * Utility method for wrapping-up how the
     * {@code de.charite.compbio.exomiser.filter.Filter} classes are created
     * using an ExomiserSettings.
     *
     * @param settings
     * @return A list of Filter objects
     */
    public List<Filter> makeFilters(ExomiserSettings settings) {
        List<Filter> variantFilterList = new ArrayList<>();

        List<FilterType> filtersRequired = determineFilterTypesToRun(settings);

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case TARGET_FILTER:
                    variantFilterList.add(getTargetFilter());
                    break;
                case FREQUENCY_FILTER:
                    variantFilterList.add(getFrequencyFilter(settings.getMaximumFrequency(), settings.removeDbSnp()));
                    break;
                case QUALITY_FILTER:
                    variantFilterList.add(getQualityFilter(settings.getMinimumQuality()));
                    break;
                case PATHOGENICITY_FILTER:
                    variantFilterList.add(getPathogenicityFilter(settings.keepNonPathogenicMissense()));
                    break;
                case INTERVAL_FILTER:
                    variantFilterList.add(getIntervalFilter(settings.getGeneticInterval()));
                    break;
                case INHERITANCE_FILTER:
                    variantFilterList.add(getInheritanceFilter(settings.getModeOfInheritance()));
                    break;
            }
        }

        return variantFilterList;
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
     * Filter on variant type that is expected potential pathogenic (Missense,
     * Intergenic etc and not off target (INTERGENIC, UPSTREAM, DOWNSTREAM).
     *
     * @return
     */
    public Filter getTargetFilter() {
        Filter targetFilter = new TargetFilter();
        logger.info("Made new: {}", targetFilter);
        return targetFilter;
    }

    /**
     * Add a frequency filter. There are several options. If the argument
     * filterOutAllDbsnp is true, then all dbSNP entries are removed
     * (dangerous). Else if the frequency is set to some value, we set this as
     * the maximum MAF. else we set the frequency filter to 100%, i.e., no
     * filtering.
     *
     * @param dataSource
     * @param maxFrequency
     * @param filterOutAllDbsnp
     * @return
     */
    public Filter getFrequencyFilter(float maxFrequency, boolean filterOutAllDbsnp) {

//        FrequencyVariantScoreDao variantTriageDAO = new FrequencyVariantScoreDao(dataSource);
        Filter frequencyFilter = new FrequencyFilter(maxFrequency, filterOutAllDbsnp);

//        if (filterOutAllDbsnp) {
//            frequencyFilter.setParameters("RS");
//        } else if (maxFrequency != null && !maxFrequency.equals("none")) {
//            frequencyFilter.(maxFrequency);
//        } else {
//            // default is freq filter at 100 i.e. keep everything so still
//            // get freq data in output and inclusion in prioritization
//            frequencyFilter.setParameters("100");
//        }
        logger.info("Made new: {}", frequencyFilter);
        return frequencyFilter;
    }

    public Filter getQualityFilter(float quality_threshold) {
        Filter filter = new QualityFilter(quality_threshold);

        logger.info("Made new Quality Filter: {}", filter);
        return filter;
    }

    public Filter getPathogenicityFilter(boolean filterOutNonpathogenic) {
        PathogenicityFilter filter = new PathogenicityFilter(filterOutNonpathogenic);
        logger.info("Made new: {}", filter);
        return filter;
    }

    public Filter getIntervalFilter(GeneticInterval interval) {

        Filter filter = new IntervalFilter(interval);

        logger.info("Made new: {}", filter);
        return filter;
    }

    public Filter getBedFilter(Set<String> commalist) {
        Filter filter = new BedFilter(commalist);
        logger.info("Made new: {}", filter);
        return filter;
    }

    public Filter getInheritanceFilter(ModeOfInheritance modeOfInheritance) {
        Filter filter = new InheritanceFilter(modeOfInheritance);
        logger.info("Made new: {}", filter);
        return filter;
    }

}
