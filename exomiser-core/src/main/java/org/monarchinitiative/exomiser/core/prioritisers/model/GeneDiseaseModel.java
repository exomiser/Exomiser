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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers.model;

import org.monarchinitiative.exomiser.core.phenotype.Organism;

import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record GeneDiseaseModel(String modelId, Organism organism, Disease disease) implements GeneModel {

    public GeneDiseaseModel {
        Objects.requireNonNull(modelId, "modelId cannot be null");
        Objects.requireNonNull(organism, "organism cannot be null");
        Objects.requireNonNull(disease, "disease cannot be null");
    }

    public String diseaseId() {
        return disease.diseaseId();
    }

    public String diseaseTerm() {
        return disease.diseaseName();
    }

    @Override
    public Organism organism() {
        return organism;
    }

    @Override
    public int entrezGeneId() {
        return disease.associatedGeneId();
    }

    @Override
    public String humanGeneSymbol() {
        return disease.associateGeneSymbol();
    }

    @Override
    public String id() {
        return modelId;
    }

    @Override
    public List<String> phenotypeIds() {
        return disease.phenotypeIds();
    }

    @Override
    public String toString() {
        if (disease != null) {
            return "GeneDiseaseModel{" +
                    "modelId='" + modelId + '\'' +
                    ", organism=" + organism +
                    ", disease=" + disease + '\'' +
                    '}';
        }

        return "GeneDiseaseModel{" +
                "modelId='" + modelId + '\'' +
                ", organism=" + organism +
                ", entrezGeneId=" + entrezGeneId() +
                ", humanGeneSymbol='" + humanGeneSymbol() + '\'' +
                ", diseaseId='" + diseaseId() + '\'' +
                ", diseaseTerm='" + diseaseTerm() + '\'' +
                ", phenotypeIds=" + phenotypeIds() +
                '}';
    }
}
