/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import java.util.Set;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityDataProvider implements VariantFilterDataProvider {

    private final VariantDataService variantDataService;
    private final VariantFilter variantFilter;
    private final Set<PathogenicitySource> pathogenicitySources;
    
    public PathogenicityDataProvider(VariantDataService variantDataService, Set<PathogenicitySource> pathogenicitySources, VariantFilter pathogenicityFilter) {
        this.variantDataService = variantDataService;
        this.pathogenicitySources = pathogenicitySources;
        this.variantFilter = pathogenicityFilter;
    }
    
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        addMissingPathogenicityData(variantEvaluation);
        return variantFilter.runFilter(variantEvaluation);
    }

    private void addMissingPathogenicityData(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getPathogenicityData().getPredictedPathogenicityScores().isEmpty()) {
            PathogenicityData pathData = variantDataService.getVariantPathogenicityData(variantEvaluation, pathogenicitySources);
            variantEvaluation.setPathogenicityData(pathData);
        }
    }

    @Override
    public FilterType getFilterType() {
        return variantFilter.getFilterType();
    }
    
}
