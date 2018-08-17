/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
