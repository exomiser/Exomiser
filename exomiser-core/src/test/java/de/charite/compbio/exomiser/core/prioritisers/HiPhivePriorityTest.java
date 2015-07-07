/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;

import de.charite.compbio.exomiser.core.model.Gene;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 *
 * @author jj8
 */
public class HiPhivePriorityTest {
    
    private HiPhivePriority instance;
    
    private List<String> hpoIds;
    private String candidateGene;
    private String disease;
    private String exomiser2params;

    @Before
    public void setUp() {
        hpoIds = new ArrayList<>();
        hpoIds.add("HP:000001");
        hpoIds.add("HP:000002");
        hpoIds.add("HP:000003");
        
        candidateGene = "GENE1";
        disease = "OMIM:100100";
        exomiser2params = "";
        
        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions(), null);
    }

    @Test
    public void testGetPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.HIPHIVE_PRIORITY));
    }

    @Ignore
    @Test
    public void testPrioritizeGenes() {
        instance.prioritizeGenes(new ArrayList<Gene>());
    }


    @Ignore
    @Test
    public void testPrioritizeGenesInBenchmarkingMode() {
        instance = new HiPhivePriority(hpoIds, new HiPhiveOptions(disease, candidateGene), null);
        instance.prioritizeGenes(new ArrayList<Gene>());
    }

    @Test
    public void testSetPriorityService() {
    }

    @Test
    public void testToString() {
        System.out.println(instance);
    }
    
}
