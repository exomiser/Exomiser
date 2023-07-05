/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyServiceImplTest {

    private OntologyServiceImpl instance;

    @Mock
    private HumanPhenotypeOntologyDao mockHpoDao;
    @Mock
    private MousePhenotypeOntologyDao mockMpoDao;
    @Mock
    private ZebraFishPhenotypeOntologyDao mockZpoDao;

    private final PhenotypeTerm fingerJointHyperExtensibility = PhenotypeTerm.of("HP:0001187", "Hyperextensibility of the finger joints");
    private final PhenotypeTerm conjunctivalNodule = PhenotypeTerm.of("HP:0009903", "Conjunctival nodule");
    private final PhenotypeTerm cleftHelix = PhenotypeTerm.of("HP:0009902", "Cleft helix");
    private final PhenotypeTerm thinEarHelix = PhenotypeTerm.of("HP:0009905", "Thin ear helix");

    private final Set<PhenotypeTerm> hpoTerms = ImmutableSet.of(fingerJointHyperExtensibility, conjunctivalNodule, cleftHelix, thinEarHelix);
    private final Set<PhenotypeTerm> mpoTerms = Collections.emptySet();
    private final Set<PhenotypeTerm> zpoTerms = Collections.emptySet();

    @BeforeEach
    public void setUp() {
        Mockito.when(mockHpoDao.getAllTerms()).thenReturn(hpoTerms);
        Mockito.when(mockHpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());

        Mockito.when(mockMpoDao.getAllTerms()).thenReturn(mpoTerms);
        Mockito.when(mockMpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());

        Mockito.when(mockZpoDao.getAllTerms()).thenReturn(zpoTerms);
        Mockito.when(mockZpoDao.getPhenotypeMatchesForHpoTerm(Mockito.any())).thenReturn(Collections.emptySet());

        instance = new OntologyServiceImpl(mockHpoDao, mockMpoDao, mockZpoDao);
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
        assertThat(instance.getPhenotypeTermForHpoId(fingerJointHyperExtensibility.id()), equalTo(fingerJointHyperExtensibility));
    }

    @Test
    public void testReturnsNullForGivenHpoIdWhenHpoIdIsUnrecognised() {
        assertThat(instance.getPhenotypeTermForHpoId("invalidId"), equalTo(null));
    }

    @Test
    void testReturnsSameHpoIdsWhenGivenObsoleteIdsAndAltHpIdsNotAvailable() {

        Mockito.when(mockHpoDao.getIdToPhenotypeTerms())
                .thenReturn(ImmutableMap.of());

        instance = new OntologyServiceImpl(mockHpoDao, mockMpoDao, mockZpoDao);

        assertThat(instance.getCurrentHpoIds(ImmutableList.of("HP:0009902", "HP:0000000")), equalTo(ImmutableList.of("HP:0009902", "HP:0000000")));
        assertThat(instance.getCurrentHpoIds(ImmutableList.of("HP:0009902", "HP:0009905", "HP:0000000")), equalTo(ImmutableList.of("HP:0009902", "HP:0009905", "HP:0000000")));
    }

    @Test
    void testReturnsCurrentHpoIdsWhenGivenObsoleteIds() {

        String obsoleteThinEarHelixId = "HP:0000000";
        String currentThinEarHelixId = "HP:0009905";
        Mockito.when(mockHpoDao.getIdToPhenotypeTerms())
                .thenReturn(ImmutableMap.of(
                "HP:0009902", cleftHelix,
                        currentThinEarHelixId, thinEarHelix,
                        obsoleteThinEarHelixId, thinEarHelix
        ));
        instance = new OntologyServiceImpl(mockHpoDao, mockMpoDao, mockZpoDao);

        ImmutableList<String> expected = ImmutableList.of("HP:0009902", currentThinEarHelixId);

        assertThat(instance.getCurrentHpoIds(ImmutableList.of("HP:0009902", obsoleteThinEarHelixId)), equalTo(expected));
        assertThat(instance.getCurrentHpoIds(ImmutableList.of("HP:0009902", currentThinEarHelixId, obsoleteThinEarHelixId)), equalTo(expected));
    }
}
