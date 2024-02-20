/*
 * The Exomiser - A tool to annotate and prioritize genomic variants 
 *                           
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for Class DiseaseInheritanceCacheReader.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DiseaseInheritanceCacheReaderTest {

    private final Map<String, InheritanceMode> result;

    public DiseaseInheritanceCacheReaderTest() {
        Resource testResource = Resource.of("src/test/resources/data/phenotype_test.hpoa");
        DiseaseInheritanceCacheReader instance = new DiseaseInheritanceCacheReader(testResource);
        result = instance.read();
    }

    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeUnknownOrphanet() {
        assertThat(result.get("ORPHA:36237"), is(nullValue()));
    }

    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeBoth() {
        assertThat(result.get("OMIM:614669"), equalTo(InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE));
    }

    @Test
    void testsXlinkedDominant() {
        assertThat(result.get("OMIM:301050"), equalTo(InheritanceMode.X_DOMINANT));
    }

    @Test
    void testXlinked() {
        // OMIM:300958 is XR & XD, so we're calling it X
        assertThat(result.get("OMIM:300958"), equalTo(InheritanceMode.X_LINKED));
    }

    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeRecessive() {
        assertThat(result.get("OMIM:100100"), equalTo(InheritanceMode.AUTOSOMAL_RECESSIVE));
    }

    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeDominant() {
        assertThat(result.get("OMIM:100200"), equalTo(InheritanceMode.AUTOSOMAL_DOMINANT));
    }

    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeUnknownDisease() {
        assertThat(result.get("OMIM:000000"), is(nullValue()));
    }

    /**
     * Test of getInheritanceMode method, of class DiseaseInheritanceCache.
     */
    @Test
    public void testGetInheritanceCodeMitochondrial() {
        assertThat(result.get("OMIM:560000"), equalTo(InheritanceMode.MITOCHONDRIAL));
    }

    @Test
    public void testIsEmptyFalse() {
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    void testDuplicatedNumericalIdentifier() {
        assertThat(result.get("OMIM:230800"), equalTo(InheritanceMode.AUTOSOMAL_RECESSIVE));
        assertThat(result.get("ORPHA:230800"), is(nullValue()));
    }
}
