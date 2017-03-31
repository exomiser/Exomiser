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
public class GeneOrthologModel implements GeneModel {
    
    private final String modelId;
    private final Organism organism;
    
    private final int entrezGeneId;
    private final String humanGeneSymbol;
    
    private final String modelGeneId;
    private final String modelGeneSymbol;
    
    private final List<String> phenotypeIds;
    
    public GeneOrthologModel(String modelId, Organism organism, int entrezGeneId, String humanGeneSymbol, String modelGeneId, String modelGeneSymbol, List<String> phenotypeIds) {
        this.modelId = modelId;
        this.organism = organism;
        
        this.entrezGeneId = entrezGeneId;
        this.humanGeneSymbol = humanGeneSymbol;
        
        this.modelGeneId = modelGeneId;
        this.modelGeneSymbol = modelGeneSymbol;
        
        this.phenotypeIds = phenotypeIds;
    }

    public String getModelGeneId() {
        return modelGeneId;
    }

    public String getModelGeneSymbol() {
        return modelGeneSymbol;
    }

    @Override
    public Organism getOrganism() {
        return organism;
    }

    @Override
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    @Override
    public String getHumanGeneSymbol() {
        return humanGeneSymbol;
    }

    @Override
    public String getId() {
        return modelId;
    }

    @Override
    public List<String> getPhenotypeIds() {
        return phenotypeIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneOrthologModel)) return false;
        GeneOrthologModel geneOrthologModel = (GeneOrthologModel) o;
        return entrezGeneId == geneOrthologModel.entrezGeneId &&
                Objects.equals(modelId, geneOrthologModel.modelId) &&
                organism == geneOrthologModel.organism &&
                Objects.equals(humanGeneSymbol, geneOrthologModel.humanGeneSymbol) &&
                Objects.equals(modelGeneId, geneOrthologModel.modelGeneId) &&
                Objects.equals(modelGeneSymbol, geneOrthologModel.modelGeneSymbol) &&
                Objects.equals(phenotypeIds, geneOrthologModel.phenotypeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, phenotypeIds);
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
