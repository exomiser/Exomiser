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
package de.charite.compbio.exomiser.core.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class GeneModel implements Model {
    
    private final String modelId;
    private final Organism organism;
    
    private final int entrezGeneId;
    private final String humanGeneSymbol;
    
    private final String modelGeneId;
    private final String modelGeneSymbol;
    
    private final List<String> phenotypeIds;

    private double score = 0d;
    private final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms;
    
    public GeneModel(String modelId, Organism organism, int entrezGeneId, String humanGeneSymbol, String modelGeneId, String modelGeneSymbol, List<String> phenotypeIds) {
        this.modelId = modelId;
        this.organism = organism;
        
        this.entrezGeneId = entrezGeneId;
        this.humanGeneSymbol = humanGeneSymbol;
        
        this.modelGeneId = modelGeneId;
        this.modelGeneSymbol = modelGeneSymbol;
        
        this.phenotypeIds = phenotypeIds;
        this.bestPhenotypeMatchForTerms = new LinkedHashMap<>();
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
    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    @Override
    public String getHumanGeneSymbol() {
        return humanGeneSymbol;
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public List<String> getPhenotypeIds() {
        return phenotypeIds;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public void setScore(double score) {
        this.score = score;
    }
    
    @Override
    public Map<PhenotypeTerm, PhenotypeMatch> getBestPhenotypeMatchForTerms() {
        return bestPhenotypeMatchForTerms;
    }
      
    @Override
    public void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match) {
        PhenotypeTerm matchQueryTerm = match.getQueryPhenotype();
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm) || bestPhenotypeMatchForTerms.get(matchQueryTerm).getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneModel)) return false;
        GeneModel geneModel = (GeneModel) o;
        return entrezGeneId == geneModel.entrezGeneId &&
                Objects.equals(modelId, geneModel.modelId) &&
                organism == geneModel.organism &&
                Objects.equals(humanGeneSymbol, geneModel.humanGeneSymbol) &&
                Objects.equals(modelGeneId, geneModel.modelGeneId) &&
                Objects.equals(modelGeneSymbol, geneModel.modelGeneSymbol) &&
                Objects.equals(phenotypeIds, geneModel.phenotypeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, organism, entrezGeneId, humanGeneSymbol, modelGeneId, modelGeneSymbol, phenotypeIds);
    }

    @Override
    public String toString() {
        return "GeneModel{" +
                "modelId='" + modelId + '\'' +
                ", organism=" + organism +
                ", entrezGeneId=" + entrezGeneId +
                ", humanGeneSymbol='" + humanGeneSymbol + '\'' +
                ", modelGeneId='" + modelGeneId + '\'' +
                ", modelGeneSymbol='" + modelGeneSymbol + '\'' +
                ", phenotypeIds=" + phenotypeIds +
                ", score=" + score +
                ", bestPhenotypeMatchForTerms=" + bestPhenotypeMatchForTerms +
                '}';
    }
}
