package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.VariantType;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter variants according to whether they are on target (i.e., located within
 * an exon or splice junction) or not. This filter also has the side effect of
 * calculating the counts of the various variant classes. The class uses the
 * annotations made by classes from the {@code jannovar.annotation} package etc.
 * <P>
 * Note that this class does not require a corresponding
 * {@link exomizer.filter.Triage Triage} object, because variants that do not
 * pass the filter are simply removed.
 *
 * @author Peter N Robinson
 * @version 0.16 (20 December, 2013)
 */
public class TargetFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(TargetFilter.class);

    private final FilterType filterType = FilterType.TARGET_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterScore passedScore = new TargetFilterScore(1f);        
    private final FilterScore failedScore = new TargetFilterScore(0f);
    
    /**
     * A set of off-target variant types such as Intergenic that we will filter
     * out from further consideration.
     */
    private EnumSet<VariantType> offTarget = EnumSet.of(VariantType.DOWNSTREAM,
            VariantType.INTERGENIC, VariantType.INTRONIC,
            VariantType.ncRNA_INTRONIC, VariantType.SYNONYMOUS,
            VariantType.UPSTREAM, VariantType.ERROR);

    /**
     * The constructor initializes the set of off-target
     * {@link jannovar.common.VariantType VariantType} constants, e.g.,
     * INTERGENIC, that we will filter out using this class.
     */
    public TargetFilter() {

    }

    /**
     * @return an integer constant (as defined in exomizer.common.Constants)
     * that will act as a flag to generate the output HTML dynamically depending
     * on the filters that the user has chosen.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Take a list of variants and apply the filter to each variant. If a
     * variant does not pass the filter, flag it as failed.
     *
     * @param variantList
     */
    @Override
    public void filterVariants(List<VariantEvaluation> variantList) {

        for (VariantEvaluation ve : variantList) {
            filterVariant(ve);
        }
    }

    @Override
    public boolean filterVariant(VariantEvaluation variantEvaluation) {
        VariantType vtype = variantEvaluation.getVariantType();
        if (offTarget.contains(vtype)) {
            //add a token failed score - this is essentially a boolean pass/fail so we're using 0 here.
            variantEvaluation.addFailedFilter(filterType, failedScore);
            return false;
        }
        variantEvaluation.addPassedFilter(filterType, passedScore);
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.filterType);
        hash = 37 * hash + Objects.hashCode(this.offTarget);
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
        if (this.filterType != other.filterType) {
            return false;
        }
        if (!Objects.equals(this.offTarget, other.offTarget)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return filterType + " filter offTarget=" + offTarget;
    }

}
