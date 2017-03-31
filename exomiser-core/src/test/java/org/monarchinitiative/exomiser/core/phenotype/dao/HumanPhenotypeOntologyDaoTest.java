/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, HumanPhenotypeOntologyDao.class})
@Sql(scripts = {"" +
        "file:src/test/resources/sql/create_hpo.sql",
        "file:src/test/resources/sql/humanPhenotypeOntologyDaoTestData.sql"})
public class HumanPhenotypeOntologyDaoTest {

    @Autowired
    private HumanPhenotypeOntologyDao instance;

    private Set<PhenotypeTerm> allHpoTerms;
    private Map<String, String> allHpoAsStrings;
    
    private final PhenotypeTerm multicysticKidneyDysplasia = PhenotypeTerm.of("HP:0000003", "Multicystic kidney dysplasia");
    private Set<PhenotypeMatch> phenotypeMatches;
    
    @Before
    public void setUp() {
        allHpoAsStrings = new HashMap<>();
        allHpoAsStrings.put("HP:0000001", "All");
        allHpoAsStrings.put("HP:0000002", "Abnormality of body height");
        allHpoAsStrings.put("HP:0000003", "Multicystic kidney dysplasia");
        allHpoAsStrings.put("HP:0000005", "Mode of inheritance");
        allHpoAsStrings.put("HP:0000006", "Autosomal dominant inheritance");
        
        phenotypeMatches = new LinkedHashSet<>();
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.769231	5.347805	2.028225214383722	HP:0100877	Renal diverticulum	HP:0000107	Renal cyst
        PhenotypeTerm renalDiverticulum = PhenotypeTerm.of("HP:0100877", "Renal diverticulum");
        PhenotypeTerm renalCyst = PhenotypeTerm.of("HP:0000107", "Renal cyst");
        PhenotypeMatch diverticulumMatch = PhenotypeMatch.builder()
                .query(multicysticKidneyDysplasia)
                .match(renalDiverticulum)
                .lcs(renalCyst)
                .simj(0.769231)
                .ic(5.347805)
                .score(2.028225214383722)
                .build();
        phenotypeMatches.add(diverticulumMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.380952	3.020727	1.0727310768221452	HP:0100881	Congenital mesoblastic nephroma	HP:0000077	Abnormality of the kidney
        PhenotypeTerm mesoblasticNephroma = PhenotypeTerm.of("HP:0100881", "Congenital mesoblastic nephroma");
        PhenotypeTerm kidneyAbnormality = PhenotypeTerm.of("HP:0000077", "Abnormality of the kidney");
        PhenotypeMatch mesoblasticMatch = PhenotypeMatch.builder()
                .query(multicysticKidneyDysplasia)
                .match(mesoblasticNephroma)
                .lcs(kidneyAbnormality)
                .simj(0.380952)
                .ic(3.020727)
                .score(1.0727310768221452)
                .build();
        phenotypeMatches.add(mesoblasticMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.75	3.442544	1.6068316612497417	HP:0100880	Nephrogenic rest	HP:0012210	Abnormal renal morphology
        PhenotypeTerm nephrogenicRest = PhenotypeTerm.of("HP:0100880", "Nephrogenic rest");
        PhenotypeTerm abnormalRenalMorphology = PhenotypeTerm.of("HP:0012210", "Abnormal renal morphology");
        PhenotypeMatch nephrogenicRestMatch = PhenotypeMatch.builder()
                .query(multicysticKidneyDysplasia)
                .match(nephrogenicRest)
                .lcs(abnormalRenalMorphology)
                .simj(0.75)
                .ic(3.442544)
                .score(1.6068316612497417)
                .build();
        phenotypeMatches.add(nephrogenicRestMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.642857	3.442544	1.48763690115246	HP:0001917	Renal amyloidosis	HP:0012210	Abnormal renal morphology
        PhenotypeTerm renalAmyloidosis = PhenotypeTerm.of("HP:0001917", "Renal amyloidosis");
        PhenotypeMatch renalAmyloidosisMatch = PhenotypeMatch.builder()
                .query(multicysticKidneyDysplasia)
                .match(renalAmyloidosis)
                .lcs(abnormalRenalMorphology)
                .simj(0.642857)
                .ic(3.442544)
                .score(1.48763690115246)
                .build();
        phenotypeMatches.add(renalAmyloidosisMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.533333	3.020727	1.269272768407093	HP:0001919	Acute kidney injury	HP:0000077	Abnormality of the kidney
        PhenotypeTerm acuteKidneyInjury = PhenotypeTerm.of("HP:0001919", "Acute kidney injury");
        PhenotypeMatch acuteKidneyInjuryMatch = PhenotypeMatch.builder()
                .query(multicysticKidneyDysplasia)
                .match(acuteKidneyInjury)
                .lcs(kidneyAbnormality)
                .ic(3.020727)
                .simj(0.533333)
                .score(1.269272768407093)
                .build();
        phenotypeMatches.add(acuteKidneyInjuryMatch);
          
        allHpoTerms = new HashSet<>();
        for (Entry<String, String> termAsStrings : allHpoAsStrings.entrySet()) {
            allHpoTerms.add(PhenotypeTerm.of(termAsStrings.getKey(), termAsStrings.getValue()));
        }
    }

    @Test
    public void testGetAllTerms() {
        assertThat(instance.getAllTerms(), equalTo(allHpoTerms));
    }
    
    @Test
    public void testGetPhenotypeMatchesForNonExistentHpoTermReturnsEmptySet() {
        PhenotypeTerm nonExistentTerm = PhenotypeTerm.of("", "");
        Set<PhenotypeMatch> matches = instance.getPhenotypeMatchesForHpoTerm(nonExistentTerm);
        assertThat(matches.isEmpty(), is(true));
    }
    
    @Test
    public void testGetPhenotypeMatchesForHpoTerm() {
        Set<PhenotypeMatch> matches = instance.getPhenotypeMatchesForHpoTerm(multicysticKidneyDysplasia);
        assertThat(matches.isEmpty(), is(false));
        assertThat(matches, equalTo(phenotypeMatches));
    }
}
