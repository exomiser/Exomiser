/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for retrieving phenotype data from the database for use by the
 * prioritisers. This class is complemented by the PriorityService. 
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class OntologyServiceImpl implements OntologyService {

    private static final Logger logger = LoggerFactory.getLogger(OntologyServiceImpl.class);

    private final HumanPhenotypeOntologyDao hpoDao;
    private final MousePhenotypeOntologyDao mpoDao;
    private final ZebraFishPhenotypeOntologyDao zpoDao;

    private final HpoIdChecker hpoIdChecker;

    @Autowired
    public OntologyServiceImpl(HumanPhenotypeOntologyDao hpoDao, MousePhenotypeOntologyDao mpoDao, ZebraFishPhenotypeOntologyDao zpoDao) {
        this.hpoDao = hpoDao;
        this.mpoDao = mpoDao;
        this.zpoDao = zpoDao;

        Map<String, PhenotypeTerm> hpAltIds = setUpHpoAltIds();
        this.hpoIdChecker = HpoIdChecker.of(hpAltIds);
    }

    private Map<String, PhenotypeTerm> setUpHpoAltIds() {
        Map<String, PhenotypeTerm> hpAltIds = hpoDao.getIdToPhenotypeTerms();
        // in cases where there old phenotype database schema is being used the above will log an exception
        // and return an empty list. In that case instead of refusing to start, revert back to the old behaviour of not
        // checking the input too closely.
        if (hpAltIds.isEmpty()) {
            Map<String, PhenotypeTerm> alternateIdToPhenotypeTerms = new LinkedHashMap<>();
            Set<PhenotypeTerm> allTerms = hpoDao.getAllTerms();
            for (PhenotypeTerm term : allTerms) {
                alternateIdToPhenotypeTerms.put(term.getId(), term);
            }
            return alternateIdToPhenotypeTerms;
        }
        return hpAltIds;
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
        try {
            return hpoIdChecker.getCurrentTerm(hpoId);
        } catch (IllegalArgumentException ex) {
            // swallow exception thrown for unrecognised HPO ids
            logger.warn("{}", ex.getMessage());
        }
        return null;
    }

    @Override
    public List<String> getCurrentHpoIds(List<String> hpoIds) {
        return hpoIds.stream()
                .map(hpoIdChecker::getCurrentId)
                .distinct()
                .toList();
    }
}
