/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.TestJannovarDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantAnnotator;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceStub;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains common methods required by the AnalysisRunnerTest classes.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public abstract class AnalysisRunnerTestBase {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisRunnerTestBase.class);
 
    final Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");

    final JannovarData testJannovarData = new TestJannovarDataFactory().getJannovarData();
    final VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(testJannovarData.getRefDict(), testJannovarData.getChromosomes());
    final VariantFactory variantFactory = new VariantFactory(new VariantAnnotator(variantContextAnnotator));

    final SampleDataFactory sampleDataFactory = new SampleDataFactory(variantFactory, testJannovarData);
    final VariantDataService stubDataService = new VariantDataServiceStub();
    
    
    Analysis makeAnalysis(Path vcfPath, AnalysisStep... analysisSteps) {
        Analysis analysis = new Analysis();
        analysis.setVcfPath(vcfPath);
        if (analysisSteps.length != 0) {
            analysis.addAllSteps(Arrays.asList(analysisSteps));
        }
        return analysis;
    }

    Map<String, Gene> makeResults(List<Gene> genes) {
        return genes.stream().collect(toMap(Gene::getGeneSymbol, gene -> gene));
    }

    void printResults(SampleData sampleData) {
        for (Gene gene : sampleData.getGenes()) {
            logger.info("{}", gene);
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                logger.info("{}", variantEvaluation);
            }
        }
    }

}
