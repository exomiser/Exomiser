package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.*;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.filters.VariantFilterRunner;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SparseAnalysisRunnerTest {

    private SparseAnalysisRunner instance;
    private final JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
    private final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(testJannovarData.getRefDict(), testJannovarData.getChromosomes());
    private final VariantFactory variantFactory = new VariantFactory(new VariantAnnotator(variantContextAnnotator));

    private final SampleDataFactory sampleDataFactory = new SampleDataFactory(variantFactory, testJannovarData);
    private final VariantDataService stubDataService = new VariantDataServiceStub();
    @Before
    public void setUp() {
        instance = new SparseAnalysisRunner(sampleDataFactory, stubDataService);
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
