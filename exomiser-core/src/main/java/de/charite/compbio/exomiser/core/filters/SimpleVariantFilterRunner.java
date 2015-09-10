/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.Filterable;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies the given {@code VariantFilter} to the {@code VariantEvaluation}.
 * This can be done in a 'non-destructive' manner such that every
 * {@code VariantEvaluation} is passed through each and every run, or in a
 * 'destructive' manner where only {@code VariantEvaluation} which pass through
 * all the desired filters are returned at the end.
 *
 * This simple implementation of the {@code VariantFilter} assumes that all the
 * necessary data has been applied to the {@code VariantEvaluation} being
 * filtered beforehand. If it hasn't then the results will be wrong
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantFilterRunner implements VariantFilterRunner {

    private static final Logger logger = LoggerFactory.getLogger(SimpleVariantFilterRunner.class);

    //TODO: this should vanish
    private final VariantDataService variantDataService;

    //TODO: There should be no need for a parameterised constructor
    public SimpleVariantFilterRunner(VariantDataService variantDataService) {
        this.variantDataService = variantDataService;
    }
   
    @Override
    public List<VariantEvaluation> run(List<VariantFilter> variantFilters, List<VariantEvaluation> variantEvaluations) {
        logger.info("Filtering {} variants using simple filtering...", variantEvaluations.size());
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            run(variantFilters, variantEvaluation);
        }
        logger.info("Ran {} filters over {} variants using simple filtering.", getFilterTypes(variantFilters), variantEvaluations.size());
        return variantEvaluations;
    }

    @Override
    public List<VariantEvaluation> run(VariantFilter filter, List<VariantEvaluation> filterables) {
        for (VariantEvaluation variantEvaluation : filterables) {
            run(filter, variantEvaluation);
        }
        return filterables;
    }

    private void run(List<VariantFilter> variantFilters, VariantEvaluation variantEvaluation) {
        for (VariantFilter filter : variantFilters) {
            run(filter, variantEvaluation);
        }
    }

    public FilterResult run(Filter filter, VariantEvaluation variantEvaluation) {
        addMissingVariantData(filter, variantEvaluation);
        return runFilterAndAddResult(filter, variantEvaluation);
    }

    //TODO: Remove this - provide functionality by decorating the filter in a VariantFilterDataProvider
    protected void addMissingVariantData(Filter filter, VariantEvaluation variantEvaluation) {
        switch (filter.getFilterType()) {
            //DONE: Add these to the new FrequencyDataProvider   
            case FREQUENCY_FILTER:
            case KNOWN_VARIANT_FILTER:
                //will require the FrequencySource in order to get the right ones
                variantEvaluation.setFrequencyData(variantDataService.getVariantFrequencyData(variantEvaluation, EnumSet.allOf(FrequencySource.class)));
                break;
            //TODO: Add these to the new PathogenicityDataProvider    
            case PATHOGENICITY_FILTER:
                //will require the PathogenicitySource in order to get the right ones specified by the user in the yaml file
                variantEvaluation.setPathogenicityData(variantDataService.getVariantPathogenicityData(variantEvaluation, EnumSet.of(PathogenicitySource.SIFT, PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER)));
                break;
            //TODO: remove CADD and NCDS - this will be handled by Pathogenicityfilter
            //Check the functionality first - they may not be identical to the Pathogenicityfilter
//            case CADD_FILTER:
//                variantDataService.setVariantCaddData(variantEvaluation);
//                break;
//            case NCDS_FILTER:
//                variantDataService.setVariantNcdsData(variantEvaluation);
//                break;
            //TODO: Add this to the new RegulatoryFeatureDataProvider    
            case REGULATORY_FEATURE_FILTER:
                variantEvaluation.setVariantEffect(variantDataService.getVariantRegulatoryFeatureData(variantEvaluation));
                break;
        }
    }

    protected FilterResult runFilterAndAddResult(Filter filter, Filterable filterable) {
        FilterResult filterResult = filter.runFilter(filterable);
        filterable.addFilterResult(filterResult);
        return filterResult;
    }

    protected Set<FilterType> getFilterTypes(List<VariantFilter> filters) {
        Set<FilterType> filtersRun = new LinkedHashSet<>();
        for (Filter filter : filters) {
            filtersRun.add(filter.getFilterType());
        }
        return filtersRun;
    }

}
