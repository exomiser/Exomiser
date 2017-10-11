/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeAnalysisServiceProviderTest {

    private static final GenomeAnalysisService HG19_SERVICE = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG19);
    private static final GenomeAnalysisService HG38_SERVICE = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG38);

    @Test(expected = NullPointerException.class)
    public void testWontInstantiateWithNullDefaultInput() {
        new GenomeAnalysisServiceProvider((GenomeAnalysisService) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWontInstantiateWithNullDefaultInputArray() {
        new GenomeAnalysisServiceProvider((GenomeAnalysisService[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testWontInstantiateWithNullAlternateInput() {
        new GenomeAnalysisServiceProvider(HG19_SERVICE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWontInstantiateWithEmptyInput() {
        new GenomeAnalysisServiceProvider();
    }

    @Test
    public void testCanAddAlternateServices() {
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(HG19_SERVICE, HG38_SERVICE);
        assertThat(instance.get(GenomeAssembly.HG38), equalTo(HG38_SERVICE));
    }

    @Test
    public void testGetProvidedAssemblies() {
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(HG19_SERVICE, HG38_SERVICE);
        assertThat(instance.getProvidedAssemblies(), equalTo(Sets.immutableEnumSet(GenomeAssembly.HG19, GenomeAssembly.HG38)));
    }

    @Test
    public void testHasServiceForSingleAssembly() {
        GenomeAnalysisService hg19Service = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG19);
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(hg19Service);
        assertThat(instance.hasServiceFor(GenomeAssembly.HG19), is(true));
        assertThat(instance.hasServiceFor(GenomeAssembly.HG38), is(false));
    }

    @Test
    public void testHasServiceForMultipleAssemblies() {
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(HG19_SERVICE, HG38_SERVICE);
        assertThat(instance.hasServiceFor(GenomeAssembly.HG19), is(true));
        assertThat(instance.hasServiceFor(GenomeAssembly.HG38), is(true));
    }

    @Test
    public void testGetReturnsExpectedService() {
        GenomeAssembly defaultAssembly = HG19_SERVICE.getGenomeAssembly();
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(HG19_SERVICE);
        assertThat(instance.get(defaultAssembly), equalTo(HG19_SERVICE));
    }

    @Test
    public void testGetOrDefaultReturnsExpectedService() {
        GenomeAssembly defaultAssembly = HG19_SERVICE.getGenomeAssembly();
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(HG19_SERVICE);
        assertThat(instance.getOrDefault(defaultAssembly, HG19_SERVICE), equalTo(HG19_SERVICE));
    }

    @Test
    public void testGetOrDefaultReturnsDefaultWhenRequestedAssemblyNotPresent() {
        GenomeAnalysisServiceProvider instance = new GenomeAnalysisServiceProvider(HG19_SERVICE);
        assertThat(instance.getOrDefault(GenomeAssembly.HG38, HG19_SERVICE), equalTo(HG19_SERVICE));
    }

    @Test
    public void testToString() {
        System.out.println(new GenomeAnalysisServiceProvider(HG19_SERVICE, HG38_SERVICE));
    }
}