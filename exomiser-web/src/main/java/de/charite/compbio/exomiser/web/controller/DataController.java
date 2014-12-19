/*
 * Copyright (C) 2014 jj8
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.controller;

import de.charite.compbio.exomiser.web.model.SelectOption;
import de.charite.compbio.exomiser.web.dao.ExomiserDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
@RequestMapping("data")
public class DataController {
    private static final Logger logger = LoggerFactory.getLogger(DataController.class);
    
    @Autowired
    private ExomiserDao exomiserDao;

    private Map<String, String> hpoTerms;
    private Set<SelectOption> hpoSelectOptions;

    private Map<String, String> diseases;
    private Set<SelectOption> diseaseSelectOptions;
    
    @PostConstruct
    private void setUp() {
        hpoTerms = exomiserDao.getHpoTerms();
        hpoSelectOptions = makeSelectOptionsFromMap(hpoTerms);
        
        diseases = exomiserDao.getDiseases();
        diseaseSelectOptions = makeSelectOptionsFromMap(diseases);
        
        logger.info("Loaded {} HPO and {} disease terms", hpoSelectOptions.size(), diseaseSelectOptions.size());
    }

    private Set<SelectOption> makeSelectOptionsFromMap(Map<String, String> inputMap) {
        Set<SelectOption> selectOptions = new HashSet<>();
        for (Entry<String, String> entry : inputMap.entrySet()) {
            selectOptions.add(new SelectOption(entry.getKey(), entry.getValue()));
        }
        return selectOptions;
    }
        
    @RequestMapping(value = "disease", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<SelectOption> getDiseaseOptionsContainingTerm(@RequestParam(value="term") String term) {
        logger.info("Searching for disease term '{}'", term);
        return findSelectOptionContainingTerm(term, diseaseSelectOptions);
    }
    
    @RequestMapping(value = "hpo", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<SelectOption> getHpoTermOptionsContainingTerm(@RequestParam(value="term") String term) {
        logger.info("Searching for HPO term '{}'", term);
        return findSelectOptionContainingTerm(term, hpoSelectOptions);
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
