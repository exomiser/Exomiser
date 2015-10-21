/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.filters.FailFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jj8
 */
public class PrioritiserRunnerTest {
    
    private PrioritiserRunner instance;
    
    private List<Prioritiser> prioritisers;
    
    private Gene genePassedFilters;
    private Gene geneFailedFilters;
    private List<Gene> genes;

    
    @Before
    public void setUp() {
        instance = new PrioritiserRunner();
        prioritisers = new ArrayList<>();
        genes = new ArrayList<>();
        genePassedFilters = new Gene("PASSED_FILTERS", 12345);
        geneFailedFilters = new Gene("FAILED_FILTERS", 23456);
        geneFailedFilters.addFilterResult(new FailFilterResult(FilterType.VARIANT_EFFECT_FILTER));
        genes.add(genePassedFilters);
        genes.add(geneFailedFilters);
    }

    @Test
    public void testPrioritiseFilteredGenesReturnsListOfFilteredGenesOnly() {
        prioritisers.add(new NoneTypePrioritiser());
        
        List<Gene> passedGenes = new ArrayList<>();
        passedGenes.add(genePassedFilters);
        
        List<Gene> filteredGenes = instance.prioritiseFilteredGenes(prioritisers, genes);
        assertThat(filteredGenes, equalTo(passedGenes));
    }

    @Test
    public void testPrioritiseGenes_ReturnsListOfAllGenesRegardlessOfFilterStatus() {
        prioritisers.add(new NoneTypePrioritiser());
        
        List<Gene> filteredGenes = instance.prioritiseGenes(prioritisers, genes);
        assertThat(filteredGenes, equalTo(genes));
    }
    
}
