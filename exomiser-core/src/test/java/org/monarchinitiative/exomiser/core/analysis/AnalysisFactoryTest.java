/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package org.monarchinitiative.exomiser.core.analysis;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisFactoryTest {

    private final PriorityFactory priorityFactory = new NoneTypePriorityFactoryStub();
    private final VariantDataService variantDataService = new VariantDataServiceStub();
    private final GeneFactory geneFactory = TestFactory.buildDefaultGeneFactory();
    private final VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory();

    private final AnalysisFactory instance = new AnalysisFactory(geneFactory, variantFactory, priorityFactory, variantDataService);

    @Test
    public void testCanMakeFullAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunnerForMode(AnalysisMode.FULL);
        assertThat(SimpleAnalysisRunner.class.isInstance(analysisRunner), is(true));
    }

    @Test
    public void testCanMakeSparseAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunnerForMode(AnalysisMode.SPARSE);
        assertThat(SparseAnalysisRunner.class.isInstance(analysisRunner), is(true));
    }

    @Test
    public void testCanMakePassOnlyAnalysisRunner() {
        AnalysisRunner analysisRunner = instance.getAnalysisRunnerForMode(AnalysisMode.PASS_ONLY);
        assertThat(PassOnlyAnalysisRunner.class.isInstance(analysisRunner), is(true));
    }

    @Test
    public void testCanMakeAnalysisBuilder() {
        assertThat(instance.getAnalysisBuilder(), notNullValue());
    }

}
