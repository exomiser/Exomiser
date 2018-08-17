/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.monarchinitiative.exomiser.core.genome.dao.RegulatoryFeatureDao;
import org.monarchinitiative.exomiser.core.genome.dao.TadDao;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.TopologicalDomain;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ExtendWith(MockitoExtension.class)
public class GenomeDataServiceImplTest {

    @InjectMocks
    private GenomeDataServiceImpl instance;

    @Mock
    private RegulatoryFeatureDao mockRegulatoryFeatureDao;
    @Mock
    private TadDao mockTadDao;
    @Mock
    private GeneFactory geneFactory;

    @Test
    public void getKnownGenes() throws Exception {
        List<Gene> allGenes = TestGeneFactory.buildGenes();
        Mockito.when(geneFactory.createKnownGenes()).thenReturn(allGenes);

        assertThat(instance.getKnownGenes(), equalTo(allGenes));
    }

    @Test
    public void getKnownGeneIdentifiers() throws Exception {
        Set<GeneIdentifier> geneIdentifiers = ImmutableSet.copyOf(TestGeneFactory.buildGeneIdentifiers());
        Mockito.when(geneFactory.getGeneIdentifiers()).thenReturn(geneIdentifiers);

        assertThat(instance.getKnownGeneIdentifiers(), equalTo(geneIdentifiers));
    }

    @Test
    public void serviceReturnsRegulatoryFeatures() {
        List<RegulatoryFeature> regulatoryFeatures = ImmutableList.of(new RegulatoryFeature(1, 10, 100, RegulatoryFeature.FeatureType.ENHANCER));
        Mockito.when(mockRegulatoryFeatureDao.getRegulatoryFeatures()).thenReturn(regulatoryFeatures);

        List<RegulatoryFeature> result = instance.getRegulatoryFeatures();
        assertThat(result, equalTo(regulatoryFeatures));
    }

    @Test
    public void serviceReturnsTopologicalDomains() {
        List<TopologicalDomain> tads = ImmutableList.of(new TopologicalDomain(1, 1, 2, Collections.emptyMap()));
        Mockito.when(mockTadDao.getAllTads()).thenReturn(tads);

        List<TopologicalDomain> topologicalDomains = instance.getTopologicallyAssociatedDomains();
        assertThat(topologicalDomains, equalTo(tads));
    }
}