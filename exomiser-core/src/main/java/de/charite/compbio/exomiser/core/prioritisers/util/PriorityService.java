/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class which offers a single interface to other services required by
 * prioritisers. This aims to simplify getting data from different Services.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class PriorityService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriorityService.class);

    @Autowired
    private OntologyService ontologyService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private DiseaseDao diseaseDao;

    public List<PhenotypeTerm> makePhenotypeTermsFromHpoIds(List<String> hpoIds) {
        List<PhenotypeTerm> phenotypeTerms = new ArrayList<>();
        for (String hpoId : hpoIds) {
            PhenotypeTerm hpoTerm = ontologyService.getPhenotypeTermForHpoId(hpoId);
            if (hpoTerm != null) {
                phenotypeTerms.add(hpoTerm);
            }
        }
        return phenotypeTerms;
    }

    public Set<PhenotypeTerm> getHpoTerms() {
        return ontologyService.getHpoTerms();
    }

    public Set<PhenotypeTerm> getMpoTerms() {
        return ontologyService.getMpoTerms();
    }

    public Set<PhenotypeTerm> getZpoTerms() {
        return ontologyService.getZpoTerms();
    }

    public List<String> getHpoIdsForDiseaseId(String diseaseId) {
        return ontologyService.getHpoIdsForDiseaseId(diseaseId);
    }

    public Set<PhenotypeMatch> getSpeciesMatchesForHpoTerm(PhenotypeTerm hpoTerm, Organism species) {
        switch (species) {
            case HUMAN:
                return ontologyService.getHpoMatchesForHpoTerm(hpoTerm);
            case MOUSE:
                return ontologyService.getMpoMatchesForHpoTerm(hpoTerm);
            case FISH:
                return ontologyService.getZpoMatchesForHpoTerm(hpoTerm);
            default:
                return Collections.emptySet();
        }
    }

    public List<Model> getModelsForOrganism(Organism species) {
        logger.info("Fetching disease/gene model phenotype annotations and HUMAN-{} gene orthologs", species);
        switch (species) {
            case HUMAN:
                return modelService.getHumanDiseaseModels();
            case MOUSE:
                return modelService.getMouseGeneModels();
            case FISH:
                return modelService.getFishGeneModels();
            default:
                return Collections.emptyList();
        }
    }

    public String getDiseaseTermForId(String diseaseId) {
        return diseaseDao.getDiseaseIdToTerms().get(diseaseId);
    }

}
