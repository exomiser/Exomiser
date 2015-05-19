/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
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
        
        instance = new HiPhivePriority(hpoIds, candidateGene, disease, exomiser2params, null);
    }

    @Test
    public void testGetPriorityType() {
    }

    @Test
    public void testPrioritizeGenes() {
    }

    @Test
    public void testGetMessages() {
    }

    @Test
    public void testDisplayInHTML() {
    }

    @Test
    public void testGetHTMLCode() {
    }

    @Test
    public void testSetPriorityService() {
    }

    @Test
    public void testToString() {
        System.out.println(instance);
    }
    
}
