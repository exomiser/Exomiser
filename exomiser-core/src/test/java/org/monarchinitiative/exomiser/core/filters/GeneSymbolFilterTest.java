/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import com.google.common.collect.ImmutableSortedSet;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneSymbolFilterTest {

    private static final String WANTED_GENE_SYMBOL = "ABC1";
    private static final String UNWANTED_GENE_SYMBOL = "DEF2";

    private VariantEvaluation variantWithGeneSymbol(String geneSymbol) {
        return TestFactory.variantBuilder(1, 1, "A", "T").geneSymbol(geneSymbol).build();
    }

    private GeneSymbolFilter geneSymbolFilter(String... geneSymbols) {
        return new GeneSymbolFilter(genesToKeep(geneSymbols));
    }

    private Set<String> genesToKeep(String... geneSymbols) {
        return ImmutableSortedSet.copyOf(geneSymbols);
    }

    @Test
    public void testGetGeneIds() {
        Set<String> genesToKeep = genesToKeep(WANTED_GENE_SYMBOL);
        GeneSymbolFilter instance = new GeneSymbolFilter(genesToKeep);
        assertThat(instance.getGeneSymbols(), equalTo(genesToKeep));
    }

    @Test
    public void testGetFilterType() {
        GeneSymbolFilter instance = new GeneSymbolFilter(Collections.emptySet());
        assertThat(instance.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }

    @Test
    public void testRunFilterOnVariantWithWantedGeneIdPassesFilter() {
        GeneSymbolFilter instance = geneSymbolFilter(WANTED_GENE_SYMBOL);
        FilterResult filterResult = instance.runFilter(variantWithGeneSymbol(WANTED_GENE_SYMBOL));

        FilterTestHelper.assertPassed(filterResult);
        assertThat(filterResult.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }

    @Test
    public void testRunFilterOnVariantWithUnWantedGeneIdFailsFilter() {
        GeneSymbolFilter instance = geneSymbolFilter(WANTED_GENE_SYMBOL);
        FilterResult filterResult = instance.runFilter(variantWithGeneSymbol(UNWANTED_GENE_SYMBOL));

        FilterTestHelper.assertFailed(filterResult);
        assertThat(filterResult.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }

    @Test
    public void testHashCode() {
        Filter instance = geneSymbolFilter(WANTED_GENE_SYMBOL);
        Filter otherFilter = geneSymbolFilter(WANTED_GENE_SYMBOL);
        assertThat(instance.hashCode(), equalTo(otherFilter.hashCode()));
    }

    @Test
    public void testEquals() {
        Filter instance = geneSymbolFilter(WANTED_GENE_SYMBOL);
        Filter otherFilter = geneSymbolFilter(WANTED_GENE_SYMBOL);
        assertThat(instance, equalTo(otherFilter));
    }

    @Test
    public void testToString() {
        GeneSymbolFilter instance = geneSymbolFilter("ABC1");
        assertThat(instance.toString(), equalTo("GeneSymbolFilter{genesToKeep=[ABC1]}"));
    }

}
