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
 * Bean for encapsulating data involved in an animal model to human gene
 * association.
 *
 * For example the disease Pfeiffer syndrome (OMIM:101600) has a set of defined
 * phenotypes encoded using HPO terms is associated with two causative genes,
 * FGFR1 (Entrez:2260) and FGFR2 (Entrez:2263).
 *
 * There are also mouse models where the mouse homologue of FGFR1 and FGFR2 have
 * been knocked-out and they too have a set of defined phenotypes. However the
 * mouse phenotypes are encoded using the MPO.
 *
 * Due to the phenotypic similarities of the mouse knockout and/or the human
 * disease it is possible to infer a likely causative gene for a given set of
 * input phenotypes. 
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public record GeneOrthologModel(String modelId, Organism organism, int entrezGeneId, String humanGeneSymbol, String modelGeneId, String modelGeneSymbol, List<String> phenotypeIds) implements GeneModel {

    public GeneOrthologModel {
        Objects.requireNonNull(modelId);
        Objects.requireNonNull(organism);

        Objects.requireNonNull(humanGeneSymbol);

        Objects.requireNonNull(modelGeneSymbol);

        Objects.requireNonNull(phenotypeIds);
        phenotypeIds = List.copyOf(phenotypeIds);
    }

    @Override
    public String id() {
        return modelId;
    }

    @Override
    public String toString() {
        return "GeneOrthologModel{" +
                "modelId='" + modelId + '\'' +
                ", organism=" + organism +
                ", entrezGeneId=" + entrezGeneId +
                ", humanGeneSymbol='" + humanGeneSymbol + '\'' +
                ", modelGeneId='" + modelGeneId + '\'' +
                ", modelGeneSymbol='" + modelGeneSymbol + '\'' +
                ", phenotypeIds=" + phenotypeIds +
                '}';
    }
}
