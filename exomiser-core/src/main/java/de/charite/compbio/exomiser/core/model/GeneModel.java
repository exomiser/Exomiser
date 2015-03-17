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
 * Bean for encapsulating data involved in a disease/animal model to human gene
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
public class GeneModel {
    
    private final int entrezGeneId;
    private final String humanGeneSymbol;
    
    private final String modelId;
    private final String modelSymbol;
    private final List<String> phenotypeIds;

    private double score = 0d;
    private final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms;
    
    public GeneModel(int entrezGeneId, String humanGeneSymbol, String modelId, String modelSymbol, List<String> phenotypeIds) {
        this.entrezGeneId = entrezGeneId;
        this.humanGeneSymbol = humanGeneSymbol;
        this.modelId = modelId;
        this.modelSymbol = modelSymbol;
        this.phenotypeIds = phenotypeIds;
        this.bestPhenotypeMatchForTerms = new LinkedHashMap<>();
    }

    public int getEntrezGeneId() {
        return entrezGeneId;
    }

    public String getHumanGeneSymbol() {
        return humanGeneSymbol;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelSymbol() {
        return modelSymbol;
    }

    public List<String> getPhenotypeIds() {
        return phenotypeIds;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
    
    public Map<PhenotypeTerm, PhenotypeMatch> getBestPhenotypeMatchForTerms() {
        return bestPhenotypeMatchForTerms;
    }
      
    public void addMatchIfAbsentOrBetterThanCurrent(PhenotypeMatch match) {
        PhenotypeTerm matchQueryTerm = match.getQueryPhenotype();
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm)) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        } else if (bestPhenotypeMatchForTerms.get(matchQueryTerm).getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        } 
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.entrezGeneId;
        hash = 79 * hash + Objects.hashCode(this.humanGeneSymbol);
        hash = 79 * hash + Objects.hashCode(this.modelId);
        hash = 79 * hash + Objects.hashCode(this.phenotypeIds);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeneModel other = (GeneModel) obj;
        if (this.entrezGeneId != other.entrezGeneId) {
            return false;
        }
        if (!Objects.equals(this.humanGeneSymbol, other.humanGeneSymbol)) {
            return false;
        }
        if (!Objects.equals(this.modelId, other.modelId)) {
            return false;
        }
        if (!Objects.equals(this.phenotypeIds, other.phenotypeIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GeneModel{score=" + score + ", entrezGeneId=" + entrezGeneId + ", humanGeneSymbol=" + humanGeneSymbol + ", modelId=" + modelId + ", modelSymbol=" + modelSymbol + ", phenotypeIds=" + phenotypeIds + '}';
    }
    
}
