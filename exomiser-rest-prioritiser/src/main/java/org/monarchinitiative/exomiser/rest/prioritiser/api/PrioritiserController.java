/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.rest.prioritiser.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.monarchinitiative.exomiser.rest.prioritiser.service.PrioritiserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
@RequestMapping("api/v1")
@Tag(name = "Prioritiser", description = "API endpoints for phenotype-based gene prioritisation")
public class PrioritiserController {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserController.class);

    private final PrioritiserService prioritiserService;

    @Autowired
    public PrioritiserController(PrioritiserService prioritiserService) {
        this.prioritiserService = prioritiserService;
    }

    @Operation(
            summary = "Prioritise genes by phenotype",
            description = "Prioritises genes based on provided phenotypes and other parameters"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully prioritised genes",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PrioritiserResultSet.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input parameters"
            )
    })
    @GetMapping(value = "prioritise", produces = MediaType.APPLICATION_JSON_VALUE)
    public PrioritiserResultSet prioritise(
            @Parameter(
                    description = "Set of HPO phenotype identifiers",
                    example = "[\"HP:0001156\", \"HP:0001363\", \"HP:0011304\", \"HP:0010055\"]",
                    required = true
            )
            @RequestParam(value = "phenotypes") Set<String> phenotypes,

            @Parameter(
                    description = "Set of NCBI gene IDs to consider in prioritisation",
                    example = "[2263, 2264]",
                    required = false
            )
            @RequestParam(value = "genes", required = false, defaultValue = "") Set<Integer> genesIds,

            @Parameter(
                    description = "Name of the prioritiser algorithm to use. One of ['hiphive', 'phenix', 'phive']",
                    example = "hiphive",
                    required = true
            )
            @RequestParam(value = "prioritiser") String prioritiserName,

            @Parameter(
                    description = "Additional parameters for the prioritiser. This is optional for the 'hiphive' prioritiser." +
                                  " values can be at least one of 'human,mouse,fish,ppi'. Will default to all, however" +
                                  " just 'human' will restrict matches to known human disease-gene associations.",
                    example = "human",
                    required = false
            )
            @RequestParam(value = "prioritiser-params", required = false, defaultValue = "") String prioritiserParams,

            @Parameter(
                    description = "Maximum number of results to return (0 for unlimited)",
                    required = false,
                    example = "20"
            )
            @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit
    ) {
        PrioritiserRequest prioritiserRequest = PrioritiserRequest.builder()
                .prioritiser(prioritiserName)
                .prioritiserParams(prioritiserParams)
                .genes(genesIds)
                .phenotypes(phenotypes)
                .limit(limit)
                .build();

        return prioritise(prioritiserRequest);
    }

    @Operation(
            summary = "Prioritise genes using POST request",
            description = "Prioritises genes based on provided request body containing phenotypes and configuration"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully prioritised genes",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PrioritiserResultSet.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body"
            )
    })
    @PostMapping(
            value = "prioritise",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PrioritiserResultSet prioritise(
            @Parameter(
                    description = "Prioritisation request parameters",
                    required = true
            )
            @RequestBody PrioritiserRequest prioritiserRequest
    ) {
        return prioritiserService.prioritise(prioritiserRequest);
    }

}
