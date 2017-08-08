/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.rest.analysis.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.PathogenicityFilter;
import org.monarchinitiative.exomiser.core.filters.PriorityScoreFilter;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.rest.analysis.ExomiserAnalysisServer;
import org.monarchinitiative.exomiser.rest.analysis.model.AnalysisResponse;
import org.monarchinitiative.exomiser.test.ExomiserStubDataConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExomiserAnalysisServer.class, ExomiserStubDataConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnalysisControllerTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private String jsonAnalysis = "";

    private ObjectMapper mapper;

    @Before
    public void setup() throws IOException{
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Analysis analysis = Analysis.builder()
                .hpoIds(Arrays.asList("HP:000001", "HP:000002"))
                .frequencySources(FrequencySource.ALL_ESP_SOURCES)
                .pathogenicitySources(EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.CADD))
                //TODO: get these to de/serialise
                .addStep(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501f))
                .addStep(new FrequencyFilter(1.0f))
                .addStep(new PathogenicityFilter(true))
                .build();

        mapper = new ObjectMapper();
        mapper.registerModule(new Jdk7Module());
        mapper.registerModule(new MyModule());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
//        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
//        mapper.addMixIn(VariantFilter.class, VariantFilterMixIn.class);
        //TODO: need to write custom AnalysisMapper.
//        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.EXISTING_PROPERTY); // all non-final types

        try {
            jsonAnalysis = mapper.writeValueAsString(analysis);
        } catch (JsonProcessingException ex) {
        }
        System.out.println("Created json analysis:\n" + jsonAnalysis);
    }

    @JsonComponent
    public class MyModule extends SimpleModule {
        public MyModule() {
            super();
        }

        @Override
        public void setupModule(SetupContext context) {
//            context.setMixInAnnotations(VariantFilter.class, VariantFilterMixIn.class);
            // and other set up, if any
        }
    }

    abstract class VariantFilterMixIn {
        VariantFilterMixIn(){}

        @JsonIgnore abstract FilterType getFilterType();

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
    public void testGetAnalysis_unKnownAnalysisId() throws Exception {
        mockMvc.perform(get("/analysis/{analysisId}", 1).accept("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAnalysis_knownAnalysisId() throws Exception {

        long analysisId = 0;

        MvcResult result = mockMvc.perform(post("/analysis")
                .content(jsonAnalysis)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        AnalysisResponse analysisResponse = mapper.readValue(responseBody, AnalysisResponse.class);
        System.out.println("Got " +  analysisResponse);

        mockMvc.perform(get("/analysis/{analysisId}", analysisResponse.getId()).accept("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPostAnalysis_jsonBody() throws Exception {
        mockMvc.perform(post("/analysis")
                .content(jsonAnalysis)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    System.out.println(mvcResult.getResponse().getContentAsString());});
    }

}