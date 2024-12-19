package org.monarchinitiative.exomiser.rest.prioritiser.service;

import io.swagger.v3.oas.annotations.Parameter;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.rest.prioritiser.api.PrioritiserRequest;
import org.monarchinitiative.exomiser.rest.prioritiser.api.PrioritiserResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public record PrioritiserService(Map<Integer, GeneIdentifier> geneIdentifiers, PriorityFactory priorityFactory) {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserService.class);

    public PrioritiserService {
        logger.info("Started PrioritiserService with GeneIdentifier cache of {} entries", geneIdentifiers.size());
    }

    public PrioritiserResultSet prioritise(PrioritiserRequest prioritiserRequest){
        logger.info("{}", prioritiserRequest);

        Instant start = Instant.now();

        Prioritiser<? extends PriorityResult> prioritiser = parsePrioritiser(prioritiserRequest.prioritiser(), prioritiserRequest
                .prioritiserParams());
        List<Gene> genes = makeGenesFromIdentifiers(prioritiserRequest.genes());

        List<PriorityResult> results = runLimitAndCollectResults(prioritiser, prioritiserRequest.phenotypes(), genes, prioritiserRequest
                .limit());

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        return new PrioritiserResultSet(prioritiserRequest, duration.toMillis(), results);
    }

    private Prioritiser<? extends PriorityResult> parsePrioritiser(String prioritiserName, String prioritiserParams) {
        return switch (prioritiserName) {
            case "phenix" -> priorityFactory.makePhenixPrioritiser();
            case "phive" -> priorityFactory.makePhivePrioritiser();
            default -> {
                HiPhiveOptions hiPhiveOptions = HiPhiveOptions.builder()
                        .runParams(prioritiserParams)
                        .build();
                yield priorityFactory.makeHiPhivePrioritiser(hiPhiveOptions);
            }
        };
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

    @SuppressWarnings("unchecked")
    private <T extends PriorityResult> List<PriorityResult> runLimitAndCollectResults(Prioritiser<T> prioritiser, List<String> phenotypes, List<Gene> genes, int limit) {
        Set<Integer> wantedGeneIds = genes.stream().map(Gene::getEntrezGeneID).collect(Collectors.toSet());

        Stream<T> resultsStream = prioritiser.prioritise(phenotypes, genes)
                .filter(result -> wantedGeneIds.contains(result.getGeneId()))
                .sorted(Comparator.naturalOrder());

        return limit == 0 ? (List<PriorityResult>) resultsStream.toList() : (List<PriorityResult>) resultsStream.limit(limit).toList();
    }

}
