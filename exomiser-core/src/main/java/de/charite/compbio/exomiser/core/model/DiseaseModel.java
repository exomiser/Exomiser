/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DiseaseModel implements Model {
    
    private final String modelId;
    private final Organism organism;
    
    private final int entrezGeneId;
    private final String humanGeneSymbol;
    
    private final String diseaseId;
    private final String diseaseTerm;
    
    private final List<String> phenotypeIds;

    private double score = 0d;
    private final Map<PhenotypeTerm, PhenotypeMatch> bestPhenotypeMatchForTerms;

    public DiseaseModel(String modelId, Organism organism, int entrezGeneId, String humanGeneSymbol, String diseaseId, String diseaseTerm, List<String> phenotypeIds) {
        this.modelId = modelId;
        this.organism = organism;
        
        this.entrezGeneId = entrezGeneId;
        this.humanGeneSymbol = humanGeneSymbol;
        
        this.diseaseId = diseaseId;
        this.diseaseTerm = diseaseTerm;
    
        this.phenotypeIds = phenotypeIds;
        this.bestPhenotypeMatchForTerms = new LinkedHashMap<>();
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
        if (!bestPhenotypeMatchForTerms.containsKey(matchQueryTerm)) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        } else if (bestPhenotypeMatchForTerms.get(matchQueryTerm).getScore() < match.getScore()) {
            bestPhenotypeMatchForTerms.put(matchQueryTerm, match);
        } 
    }
    
}
