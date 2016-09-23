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
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.model.Gene;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author jj8
 */
public class PrioritiserRunnerTest {

    private List<Prioritiser> prioritisers;
    
    private Gene genePassedFilters;
    private Gene geneFailedFilters;
    private List<Gene> genes;

    
    @Before
    public void setUp() {
        prioritisers = new ArrayList<>();
        genes = new ArrayList<>();
        genePassedFilters = new Gene("PASSED_FILTERS", 12345);
        geneFailedFilters = new Gene("FAILED_FILTERS", 23456);
        geneFailedFilters.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        genes.add(genePassedFilters);
        genes.add(geneFailedFilters);
    }

    @Test
    public void testPrioritiseFilteredGenesReturnsListOfFilteredGenesOnly() {
        prioritisers.add(new NoneTypePrioritiser());
        
        List<Gene> passedGenes = new ArrayList<>();
        passedGenes.add(genePassedFilters);
        
        List<Gene> filteredGenes = PrioritiserRunner.prioritiseFilteredGenes(prioritisers, genes);
        assertThat(filteredGenes, equalTo(passedGenes));
    }

    @Test
    public void testPrioritiseGenes_ReturnsListOfAllGenesRegardlessOfFilterStatus() {
        prioritisers.add(new NoneTypePrioritiser());
        
        List<Gene> filteredGenes = PrioritiserRunner.prioritiseGenes(prioritisers, genes);
        assertThat(filteredGenes, equalTo(genes));
    }
    
}
