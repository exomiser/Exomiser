package org.monarchinitiative.exomiser.core.prioritisers.util;

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.model.Model;
import org.monarchinitiative.exomiser.core.model.PhenotypeMatch;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 8.0.0
 */
public class ModelPhenotypeMatchScore {

    private final double score;
    private final Model model;
    private final List<PhenotypeMatch> bestPhenotypeMatches;

    private ModelPhenotypeMatchScore(double score, Model model, List<PhenotypeMatch> bestPhenotypeMatches) {
        this.score = score;
        this.model = model;
        this.bestPhenotypeMatches = bestPhenotypeMatches;
    }

    public static ModelPhenotypeMatchScore of(double score, Model model, List<PhenotypeMatch> bestPhenotypeMatches) {
        return new ModelPhenotypeMatchScore(score, model, ImmutableList.copyOf(bestPhenotypeMatches));
    }

    public double getScore() {
        return score;
    }

    public Model getModel() {
        return model;
    }

    public List<PhenotypeMatch> getBestPhenotypeMatches() {
        return bestPhenotypeMatches;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelPhenotypeMatchScore that = (ModelPhenotypeMatchScore) o;
        return Double.compare(that.score, score) == 0 &&
                Objects.equals(model, that.model) &&
                Objects.equals(bestPhenotypeMatches, that.bestPhenotypeMatches);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, model, bestPhenotypeMatches);
    }

    @Override
    public String toString() {
        return "ModelPhenotypeMatchScore{" +
                "score=" + score +
                ", model=" + model +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                '}';
    }
}
