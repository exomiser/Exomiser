/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriorityResult;
import java.util.Collections;
import java.util.List;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridAdaptorTest {
    
    PhenoGridAdaptor instance;
    
    List<HiPhivePriorityResult> hiPhiveResults;
    String phenoGridId = "hiPhive specified phenotypes";
    
    @Before
    public void setUp() {
        instance = new PhenoGridAdaptor();
    }

    @Test
    public void testPhenoGridFromEmptyHiPhiveResults() {
        hiPhiveResults = Collections.emptyList();
        PhenoGrid output = instance.makePhenoGridFromHiPhiveResults(phenoGridId, hiPhiveResults);
        assertThat(output, notNullValue());
        PhenoGridQueryTerms queryTerms = output.getPhenoGridQueryTerms();
        assertThat(queryTerms, notNullValue());
        assertThat(queryTerms.getId(), equalTo(phenoGridId));
        assertThat(queryTerms.getPhenotypeIds().isEmpty(), is(true));
        assertThat(output.getPhenoGridMatchGroups().isEmpty(), is(true));
    }
    
}
