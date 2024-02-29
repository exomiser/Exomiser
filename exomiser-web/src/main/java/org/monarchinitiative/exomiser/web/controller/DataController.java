/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.web.controller;

import org.monarchinitiative.exomiser.web.dao.ExomiserDao;
import org.monarchinitiative.exomiser.web.model.SelectOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
@RequestMapping("data")
public class DataController {
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
    
    private final ExomiserDao exomiserDao;

    private Set<SelectOption> hpoSelectOptions;
    private Set<SelectOption> diseaseSelectOptions;
    private Set<SelectOption> geneSelectOptions;

    @Autowired
    public DataController(ExomiserDao exomiserDao) {
        this.exomiserDao = exomiserDao;
    }

    @PostConstruct
    private void setUp() {
        Map<String, String> hpoTerms = exomiserDao.getHpoTerms();
        hpoSelectOptions = makeSelectOptionsFromMap(hpoTerms);

        Map<String, String> diseases = exomiserDao.getDiseases();
        diseaseSelectOptions = makeSelectOptionsFromMap(diseases);

        Map<String, String> genes = exomiserDao.getGenes();
        geneSelectOptions = makeSelectOptionsFromMap(genes);
        
        logger.info("Loaded {} HPO, {} disease and {} gene select options", hpoSelectOptions.size(), diseaseSelectOptions.size(), geneSelectOptions.size());
    }

    private Set<SelectOption> makeSelectOptionsFromMap(Map<String, String> inputMap) {
        Set<SelectOption> selectOptions = new HashSet<>();
        for (Entry<String, String> entry : inputMap.entrySet()) {
            selectOptions.add(new SelectOption(entry.getKey(), entry.getValue()));
        }
        return selectOptions;
    }
        
    @GetMapping(value = "disease", produces = "application/json;charset=UTF-8")
    public @ResponseBody List<SelectOption> getDiseaseOptionsContainingTerm(@RequestParam(value="term") String term) {
        logger.info("Searching for disease term '{}'", term);
        return findSelectOptionContainingTerm(term, diseaseSelectOptions);
    }
    
    @GetMapping(value = "hpo", produces = "application/json;charset=UTF-8")
    public @ResponseBody List<SelectOption> getHpoTermOptionsContainingTerm(@RequestParam(value="term") String term) {
        logger.info("Searching for HPO term '{}'", term);
        return findSelectOptionContainingTerm(term, hpoSelectOptions);
    }

    @GetMapping(value = "gene", produces = "application/json;charset=UTF-8")
    public @ResponseBody List<SelectOption> getGeneOptionsContainingTerm(@RequestParam(value="term") String term) {
        logger.info("Searching for gene name '{}'", term);
        return findSelectOptionContainingTerm(term, geneSelectOptions);
    }

    
    private List<SelectOption> findSelectOptionContainingTerm(String term, Set<SelectOption> selectOptions) {
        List<SelectOption> matches = new ArrayList<>();
        
        for (SelectOption selectOption : selectOptions) {
            if (selectOption.getText().toLowerCase().contains(term.toLowerCase())) {
                matches.add(selectOption);
            }
        }
        Collections.sort(matches);
        logger.info("Returning {}", matches);
        return matches;
    }

}
