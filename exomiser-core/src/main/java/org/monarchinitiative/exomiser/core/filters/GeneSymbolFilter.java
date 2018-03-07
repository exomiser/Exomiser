/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

/**
 * VariantFilter to remove any variants belonging to genes not on a user-entered
 * list of genes. This filter will match on gene symbol.
 * <p>
 * Note: this could be done as a GeneFilter but will be most efficient to run as
 * the first variantFilter
 *
 * @author Damian Smedley
 * @author Jules Jacobsen
 */
public class GeneSymbolFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(GeneSymbolFilter.class);

    private static final FilterType filterType = FilterType.ENTREZ_GENE_ID_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    private final Set<String> genesToKeep;

    public GeneSymbolFilter(Set<String> genesToKeep) {
        this.genesToKeep = genesToKeep;
    }

    public Set<String> getGeneSymbols() {
        return genesToKeep;
    }

    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        if (genesToKeep.contains(variantEvaluation.getGeneSymbol())) {
            return PASS;
        }
        return FAIL;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(GeneSymbolFilter.filterType);
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
        final GeneSymbolFilter other = (GeneSymbolFilter) obj;
        return Objects.equals(this.genesToKeep, other.genesToKeep);
    }

    @Override
    public String toString() {
        return "GeneSymbolFilter{" + "genesToKeep=" + genesToKeep + '}';
    }

}
