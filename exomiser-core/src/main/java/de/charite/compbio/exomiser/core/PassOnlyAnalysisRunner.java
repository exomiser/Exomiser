package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PassOnlyAnalysisRunner extends AbstractAnalysisRunner {

    private static final Logger logger = LoggerFactory.getLogger(PassOnlyAnalysisRunner.class);

    public PassOnlyAnalysisRunner(SampleDataFactory sampleDataFactory, VariantDataService variantDataService) {
        super(sampleDataFactory, new SparseVariantFilterRunner(variantDataService), new SimpleGeneFilterRunner());
    }

    @Override
    public void runAnalysis(Analysis analysis) {

        final SampleData sampleData = makeSampleData(analysis);

        logger.info("Running analysis on sample: {}", sampleData.getSampleNames());
        long startAnalysisTimeMillis = System.currentTimeMillis();

        List<Gene> knownGenes = sampleDataFactory.createKnownGenes();

        final Pedigree pedigree = sampleData.getPedigree();
        runSteps(getNonVariantFilterSteps(analysis), knownGenes, pedigree);

        Map<String, Gene> passedGenes = createPassedGenes(knownGenes);
        //variants take up 99% of all the memory in an analysis - this scales approximately linearly with the sample size
        //so for whole genomes this is best run as a stream to filter out the unwanted variants
        List<VariantFilter> variantFilters = getVariantFilterSteps(analysis);
        Path vcfPath = analysis.getVcfPath();
        final List<VariantEvaluation> variantEvaluations = streamAndFilterVariantEvaluations(vcfPath, passedGenes, variantFilters);
        List<Gene> genes = getGenesWithVariants(passedGenes);

        logger.info("Filtered {} genes containing {} filtered variants", genes.size(), variantEvaluations.size());

        //run all the other steps
        //run steps requiring fully filtered genes and variants (inheritanceFilter and OmimPrioritiser)
//        runSteps(getNonVariantFilterSteps(analysis), knownGenes, pedigree);

        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());

        sampleData.setGenes(genes);
        sampleData.setVariantEvaluations(variantEvaluations);

        long endAnalysisTimeMillis = System.currentTimeMillis();
        double analysisTimeSecs = (double) (endAnalysisTimeMillis - startAnalysisTimeMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeSecs);
    }

    private SampleData makeSampleData(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleDataWithoutVariantsOrGenes(analysis.getVcfPath(), analysis.getPedPath());
        analysis.setSampleData(sampleData);
        return sampleData;
    }

    private Map<String, Gene> createPassedGenes(List<Gene> genes) {
        Map<String, Gene> passedGenes = genes
                .parallelStream()
                .filter(Gene::passedFilters)
                .collect(toConcurrentMap(Gene::getGeneSymbol, gene -> (gene)));
        logger.info("Filtered {} genes - {} passed", genes.size(), passedGenes.size());
        return passedGenes;
    }

    private List<Gene> getGenesWithVariants(Map<String, Gene> passedGenes) {
        return passedGenes.values()
                .stream()
                .filter(gene -> !gene.getVariantEvaluations().isEmpty())
                .collect(toList());
    }

    private List<VariantEvaluation> streamAndFilterVariantEvaluations(Path vcfPath, Map<String, Gene> passedGenes, List<VariantFilter> variantFilters) {

        final int[] streamed = {0};
        final int[] passed = {0};
        VariantFactory variantFactory = sampleDataFactory.getVariantFactory();
        List<VariantEvaluation> streamedAndFiltered;
        try (Stream<VariantEvaluation> variantEvaluationStream = variantFactory.streamVariantEvaluations(vcfPath)) {
            //WARNING!!! THIS IS NOT THREADSAFE DO NOT USE PARALLEL STREAMS
            streamedAndFiltered = variantEvaluationStream
                    .filter(variantEvaluation -> {
                        //yep, logging logic
                        streamed[0]++;
                        if (streamed[0] % 100000 == 0) {
                            logger.info("Analysed {} variants - {} passed filters", streamed[0], passed[0]);
                        }
                        //Only continue on to the filters if the variant the gene in located in is predicted as a phenotype match,
                        //this should drastically reduce the number of collected variants
                        return passedGenes.containsKey(variantEvaluation.getGeneSymbol());
                    })
                    .filter(variantEvaluation -> {
                        //loop through the filters and only run if the variantEvaluation has passed all prior filters
                        variantFilters.stream().filter(filter -> variantEvaluation.passedFilters()).forEach(filter -> {
                            variantFilterRunner.run(filter, variantEvaluation);
                        });
                        if (variantEvaluation.passedFilters()) {
                            //yep, logging logic
                            passed[0]++;
                            Gene gene = passedGenes.get(variantEvaluation.getGeneSymbol());
                            gene.addVariant(variantEvaluation);
                            return true;
                        }
                        return false;
                    })
                    .collect(toList());
        }
        logger.info("Filtered {} variants - {} passed", streamed[0], passed[0]);
        return streamedAndFiltered;
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
