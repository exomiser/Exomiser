/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static de.charite.compbio.exomiser.core.filters.FilterTestHelper.assertFailed;
import static de.charite.compbio.exomiser.core.filters.FilterTestHelper.assertPassed;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class EntrezGeneIdFilterTest {
    
    private EntrezGeneIdFilter instance;
    private Set<Integer> genesToKeep;
    
    private static final int WANTED_GENE_ID = 1;
    private static final int UNWANTED_GENE_ID = 0;
    
    private VariantEvaluation wantedPassesFilter;
    private VariantEvaluation unwantedFailsFilter;

    @Before
    public void setUp() {
        initVariants();
        
        genesToKeep = new HashSet<>();
        genesToKeep.add(WANTED_GENE_ID);
        instance = new EntrezGeneIdFilter(genesToKeep);
    }

    private void initVariants() {
        wantedPassesFilter = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").geneId(WANTED_GENE_ID).build();
        unwantedFailsFilter = new VariantEvaluation.VariantBuilder(1, 1, "A", "T").geneId(UNWANTED_GENE_ID).build();
    }

    @Test
    public void testGetGeneIds() {
        assertThat(instance.getGeneIds(), equalTo(genesToKeep));
    }
    
    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }

    @Test
    public void testRunFilterOnVariantWithWantedGeneIdPassesFilter() {
        genesToKeep.add(wantedPassesFilter.getEntrezGeneId());
        
        FilterResult filterResult = instance.runFilter(wantedPassesFilter);

        assertPassed(filterResult);
        assertThat(filterResult.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }
    
    @Test
    public void testRunFilterOnVariantWithUnWantedGeneIdFailsFilter() {
        
        FilterResult filterResult = instance.runFilter(unwantedFailsFilter);

        assertFailed(filterResult);
        assertThat(filterResult.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }

    @Test
    public void testHashCode() {
        Filter otherFilter = new EntrezGeneIdFilter(genesToKeep);
        assertThat(instance.hashCode(), equalTo(otherFilter.hashCode()));
    }

    @Test
    public void testEquals() {
    }

    @Test
    public void testToString() {
        assertThat(instance.toString(), equalTo("EntrezGeneIdFilter{genesToKeep=[1]}"));
    }
    
}
