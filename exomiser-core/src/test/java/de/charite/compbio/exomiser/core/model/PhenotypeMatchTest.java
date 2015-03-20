/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeMatchTest {
    
    private PhenotypeMatch instance;
    
    private PhenotypeTerm lcs;
    private PhenotypeTerm queryPhenotype;
    private PhenotypeTerm matchPhenotype;
    private double simJ;
    private double score;
    
    @Before
    public void setUp() {
        lcs = new PhenotypeTerm("ID:12345", "nose", 1.00);
        queryPhenotype = new PhenotypeTerm("ID:12344", "big nose", 2.00);
        matchPhenotype = new PhenotypeTerm("ID:12355", "little nose", 2.00); 
        simJ = 0.8;
        score = 1.26;
        
        instance = new PhenotypeMatch(queryPhenotype, matchPhenotype, simJ, score, lcs);
    }

    @Test
    public void testGetQueryPhenotypeId() {
        assertThat(instance.getQueryPhenotypeId(), equalTo(queryPhenotype.getId()));
    }
    
    @Test
    public void testGetQueryPhenotypeIdForNullInstance() {
        instance = new PhenotypeMatch(null, matchPhenotype, simJ, score, lcs);
        assertThat(instance.getQueryPhenotypeId(), equalTo("null"));
    }
    
    @Test
    public void testGetQueryPhenotype() {
        assertThat(instance.getQueryPhenotype(), equalTo(queryPhenotype));
    }

    @Test
    public void testGetMatchPhenotypeId() {
        assertThat(instance.getMatchPhenotypeId(), equalTo(matchPhenotype.getId()));
    }
    
    @Test
    public void testGetMatchPhenotypeIdForNullInstance() {
        instance = new PhenotypeMatch(queryPhenotype, null, simJ, score, lcs);
        assertThat(instance.getMatchPhenotypeId(), equalTo("null"));
    }
    
    @Test
    public void testGetMatchPhenotype() {
        assertThat(instance.getMatchPhenotype(), equalTo(matchPhenotype));
    }

    @Test
    public void testGetLcs() {
        assertThat(instance.getLcs(), equalTo(lcs));
    }

    @Test
    public void testGetSimJ() {
        assertThat(instance.getSimJ(), equalTo(simJ));
    }
    
    @Test
    public void testGetScore() {
        assertThat(instance.getScore(), equalTo(score));
    }
    
    @Test
    public void testHashCode() {
        assertThat(instance.hashCode(), equalTo(instance.hashCode()));
    }

    @Test
    public void testEquals() {
        assertThat(instance, equalTo(instance));
    }

    @Test
    public void testToString() {
        assertThat(instance.toString().isEmpty(), is(false));
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
