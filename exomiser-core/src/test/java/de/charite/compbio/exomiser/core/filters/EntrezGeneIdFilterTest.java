/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class EntrezGeneIdFilterTest {
    
    private EntrezGeneIdFilter instance;
    private Set<Integer> genesToKeep;
    
    private static final int WANTED_GENE_ID = 1;
    private static final int UNWANTED_GENE_ID = 0;
    
    @Mock
    private VariantEvaluation wantedPassesFilter;
    @Mock
    private VariantEvaluation unwantedFailsFilter;

    @Before
    public void setUp() {
        initMocks();
        
        genesToKeep = new HashSet<>();
        genesToKeep.add(WANTED_GENE_ID);
        instance = new EntrezGeneIdFilter(genesToKeep);
    }

    private void initMocks() {
        Mockito.when(wantedPassesFilter.getEntrezGeneID()).thenReturn(WANTED_GENE_ID);
        Mockito.when(unwantedFailsFilter.getEntrezGeneID()).thenReturn(UNWANTED_GENE_ID);
    }

    @Test
    public void testGetFilterType() {
        assertThat(instance.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
    }

    @Test
    public void testRunFilterOnVariantWithWantedGeneIdPassesFilter() {
        genesToKeep.add(wantedPassesFilter.getEntrezGeneID());
        
        FilterResult filterResult = instance.runFilter(wantedPassesFilter);

        assertThat(filterResult.passedFilter(), is(true));
        assertThat(filterResult.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.PASS));
    }
    
    @Test
    public void testRunFilterOnVariantWithUnWantedGeneIdFailsFilter() {
        
        FilterResult filterResult = instance.runFilter(unwantedFailsFilter);

        assertThat(filterResult.passedFilter(), is(false));
        assertThat(filterResult.getFilterType(), equalTo(FilterType.ENTREZ_GENE_ID_FILTER));
        assertThat(filterResult.getResultStatus(), equalTo(FilterResultStatus.FAIL));
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
        assertThat(instance.toString(), equalTo("Genes to keep filter gene list = [1]"));
    }
    
}
