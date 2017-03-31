/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers.phenogrid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchTest {

    private PhenoGridMatch instance;

    private final String id = "OMIM:100000";
    private final String label = "Gruffalo Syndrome";
    private final String type = "disease";

    private List<PhenotypeMatch> phenotypeMatches;
    private PhenoGridMatchScore score;
    private PhenoGridMatchTaxon taxon;
    
    private final TestPhenoGridObjectCache matchCache = TestPhenoGridObjectCache.getInstance();

    @Before
    public void setUp() {
        phenotypeMatches = matchCache.getPhenotypeMatches();

        score = new PhenoGridMatchScore("hiPhive", 99, 0);
        taxon = new PhenoGridMatchTaxon("NCBITaxon:10090", "Gruff gruffulus");

        instance = new PhenoGridMatch(id, label, type, phenotypeMatches, score, taxon);
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
    public void getScore() {
        assertThat(instance.getScore(), equalTo(score));
    }
    
    @Test
    public void getTaxon() {
        assertThat(instance.getTaxon(), equalTo(taxon));
    }
    
    @Test
    public void testGetQueryTermIds() {
        Set queryTermIds = new LinkedHashSet();
        for (PhenotypeMatch phenotypeMatch : phenotypeMatches) {
            PhenotypeTerm queryPhenotype = phenotypeMatch.getQueryPhenotype();
            queryTermIds.add(queryPhenotype.getId());
        }
        assertThat(instance.getQueryTermIds(), equalTo(queryTermIds));
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
