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
package org.monarchinitiative.exomiser.core;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.*;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.phenotype.service.TestOntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactoryImpl;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserTest {

    private static final Path VCF_PATH = Paths.get("src/test/resources/smallTest.vcf");

    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(TestFactory
            .buildDefaultHg19GenomeAnalysisService());
    private final PriorityFactory priorityFactory = new PriorityFactoryImpl(TestPriorityServiceFactory.testPriorityService(), null, null);
    private final OntologyService ontologyService = TestOntologyService.builder().build();

    private final AnalysisFactory analysisFactory = new AnalysisFactory(genomeAnalysisServiceProvider, priorityFactory, ontologyService);
    //AnalysisFactory is only ever used here, but it provides a clean interface to the Analysis module
    private final Exomiser instance = new Exomiser(analysisFactory);

    private final Sample sample = Sample.builder().vcfPath(VCF_PATH).build();

    private Analysis makeAnalysisWithMode(AnalysisMode analysisMode) {
        return instance.getAnalysisBuilder()
                .analysisMode(analysisMode)
                .build();
    }

    @Test
    public void canRunAnalysisFull() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.FULL);
        AnalysisResults analysisResults = instance.run(sample, analysis);
        analysisResults.getGenes().forEach(System.out::println);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
    }
    
    @Test
    public void canRunAnalysisPassOnly() {
        Analysis analysis = makeAnalysisWithMode(AnalysisMode.PASS_ONLY);
        AnalysisResults analysisResults = instance.run(sample, analysis);
        assertThat(analysisResults.getGenes().size(), equalTo(2));
    }

    @Test
    public void canRunAnalysisUsingAlternateGenomeAssemblyPassOnly() {
        GenomeAnalysisService grch37Service = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG19);
        GenomeAnalysisService grch38Service = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG38);

        GenomeAnalysisServiceProvider twoAssemblyProvider = new GenomeAnalysisServiceProvider(grch37Service, grch38Service);
        AnalysisFactory analysisFactory = new AnalysisFactory(twoAssemblyProvider, priorityFactory, ontologyService);

        Exomiser twoAssembliesSupportedExomiser = new Exomiser(analysisFactory);

        Sample hg37Sample = Sample.builder()
                .vcfPath(VCF_PATH)
                .genomeAssembly(GenomeAssembly.HG19)
                .build();

        Analysis hg37Analysis = twoAssembliesSupportedExomiser.getAnalysisBuilder()
                .analysisMode(AnalysisMode.PASS_ONLY)
                .build();
        AnalysisResults hg37AnalysisResults = twoAssembliesSupportedExomiser.run(hg37Sample, hg37Analysis);
        assertThat(hg37AnalysisResults.getGenes().size(), equalTo(2));


        Sample hg38Sample = Sample.builder()
                .vcfPath(VCF_PATH)
                .genomeAssembly(GenomeAssembly.HG38)
                .build();

        Analysis hg38Analysis = twoAssembliesSupportedExomiser.getAnalysisBuilder()
                .analysisMode(AnalysisMode.PASS_ONLY)
                .build();
        AnalysisResults hg38AnalysisResults = twoAssembliesSupportedExomiser.run(hg38Sample, hg38Analysis);
        assertThat(hg38AnalysisResults.getGenes().size(), equalTo(2));
    }

    @Test
    public void canGetAnalysisBuilder() {
        AnalysisBuilder analysisBuilder = instance.getAnalysisBuilder();
        assertThat(analysisBuilder, instanceOf(AnalysisBuilder.class));
    }

 }
