package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.*;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.MockPrioritiser;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunnerTest {

    private PassOnlyAnalysisRunner instance;

    private final Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");

    @Before
    public void setUp() {

        JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
        VariantFactory variantFactory = new VariantFactory(new VariantAnnotationsFactory(testJannovarData));

        VariantDataService stubDataService = new VariantDataServiceStub();
        instance = new PassOnlyAnalysisRunner(variantFactory, stubDataService);
    }

    private Analysis makeAnalysis(Path vcfPath, AnalysisStep... analysisSteps) {
        Analysis analysis = new Analysis();
        analysis.setVcfPath(vcfPath);
        if (analysisSteps.length != 0) {
            analysis.addAllSteps(Arrays.asList(analysisSteps));
        }
        return analysis;
    }

    private void printResults(SampleData sampleData) {
        for (Gene gene : sampleData.getGenes()) {
            System.out.printf("%s%n", gene);
            gene.getVariantEvaluations().forEach(System.out::println);
        }
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
    public void testRunAnalysis_VariantFilterOnly() {
        VariantFilter intervalFilter = new IntervalFilter(new GeneticInterval(1, 145508800, 145508800));

        Analysis analysis = makeAnalysis(vcfPath, intervalFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));
    }

    @Ignore
    @Test
    public void testRunAnalysis_PrioritiserAndPriorityScoreFilterOnly() {
        Integer expectedGeneId = 9939;
        Float desiredPrioritiserScore = 0.9f;
        Map<Integer, Float> geneIdPrioritiserScores = new HashMap<>();
        geneIdPrioritiserScores.put(expectedGeneId, desiredPrioritiserScore);

        PriorityType prioritiserTypeToMock = PriorityType.HIPHIVE_PRIORITY;
        Prioritiser prioritiser = new MockPrioritiser(prioritiserTypeToMock, geneIdPrioritiserScores);
        GeneFilter priorityScoreFilter = new PriorityScoreFilter(prioritiserTypeToMock, desiredPrioritiserScore - 0.1f);

        Analysis analysis = makeAnalysis(vcfPath, prioritiser, priorityScoreFilter);
        instance.runAnalysis(analysis);

        SampleData sampleData = analysis.getSampleData();
        printResults(sampleData);
        assertThat(sampleData.getGenes().size(), equalTo(1));
        Gene passed = sampleData.getGenes().get(0);
        assertThat(passed.passedFilters(), is(true));
        assertThat(passed.getEntrezGeneID(), equalTo(expectedGeneId));
        assertThat(passed.getPriorityScore(), equalTo(desiredPrioritiserScore));
//        assertThat(passed.getNumberOfVariants(), equalTo(1));
    }

}
