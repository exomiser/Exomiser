/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.filter;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
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

    /**
     * Filter on variant type that is expected potential pathogenic (Missense,
     * Intergenic etc and not off target (INTERGENIC, UPSTREAM, DOWNSTREAM).
     * @return 
     */
    public static Filter getTargetFilter() {
        Filter targetFilter = new TargetFilter();
        logger.info("Made new TargetFilter: {}" + targetFilter);
        return targetFilter;
    }

    /**
     * Add a frequency filter. There are several options. If the argument
     * filterOutAllDbsnp is true, then all dbSNP entries are removed
     * (dangerous). Else if the freuqency is set to some value, we set this is
     * the maximum MAF. else we set the frequency filter to 100%, i.e., no
     * filtering.
     * @param frequency_threshold
     * @param filterOutAllDbsnp
     * @return 
     * @throws de.charite.compbio.exomiser.exception.ExomizerInitializationException
     */
    public static Filter getFrequencyFilter(String frequency_threshold, boolean filterOutAllDbsnp) throws ExomizerInitializationException {
        Filter filter = new FrequencyFilter();

        if (filterOutAllDbsnp) {
            filter.setParameters("RS");
        } else if (frequency_threshold != null
                && !frequency_threshold.equals("none")) {
            filter.setParameters(frequency_threshold);
        } else {
            // default is freq filter at 100 i.e. keep everything so still
            // get freq data in output and inclusion in prioritization
            filter.setParameters("100");
        }
        logger.info("Made new Frequency Filter: {}", filter);
        return filter;
    }

    public static Filter getQualityFilter(String quality_threshold) throws ExomizerInitializationException {
        Filter filter = new QualityFilter();
        filter.setParameters(quality_threshold);

        logger.info("Made new Quality Filter: {}", filter);
        return filter;
    }

    public static Filter getPathogenicityFilter(boolean filterOutNonpathogenic, boolean removeSyn) throws ExomizerInitializationException {
        PathogenicityFilter filter = new PathogenicityFilter();
        if (filterOutNonpathogenic) {
            filter.setParameters("filter");
        }
        filter.set_syn_filter_status(removeSyn);
        logger.info("Made new Pathogenicity Filter: {}", filter);
        return filter;
    }

    public static Filter getLinkageFilter(String interval) throws ExomizerInitializationException {

        Filter filter = new IntervalFilter();
        filter.setParameters(interval);
        
        logger.info("Made new Linkage Filter: {}", filter);
        return filter;
    }
    
    public static Filter getBedFilter(Set<String> commalist) throws ExomizerInitializationException {
	Filter filter = new BedFilter(commalist);
	logger.info("Made new Bed Filter: {}", filter);
        return filter;
    }

}
