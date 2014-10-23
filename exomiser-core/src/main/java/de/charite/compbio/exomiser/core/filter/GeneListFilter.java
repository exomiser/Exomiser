package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.exome.Variant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter variants according to a linkage interval. For instance, if the
 * interval is chr2:12345-67890, then we would only keep variants located
 * between positions 12345 and 67890 on chromosome 2. All other variants are
 * discarded.
 * <P>
 * The interval must be given as chr2:12345-67890 (format), otherwise, an error
 * message is given and no filtering is done.
 *
 * @author Peter N Robinson
 * @version 0.08 (April 28, 2013)
 */
public class GeneListFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(GeneListFilter.class);

    private static final FilterType filterType = FilterType.GENE_LIST_FILTER;

    //add a token pass/failed score - this is essentially a boolean pass/fail, where 1 = pass and 0 = fail
    private final FilterResult passFilterResult = new GeneListFilterResult(1f, FilterResultStatus.PASS);
    private final FilterResult failedFilterResult = new GeneListFilterResult(0f, FilterResultStatus.FAIL);

    private List<Integer> genesToKeep;

    /**
     * Constructor defining the genetic interval.
     *
     * @param interval the interval based on a String such as chr2:12345-67890.
     */
    public GeneListFilter(List<Integer> genesToKeep) {
        this.genesToKeep = genesToKeep;

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

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (genesToKeep.contains(variantEvaluation.getVariant().getEntrezGeneID())){
            return passFilterResult;
        }
        return failedFilterResult;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(GeneListFilter.filterType);
        hash = 97 * hash + Objects.hashCode(this.genesToKeep);
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
        final GeneListFilter other = (GeneListFilter) obj;
        return Objects.equals(this.genesToKeep, other.genesToKeep);
    }

    @Override
    public String toString() {
        return filterType + " filter gene list = " + genesToKeep.toString();
    }

}
