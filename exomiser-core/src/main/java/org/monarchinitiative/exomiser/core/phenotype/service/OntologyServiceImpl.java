/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype.service;

import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers. This class is complemented by the PriorityService. 
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class OntologyServiceImpl implements OntologyService {

    private final HumanPhenotypeOntologyDao hpoDao;
    private final MousePhenotypeOntologyDao mpoDao;
    private final ZebraFishPhenotypeOntologyDao zpoDao;

    @Autowired
    public OntologyServiceImpl(HumanPhenotypeOntologyDao hpoDao, MousePhenotypeOntologyDao mpoDao, ZebraFishPhenotypeOntologyDao zpoDao) {
        this.hpoDao = hpoDao;
        this.mpoDao = mpoDao;
        this.zpoDao = zpoDao;
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

}
