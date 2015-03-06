/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.DiseaseIdentifier;
import de.charite.compbio.exomiser.core.model.GeneIdentifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
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
@Sql(scripts = {"file:src/test/resources/sql/create_disease.sql", "file:src/test/resources/sql/create_disease_hp.sql", "file:src/test/resources/sql/diseaseDaoTestData.sql"})
public class DefaultDiseaseDaoTest {
    
    @Autowired
    DiseaseDao instance;
    
    @Test(expected = UnsupportedOperationException.class)
    public void testGetDisease() {
        instance.getDisease(new DiseaseIdentifier("OMIM:101600"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetAllDiseases() {
        instance.getAllDiseases();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetKnownDiseasesForGene() {
        instance.getKnownDiseasesForGene(new GeneIdentifier("Fgfr2", "MGI:11111"));
    }

    @Test
    public void testGetHpoIdsForDiseaseId() {
        Set<String> omim101600HpoIds = new TreeSet<>(
                Arrays.asList("HP:0000174","HP:0000194","HP:0000218","HP:0000238",
                        "HP:0000244","HP:0000272","HP:0000303","HP:0000316","HP:0000322",
                        "HP:0000324","HP:0000327","HP:0000348","HP:0000431","HP:0000452",
                        "HP:0000453","HP:0000470","HP:0000486","HP:0000494","HP:0000508",
                        "HP:0000586","HP:0000678","HP:0001156","HP:0001249","HP:0002308",
                        "HP:0002676","HP:0002780","HP:0003041","HP:0003070","HP:0003196",
                        "HP:0003272","HP:0003307","HP:0003795","HP:0004209","HP:0004322",
                        "HP:0004440","HP:0005048","HP:0005280","HP:0005347","HP:0006101",
                        "HP:0006110","HP:0009602","HP:0009773","HP:0010055","HP:0010669",
                        "HP:0011304"));
        assertThat(instance.getHpoIdsForDiseaseId("OMIM:101600"), equalTo(omim101600HpoIds));
    }
    
}
