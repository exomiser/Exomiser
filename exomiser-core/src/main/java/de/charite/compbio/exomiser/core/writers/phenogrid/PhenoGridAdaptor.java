/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import de.charite.compbio.exomiser.core.model.DiseaseModel;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriorityResult;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Makes a Phenogrid from a set of HiPhivePriorityResults
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridAdaptor {

    private static final PhenoGridMatchTaxon HUMAN_TAXON = new PhenoGridMatchTaxon("NCBITaxon:9606", Organism.HUMAN.getSpeciesName());
    private static final PhenoGridMatchTaxon MOUSE_TAXON = new PhenoGridMatchTaxon("NCBITaxon:10090", Organism.MOUSE.getSpeciesName());
    private static final PhenoGridMatchTaxon FISH_TAXON = new PhenoGridMatchTaxon("NCBITaxon:7955", Organism.FISH.getSpeciesName());

    public PhenoGrid makePhenoGridFromHiPhiveResults(List<HiPhivePriorityResult> hiPhiveResults) {
        Set<String> phenotypeIds = new LinkedHashSet<>();
        if (!hiPhiveResults.isEmpty()) {
            HiPhivePriorityResult result = hiPhiveResults.get(0);
            for (PhenotypeTerm phenoTerm : result.getQueryPhenotypeTerms()) {
                phenotypeIds.add(phenoTerm.getId());
            }
        }
        PhenoGridQueryTerms phenoGridQueryTerms = new PhenoGridQueryTerms("hiPhive specified phenotypes", phenotypeIds);
        
        List<Model> diseaseModels = new ArrayList<>();
        List<Model> mouseModels = new ArrayList<>();
        List<Model> fishModels = new ArrayList<>();

        for (HiPhivePriorityResult result : hiPhiveResults) {
            for (Model model : result.getPhenotypeEvidence()) {
                switch(model.getOrganism()) {
                    case HUMAN:
                        diseaseModels.add(model);
                        break;
                    case MOUSE:
                        mouseModels.add(model);
                        break;
                    case FISH:
                        fishModels.add(model);
                        break;
                }
            }
        }
        List<PhenoGridMatchGroup> phenoGridMatchGroups = createPhenogridMatchGroups(phenotypeIds, diseaseModels, mouseModels, fishModels);
        
        return new PhenoGrid(phenoGridQueryTerms, phenoGridMatchGroups);        
    }

    private List<PhenoGridMatchGroup> createPhenogridMatchGroups(Set<String> phenotypeIds, List<Model> diseaseModels, List<Model> mouseModels, List<Model> fishModels) {
        List<PhenoGridMatchGroup> phenoGridMatchGroups = new ArrayList<>();
        
        PhenoGridMatchGroup diseaseMatchGroup = makePhenoGridMatchGroup(HUMAN_TAXON, diseaseModels, phenotypeIds);
        phenoGridMatchGroups.add(diseaseMatchGroup);
        
        PhenoGridMatchGroup mouseMatchGroup = makePhenoGridMatchGroup(MOUSE_TAXON, mouseModels, phenotypeIds);
        phenoGridMatchGroups.add(mouseMatchGroup);
        
        PhenoGridMatchGroup fishMatchGroup = makePhenoGridMatchGroup(FISH_TAXON, fishModels, phenotypeIds);
        phenoGridMatchGroups.add(fishMatchGroup);
        
        return phenoGridMatchGroups;
    }

    private PhenoGridMatchGroup makePhenoGridMatchGroup(PhenoGridMatchTaxon taxon, List<Model> geneModels, Set<String> phenotypeIds) {
        List<PhenoGridMatch> phenoGridMatches = makePhenogridMatchesFromGeneModels(geneModels, taxon);
        PhenoGridMatchGroup phenoGridMatchGroup = new PhenoGridMatchGroup(phenoGridMatches, phenotypeIds);
        return phenoGridMatchGroup;
    }

    private List<PhenoGridMatch> makePhenogridMatchesFromGeneModels(List<Model> diseaseGeneModels, PhenoGridMatchTaxon taxon) {
        List<PhenoGridMatch> phenoGridMatches = new ArrayList<>();
        int modelCount = 0;
        for (Model model : diseaseGeneModels) {
            PhenoGridMatchScore score = new PhenoGridMatchScore("hiPhive", (int) (model.getScore() * 100f), modelCount++);
            List<PhenotypeMatch> phenotypeMatches = new ArrayList<>(model.getBestPhenotypeMatchForTerms().values());
            if (model.getOrganism() == Organism.HUMAN) {
                PhenoGridMatch match = makeDiseasePhenoGridMatch(model, phenotypeMatches, score, taxon);
                phenoGridMatches.add(match);            
            } else {
                PhenoGridMatch match = makeGenePhenoGridMatch(model, phenotypeMatches, score, taxon);
                phenoGridMatches.add(match);  
            }
        }
        
        return phenoGridMatches;
    }

    private PhenoGridMatch makeDiseasePhenoGridMatch(Model model, List<PhenotypeMatch> phenotypeMatches, PhenoGridMatchScore score, PhenoGridMatchTaxon taxon) {
        DiseaseModel diseaseModel = (DiseaseModel) model;
        return new PhenoGridMatch(diseaseModel.getDiseaseId(), diseaseModel.getDiseaseTerm(), "disease", phenotypeMatches, score, taxon);
    }
    
    private PhenoGridMatch makeGenePhenoGridMatch(Model model, List<PhenotypeMatch> phenotypeMatches, PhenoGridMatchScore score, PhenoGridMatchTaxon taxon) {
        GeneModel geneModel = (GeneModel) model;
        return new PhenoGridMatch(geneModel.getModelGeneId(), geneModel.getModelGeneSymbol(), "gene", phenotypeMatches, score, taxon);
    }


    
}
