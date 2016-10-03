/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSortedMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ModelPhenotypeMatch implements Model {
    //TODO: should this be a GeneModelPhenotypeMatch? - this is referenced in Phive
    private final double score;
    private final Model model;
    private final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms;

    public ModelPhenotypeMatch(double score, Model model, Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms) {
        this.score = score;
        this.model = model;
        this.bestPhenotypeMatchForTerms = ImmutableSortedMap.copyOf(bestPhenotypeMatchForTerms);
    }

    public double getScore() {
        return score;
    }

    public Model getModel() {
        return model;
    }

    public Map<PhenotypeTerm, PhenotypeMatch> getBestPhenotypeMatchForTerms() {
        return bestPhenotypeMatchForTerms;
    }

    @JsonIgnore
    @Override
    public String getModelId() {
        return model.getModelId();
    }

    @JsonIgnore
    @Override
    public Organism getOrganism() {
        return model.getOrganism();
    }

    @JsonIgnore
    @Override
    public int getEntrezGeneId() {
        return model.getEntrezGeneId();
    }

    @JsonIgnore
    @Override
    public String getHumanGeneSymbol() {
        return model.getHumanGeneSymbol();
    }

    @JsonIgnore
    @Override
    public List<String> getPhenotypeIds() {
        return model.getPhenotypeIds();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelPhenotypeMatch)) return false;
        ModelPhenotypeMatch that = (ModelPhenotypeMatch) o;
        return Double.compare(that.score, score) == 0 &&
                Objects.equals(model, that.model) &&
                Objects.equals(bestPhenotypeMatchForTerms, that.bestPhenotypeMatchForTerms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, bestPhenotypeMatchForTerms, score);
    }


    @Override
    public String toString() {
        return "ModelPhenotypeMatch{" +
                "score=" + score +
                ", model=" + model +
                ", bestPhenotypeMatchForTerms=" + bestPhenotypeMatchForTerms +
                '}';
    }
}
