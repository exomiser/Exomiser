package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.monarchinitiative.exomiser.core.model.Model;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@FunctionalInterface
public interface ModelScorer {

    ModelPhenotypeMatchScore scoreModel(Model model);

    /**
     * Produces a {@link ModelScorer} which will score human models only, e.g. disease models or individuals where
     * their phenotypes are encoded using HPO terms.
     *
     * @param organismPhenotypeMatches The HP to HP PhenotypeMatches for the query Phenotypes.
     */
    static ModelScorer forSameSpecies(OrganismPhenotypeMatches organismPhenotypeMatches) {
        int numQueryPhenotypes = organismPhenotypeMatches.getQueryTerms().size();
        return new PhiveModelScorer(organismPhenotypeMatches, numQueryPhenotypes);
    }

    /**
     * Produces a {@link ModelScorer} which will score models between human and a single other species. Requires an
     * {@link OrganismPhenotypeMatches} for the relevant organism i.e. mouse or fish.
     *
     * @param organismPhenotypeMatches The HP to MP/ZP PhenotypeMatches for the query Phenotypes.
     */
    static ModelScorer forSingleCrossSpecies(OrganismPhenotypeMatches organismPhenotypeMatches) {
        int numQueryPhenotypes = organismPhenotypeMatches.getBestPhenotypeMatches().size();
        return new PhiveModelScorer(organismPhenotypeMatches, numQueryPhenotypes);
    }

    /**
     * Produces a {@link ModelScorer} which will score models across multiple species. This requires a reference
     * species (human) so that the scores are scaled correctly across the species.
     *
     * @param theoreticalModel for the reference organism - this should be for the HP-HP hits.
     * @param organismPhenotypeMatches the best phenotype matches for the organism i.e. HP-HP, HP-MP or HP-ZP matches.
     */
    static ModelScorer forMultiCrossSpecies(TheoreticalModel theoreticalModel, OrganismPhenotypeMatches organismPhenotypeMatches) {
        int numQueryPhenotypes = theoreticalModel.getQueryTerms().size();
        return new PhiveModelScorer(theoreticalModel, organismPhenotypeMatches, numQueryPhenotypes);
    }
}
