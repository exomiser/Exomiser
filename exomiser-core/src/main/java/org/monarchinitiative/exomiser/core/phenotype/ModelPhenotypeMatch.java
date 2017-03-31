package org.monarchinitiative.exomiser.core.phenotype;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Value class for storing the results of a match between a model and a query.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 8.0.0
 */
public final class ModelPhenotypeMatch implements Comparable<ModelPhenotypeMatch> {

    private final double score;
    private final Model model;
    private final List<PhenotypeMatch> bestPhenotypeMatches;

    ModelPhenotypeMatch(double score, Model model, List<PhenotypeMatch> bestPhenotypeMatches) {
        this.score = score;
        this.model = model;
        this.bestPhenotypeMatches = bestPhenotypeMatches;
    }

    public static ModelPhenotypeMatch of(double score, Model model, List<PhenotypeMatch> bestPhenotypeMatches) {
        return new ModelPhenotypeMatch(score, model, ImmutableList.copyOf(bestPhenotypeMatches));
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

    /**
     * The natural order of this class is the opposite of the natural numerical order.
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(ModelPhenotypeMatch o) {
        return -Double.compare(this.score, o.score);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelPhenotypeMatch that = (ModelPhenotypeMatch) o;
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
        return "ModelPhenotypeMatch{" +
                "score=" + score +
                ", model=" + model +
                ", bestPhenotypeMatches=" + bestPhenotypeMatches +
                '}';
    }
}
