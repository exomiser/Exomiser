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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.*;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.genome.VariantDataServiceStub;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactoryImpl;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserTest {

    private static final Path VCF_PATH = Paths.get("src/test/resources/smallTest.vcf");

    private final VariantDataService stubDataService = new VariantDataServiceStub();
    private final PriorityFactory priorityFactory = new PriorityFactoryImpl(TestPriorityServiceFactory.TEST_SERVICE, null, null, null);

    private final AnalysisFactory analysisFactory = new AnalysisFactory(TestFactory.buildDefaultGeneFactory(), TestFactory
            .buildDefaultVariantFactory(), priorityFactory, stubDataService);
    //AnalysisFactory is only ever used here, but it provides a clean interface to the Analysis module
    private Exomiser instance = new Exomiser(analysisFactory);

    private Analysis makeAnalysisWithMode(AnalysisMode analysisMode) {
        return instance.getAnalysisBuilder()
                .vcfPath(VCF_PATH)
                .analysisMode(analysisMode)
                .build();
    }
    
    @Test
    public void canRunAnalysis_Full() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.FULL);
        AnalysisResults analysisResults = instance.run(analysis);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
    }
    
    @Test
    public void canRunAnalysis_Sparse() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.SPARSE);
        AnalysisResults analysisResults = instance.run(analysis);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
    }
    
    @Test
    public void canRunAnalysis_PassOnly() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.PASS_ONLY);
        AnalysisResults analysisResults = instance.run(analysis);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
    }

    @Test
    public void canGetAnalysisBuilder() {
        AnalysisBuilder analysisBuilder = instance.getAnalysisBuilder();
        assertThat(analysisBuilder, notNullValue());
    }

 }
