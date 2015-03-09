/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class OntologyServiceImpl implements OntologyService {

    @Autowired
    private DiseaseDao diseaseDao;

    @Cacheable(value="diseaseHp")
    @Override
    public List<String> getHpoIdsForDiseaseId(String diseaseId) {
        return new ArrayList<>(diseaseDao.getHpoIdsForDiseaseId(diseaseId));
    }

}
