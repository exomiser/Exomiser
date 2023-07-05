/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.HiPhiveProteinInteractionScorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Filter genes according phenotypic similarity and to the random walk proximity
 * in the protein-protein interaction network.
 *
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriority implements Prioritiser<HiPhivePriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(HiPhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.HIPHIVE_PRIORITY;
    private static final double HIGH_QUALITY_SCORE_CUTOFF = 0.6;

    private final HiPhiveOptions options;
    private final DataMatrix randomWalkMatrix;
    private final PriorityService priorityService;

    /**
     * @param options
     * @param randomWalkMatrix
     */
    public HiPhivePriority(HiPhiveOptions options, DataMatrix randomWalkMatrix, PriorityService priorityService) {
        this.options = options;
        this.randomWalkMatrix = randomWalkMatrix;
        this.priorityService = priorityService;
    }

    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    public HiPhiveOptions getOptions() {
        return options;
    }

    @Override
    public Stream<HiPhivePriorityResult> prioritise(Sample sample, List<Gene> genes) {
        List<String> hpoIds = sample.getHpoIds();
        if (options.isBenchmarkingEnabled()) {
            logger.debug("Running in benchmarking mode for disease: {} and candidateGene: {}", options.getDiseaseId(), options
                    .getCandidateGeneSymbol());
        }
        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        ListMultimap<Integer, GeneModelPhenotypeMatch> allScoredModelsByGene = makeGeneModelsForOrganisms(hpoPhenotypeTerms, options
                .getOrganismsToRun(), genes);

        HiPhiveProteinInteractionScorer ppiScorer = makeHiPhiveProteinInteractionScorer(allScoredModelsByGene, options.runPpi());

        logger.debug("Prioritising genes...");
        return genes.stream().map(makeHiPhivePriorityResult(hpoPhenotypeTerms, allScoredModelsByGene, ppiScorer));
    }

    private Function<Gene, HiPhivePriorityResult> makeHiPhivePriorityResult(List<PhenotypeTerm> hpoPhenotypeTerms, ListMultimap<Integer, GeneModelPhenotypeMatch> allScoredModelsByGene, HiPhiveProteinInteractionScorer ppiScorer) {
        return gene -> {
            Integer entrezGeneId = gene.getEntrezGeneID();
            String geneSymbol = gene.getGeneSymbol();

            List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = allScoredModelsByGene.get(entrezGeneId);

            double phenoScore = getMaxGenePhenoScore(geneModelPhenotypeMatches);

            GeneMatch closestPhenoMatchInNetwork = ppiScorer.getClosestPhenoMatchInNetwork(entrezGeneId);
            List<GeneModelPhenotypeMatch> closestPhysicallyInteractingGeneModels = closestPhenoMatchInNetwork.getBestMatchModels();
            double ppiScore = closestPhenoMatchInNetwork.getScore();

            double score = Double.max(phenoScore, ppiScore);

            logger.debug("Making result for {} {} score={} phenoScore={} walkerScore={}", geneSymbol, entrezGeneId, score, phenoScore, ppiScore);
            return new HiPhivePriorityResult(entrezGeneId, geneSymbol, score, hpoPhenotypeTerms, geneModelPhenotypeMatches, closestPhysicallyInteractingGeneModels, ppiScore, matchesCandidateGeneSymbol(geneSymbol));
        };
    }

    private double getMaxGenePhenoScore(List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches) {
        double phenoScore = 0;
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : geneModelPhenotypeMatches) {
            phenoScore = Double.max(phenoScore, geneModelPhenotypeMatch.getScore());
        }
        return phenoScore;
    }

    private boolean matchesCandidateGeneSymbol(String geneSymbol) {
        //new Jannovar labelling can have multiple genes per var but first one is most pathogenic- we'll take this one.
        return options.getCandidateGeneSymbol().equals(geneSymbol) || geneSymbol.startsWith(options.getCandidateGeneSymbol() + ",");
    }

    private HiPhiveProteinInteractionScorer makeHiPhiveProteinInteractionScorer(ListMultimap<Integer, GeneModelPhenotypeMatch> allScoredModelsByGene, boolean runPpi) {
        if (runPpi) {
            logger.debug("Creating PPI scorer ");
            ListMultimap<Integer, GeneModelPhenotypeMatch> bestGeneModels = ArrayListMultimap.create();
            Multimaps.asMap(allScoredModelsByGene).forEach((integer, geneModelPhenotypeMatches) -> {
                for (Organism organism : options.getOrganismsToRun()) {
                    geneModelPhenotypeMatches.stream()
                            .filter(geneModelPhenotypeMatch -> geneModelPhenotypeMatch.getOrganism() == organism)
                            .max(Comparator.comparing(GeneModelPhenotypeMatch::getScore))
                            .ifPresent(geneModelPhenotypeMatch -> bestGeneModels.put(integer, geneModelPhenotypeMatch));
                }
            });
            return new HiPhiveProteinInteractionScorer(randomWalkMatrix, bestGeneModels, HIGH_QUALITY_SCORE_CUTOFF);
        }
        return HiPhiveProteinInteractionScorer.empty();
    }

    private ListMultimap<Integer, GeneModelPhenotypeMatch> makeGeneModelsForOrganisms(List<PhenotypeTerm> hpoPhenotypeTerms, Set<Organism> organismsToCompare, List<Gene> genes) {

        //CAUTION!! this must always run in order that the best score is set - HUMAN runs first as we are comparing HP to other phenotype ontology terms.
        PhenotypeMatcher referenceOrganismPhenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(hpoPhenotypeTerms, Organism.HUMAN);
        QueryPhenotypeMatch referenceQueryPhenotypeMatch = referenceOrganismPhenotypeMatcher.getQueryPhenotypeMatch();
        if (referenceQueryPhenotypeMatch.getBestPhenotypeMatches().isEmpty()) {
            logger.warn("{} has no phenotype matches for input set {}", referenceQueryPhenotypeMatch, hpoPhenotypeTerms);
        }
        List<PhenotypeMatcher> phenotypeMatchers = createPhenotypeMatchers(hpoPhenotypeTerms, referenceOrganismPhenotypeMatcher, organismsToCompare);

        ListMultimap<Integer, GeneModelPhenotypeMatch> scoredModelsByGene = ArrayListMultimap.create();
        Set<Integer> wantedGeneIds = genes.stream().map(Gene::getEntrezGeneID).collect(toUnmodifiableSet());
        for (PhenotypeMatcher organismPhenotypeMatcher : phenotypeMatchers) {
            Set<GeneModel> modelsToScore = priorityService.getModelsForOrganism(organismPhenotypeMatcher.getOrganism())
                    .stream()
                    // remove known disease-gene models for purposes of benchmarking i.e to simulate novel gene discovery performance
                    .filter(removeBenchmarkingModels())
                    .filter(model -> wantedGeneIds.contains(model.getEntrezGeneId()))
                    .collect(toUnmodifiableSet());

            List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = scoreModels(referenceQueryPhenotypeMatch, organismPhenotypeMatcher, modelsToScore);
            for (GeneModelPhenotypeMatch scoredModel : geneModelPhenotypeMatches) {
                if (scoredModel.getScore() > 0) {
                    scoredModelsByGene.put(scoredModel.getEntrezGeneId(), scoredModel);
                }
            }
        }
        return scoredModelsByGene;
    }

    private List<PhenotypeMatcher> createPhenotypeMatchers(List<PhenotypeTerm> hpoPhenotypeTerms, PhenotypeMatcher referenceOrganismPhenotypeMatcher, Set<Organism> organismsToCompare) {
        List<PhenotypeMatcher> phenotypeMatchers = new ArrayList<>();
        for (Organism organism : organismsToCompare) {
            if (organism == referenceOrganismPhenotypeMatcher.getOrganism()) {
                //no need to re-query the database for these
                phenotypeMatchers.add(referenceOrganismPhenotypeMatcher);
            } else {
                PhenotypeMatcher phenotypeMatcher = priorityService.getPhenotypeMatcherForOrganism(hpoPhenotypeTerms, organism);
                phenotypeMatchers.add(phenotypeMatcher);
            }
        }
        return List.copyOf(phenotypeMatchers);
    }

    private Predicate<GeneModel> removeBenchmarkingModels() {
        return geneModel -> !options.isBenchmarkingModel(geneModel);
    }

    // n.b. this is *almost* identical to PhivePriority.scoreModels() the only difference is in HiPhive we're comparing the input terms
    // against all possible models (disease, mouse, fish), whereas in Phive we're only comparing against mouse.
    // For HiPhive the referenceQueryPhenotypeMatch is going to be an HPO self-hit for every term in the query set so the
    // scoreModelPhenotypeMatch uses hpoIds.size() as the numMatchedQueryPhenotypes.
    private List<GeneModelPhenotypeMatch> scoreModels(QueryPhenotypeMatch referenceQueryPhenotypeMatch, PhenotypeMatcher organismPhenotypeMatcher, Collection<GeneModel> models) {
        Organism organism = organismPhenotypeMatcher.getOrganism();

        ModelScorer<GeneModel> modelScorer = PhenodigmModelScorer.forMultiCrossSpecies(referenceQueryPhenotypeMatch, organismPhenotypeMatcher);

        logger.debug("Scoring {} models", organism);
        Instant timeStart = Instant.now();
        //running this in parallel here can cut the overall time for this method in half or better - ~650ms -> ~350ms on Pfeiffer test set.
        List<GeneModelPhenotypeMatch> geneModelPhenotypeMatches = models.parallelStream()
                .map(modelScorer::scoreModel)
                // TODO why have a GeneModelPhenotypeMatch? It's simply a ModelPhenotypeMatch<GeneModel>
                .map(GeneModelPhenotypeMatch::new)
                .collect(toUnmodifiableList());

        Duration duration = Duration.between(timeStart, Instant.now());
        logger.debug("Scored {} {} models - {} ms", models.size(), organism, duration.toMillis());
        return geneModelPhenotypeMatches;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.randomWalkMatrix);
        hash = 73 * hash + Objects.hashCode(this.options);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HiPhivePriority other = (HiPhivePriority) obj;
        return Objects.equals(this.randomWalkMatrix, other.randomWalkMatrix) && Objects.equals(this.options, other.options);
    }

    @Override
    public String toString() {
        return "HiPhivePriority{" +
                "options=" + options +
                '}';
    }
}
