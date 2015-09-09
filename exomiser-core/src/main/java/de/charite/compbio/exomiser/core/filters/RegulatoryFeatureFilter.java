package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class RegulatoryFeatureFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureFilter.class);

    private static final FilterType filterType = FilterType.REGULATORY_FEATURE_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passedFilterResult = new PassFilterResult(filterType);
    private final FilterResult failedFilterResult = new FailFilterResult(filterType);

    /**
     * The constructor initializes the set of off-target
     * {@link jannovar.common.VariantType VariantType} constants, e.g.,
     * INTERGENIC, that we will runFilter out using this class.
     */
    public RegulatoryFeatureFilter() {
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation filterable) {
        VariantEffect effect = filterable.getVariantEffect();
        // Note the INTERGENIC/UPSTREAM variants have already been assessed by the RegFeatureDAO and VariantEffect set to REGULATORY_FEATURE if in a known region
        // TODO make below nicer using a Jannovar method hopefully 
        if (effect.equals(VariantEffect.INTERGENIC_VARIANT) || effect.equals(VariantEffect.UPSTREAM_GENE_VARIANT)){
            Annotation a = filterable.getAnnotations().get(0);//.getHighestImpactAnnotation();
            String intergenicAnnotation = a.toVCFAnnoString(filterable.getAlt());        
            int dist = Math.abs(Integer.parseInt(intergenicAnnotation.split("\\|")[14])); 
            if (dist > 0 && dist < 20000){
                return passedFilterResult;
            }
            return failedFilterResult;
        }
        return passedFilterResult;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(RegulatoryFeatureFilter.filterType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegulatoryFeatureFilter other = (RegulatoryFeatureFilter) obj;
        return Objects.equals(this.getFilterType(), other.getFilterType());
    }

    @Override
    public String toString() {
        return "RegulatoryFeatureFilter{" + '}';
    }
    
}
