/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.filter;

import de.charite.compbio.exomiser.dao.FrequencyVariantScoreDao;
import de.charite.compbio.exomiser.dao.PathogenicityVariantScoreDao;
import de.charite.compbio.exomiser.util.ExomiserSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory class for handling creation of Filter objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class FilterFactory {

    private static final Logger logger = LoggerFactory.getLogger(FilterFactory.class);

    @Autowired
    private DataSource dataSource;

    public FilterFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

        if (settings.removeOffTargetVariants()) {
            variantFilterList.add(getTargetFilter());
        }
        variantFilterList.add(getFrequencyFilter(settings.getMaximumFrequency(), settings.removeDbSnp()));

        if (settings.getMinimumQuality() != 0) {
            variantFilterList.add(getQualityFilter(settings.getMinimumQuality()));
        }
        /*
         * the following shows P for everything and filters out if
         * use_pathogenicity_filter==true.
         */
        
//        TODO: is removeOffTargetVariants the correct switch for this? CHECK ORIGINAL CODE!!!!
        //also if we 
        variantFilterList.add(getPathogenicityFilter(settings.includePathogenic(), settings.removeOffTargetVariants()));

        if (!settings.getGeneticInterval().isEmpty()) {
            variantFilterList.add(getIntervalFilter(settings.getGeneticInterval()));
        }

        return variantFilterList;
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

        FrequencyVariantScoreDao variantTriageDAO = new FrequencyVariantScoreDao(dataSource);
        Filter frequencyFilter = new FrequencyFilter(variantTriageDAO, maxFrequency, filterOutAllDbsnp);

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

    public Filter getPathogenicityFilter(boolean filterOutNonpathogenic, boolean removeSynonomousVariants) {
//        //TODO: This needs to be linked to the ExomiserSettings 
        logger.info("Making pathogenicity filter - filterOutNonpathogenic: {} removeSynonomousVariants: {}", filterOutNonpathogenic, removeSynonomousVariants);
        PathogenicityVariantScoreDao pathogenicityTriageDao = new PathogenicityVariantScoreDao(dataSource);
        PathogenicityFilter filter = new PathogenicityFilter(pathogenicityTriageDao, filterOutNonpathogenic);
//        if (filterOutNonpathogenic) {
//            filter.setParameters("filter");
//        }
        filter.setRemoveSynonomousVariants(removeSynonomousVariants);
        logger.info("Made new: {}", filter);
        return filter;
    }

    public Filter getIntervalFilter(String interval) {

        Filter filter = new IntervalFilter(interval);

        logger.info("Made new: {}", filter);
        return filter;
    }

    public Filter getBedFilter(Set<String> commalist) {
        Filter filter = new BedFilter(commalist);
        logger.info("Made new: {}", filter);
        return filter;
    }

}
