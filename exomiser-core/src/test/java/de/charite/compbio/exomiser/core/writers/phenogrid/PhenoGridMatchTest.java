/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules
 */
public class PhenoGridMatchTest {
    
    private PhenoGridMatch instance;
    
    private String id = "OMIM:100000";
    private String label = "Gruffalo Syndrome";
    private String type = "disease";
    
    private List<PhenotypeMatch> phenotypeMatches;
    
    private PhenotypeMatch fullMatch;
    private PhenotypeTerm knobblyKnee;
    private PhenotypeTerm wobblyKnee;
    private PhenotypeTerm unstableKnee;
        
    private PhenotypeMatch noMatch;
    private PhenotypeTerm purplePrickles;
    
    
    @Before
    public void setUp() {
        phenotypeMatches = new ArrayList<>();

        knobblyKnee = new PhenotypeTerm("GRUF:123", "Knobbly knees", 5.0);
        wobblyKnee = new PhenotypeTerm("GRUF:124", "Wobbly knees", 5.0);
        unstableKnee = new PhenotypeTerm("GRUF:120", "Unstable knees", 3.0);
        fullMatch = new PhenotypeMatch(knobblyKnee, wobblyKnee, 0.9, unstableKnee);
        phenotypeMatches.add(fullMatch);
        
        purplePrickles = new PhenotypeTerm("GRUF:111", "Purple prickles", 4.0);
        noMatch = new PhenotypeMatch(purplePrickles, null, 0.0, null);
        phenotypeMatches.add(noMatch);
        
        instance = new PhenoGridMatch(id, label, type, phenotypeMatches);
    }

    @Test
    public void testGetId() {
        assertThat(instance.getId(), equalTo(id));
    }
 
    @Test
    public void testGetLabel() {
        assertThat(instance.getLabel(), equalTo(label));
    }
    
    @Test
    public void testGetType() {
        assertThat(instance.getType(), equalTo(type));
    }
    
    @Test
    public void getPhenotypeMatches() {
        assertThat(instance.getMatches(), equalTo(phenotypeMatches));
    }
    
    @Test
    public void testJsonWrite() {
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
