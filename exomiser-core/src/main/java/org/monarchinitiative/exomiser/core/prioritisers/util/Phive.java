package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.monarchinitiative.exomiser.core.model.Model;
import org.monarchinitiative.exomiser.core.model.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.model.PhenotypeMatch;

import java.util.List;
import java.util.function.Function;

/**
 * Interface defining the Phive algorithm for scoring the semantic similarity of a model against the best theoretical model
 * for a set of phenotypes in a given organism.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Phive {

    default Function<Model, ModelPhenotypeMatch> scoreModelPhenotypeMatch(TheoreticalModel bestTheoreticalModel, OrganismPhenotypeMatches organismPhenotypeMatches, int numMatchedQueryPhenotypes) {
        return model -> {
            List<PhenotypeMatch> bestForwardAndBackwardMatches = organismPhenotypeMatches.calculateBestForwardAndReciprocalMatches(model.getPhenotypeIds());

            List<PhenotypeMatch> bestPhenotypeMatchesByTerm = organismPhenotypeMatches.calculateBestPhenotypeMatchesByTerm(bestForwardAndBackwardMatches);

            //Remember the model needs to collect its best matches from the forward and backward best matches otherwise the modelMaxMatchScore will be zero.
            double modelMaxMatchScore = bestPhenotypeMatchesByTerm.stream()
                    .mapToDouble(PhenotypeMatch::getScore)
                    .max()
                    .orElse(0);

            double modelBestAvgScore = calculateModelBestAvgScore(numMatchedQueryPhenotypes, bestForwardAndBackwardMatches);

            double modelScore = bestTheoreticalModel.compare(modelMaxMatchScore, modelBestAvgScore);
            return new ModelPhenotypeMatch(modelScore, model, bestPhenotypeMatchesByTerm);
        };
    }

    default double calculateModelBestAvgScore(int numMatchedQueryPhenotypes, List<PhenotypeMatch> bestForwardAndBackwardMatches) {
        double sumBestForwardAndBackwardMatchScores = bestForwardAndBackwardMatches.stream().mapToDouble(PhenotypeMatch::getScore).sum();
        long numMatchedModelPhenotypes = bestForwardAndBackwardMatches.stream().map(PhenotypeMatch::getMatchPhenotypeId).distinct().count();

        int totalPhenotypesWithMatch = numMatchedQueryPhenotypes + (int) numMatchedModelPhenotypes;

        return sumBestForwardAndBackwardMatchScores / totalPhenotypesWithMatch;
    }
}
