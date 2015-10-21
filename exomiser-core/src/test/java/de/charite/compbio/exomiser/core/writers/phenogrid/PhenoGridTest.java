/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridTest {
    
    private PhenoGrid instance;
    
    private PhenoGridQueryTerms queryTerms;
    private List<PhenoGridMatchGroup> phenoGridMatchGroups;
    
    private final TestPhenoGridObjectCache testObjectCache = TestPhenoGridObjectCache.getInstance();
    
    @Before
    public void setUp() {
        String id = "hiPhive specified phenotypes";
        Set<String> queryTermIds= new TreeSet<>();
        queryTermIds.addAll(Arrays.asList("GRUF:111", "GRUF:222", "GRUF:333", "GRUF:444"));
        
        phenoGridMatchGroups = testObjectCache.getPhenoGridMatchGroups();
        
        queryTerms = new PhenoGridQueryTerms(id, queryTermIds);
        instance = new PhenoGrid(queryTerms, phenoGridMatchGroups);
    }

    @Test
    public void testGetPhenoGridQueryTerms() {
    }

    @Test
    public void testGetPhenoGridMatchGroups() {
    }
    
    @Test
    public void testJsonOutput() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        try {
            String jsonString = mapper.writeValueAsString(instance);
            System.out.println(jsonString);
        } catch (JsonProcessingException ex) {
            System.out.println(ex);
        }
    }

}
