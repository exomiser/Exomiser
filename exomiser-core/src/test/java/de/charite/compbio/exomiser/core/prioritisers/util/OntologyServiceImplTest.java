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
package de.charite.compbio.exomiser.core.prioritisers.util;

import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.dao.HumanPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.MousePhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    private final PhenotypeTerm fingerJointHyperExtensibility = PhenotypeTerm.of("HP:0001187", "Hyperextensibility of the finger joints");
    private final PhenotypeTerm conjunctivalNodule = PhenotypeTerm.of("HP:0009903", "Conjunctival nodule");
    private final PhenotypeTerm cleftHelix = PhenotypeTerm.of("HP:0009902", "Cleft helix");
    private final PhenotypeTerm thinEarHelix = PhenotypeTerm.of("HP:0009905", "Thin ear helix");

    @Before
    public void setUp() {
        diseaseHpoIds = new TreeSet<>();
        diseaseHpoIds.addAll(Arrays.asList("HP:000000", "HP:000001", "HP:000002", "HP:000003", "HP:000004"));

        hpoTerms = new HashSet<>();
        setUpHpoTerms();

        setUpDaoMocks();
    }

    private void setUpHpoTerms() {
        hpoTerms.add(fingerJointHyperExtensibility);
        hpoTerms.add(conjunctivalNodule);
        hpoTerms.add(cleftHelix);
        hpoTerms.add(thinEarHelix);
    }

    private void setUpDaoMocks() {
        setUpDiseaseDaoMock();
        Mockito.when(mockHpoDao.getAllTerms()).thenReturn(hpoTerms);
        Mockito.when(mockHpoDao.getPhenotypeMatchesForHpoTerm((PhenotypeTerm) Mockito.any())).thenReturn(Collections.EMPTY_SET);

        Mockito.when(mockMpoDao.getAllTerms()).thenReturn(mpoTerms);
        Mockito.when(mockMpoDao.getPhenotypeMatchesForHpoTerm((PhenotypeTerm) Mockito.any())).thenReturn(Collections.EMPTY_SET);

        Mockito.when(mockZpoDao.getAllTerms()).thenReturn(zpoTerms);
        Mockito.when(mockZpoDao.getPhenotypeMatchesForHpoTerm((PhenotypeTerm) Mockito.any())).thenReturn(Collections.EMPTY_SET);
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

    @Test
    public void canGetPhenotypeMatchesForHpoTerm() {
        assertThat(instance.getHpoMatchesForHpoTerm(cleftHelix), equalTo(Collections.EMPTY_SET));
    }

    @Test
    public void canGetPhenotypeMatchesForMpoTerm() {
        assertThat(instance.getMpoMatchesForHpoTerm(cleftHelix), equalTo(Collections.EMPTY_SET));
    }

    @Test
    public void canGetPhenotypeMatchesForZpoTerm() {
        assertThat(instance.getZpoMatchesForHpoTerm(cleftHelix), equalTo(Collections.EMPTY_SET));
    }

    @Test
    public void testReturnsPhenotypeTermForGivenHpoId() {
        assertThat(instance.getPhenotypeTermForHpoId(fingerJointHyperExtensibility.getId()), equalTo(fingerJointHyperExtensibility));
    }

    @Test
    public void testReturnsNullForGivenHpoIdWhenHpoIdIsUnrecognised() {
        assertThat(instance.getPhenotypeTermForHpoId("invalidId"), equalTo(null));
    }
}
