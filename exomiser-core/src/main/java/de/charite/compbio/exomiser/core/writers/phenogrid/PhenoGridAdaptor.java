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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes a Phenogrid from a set of HiPhivePriorityResults
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(PhenoGridAdaptor.class);
    
    protected static final PhenoGridMatchTaxon HUMAN_TAXON = new PhenoGridMatchTaxon("NCBITaxon:9606", Organism.HUMAN.getSpeciesName());
    protected static final PhenoGridMatchTaxon MOUSE_TAXON = new PhenoGridMatchTaxon("NCBITaxon:10090", Organism.MOUSE.getSpeciesName());
    protected static final PhenoGridMatchTaxon FISH_TAXON = new PhenoGridMatchTaxon("NCBITaxon:7955", Organism.FISH.getSpeciesName());

    public PhenoGrid makePhenoGridFromHiPhiveResults(String phenoGridId, List<HiPhivePriorityResult> hiPhiveResults) {
        Set<String> phenotypeIds = new LinkedHashSet<>();
        if (!hiPhiveResults.isEmpty()) {
            HiPhivePriorityResult result = hiPhiveResults.get(0);
            for (PhenotypeTerm phenoTerm : result.getQueryPhenotypeTerms()) {
                phenotypeIds.add(phenoTerm.getId());
            }
        }
        PhenoGridQueryTerms phenoGridQueryTerms = new PhenoGridQueryTerms(phenoGridId, phenotypeIds);
        
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
        
        if (!diseaseModels.isEmpty()){
            PhenoGridMatchGroup diseaseMatchGroup = makePhenoGridMatchGroup(HUMAN_TAXON, diseaseModels, phenotypeIds);
            phenoGridMatchGroups.add(diseaseMatchGroup);
        }
        
        if (!mouseModels.isEmpty()){
            PhenoGridMatchGroup mouseMatchGroup = makePhenoGridMatchGroup(MOUSE_TAXON, mouseModels, phenotypeIds);
            phenoGridMatchGroups.add(mouseMatchGroup);
        }
        
        if(!fishModels.isEmpty()) {
            PhenoGridMatchGroup fishMatchGroup = makePhenoGridMatchGroup(FISH_TAXON, fishModels, phenotypeIds);
            phenoGridMatchGroups.add(fishMatchGroup);
        }
        
        return phenoGridMatchGroups;
    }

    private PhenoGridMatchGroup makePhenoGridMatchGroup(PhenoGridMatchTaxon taxon, List<Model> models, Set<String> phenotypeIds) {
        List<PhenoGridMatch> phenoGridMatches = makePhenogridMatchesFromModels(models, taxon);
        PhenoGridMatchGroup phenoGridMatchGroup = new PhenoGridMatchGroup(phenoGridMatches, phenotypeIds);
        return phenoGridMatchGroup;
    }

    private List<PhenoGridMatch> makePhenogridMatchesFromModels(List<Model> diseaseGeneModels, PhenoGridMatchTaxon taxon) {
        List<PhenoGridMatch> phenoGridMatches = new ArrayList<>();
        //the models will be ordered according to the exomiser combined score, we want to re-order things purely by phenotype score
        Collections.sort(diseaseGeneModels, new DescendingScoreBasedModelComparator());
        
        int modelCount = 0;
        for (Model model : diseaseGeneModels) {
            PhenoGridMatchScore score = new PhenoGridMatchScore("hiPhive", (int) (model.getScore() * 100f), modelCount++);
            logger.debug("Made new {} score modelScore:{} gridScore:{} rank:{}", model.getOrganism(), model.getScore(), score.getScore(), score.getRank());
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

    private static class DescendingScoreBasedModelComparator implements Comparator<Model> {
        @Override
        public int compare(Model model1, Model model2) {
            //we want the results in descending order i.e. greater score first
            return - Double.compare(model1.getScore(), model2.getScore());
        }
    }
    
}
