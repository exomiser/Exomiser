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
package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneFactoryTest {

    private static final JannovarData DEFAULT_JANNOVAR_DATA = TestFactory.buildDefaultJannovarData();

    private GeneFactory instance = new GeneFactory(DEFAULT_JANNOVAR_DATA);

    @Test
    public void testCreateKnownGeneIds() {
        Set<GeneIdentifier> expected = Sets.newHashSet(TestFactory.buildGeneIdentifiers());
        Set<GeneIdentifier> knownGeneIds = instance.createKnownGeneIds();
        assertThat(knownGeneIds, equalTo(expected));
    }

    @Test
    public void testCreateKnownGenes() {
        Set<Gene> expected = Sets.newHashSet(TestGeneFactory.buildGenes());
        Set<Gene> knownGenes = Sets.newHashSet(instance.createKnownGenes());
        assertThat(knownGenes, equalTo(expected));
    }

    @Test
    public void testCreateKnownGeneIdentifiers() throws Exception {
        Map<String, String> expected = TestFactory.buildGeneIdentifiers().stream().collect(toMap(GeneIdentifier::getEntrezId, GeneIdentifier::getGeneSymbol));
        Map<String, String> knownGenes = instance.createKnownGeneIdentifiers();
        assertThat(knownGenes, equalTo(expected));
    }
}
