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

package org.monarchinitiative.exomiser.rest.prioritiser.api;

import org.monarchinitiative.exomiser.core.genome.GeneFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
public class PrioritiserController {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserController.class);

    private final PriorityFactory priorityFactory;
    private final Map<Integer, GeneIdentifier> geneIdentifiers;

    @Autowired
    public PrioritiserController(PriorityFactory priorityFactory, GeneFactory geneFactory) {
        this.priorityFactory = priorityFactory;
        this.geneIdentifiers = geneFactory.createKnownGeneIds().stream()
                .filter(GeneIdentifier::hasEntrezId)
                .collect(toImmutableMap(GeneIdentifier::getEntrezIdAsInteger, Function.identity()));
        logger.info("Created GeneIdentifier cache with {} entries", geneIdentifiers.size());
    }

    @GetMapping(value = "about")
    public String about() {
        return "This service will return a collection of prioritiser results for any given set of:" +
                "\n\t - HPO identifiers e.g. HPO:00001" +
                "\n\t - Entrez gene identifiers e.g. 23364" +
                "\n\t - Specified prioritiser e.g. hiphive along with any prioritiser specific commands e.g. human,mouse,fish,ppi";
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PrioritiserResultSet prioritise(@RequestParam(value = "phenotypes") List<String> phenotypes,
                                           @RequestParam(value = "genes", required = false, defaultValue = "") List<Integer> genesIds,
                                           @RequestParam(value = "prioritiser") String prioritiserName,
                                           @RequestParam(value = "prioritiser-params", required = false, defaultValue = "") String prioritiserParams,
                                           @RequestParam(value = "limit", required = false, defaultValue = "0") Integer limit
    ) {

        logger.info("phenotypes: {}({}) genes: {} prioritiser: {} prioritiser-params: {}", phenotypes, phenotypes.size(), genesIds, prioritiserName, prioritiserParams);

        Instant start = Instant.now();

        Prioritiser prioritiser = parsePrioritser(prioritiserName, prioritiserParams);
        List<String> uniquePhenotypes = phenotypes.stream().distinct().collect(toImmutableList());
        List<Gene> genes = parseGeneIdentifiers(genesIds);
        List<PriorityResult> results = runLimitAndCollectResults(prioritiser, uniquePhenotypes, genes, limit);

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("phenotypes", phenotypes.toString());
        params.put("genes", genesIds.toString());
        params.put("prioritiser", prioritiserName);
        params.put("prioritiser-params", prioritiserParams);
        params.put("limit", limit.toString());

        return new PrioritiserResultSet(params, duration.toMillis(), results);
    }

    private Prioritiser parsePrioritser(String prioritiserName, String prioritiserParams) {
        switch(prioritiserName) {
            case "phenix":
                return priorityFactory.makePhenixPrioritiser();
            case "phive":
                return priorityFactory.makePhivePrioritiser();
            case "hiphive":
            default:
                HiPhiveOptions hiPhiveOptions = HiPhiveOptions.builder()
                        .runParams(prioritiserParams)
                        .build();
                return priorityFactory.makeHiPhivePrioritiser(hiPhiveOptions);
        }
    }

    private List<Gene> parseGeneIdentifiers(List<Integer> genesIds) {
        if (genesIds.isEmpty()) {
            logger.info("Gene identifiers not specified - will compare against all known genes.");
            //If not specified, we'll assume they want to use the whole genome. Should save people a lot of typing.
            //n.b. Gene is mutable so these can't be cached and returned.
            return geneIdentifiers.values().parallelStream()
                    .map(Gene::new)
                    .collect(toImmutableList());
        }
        // This is a hack - really the Prioritiser should only work on GeneIds, but currently this isn't possible as
        // OmimPrioritiser uses some properties of Gene
        return genesIds.stream()
                .map(id -> new Gene(geneIdentifiers.getOrDefault(id, unrecognisedGeneIdentifier(id))))
                .collect(toImmutableList());
    }

    private GeneIdentifier unrecognisedGeneIdentifier(Integer id) {
        return GeneIdentifier.builder().geneSymbol("GENE:" + id).build();
    }

    private List<PriorityResult> runLimitAndCollectResults(Prioritiser prioritiser, List<String> phenotypes, List<Gene> genes, int limit) {
        Stream<? extends PriorityResult> resultsStream = prioritiser.prioritise(phenotypes, genes)
                .sorted(Comparator.naturalOrder());
        logger.info("Finished {}", prioritiser.getPriorityType());
        if (limit == 0) {
            return resultsStream.collect(toImmutableList());
        }
        return resultsStream.limit(limit).collect(toImmutableList());
    }

}
