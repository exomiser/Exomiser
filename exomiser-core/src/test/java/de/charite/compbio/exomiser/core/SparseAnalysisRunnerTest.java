package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.*;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilterRunner;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SparseAnalysisRunnerTest {

    private SparseAnalysisRunner instance;

    @Before
    public void setUp() {

        JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
        VariantFactory variantFactory = new VariantFactory(new VariantAnnotationsFactory(testJannovarData));

        VariantDataService stubDataService = new VariantDataServiceStub();
        VariantFilterRunner variantFilterRunner = new SparseVariantFilterRunner(stubDataService);
        instance = new SparseAnalysisRunner(variantFactory, stubDataService);
    }

    @Test
    public void canRunAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setVcfPath(Paths.get("src/test/resources/smallTest.vcf"));

        instance.runAnalysis(analysis);
        SampleData sampleData = analysis.getSampleData();
        for (Gene gene : sampleData.getGenes() ) {
            System.out.printf("%s%n", gene.getGeneSymbol());
            gene.getVariantEvaluations().forEach(System.out::println);
        }
    }
}
