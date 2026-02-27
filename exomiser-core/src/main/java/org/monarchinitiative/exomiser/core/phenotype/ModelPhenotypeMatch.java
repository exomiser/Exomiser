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

package org.monarchinitiative.exomiser.core.phenotype;

import java.util.List;
import java.util.Objects;

/**
 * Value class for storing the results of a match between a model and a query.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 8.0.0
 */
public record ModelPhenotypeMatch<T extends Model>(double score, T model, List<PhenotypeMatch> bestPhenotypeMatches) implements Comparable<ModelPhenotypeMatch<T>> {

    public ModelPhenotypeMatch {
        Objects.requireNonNull(model);
        Objects.requireNonNull(bestPhenotypeMatches);
        bestPhenotypeMatches = List.copyOf(bestPhenotypeMatches);
    }

    public static <T extends Model> ModelPhenotypeMatch<T> of(double score, T model, List<PhenotypeMatch> bestPhenotypeMatches) {
        return new ModelPhenotypeMatch<>(score, model, bestPhenotypeMatches);
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
    public String toString() {
        return "ModelPhenotypeMatch{" + "score=" + score + ", model=" + model + ", bestPhenotypeMatches=" + bestPhenotypeMatches + '}';
    }
}
