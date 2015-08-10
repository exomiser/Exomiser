/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.*;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import org.apache.commons.jexl2.parser.ParserConstants;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleAnalysisRunnerTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAnalysisRunnerTest.class);

    private SimpleAnalysisRunner instance;

    private final Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
    private final JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
    private final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(testJannovarData.getRefDict(), testJannovarData.getChromosomes());
    private final VariantFactory variantFactory = new VariantFactory(new VariantAnnotator(variantContextAnnotator));

    private final SampleDataFactory sampleDataFactory = new SampleDataFactory(variantFactory, testJannovarData);
    private final VariantDataService stubDataService = new VariantDataServiceStub();

    @Before
    public void setUp() {
        instance = new SimpleAnalysisRunner(sampleDataFactory, stubDataService);
    }

    private Analysis makeAnalysis(Path vcfPath, AnalysisStep... analysisSteps) {
        Analysis analysis = new Analysis();
        analysis.setVcfPath(vcfPath);
        if (analysisSteps.length != 0) {
            analysis.addAllSteps(Arrays.asList(analysisSteps));
        }
        return analysis;
    }

    private Map<String, Gene> makeResults(List<Gene> genes) {
        return genes.stream().collect(toMap(Gene::getGeneSymbol, gene -> (gene)));
    }

    private void printResults(SampleData sampleData) {
        for (Gene gene : sampleData.getGenes()) {
            logger.info("{}", gene);
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                logger.info("{}", variantEvaluation);
            }
        }
    }

    @Test
    public void canRunAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setVcfPath(vcfPath);
        instance.runAnalysis(analysis);
    }

    @Test
    public void testRunAnalysis_NoFiltersNoPrioritisers() {
        Analysis analysis = makeAnalysis(vcfPath, new AnalysisStep[]{});
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));
    }

    @Test
    public void testRunAnalysis_SingleVariantFilterOnly() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getVariantEvaluations().size(), equalTo(1));

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(true));
        assertThat(rbm8a.getVariantEvaluations().size(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().size(), equalTo(1));
        VariantEvaluation variantEvaluation = rbm8a.getPassedVariantEvaluations().get(0);
        assertThat(variantEvaluation.getChromosome(), equalTo(1));
        assertThat(variantEvaluation.getPosition(), equalTo(145508800));

    }

    @Test
    public void testRunAnalysis_TwoVariantFilters_AllVariantsShouldHaveAllVariantFilterResults() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));
        VariantFilter qualityFilter = new QualityFilter(9999999f);

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter, qualityFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(2));

        Map<String, Gene> results = makeResults(sampleData.getGenes());

        Gene gnrh2 = results.get("GNRHR2");
        assertThat(gnrh2.passedFilters(), is(false));
        assertThat(gnrh2.getVariantEvaluations().size(), equalTo(1));
        for (VariantEvaluation variantEvaluation : gnrh2.getVariantEvaluations()) {
            assertThat(variantEvaluation.passedFilter(FilterType.QUALITY_FILTER), is(false));
        }

        Gene rbm8a = results.get("RBM8A");
        assertThat(rbm8a.passedFilters(), is(false));
        assertThat(rbm8a.getVariantEvaluations().size(), equalTo(2));
        assertThat(rbm8a.getPassedVariantEvaluations().isEmpty(), is(true));
        for (VariantEvaluation variantEvaluation : rbm8a.getVariantEvaluations()) {
            assertThat(variantEvaluation.passedFilter(FilterType.QUALITY_FILTER), is(false));
            if (variantEvaluation.getPosition() == 145508800) {
                assertThat(variantEvaluation.passedFilter(FilterType.INTERVAL_FILTER), is(true));
            }
        }

    }

}
