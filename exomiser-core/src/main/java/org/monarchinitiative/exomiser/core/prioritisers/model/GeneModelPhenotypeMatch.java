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

package org.monarchinitiative.exomiser.core.prioritisers.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record GeneModelPhenotypeMatch(double score, GeneModel model, List<PhenotypeMatch> bestPhenotypeMatches) implements GeneModel {

    public GeneModelPhenotypeMatch {
        Objects.requireNonNull(model);
        Objects.requireNonNull(bestPhenotypeMatches);
        bestPhenotypeMatches = List.copyOf(bestPhenotypeMatches);
    }

    @JsonIgnore
    @Override
    public String id() {
        return model.id();
    }

    @JsonIgnore
    @Override
    public List<String> phenotypeIds() {
        return model.phenotypeIds();
    }

    @JsonIgnore
    @Override
    public Organism organism() {
        return model.organism();
    }

    @JsonIgnore
    @Override
    public int entrezGeneId() {
        return model.entrezGeneId();
    }

    @JsonIgnore
    @Override
    public String humanGeneSymbol() {
        return model.humanGeneSymbol();
    }


    @Override
    public String toString() {
        return "GeneModelPhenotypeMatch{" +
               "score=" + score +
               ", model=" + model +
               ", bestPhenotypeMatches=" + bestPhenotypeMatches +
               '}';
    }
}
