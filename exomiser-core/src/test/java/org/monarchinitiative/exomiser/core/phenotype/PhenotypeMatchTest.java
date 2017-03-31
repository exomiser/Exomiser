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
package org.monarchinitiative.exomiser.core.phenotype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenotypeMatchTest {
    
    private PhenotypeMatch instance;
    
    private PhenotypeTerm lcs;
    private PhenotypeTerm queryPhenotype;
    private PhenotypeTerm matchPhenotype;
    private double ic;
    private double simJ;
    private double score;
    
    @Before
    public void setUp() {
        lcs = PhenotypeTerm.of("ID:12345", "nose");
        queryPhenotype = PhenotypeTerm.of("ID:12344", "big nose");
        matchPhenotype = PhenotypeTerm.of("ID:12355", "little nose");
        ic = 1.00;
        simJ = 0.8;
        score = 1.26;

        instance = PhenotypeMatch.builder().query(queryPhenotype).match(matchPhenotype).lcs(lcs).ic(ic).simj(simJ).score(score).build();
    }

    @Test
    public void testGetQueryPhenotypeId() {
        assertThat(instance.getQueryPhenotypeId(), equalTo(queryPhenotype.getId()));
    }
    
    @Test
    public void testGetQueryPhenotypeIdForNullInstance() {
        instance = PhenotypeMatch.builder().query(null).match(matchPhenotype).lcs(lcs).ic(ic).simj(simJ).score(score).build();
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
        instance = PhenotypeMatch.builder().query(queryPhenotype).match(null).lcs(lcs).ic(ic).simj(simJ).score(score).build();
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
    public void testGetIc() {
        assertThat(instance.getIc(), equalTo(ic));
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
