/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class PrioritiserServiceImplTest {
    
    @InjectMocks
    private PrioritiserServiceImpl instance;
    
    @Mock
    DiseaseDao mockDiseaseDao;
    
    private final String diseaseIdInDatabase = "DISEASE:00000";
    private final String diseaseIdNotInDatabase = "DISEASE:99999";
    private Set<String> diseaseHpoIds;
    
    @Before
    public void setUp() {
        diseaseHpoIds = new TreeSet<>();
        diseaseHpoIds.addAll(Arrays.asList("HP:000000", "HP:000001", "HP:000002", "HP:000003", "HP:000004"));
        
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
    
}
