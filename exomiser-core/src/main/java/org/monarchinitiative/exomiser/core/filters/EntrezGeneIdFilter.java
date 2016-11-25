package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * VariantFilter to remove any variants belonging to genes not on a user-entered
 * list of genes.
 *
 * Note: this could be done as a GeneFilter but will be most efficient to run as
 * the first variantFilter
 *
 * @author Damian Smedley
 * @author Jules Jacobsen
 */
public class EntrezGeneIdFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(EntrezGeneIdFilter.class);

    private static final FilterType filterType = FilterType.ENTREZ_GENE_ID_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    private final Set<Integer> genesToKeep;

    public EntrezGeneIdFilter(Set<Integer> genesToKeep) {
        this.genesToKeep = genesToKeep;
    }

    public Set<Integer> getGeneIds() {
        return genesToKeep;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (genesToKeep.contains(variantEvaluation.getEntrezGeneId())) {
            return PASS;
        }
        return FAIL;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(EntrezGeneIdFilter.filterType);
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
        final EntrezGeneIdFilter other = (EntrezGeneIdFilter) obj;
        return Objects.equals(this.genesToKeep, other.genesToKeep);
    }

    @Override
    public String toString() {
        return "EntrezGeneIdFilter{" + "genesToKeep=" + genesToKeep + '}';
    }

}
