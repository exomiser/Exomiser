/*
 * Copyright (C) 2014 jj8
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.controller;

import config.*;
import de.charite.compbio.exomiser.web.config.WebAppConfig;
import de.charite.compbio.exomiser.web.dao.ExomiserDao;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppConfig.class, TestExomiserConfig.class, TestDaoConfig.class})
public class DataControllerTest {
    
    private MockMvc mockMvc;
    
    @Autowired
    private ExomiserDao mockExomiserDao;

    @Autowired
    private WebApplicationContext webApplicationContext;
 
    @Before
    public void setUp() {
        //We have to reset our mock between tests because the mock objects
        //are managed by the Spring container. If we would not reset them,
        //stubbing and verified behavior would "leak" from one test to another.
//        Mockito.reset(todoServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    private void assertOneGruffaloDiseaseOptionIsReturned(String inputTerm) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/data/disease?term=%s", inputTerm)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(content().string("[{\"text\":\"Gruffalo syndrome\",\"value\":\"OMIM:101600\"}]"))
                .andExpect(jsonPath("$[0].text").value("Gruffalo syndrome"))
                .andExpect(jsonPath("$[0].value").value("OMIM:101600"));
    }
   

    @Test
    public void getDiseaseOptionsMatchingStartOfResult() throws Exception {
        String inputTerm = "Gruff";
        assertOneGruffaloDiseaseOptionIsReturned(inputTerm);
    }

    @Test
    public void getDiseaseOptionsAllLowerCaseStillReturnsOption() throws Exception {
        String inputTerm = "gruff";
        assertOneGruffaloDiseaseOptionIsReturned(inputTerm);
    }

    @Test
    public void getDiseaseOptionsMatchingPartOfResult() throws Exception {
        String inputTerm = "ruff";
        assertOneGruffaloDiseaseOptionIsReturned(inputTerm);
    }
    
    @Test
    public void getDiseaseOptionsReturnsAllCommonMatches() throws Exception {
        String inputTerm = "syndrome";
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/data/disease?term=%s", inputTerm)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Gruffalo syndrome"))
                .andExpect(jsonPath("$[0].value").value("OMIM:101600"))
                .andExpect(jsonPath("$[1].text").value("Mouse syndrome"))
                .andExpect(jsonPath("$[1].value").value("OMIM:101200"));
    }

    @Test
    public void getHpoOptionsContainingTermE() throws Exception {
        String inputTerm = "e";
//        hpoTerms.put("HP:0001234", "Purple prickles");
//        hpoTerms.put("HP:5678000", "Knobbly knees");
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/data/hpo?term=%s", inputTerm)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Knobbly knees"))
                .andExpect(jsonPath("$[0].value").value("HP:5678000"))
                .andExpect(jsonPath("$[1].text").value("Purple prickles"))
                .andExpect(jsonPath("$[1].value").value("HP:0001234"))
                ;    
    }
    
    @Test
    public void getHpoOptionsContainingTermPurple() throws Exception {
        String inputTerm = "purple";
//        hpoTerms.put("HP:0001234", "Purple prickles");
//        hpoTerms.put("HP:5678000", "Knobbly knees");
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/data/hpo?term=%s", inputTerm)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Purple prickles"))
                .andExpect(jsonPath("$[0].value").value("HP:0001234"));    
    }
}
