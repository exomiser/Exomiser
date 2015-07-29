package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.vcf.VCFFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunner extends AbstractAnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(PassOnlyAnalysisRunner.class);

    //required for handling the sampleData creation/annotation
    private final VariantFactory variantFactory;

    public PassOnlyAnalysisRunner(VariantFactory variantFactory, VariantDataService variantDataService) {
        //TODO: make a SparseGeneFilterRunner?
        //geneFilterRunner = new SparseGeneFilterRunner();
        super(variantFactory, new SparseVariantFilterRunner(variantDataService), new SimpleGeneFilterRunner());
        this.variantFactory = variantFactory;
    }

    @Override
    public void runAnalysis(Analysis analysis) {

        final SampleData sampleData = makeSampleData(analysis);

        logger.info("Running analysis on sample: {}", sampleData.getSampleNames());
        long startAnalysisTimeMillis = System.currentTimeMillis();

        //variants take up 99% of all the memory in an analysis - this scales approximately linearly with the sample size
        //so for whole genomes this is best run as a stream to filter out the unwanted variants
        List<VariantFilter> variantFilters = getVariantFilterSteps(analysis);
        Path vcfPath = analysis.getVcfPath();
        final List<VariantEvaluation> variantEvaluations = streamAndFilterVariantEvaluations(vcfPath, variantFilters);
        sampleData.setVariantEvaluations(variantEvaluations);
        final List<Gene> genes = sampleDataFactory.createGenes(variantEvaluations);
        sampleData.setGenes(genes);
        final Pedigree pedigree = sampleData.getPedigree();

        //run all the other steps
        runSteps(getNonVariantFilterSteps(analysis), genes, pedigree);
        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());

        long endAnalysisTimeMillis = System.currentTimeMillis();
        double analysisTimeSecs = (double) (endAnalysisTimeMillis - startAnalysisTimeMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeSecs);
    }

    private SampleData makeSampleData(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleDataWithoutVariantsOrGenes(analysis.getVcfPath(), analysis.getPedPath());
        analysis.setSampleData(sampleData);
        return sampleData;
    }

    private List<VariantEvaluation> streamAndFilterVariantEvaluations(Path vcfPath, List<VariantFilter> variantFilters) {

        final int[] streamed = {0};
        final int[] passed = {0};

        try (Stream<VariantEvaluation> variantEvaluationStream = variantFactory.streamVariantEvaluations(vcfPath);) {
            //WARNING!!! THIS IS NOT THREADSAFE DO NOT USE PARALLEL STREAMS
            return variantEvaluationStream.filter(variantEvaluation -> {
                //loop through the filters and only run if the variantEvaluation has passed all prior filters
                variantFilters.stream().filter(filter -> variantEvaluation.passedFilters()).forEach(filter -> {
                    streamed[0]++;
                    variantFilterRunner.run(filter, variantEvaluation);
                });
                //yep, logging logic
                if (variantEvaluation.passedFilters()) {
                    passed[0]++;
                }
                if (streamed[0] % 100000 == 0) {
                    logger.info("Analysed {} variants - {} passed filters", streamed[0], passed[0]);
                }
                return variantEvaluation.passedFilters();
            })
                    .collect(toList());
        }
    }

    private List<VariantFilter> getVariantFilterSteps(Analysis analysis) {
        logger.info("Filtering variants with:");
        return analysis.getAnalysisSteps()
                .stream()
                .filter(analysisStep -> (VariantFilter.class.isInstance(analysisStep)))
                .map(step -> {
                    logger.info("{}", step);
                    return (VariantFilter) step;
                })
                .collect(toList());
    }

    private List<AnalysisStep> getNonVariantFilterSteps(Analysis analysis) {
        return analysis.getAnalysisSteps()
                .stream()
                .filter(analysisStep -> (!VariantFilter.class.isInstance(analysisStep)))
                .collect(toList());
    }
}
