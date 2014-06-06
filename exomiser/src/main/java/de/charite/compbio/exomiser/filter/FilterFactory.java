/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.filter;

import de.charite.compbio.exomiser.dao.FrequencyTriageDAO;
import de.charite.compbio.exomiser.dao.PathogenicityTriageDAO;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
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
     * @return
     * @throws ExomizerInitializationException
     */
    public List<Filter> makeFilters(ExomiserSettings settings) {
        List<Filter> variantFilterList = new ArrayList<>();
        //

        if (settings.removeOffTargetVariants()) {
            variantFilterList.add(getTargetFilter());
        }
        //TODO: REMOVE THE CONVERSION STRING -> FLOAT -> STRING -> FLOAT
        variantFilterList.add(getFrequencyFilter(settings.getMaximumFrequency(), settings.removeDbSnp()));
        

        if (settings.getMinimumQuality() != 0) {
            try {
                //TODO: REMOVE THE CONVERSION STRING -> FLOAT -> STRING -> FLOAT
                variantFilterList.add(getQualityFilter(String.valueOf(settings.getMinimumQuality())));
            } catch (ExomizerInitializationException ex) {
                logger.error(null, ex);
            }
        }
        try {
            /*
             * the following shows P for everything and filters out if
             * use_pathogenicity_filter==true.
             */
            variantFilterList.add(getPathogenicityFilter(settings.includePathogenic(), settings.removeOffTargetVariants()));
        } catch (ExomizerInitializationException ex) {
            logger.error(null, ex);
        }

        if (!settings.getGeneticInterval().isEmpty()) {
            try {
                variantFilterList.add(getLinkageFilter(settings.getGeneticInterval()));
            } catch (ExomizerInitializationException ex) {
                logger.error(null, ex);
            }
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
        logger.info("Made new Filter: {}", targetFilter);
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
     * @throws
     * de.charite.compbio.exomiser.exception.ExomizerInitializationException
     */
    public Filter getFrequencyFilter(float maxFrequency, boolean filterOutAllDbsnp) {

        FrequencyTriageDAO variantTriageDAO = new FrequencyTriageDAO(dataSource);
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
        logger.info("Made new Filter: {}", frequencyFilter);
        return frequencyFilter;
    }

    public Filter getQualityFilter(String quality_threshold) throws ExomizerInitializationException {
        Filter filter = new QualityFilter();
        filter.setParameters(quality_threshold);

        logger.info("Made new Quality Filter: {}", filter);
        return filter;
    }

    public Filter getPathogenicityFilter(boolean filterOutNonpathogenic, boolean removeSynonomousVariants) throws ExomizerInitializationException {

        PathogenicityTriageDAO pathogenicityTriageDao = new PathogenicityTriageDAO(dataSource);
        PathogenicityFilter filter = new PathogenicityFilter(pathogenicityTriageDao);
        if (filterOutNonpathogenic) {
            filter.setParameters("filter");
        }
        filter.setSynonymousFilterStatus(removeSynonomousVariants);
        logger.info("Made new Filter: {}", filter);
        return filter;
    }

    public Filter getLinkageFilter(String interval) throws ExomizerInitializationException {

        Filter filter = new IntervalFilter();
        filter.setParameters(interval);

        logger.info("Made new Filter: {}", filter);
        return filter;
    }

    public Filter getBedFilter(Set<String> commalist) throws ExomizerInitializationException {
        Filter filter = new BedFilter(commalist);
        logger.info("Made new Filter: {}", filter);
        return filter;
    }

}
