package de.charite.compbio.exomiser.core.filters;

import java.lang.*;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.annotation.Annotation;


import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter variants according to whether they are on target (i.e., located
 * within an exon or splice junction) or not. This runFilter also has the side
 * effect of calculating the counts of the various variant classes. The class
 * uses the annotations made by classes from the {@code jannovar.annotation}
 * package etc.
 * <P>
 * Note that this class does not require a corresponding
 * {@link exomizer.filter.Triage Triage} object, because variants that do not
 * pass the runFilter are simply removed.
 *
 * @author Peter N Robinson
 * @version 0.16 (20 December, 2013)
 */
public class TargetFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(TargetFilter.class);

    private static final FilterType filterType = FilterType.TARGET_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passedFilterResult = new TargetFilterResult(1f, FilterResultStatus.PASS);
    private final FilterResult failedFilterResult = new TargetFilterResult(0f, FilterResultStatus.FAIL);

    /**
     * A set of off-target variant types such as Intergenic that we will
     * runFilter out from further consideration.
     */
    private final Set<VariantEffect> offTargetVariantTypes;

    /**
     * The constructor initializes the set of off-target
     * {@link jannovar.common.VariantType VariantType} constants, e.g.,
     * INTERGENIC, that we will runFilter out using this class.
     */
    public TargetFilter() {
        offTargetVariantTypes = EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT, VariantEffect.DOWNSTREAM_GENE_VARIANT);
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation filterable) {
        VariantEffect effect = filterable.getVariantEffect();
        // TODO make below nicer using a Jannovar method hopefully 
        if (effect.equals(VariantEffect.INTERGENIC_VARIANT) || effect.equals(VariantEffect.UPSTREAM_GENE_VARIANT)){
            Annotation a = filterable.getAnnotationList().getHighestImpactAnnotation();
            String intergenicAnnotation = a.toVCFAnnoString(filterable.getAlt());
            //int dist = Math.abs(Integer.parseInt(intergenicAnnotation.split("\\|")[14]));            
            int dist = Math.abs(Integer.parseInt(intergenicAnnotation.split("\\|")[14])); 
            if (dist > 0 && dist < 5000){
                return passedFilterResult;
            }
            return failedFilterResult;
        }
        else if (offTargetVariantTypes.contains(effect)) {
            return failedFilterResult;
        }
        return passedFilterResult;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(TargetFilter.filterType);
        hash = 37 * hash + Objects.hashCode(this.offTargetVariantTypes);
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
        final TargetFilter other = (TargetFilter) obj;
        return Objects.equals(this.offTargetVariantTypes, other.offTargetVariantTypes);
    }

    @Override
    public String toString() {
        return filterType + " filter offTarget=" + offTargetVariantTypes;
    }

}
