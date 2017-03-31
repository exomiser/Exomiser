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
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneDiseaseModel implements GeneModel {
    
    private final String modelId;
    private final Organism organism;
    
    private final int entrezGeneId;
    private final String humanGeneSymbol;
    
    private final String diseaseId;
    private final String diseaseTerm;
    
    private final List<String> phenotypeIds;

    public GeneDiseaseModel(String modelId, Organism organism, int entrezGeneId, String humanGeneSymbol, String diseaseId, String diseaseTerm, List<String> phenotypeIds) {
        this.modelId = modelId;
        this.organism = organism;
        
        this.entrezGeneId = entrezGeneId;
        this.humanGeneSymbol = humanGeneSymbol;
        
        this.diseaseId = diseaseId;
        this.diseaseTerm = diseaseTerm;
    
        this.phenotypeIds = phenotypeIds;
    }
    
    public String getDiseaseId() {
        return diseaseId;
    }

    public String getDiseaseTerm() {
        return diseaseTerm;
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
        if (!(o instanceof GeneDiseaseModel)) return false;
        GeneDiseaseModel that = (GeneDiseaseModel) o;
        return entrezGeneId == that.entrezGeneId &&
                Objects.equals(modelId, that.modelId) &&
                organism == that.organism &&
                Objects.equals(humanGeneSymbol, that.humanGeneSymbol) &&
                Objects.equals(diseaseId, that.diseaseId) &&
                Objects.equals(diseaseTerm, that.diseaseTerm) &&
                Objects.equals(phenotypeIds, that.phenotypeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, organism, entrezGeneId, humanGeneSymbol, diseaseId, diseaseTerm, phenotypeIds);
    }

    @Override
    public String toString() {
        return "GeneDiseaseModel{" +
                "modelId='" + modelId + '\'' +
                ", organism=" + organism +
                ", entrezGeneId=" + entrezGeneId +
                ", humanGeneSymbol='" + humanGeneSymbol + '\'' +
                ", diseaseId='" + diseaseId + '\'' +
                ", diseaseTerm='" + diseaseTerm + '\'' +
                ", phenotypeIds=" + phenotypeIds +
                '}';
    }
}
