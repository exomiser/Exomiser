/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

import java.util.List;
import java.util.ArrayList;
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

    /**
     * Utility method for wrapping-up how the {@code VariantFilter} classes are
     * created using an ExomiserSettings.
     *
     * @param settings
     * @return A list of {@code VariantFilter} objects
     */
    public List<VariantFilter> makeVariantFilters(ExomiserSettings settings) {
        List<VariantFilter> variantFilters = new ArrayList<>();

        List<FilterType> filtersRequired = settings.getFilterTypesToRun();

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case ENTREZ_GENE_ID_FILTER:
                    variantFilters.add(getEntrezGeneIdFilter(settings.getGenesToKeep()));
                    break;
                case TARGET_FILTER:
                    variantFilters.add(getTargetFilter());
                    break;
                case FREQUENCY_FILTER:
                    variantFilters.add(getFrequencyFilter(settings.getMaximumFrequency(), settings.removeKnownVariants()));
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
                default:
                    //do nothing
            }
            logger.info("Added {} filter" , filterType);
        }

        return variantFilters;
    }

    /**
     * Makes a list of {@code GeneFilter}
     *
     * @param settings
     * @return GeneFilters to run
     */
    public List<GeneFilter> makeGeneFilters(ExomiserSettings settings) {
        List<GeneFilter> geneFilters = new ArrayList<>();

        List<FilterType> filtersRequired = settings.getFilterTypesToRun();

        for (FilterType filterType : filtersRequired) {
            switch (filterType) {
                case INHERITANCE_FILTER:
                    geneFilters.add(getInheritanceFilter(settings.getModeOfInheritance()));
                    break;
                default:
                    //do nothing
            }
        }

        return geneFilters;
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
     * VariantFilter to remove any variants belonging to genes not on a
     * user-entered list of genes. Note: this could be done as a GeneFilter but
     * will be most efficient to run as the first variantFilter.
     *
     * @return
     */
    public VariantFilter getEntrezGeneIdFilter(Set<Integer> genesToKeep) {
        VariantFilter geneListFilter = new EntrezGeneIdFilter(genesToKeep);
        logger.info("Made new: {}", geneListFilter);
        return geneListFilter;
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

    public VariantFilter getQualityFilter(float qualityThreshold) {
        VariantFilter filter = new QualityFilter(qualityThreshold);

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
        GeneFilter filter = new GeneInheritanceFilter(modeOfInheritance);
        logger.info("Made new: {}", filter);
        return filter;
    }

    public GeneFilter getPriorityScoreFilter(float minPriorityScore) {
        GeneFilter filter = new GenePriorityScoreFilter(minPriorityScore);
        logger.info("Made new: {}", filter);
        return filter;
    }
}
