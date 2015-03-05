/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchGroupTest {
    
    private PhenoGridMatchGroup instance;
    
    private List<PhenoGridMatch> matches;
    private List<String> queryPhenotypeTermIds;
    
    //what's this cutoff for? the IC? 
    private static final int CUTOFF = 10; 

    private final TestPhenoGridObjectCache matchCache = TestPhenoGridObjectCache.getInstance();

    @Before
    public void setUp() {
        matches = matchCache.getPhenoGridMatches();
        queryPhenotypeTermIds = matchCache.getQueryPhenotypeTermIds();
        
        instance = new PhenoGridMatchGroup(matches, queryPhenotypeTermIds);
    }

    @Test
    public void testGetMatches() {
        assertThat(instance.getMatches(), equalTo(matches));
    }

    @Test
    public void testGetQueryPhenotypeTermIds() {
        Set expectedqueryTermIds = new TreeSet<>(queryPhenotypeTermIds);
        assertThat(instance.getQueryPhenotypeTermIds(), equalTo(expectedqueryTermIds));
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
