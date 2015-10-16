package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RegulatoryFeatureFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureFilter.class);

    private static final FilterType filterType = FilterType.REGULATORY_FEATURE_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passedFilterResult = new PassFilterResult(filterType);
    private final FilterResult failedFilterResult = new FailFilterResult(filterType);

    public RegulatoryFeatureFilter() {
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        VariantEffect effect = variantEvaluation.getVariantEffect();
        // Note the INTERGENIC/UPSTREAM variants have already been assessed by the RegFeatureDAO and VariantEffect set to REGULATORY_REGION_VARIANT if in a known region
        // TODO make below nicer using a Jannovar method hopefully 
        if (effect.equals(VariantEffect.INTERGENIC_VARIANT) || effect.equals(VariantEffect.UPSTREAM_GENE_VARIANT)){
            // GeneReassigner can assign a new empty list
            if (variantEvaluation.getAnnotations().isEmpty()){
                return failedFilterResult;
            }
            Annotation annotation = variantEvaluation.getAnnotations().get(0);//.getHighestImpactAnnotation();
            String intergenicAnnotation = annotation.toVCFAnnoString(variantEvaluation.getAlt());        
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
