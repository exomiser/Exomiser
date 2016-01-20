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

package de.charite.compbio.exomiser.rest.analysis.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.rest.analysis.ExomiserConfig;
import de.charite.compbio.exomiser.rest.analysis.ExomiserRestAnalysisApplication;
import org.codehaus.groovy.tools.shell.IO;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ExomiserRestAnalysisApplication.class, ExomiserConfig.class})
public class AnalysisControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setup() throws IOException{
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetAnalysis_noAnalysisId() throws Exception {
        mockMvc.perform(get("/analysis"))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> mvcResult
                        .getResponse()
                        .getContentAsString()
                        .contentEquals("You can upload an analysis YAML file by posting to this URL."));
    }

    @Test
    public void testGetAnalysis_knownAnalysisId() throws Exception {
        //minimal faked response kind of a test
        mockMvc.perform(get("/analysis/{analysisId}", 1).accept("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPostAnalysis_jsonBody() throws Exception {
        Analysis analysis = new Analysis();
        analysis.setFrequencySources(FrequencySource.ALL_ESP_SOURCES);
        analysis.setPathogenicitySources(EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.CADD));
        //TODO: get these to serialise
//        analysis.addStep(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501f));
//        analysis.addStep(new FrequencyFilter(1.0f));
//        analysis.addStep(new PathogenicityFilter(true));

        ObjectMapper mapper = new ObjectMapper();
        //required for correct output of Path types
        mapper.registerModule(new Jdk7Module());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        String jsonSettings = "";
        try {
            jsonSettings = mapper.writeValueAsString(analysis);
        } catch (JsonProcessingException ex) {
//            logger.error("Unable to process JSON settings", ex);
        }

        mockMvc.perform(post("/analysis")
                .content(jsonSettings)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    System.out.println(mvcResult.getResponse().getContentAsString());});
    }

}