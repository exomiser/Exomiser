package org.monarchinitiative.exomiser.core.phenotype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class implementing the Phenodigm (PHENOtype comparisons for DIsease Genes and Models) algorithm for scoring the
 * semantic similarity of a model against the best theoretical model for a set of phenotypes in a given organism.
 * See original publication here - https://doi.org/10.1093/database/bat025
 *
 * @since 8.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenodigmModelScorer implements ModelScorer {

    private static final Logger logger = LoggerFactory.getLogger(PhenodigmModelScorer.class);

    private final double theoreticalMaxMatchScore;
    private final double theoreticalBestAvgScore;

    private final PhenotypeMatcher organismPhenotypeMatcher;
    private final int numQueryPhenotypes;

    /**
     * Use this constructor when running a single (HP-HP) or single cross-species (e.g. HP-MP) comparisons.
     * For multi cross-species comparisons use the constructor which requires the {@link QueryPhenotypeMatch} against which
     * all models are compared.
     *
     * @param organismPhenotypeMatcher the best phenotype matches for this organism e.g. HP-HP, HP-MP or HP-MP
     * @param numQueryPhenotypes
     */
    private PhenodigmModelScorer(PhenotypeMatcher organismPhenotypeMatcher, int numQueryPhenotypes) {
        this(organismPhenotypeMatcher.getQueryPhenotypeMatch(), organismPhenotypeMatcher, numQueryPhenotypes);
    }

    /**
     * Use this constructor when running multi cross-species comparisons. For single or single cross-species comparisons,
     * use the alternate constructor.
     *
     * @param queryPhenotypeMatch against which all models are compared.
     * @param organismPhenotypeMatcher the best phenotype matches for this organism e.g. HP-HP, HP-MP or HP-MP
     * @param numQueryPhenotypes
     */
    private PhenodigmModelScorer(QueryPhenotypeMatch queryPhenotypeMatch, PhenotypeMatcher organismPhenotypeMatcher, int numQueryPhenotypes) {
        this.theoreticalMaxMatchScore = queryPhenotypeMatch.getMaxMatchScore();
        this.theoreticalBestAvgScore = queryPhenotypeMatch.getBestAvgScore();

        this.organismPhenotypeMatcher = organismPhenotypeMatcher;
        this.numQueryPhenotypes = numQueryPhenotypes;
        logOrganismPhenotypeMatches();
    }

    /**
     * Produces a {@link ModelScorer} which will score human models only, e.g. disease models or individuals where
     * their phenotypes are encoded using HPO terms.
     *
     * @param phenotypeMatcher The HP to HP PhenotypeMatches for the query Phenotypes.
     */
    public static PhenodigmModelScorer forSameSpecies(PhenotypeMatcher phenotypeMatcher) {
        int numQueryPhenotypes = phenotypeMatcher.getQueryTerms().size();
        return new PhenodigmModelScorer(phenotypeMatcher, numQueryPhenotypes);
    }

    /**
     * Produces a {@link ModelScorer} which will score models between human and a single other species. Requires an
     * {@link PhenotypeMatcher} for the relevant organism i.e. mouse or fish.
     *
     * @param phenotypeMatcher The HP to MP/ZP PhenotypeMatches for the query Phenotypes.
     */
    public static PhenodigmModelScorer forSingleCrossSpecies(PhenotypeMatcher phenotypeMatcher) {
        int numQueryPhenotypes = phenotypeMatcher.getBestPhenotypeMatches().size();
        return new PhenodigmModelScorer(phenotypeMatcher, numQueryPhenotypes);
    }

    /**
     * Produces a {@link ModelScorer} which will score models across multiple species. This requires a reference
     * species (human) so that the scores are scaled correctly across the species.
     *
     * @param referenceOrganismQueryPhenotypeMatch {@link QueryPhenotypeMatch} for the reference organism - this should be for the HP-HP hits.
     * @param phenotypeMatcher                     the {@link PhenotypeMatcher} for the relevant organism i.e. HP-HP, HP-MP or HP-ZP matches.
     */
    public static PhenodigmModelScorer forMultiCrossSpecies(QueryPhenotypeMatch referenceOrganismQueryPhenotypeMatch, PhenotypeMatcher phenotypeMatcher) {
        int numQueryPhenotypes = referenceOrganismQueryPhenotypeMatch.getQueryTerms().size();
        return new PhenodigmModelScorer(referenceOrganismQueryPhenotypeMatch, phenotypeMatcher, numQueryPhenotypes);
    }

    private void logOrganismPhenotypeMatches() {
        logger.info("Best {} phenotype matches:", organismPhenotypeMatcher.getOrganism());
        Map<PhenotypeTerm, Set<PhenotypeMatch>> termPhenotypeMatches = organismPhenotypeMatcher.getTermPhenotypeMatches();
        for (Map.Entry<PhenotypeTerm, Set<PhenotypeMatch>> entry : termPhenotypeMatches.entrySet()) {
            PhenotypeTerm queryTerm = entry.getKey();
            Set<PhenotypeMatch> matches = entry.getValue();
            if (matches.isEmpty()) {
                logger.info("{}-NOT MATCHED", queryTerm.getId());
            } else {
                PhenotypeMatch bestMatch = matches.stream()
                        .max(Comparator.comparingDouble(PhenotypeMatch::getScore))
                        .get();
                logger.info("{}-{}={}", queryTerm.getId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
            }
        }
        QueryPhenotypeMatch organismQueryPhenotypeMatch = organismPhenotypeMatcher.getQueryPhenotypeMatch();
        logger.info("bestMaxScore={} bestAvgScore={}", organismQueryPhenotypeMatch.getMaxMatchScore(), organismQueryPhenotypeMatch
                .getBestAvgScore());
    }

    @Override
    public ModelPhenotypeMatch scoreModel(Model model) {
        PhenodigmMatchRawScore rawModelScore = organismPhenotypeMatcher.matchPhenotypeIds(model.getPhenotypeIds());
        double score = calculateCombinedScore(rawModelScore);
        return ModelPhenotypeMatch.of(score, model, rawModelScore.getBestPhenotypeMatches());
    }

    private double calculateCombinedScore(PhenodigmMatchRawScore rawModelScore) {
        double maxModelMatchScore = rawModelScore.getMaxModelMatchScore();
        double sumModelBestMatchScores = rawModelScore.getSumModelBestMatchScores();
        int numMatchingPhenotypesForModel = rawModelScore.getMatchingPhenotypes().size();

        /*
         * hpIdsWithPhenotypeMatch.size() = no. of HPO disease annotations for human and the no. of annotations with an entry in hp_*_mappings table for other species
         * matchedPhenotypeIDsForModel.size() = no. of annotations for model with a match in hp_*_mappings table for at least one of the disease annotations
         * Aug 2015 - changed calculation to take into account all HPO terms for averaging after DDD benchmarking - keeps consistent across species then
         */
        //int rowColumnCount = hpIdsWithPhenotypeMatch.size() + matchedPhenotypeIdsForModel.size();

        /*
         * In order to have a strict symmetrical comparison all the query and model phenotypes in the HP-MP table should be taken into account (these are the significant matches)
         * so we should be using something close to totalPhenotypes = numQueryPhenotypes + numModelPhenotypes, however in the benchmarking it became apparent that
         * models with large numbers of phenotypes (e.g. 40+) performed badly compared to models with smaller number when matched against a small query. So we have
         * implemented a sort of semi-symmetrical comparison which only takes into account the model terms matching those in the query HP-MP subsets.
         */

        int totalPhenotypesWithMatch = numQueryPhenotypes + numMatchingPhenotypesForModel;
        if (sumModelBestMatchScores > 0) {
            double modelBestAvgScore = sumModelBestMatchScores / totalPhenotypesWithMatch;
            // calculate combined score
            double combinedScore = 50 * (maxModelMatchScore / theoreticalMaxMatchScore + modelBestAvgScore / theoreticalBestAvgScore);
            if (combinedScore > 100) {
                combinedScore = 100;
            }
            return combinedScore / 100;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhenodigmModelScorer that = (PhenodigmModelScorer) o;
        return Double.compare(that.theoreticalMaxMatchScore, theoreticalMaxMatchScore) == 0 &&
                Double.compare(that.theoreticalBestAvgScore, theoreticalBestAvgScore) == 0 &&
                numQueryPhenotypes == that.numQueryPhenotypes &&
                Objects.equals(organismPhenotypeMatcher, that.organismPhenotypeMatcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(theoreticalMaxMatchScore, theoreticalBestAvgScore, organismPhenotypeMatcher, numQueryPhenotypes);
    }

    @Override
    public String toString() {
        return "PhenodigmModelScorer{" +
                "theoreticalMaxMatchScore=" + theoreticalMaxMatchScore +
                ", theoreticalBestAvgScore=" + theoreticalBestAvgScore +
                ", organismPhenotypeMatcher=" + organismPhenotypeMatcher +
                ", numQueryPhenotypes=" + numQueryPhenotypes +
                '}';
    }
}
