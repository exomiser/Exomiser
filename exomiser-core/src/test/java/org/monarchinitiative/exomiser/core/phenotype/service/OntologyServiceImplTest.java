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
package org.monarchinitiative.exomiser.core.phenotype.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
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
    private HumanPhenotypeOntologyDao mockHpoDao;
    @Mock
    private MousePhenotypeOntologyDao mockMpoDao;
    @Mock
    private ZebraFishPhenotypeOntologyDao mockZpoDao;

    private List<String> diseaseHpoIds;
    private Set<PhenotypeTerm> hpoTerms;
    private Set<PhenotypeTerm> mpoTerms;
    private Set<PhenotypeTerm> zpoTerms;

    private final PhenotypeTerm fingerJointHyperExtensibility = PhenotypeTerm.of("HP:0001187", "Hyperextensibility of the finger joints");
    private final PhenotypeTerm conjunctivalNodule = PhenotypeTerm.of("HP:0009903", "Conjunctival nodule");
    private final PhenotypeTerm cleftHelix = PhenotypeTerm.of("HP:0009902", "Cleft helix");
    private final PhenotypeTerm thinEarHelix = PhenotypeTerm.of("HP:0009905", "Thin ear helix");

    @Before
    public void setUp() {
        diseaseHpoIds = new ArrayList<>();
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
        Mockito.when(mockHpoDao.getAllTerms()).thenReturn(hpoTerms);
        Mockito.when(mockHpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());

        Mockito.when(mockMpoDao.getAllTerms()).thenReturn(mpoTerms);
        Mockito.when(mockMpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());

        Mockito.when(mockZpoDao.getAllTerms()).thenReturn(zpoTerms);
        Mockito.when(mockZpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());
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
        assertThat(instance.getHpoMatchesForHpoTerm(cleftHelix), equalTo(Collections.emptySet()));
    }

    @Test
    public void canGetPhenotypeMatchesForMpoTerm() {
        assertThat(instance.getMpoMatchesForHpoTerm(cleftHelix), equalTo(Collections.emptySet()));
    }

    @Test
    public void canGetPhenotypeMatchesForZpoTerm() {
        assertThat(instance.getZpoMatchesForHpoTerm(cleftHelix), equalTo(Collections.emptySet()));
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
