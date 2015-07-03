package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

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
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEffectFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(VariantEffectFilter.class);

    private static final FilterType filterType = FilterType.VARIANT_EFFECT_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passedFilterResult = new PassFilterResult(filterType, 1f);
    private final FilterResult failedFilterResult = new FailFilterResult(filterType, 0f);

    private final Set<VariantEffect> offTargetVariantTypes;
    
    public VariantEffectFilter(Set<VariantEffect> notWanted) {
        offTargetVariantTypes = notWanted;
    }

    public Set<VariantEffect> getOffTargetVariantTypes() {
        return offTargetVariantTypes;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation filterable) {
        VariantEffect effect = filterable.getVariantEffect();
        if (offTargetVariantTypes.contains(effect)) {
            return failedFilterResult;
        }
        return passedFilterResult;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(VariantEffectFilter.filterType);
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
        final VariantEffectFilter other = (VariantEffectFilter) obj;
        return Objects.equals(this.offTargetVariantTypes, other.offTargetVariantTypes);
    }

    @Override
    public String toString() {
        return "VariantEffectFilter{" + "offTargetVariantTypes=" + offTargetVariantTypes + '}';
    }

}
