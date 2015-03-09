/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
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

    @Before
    public void setUp() {
        allHpoAsStrings = new HashMap<>();
        allHpoAsStrings.put("HP:0000001", "All");
        allHpoAsStrings.put("HP:0000002", "Abnormality of body height");
        allHpoAsStrings.put("HP:0000003", "Multicystic kidney dysplasia");
        allHpoAsStrings.put("HP:0000005", "Mode of inheritance");
        allHpoAsStrings.put("HP:0000006", "Autosomal dominant inheritance");
        
        allHpoTerms = new HashSet<>();
        for (Entry<String, String> termAsStrings : allHpoAsStrings.entrySet()) {
            allHpoTerms.add(new PhenotypeTerm(termAsStrings.getKey(), termAsStrings.getValue(), 0d));
        }
    }

    @Test
    public void testGetAllTerms() {
        assertThat(instance.getAllTerms(), equalTo(allHpoTerms));
    }

}
