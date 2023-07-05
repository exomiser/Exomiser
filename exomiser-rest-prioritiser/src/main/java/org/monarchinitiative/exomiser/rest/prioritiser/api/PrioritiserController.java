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

import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
public class PrioritiserController {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserController.class);

    private final Map<Integer, GeneIdentifier> geneIdentifiers;
    private final PriorityFactory priorityFactory;

    @Autowired
    public PrioritiserController(Map<Integer, GeneIdentifier> geneIdentifiers, PriorityFactory priorityFactory) {
        this.geneIdentifiers = geneIdentifiers;
        this.priorityFactory = priorityFactory;
        logger.info("Started PrioritiserController with GeneIdentifier cache of {} entries", geneIdentifiers.size());
    }

    @GetMapping(value = "/about")
    public String about() {
        byte[] bytes = new byte[0];
        try {
            bytes = new ClassPathResource("about.html").getInputStream().readAllBytes();
        } catch (IOException e) {
            logger.error("", e);
        }
        return new String(bytes);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PrioritiserResultSet prioritise(@RequestParam(value = "phenotypes") Set<String> phenotypes,
                                           @RequestParam(value = "genes", required = false, defaultValue = "") Set<Integer> genesIds,
                                           @RequestParam(value = "prioritiser") String prioritiserName,
                                           @RequestParam(value = "prioritiser-params", required = false, defaultValue = "") String prioritiserParams,
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

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PrioritiserResultSet prioritise(@RequestBody PrioritiserRequest prioritiserRequest) {
        logger.info("{}", prioritiserRequest);

        Instant start = Instant.now();

        Prioritiser<? extends PriorityResult> prioritiser = parsePrioritiser(prioritiserRequest.getPrioritiser(), prioritiserRequest
                .getPrioritiserParams());
        List<Gene> genes = makeGenesFromIdentifiers(prioritiserRequest.getGenes());

        List<PriorityResult> results = runLimitAndCollectResults((Prioritiser<PriorityResult>) prioritiser, prioritiserRequest.getPhenotypes(), genes, prioritiserRequest
                .getLimit())
                .toList();

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        return new PrioritiserResultSet(prioritiserRequest, duration.toMillis(), results);
    }

    private Prioritiser<? extends PriorityResult> parsePrioritiser(String prioritiserName, String prioritiserParams) {
        switch (prioritiserName) {
            case "phenix" -> {
                return priorityFactory.makePhenixPrioritiser();
            }
            case "phive" -> {
                return priorityFactory.makePhivePrioritiser();
            }
            default -> {
                HiPhiveOptions hiPhiveOptions = HiPhiveOptions.builder()
                        .runParams(prioritiserParams)
                        .build();
                return priorityFactory.makeHiPhivePrioritiser(hiPhiveOptions);
            }
        }
    }

    private List<Gene> makeGenesFromIdentifiers(Collection<Integer> genesIds) {
        if (genesIds.isEmpty()) {
            logger.info("Gene identifiers not specified - will compare against all known genes.");
            //If not specified, we'll assume they want to use the whole genome. Should save people a lot of typing.
            //n.b. Gene is mutable so these can't be cached and returned.
            return allGenes();
        }
        // This is a hack - really the Prioritiser should only work on GeneIds, but currently this isn't possible as
        // OmimPrioritiser uses some properties of Gene
        return genesIds.stream()
                .map(id -> new Gene(geneIdentifiers.getOrDefault(id, unrecognisedGeneIdentifier(id))))
                .toList();
    }

    private List<Gene> allGenes() {
        return geneIdentifiers.values().parallelStream()
                .map(Gene::new)
                .toList();
    }

    private GeneIdentifier unrecognisedGeneIdentifier(Integer id) {
        return GeneIdentifier.builder().geneSymbol("GENE:" + id).build();
    }

    private Stream<PriorityResult> runLimitAndCollectResults(Prioritiser<PriorityResult> prioritiser, List<String> phenotypes, List<Gene> genes, int limit) {
        Set<Integer> wantedGeneIds = genes.stream().map(Gene::getEntrezGeneID).collect(Collectors.toSet());
        Sample sample = Sample.builder().hpoIds(phenotypes).build();
        Stream<PriorityResult> resultsStream = prioritiser.prioritise(sample, genes)
                .filter(result -> wantedGeneIds.contains(result.getGeneId()))
                .sorted(Comparator.naturalOrder());

        logger.info("Finished {}", prioritiser.getPriorityType());
        if (limit == 0) {
            return resultsStream;
        }
        return resultsStream.limit(limit);
    }

}
