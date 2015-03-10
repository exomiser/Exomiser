/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DaoTestConfig.class)
@Sql(scripts = {"file:src/test/resources/sql/create_hpo.sql", "file:src/test/resources/sql/humanPhenotypeOntologyDaoTestData.sql"})
public class HumanPhenotypeOntologyDaoTest {

    @Autowired
    HumanPhenotypeOntologyDao instance;

    private Set<PhenotypeTerm> allHpoTerms;
    private Map<String, String> allHpoAsStrings;
    
    private final PhenotypeTerm multicysticKidneyDysplasia = new PhenotypeTerm("HP:0000003", "Multicystic kidney dysplasia", 0d);
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
        PhenotypeTerm renalDiverticulum = new PhenotypeTerm("HP:0100877", "Renal diverticulum", 0d);
        PhenotypeTerm renalCyst = new PhenotypeTerm("HP:0000107", "Renal cyst", 5.347805d);
        PhenotypeMatch diverticulumMatch = new PhenotypeMatch(multicysticKidneyDysplasia, renalDiverticulum, 0.769231, 2.028225214383722, renalCyst);
        phenotypeMatches.add(diverticulumMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.380952	3.020727	1.0727310768221452	HP:0100881	Congenital mesoblastic nephroma	HP:0000077	Abnormality of the kidney
        PhenotypeTerm mesoblasticNephroma = new PhenotypeTerm("HP:0100881", "Congenital mesoblastic nephroma", 0d);
        PhenotypeTerm kidneyAbnormality = new PhenotypeTerm("HP:0000077", "Abnormality of the kidney", 3.020727d);
        PhenotypeMatch mesoblasticMatch = new PhenotypeMatch(multicysticKidneyDysplasia, mesoblasticNephroma, 0.380952, 1.0727310768221452, kidneyAbnormality);
        phenotypeMatches.add(mesoblasticMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.75	3.442544	1.6068316612497417	HP:0100880	Nephrogenic rest	HP:0012210	Abnormal renal morphology
        PhenotypeTerm nephrogenicRest = new PhenotypeTerm("HP:0100880", "Nephrogenic rest", 0d);
        PhenotypeTerm abnormalRenalMorphology = new PhenotypeTerm("HP:0012210", "Abnormal renal morphology", 3.442544d);
        PhenotypeMatch nephrogenicRestMatch = new PhenotypeMatch(multicysticKidneyDysplasia, nephrogenicRest, 0.75, 1.6068316612497417, abnormalRenalMorphology);
        phenotypeMatches.add(nephrogenicRestMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.642857	3.442544	1.48763690115246	HP:0001917	Renal amyloidosis	HP:0012210	Abnormal renal morphology
        PhenotypeTerm renalAmyloidosis = new PhenotypeTerm("HP:0001917", "Renal amyloidosis", 0d);
        PhenotypeMatch renalAmyloidosisMatch = new PhenotypeMatch(multicysticKidneyDysplasia, renalAmyloidosis, 0.642857, 1.48763690115246, abnormalRenalMorphology);
        phenotypeMatches.add(renalAmyloidosisMatch);
        
//        SIMJ	IC	SCORE	HP_ID_HIT	HP_HIT_TERM	LCS_ID	LCS_TERM
//0.533333	3.020727	1.269272768407093	HP:0001919	Acute kidney injury	HP:0000077	Abnormality of the kidney
        PhenotypeTerm acuteKidneyInjury = new PhenotypeTerm("HP:0001919", "Acute kidney injury", 0d);
        PhenotypeMatch acuteKidneyInjuryMatch = new PhenotypeMatch(multicysticKidneyDysplasia, acuteKidneyInjury, 0.533333, 1.269272768407093, kidneyAbnormality);
        phenotypeMatches.add(acuteKidneyInjuryMatch);
          
        allHpoTerms = new HashSet<>();
        for (Entry<String, String> termAsStrings : allHpoAsStrings.entrySet()) {
            allHpoTerms.add(new PhenotypeTerm(termAsStrings.getKey(), termAsStrings.getValue(), 0d));
        }
    }

    @Test
    public void testGetAllTerms() {
        assertThat(instance.getAllTerms(), equalTo(allHpoTerms));
    }
    
    @Test
    public void testGetPhenotypeMatchesForNonExistentHpoTermReturnsEmptySet() {
        PhenotypeTerm nonExistentTerm = new PhenotypeTerm("", "", 0d);
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
