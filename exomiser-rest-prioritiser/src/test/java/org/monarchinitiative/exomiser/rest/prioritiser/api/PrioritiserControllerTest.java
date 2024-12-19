package org.monarchinitiative.exomiser.rest.prioritiser.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.rest.prioritiser.service.PrioritiserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrioritiserController.class)
class PrioritiserControllerTest {

    private static final String API_V_1_PRIORITISE_GENE = "/api/v1/prioritise/gene";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrioritiserService prioritiserService;

    private PrioritiserResultSet sampleResultSet;
    private PrioritiserRequest sampleRequest;

    @BeforeEach
    void setUp() {
        // Create sample request data
        sampleRequest = new PrioritiserRequest(
                List.of("HP:0001250", "HP:0001251"),
                List.of(1234, 5678),
                "hiphive",
                "human,mouse,ppi",
                10
        );

        // Create sample response data
        List<PriorityResult> results = List.of(
                new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, 1234, "", 0.95),
                new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, 5678, "", 0.85)
        );

        sampleResultSet = new PrioritiserResultSet(
                sampleRequest,
                100L, // queryTime
                results
        );
    }

    private record MockPriorityResult(PriorityType priorityType, int geneId, String geneSymbol,
                                      double score) implements PriorityResult {
        @Override
        public int getGeneId() {
            return geneId;
        }

        @Override
        public String getGeneSymbol() {
            return geneSymbol;
        }

        @Override
        public double getScore() {
            return score;
        }

        @Override
        public PriorityType getPriorityType() {
            return priorityType;
        }
    }

    @Nested
    @DisplayName("GET prioritisation endpoint tests")
    class GetPrioritisationTests {

        @Test
        @DisplayName("Should return prioritised results with valid parameters")
        void shouldReturnPrioritisedResults() throws Exception {
            when(prioritiserService.prioritiseGenes(any()))
                    .thenReturn(sampleResultSet);

            mockMvc.perform(get(API_V_1_PRIORITISE_GENE)
                            .param("phenotypes", "HP:0001250,HP:0001251")
                            .param("genes", "1234,5678")
                            .param("prioritiser", "phenix")
                            .param("prioritiser-params", "{\"key\":\"value\"}")
                            .param("limit", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(2)))
                    .andExpect(jsonPath("$.results[0].geneId", is(1234)))
                    .andExpect(jsonPath("$.results[0].score", is(0.95)))
                    .andExpect(jsonPath("$.queryTime", is(100)));
        }

        @Test
        @DisplayName("Should handle missing optional parameters")
        void shouldHandleMissingOptionalParams() throws Exception {
            when(prioritiserService.prioritiseGenes(any()))
                    .thenReturn(sampleResultSet);

            mockMvc.perform(get(API_V_1_PRIORITISE_GENE)
                            .param("phenotypes", "HP:0001250")
                            .param("prioritiser", "phenix")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results").exists());
        }

        @Test
        @DisplayName("Should return 400 when required parameters are missing")
        void shouldReturn400WhenMissingRequiredParams() throws Exception {
            mockMvc.perform(get(API_V_1_PRIORITISE_GENE)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST prioritisation endpoint tests")
    class PostPrioritisationTests {

        @Test
        @DisplayName("Should process valid POST request")
        void shouldProcessValidPostRequest() throws Exception {
            when(prioritiserService.prioritiseGenes(any(PrioritiserRequest.class)))
                    .thenReturn(sampleResultSet);

            mockMvc.perform(post(API_V_1_PRIORITISE_GENE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.params.phenotypes", hasSize(2)))
                    .andExpect(jsonPath("$.results", hasSize(2)))
                    .andExpect(jsonPath("$.queryTime", is(100)));
        }

        @Test
        @DisplayName("Should handle POST request with minimal required fields")
        void shouldHandleMinimalPostRequest() throws Exception {
            PrioritiserRequest minimalRequest = new PrioritiserRequest(
                    List.of("HP:0001250"),
                    List.of(),
                    "hiphive",
                    "",
                    0
            );

            when(prioritiserService.prioritiseGenes(any(PrioritiserRequest.class)))
                    .thenReturn(new PrioritiserResultSet(minimalRequest, 50L, List.of()));

            mockMvc.perform(post(API_V_1_PRIORITISE_GENE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(minimalRequest))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.params.phenotypes", hasSize(1)))
                    .andExpect(jsonPath("$.results", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 400 for invalid request body")
        void shouldReturn400ForInvalidRequestBody() throws Exception {
            String invalidJson = "{\"phenotypes\": null, \"prioritiser\": null}";

            mockMvc.perform(post(API_V_1_PRIORITISE_GENE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty results")
        void shouldHandleEmptyResults() throws Exception {
            PrioritiserResultSet emptyResultSet = new PrioritiserResultSet(
                    sampleRequest,
                    50L,
                    List.of()
            );

            when(prioritiserService.prioritiseGenes(any(PrioritiserRequest.class)))
                    .thenReturn(emptyResultSet);

            mockMvc.perform(post(API_V_1_PRIORITISE_GENE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.results", hasSize(0)));
        }
    }

    @Disabled
    @Nested
    @DisplayName("OpenAPI documentation endpoint tests")
    class OpenApiEndpointTests {

        @Test
        @DisplayName("Should serve OpenAPI documentation JSON")
        void shouldServeOpenApiDocs() throws Exception {
            mockMvc.perform(get("/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.openapi", is("3.0.1")))
                    .andExpect(jsonPath("$.info.title", is("Prioritiser")))
                    .andExpect(jsonPath("$.paths.api.v1", notNullValue()))
                    .andExpect(jsonPath("$.paths.api.v1.prioritise", notNullValue()))
            ;
        }

        @Test
        @DisplayName("Should serve Swagger UI page")
        void shouldServeSwaggerUi() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/html"))
                    .andExpect(content().string(containsString("swagger-ui")));
        }
    }

}