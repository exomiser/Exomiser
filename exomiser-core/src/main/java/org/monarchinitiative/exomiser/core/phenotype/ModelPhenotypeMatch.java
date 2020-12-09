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
public final class ModelPhenotypeMatch<T extends Model> implements Comparable<ModelPhenotypeMatch<T>> {

    private final double score;
    private final T model;
    private final List<PhenotypeMatch> bestPhenotypeMatches;

    private ModelPhenotypeMatch(double score, T model, List<PhenotypeMatch> bestPhenotypeMatches) {
        this.score = score;
        this.model = Objects.requireNonNull(model);
        this.bestPhenotypeMatches = Objects.requireNonNull(bestPhenotypeMatches);
    }

    public static <T extends Model> ModelPhenotypeMatch<T> of(double score, T model, List<PhenotypeMatch> bestPhenotypeMatches) {
        return new ModelPhenotypeMatch<>(score, model, ImmutableList.copyOf(bestPhenotypeMatches));
    }

    public double getScore() {
        return score;
    }

    public T getModel() {
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
        if (!(o instanceof ModelPhenotypeMatch)) return false;
        ModelPhenotypeMatch<?> that = (ModelPhenotypeMatch<?>) o;
        return Double.compare(that.score, score) == 0 &&
                model.equals(that.model) &&
                bestPhenotypeMatches.equals(that.bestPhenotypeMatches);
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
