/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.dao.HumanPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.MousePhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.OntologyDao;
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class OntologyServiceImplTest {
    
    @InjectMocks
    private OntologyServiceImpl instance;
    
    @Mock
    DiseaseDao mockDiseaseDao;
    
    @Mock
    HumanPhenotypeOntologyDao mockHpoDao;
    @Mock
    MousePhenotypeOntologyDao mockMpoDao;
    @Mock
    ZebraFishPhenotypeOntologyDao mockZpoDao;
    
    private final String diseaseIdInDatabase = "DISEASE:00000";
    private final String diseaseIdNotInDatabase = "DISEASE:99999";
    private Set<String> diseaseHpoIds;
    private Set<PhenotypeTerm> hpoTerms;
    private Set<PhenotypeTerm> mpoTerms;
    private Set<PhenotypeTerm> zpoTerms;
    
    @Before
    public void setUp() {
        diseaseHpoIds = new TreeSet<>();
        diseaseHpoIds.addAll(Arrays.asList("HP:000000", "HP:000001", "HP:000002", "HP:000003", "HP:000004"));

        hpoTerms = new HashSet<>();
        setUpHpoTerms();
        
        setUpDaoMocks();
    }

    private void setUpHpoTerms() {
        PhenotypeTerm fingerJointHyperExtensibility = new PhenotypeTerm("HP:0001187", "Hyperextensibility of the finger joints", 0.0);
        hpoTerms.add(fingerJointHyperExtensibility);
        PhenotypeTerm conjunctivalNodule = new PhenotypeTerm("HP:0009903", "Conjunctival nodule", 0.0);
        hpoTerms.add(conjunctivalNodule);
        PhenotypeTerm cleftHelix = new PhenotypeTerm("HP:0009902", "Cleft helix", 0.0);
        hpoTerms.add(cleftHelix);
        PhenotypeTerm thinEarHelix = new PhenotypeTerm("HP:0009905", "Thin ear helix", 0.0);
        hpoTerms.add(thinEarHelix);
    }

    private void setUpDaoMocks() {
        setUpDiseaseDaoMock();
        Mockito.when(mockHpoDao.getAllTerms()).thenReturn(hpoTerms);
        Mockito.when(mockMpoDao.getAllTerms()).thenReturn(mpoTerms);
        Mockito.when(mockZpoDao.getAllTerms()).thenReturn(zpoTerms);
    }

    private void setUpDiseaseDaoMock() {
        Mockito.when(mockDiseaseDao.getHpoIdsForDiseaseId(diseaseIdInDatabase)).thenReturn(diseaseHpoIds);
        Set<String> emptyStringSet = Collections.emptySet();
        Mockito.when(mockDiseaseDao.getHpoIdsForDiseaseId(diseaseIdNotInDatabase)).thenReturn(emptyStringSet);
    }

    @Test
    public void testGetHpoIdsForDiseaseInDatabase() {
        List<String> diseaseHpoIdList = new ArrayList<>(diseaseHpoIds);
        assertThat(instance.getHpoIdsForDiseaseId(diseaseIdInDatabase), equalTo(diseaseHpoIdList));
    }
    
    @Test
    public void testGetHpoIdsForDiseaseNotInDatabaseReturnsEmptyList() {
        assertThat(instance.getHpoIdsForDiseaseId(diseaseIdNotInDatabase).isEmpty(), is(true));
    }
    
    @Test
    public void testGetHpoTerms() {
        assertThat(instance.getHpoTerms(), equalTo(hpoTerms));
    }
    
    @Test
    public void testGetMpoTerms() {
        assertThat(instance.getMpoTerms(), equalTo(mpoTerms));
    }
    
    @Test
    public void testGetZpoTerms() {
        assertThat(instance.getZpoTerms(), equalTo(zpoTerms));
    }
}
