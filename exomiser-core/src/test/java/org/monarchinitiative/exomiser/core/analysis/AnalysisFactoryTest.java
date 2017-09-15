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

package org.monarchinitiative.exomiser.core.analysis;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisFactoryTest {

    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(TestFactory
            .buildStubGenomeAnalysisService(GenomeAssembly.HG19));
    private final VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory();
    private final PriorityFactory priorityFactory = new NoneTypePriorityFactoryStub();

    private final AnalysisFactory instance = new AnalysisFactory(genomeAnalysisServiceProvider, variantFactory, priorityFactory);

    @Test
    public void testCanMakeFullAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunner(GenomeAssembly.HG19, AnalysisMode.FULL);
        assertThat(analysisRunner, instanceOf(SimpleAnalysisRunner.class));
    }

    @Test
    public void testCanMakeSparseAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunner(GenomeAssembly.HG19, AnalysisMode.SPARSE);
        assertThat(analysisRunner, instanceOf(SparseAnalysisRunner.class));
    }

    @Test
    public void testCanMakePassOnlyAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunner(GenomeAssembly.HG19, AnalysisMode.PASS_ONLY);
        assertThat(analysisRunner, instanceOf(PassOnlyAnalysisRunner.class));
    }

    @Test(expected = UnsupportedGenomeAssemblyException.class)
    public void testGetAnalysisRunnerThrowsExceptionWhenUnsupportedGenomeAssemblyIsSpecified() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunner(GenomeAssembly.HG38, AnalysisMode.FULL);
        assertThat(analysisRunner, instanceOf(SimpleAnalysisRunner.class));
    }

    @Test
    public void testCanMakeAnalysisBuilder() {
        assertThat(instance.getAnalysisBuilder(), notNullValue());
    }

}
