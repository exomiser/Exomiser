/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.dao.HumanPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.MousePhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers. This class is complemented by the PriorityService. 
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class OntologyServiceImpl implements OntologyService {

    @Autowired
    private DiseaseDao diseaseDao;

    @Autowired
    private HumanPhenotypeOntologyDao hpoDao;
    @Autowired
    private MousePhenotypeOntologyDao mpoDao;
    @Autowired
    private ZebraFishPhenotypeOntologyDao zpoDao;

    @Cacheable(value = "diseaseHp")
    @Override
    public List<String> getHpoIdsForDiseaseId(String diseaseId) {
        return new ArrayList<>(diseaseDao.getHpoIdsForDiseaseId(diseaseId));
    }

    @Cacheable(value = "hpo")
    @Override
    public Set<PhenotypeTerm> getHpoTerms() {
        return hpoDao.getAllTerms();
    }

    @Cacheable(value = "mpo")
    @Override
    public Set<PhenotypeTerm> getMpoTerms() {
        return mpoDao.getAllTerms();
    }

    @Cacheable(value = "zpo")
    @Override
    public Set<PhenotypeTerm> getZpoTerms() {
        return zpoDao.getAllTerms();
    }

    @Override
    public Set<PhenotypeMatch> getHpoMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        return hpoDao.getPhenotypeMatchesForHpoTerm(hpoTerm);
    }

    @Override
    public Set<PhenotypeMatch> getMpoMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        return mpoDao.getPhenotypeMatchesForHpoTerm(hpoTerm);
    }

    @Override
    public Set<PhenotypeMatch> getZpoMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        return zpoDao.getPhenotypeMatchesForHpoTerm(hpoTerm);
    }

    /**
     * Returns the matching HPO PhenotypeTerm for a given HPO id or null if the
     * term cannot be found.
     *
     * @param hpoId
     * @return
     */
    @Override
    public PhenotypeTerm getPhenotypeTermForHpoId(String hpoId) {
        for (PhenotypeTerm hpoTerm : getHpoTerms()) {
            if (hpoTerm.getId().equals(hpoId)) {
                return hpoTerm;
            }
        }
        return null;
    }

    //TODO: is this needed in any othoer prioritiser - was in HiPhive, but might be redundant now.
    private Map<String, PhenotypeTerm> makeGenericOntologyTermCache(Set<PhenotypeTerm> allPhenotypeTerms) {
        Map<String, PhenotypeTerm> termsCache = new HashMap();
        for (PhenotypeTerm term : allPhenotypeTerms) {
            termsCache.put(term.getId(), term);
        }
        return termsCache;
    }
}
