package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.filters.*;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriority;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

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

        final SampleData sampleData = makeSampleDataWithoutGenesOrVariants(analysis);

        logger.info("Running analysis on sample: {}", sampleData.getSampleNames());
        long startAnalysisTimeMillis = System.currentTimeMillis();

        final Pedigree pedigree = sampleData.getPedigree();
        final Path vcfPath = analysis.getVcfPath();
        Map<String, Gene> knownGenes = makeKnownGenes();

        runSteps(vcfPath, getNonVariantNonInheritanceDependentSteps(analysis), new ArrayList<>(knownGenes.values()), pedigree);
        Map<String, Gene> passedGenes = getPassedGenes(knownGenes);


//        some kind of multi-map with ordered duplicate keys would allow for easy grouping of steps for running the groups together.
        
//        need to merge variant filters into largest blocks possible, figure out where the first prioritiser + priorityScoreFilter is then decide on best way to run it all ;
        //variants take up 99% of all the memory in an analysis - this scales approximately linearly with the sample size
        //so for whole genomes this is best run as a stream to filter out the unwanted variants
        List<VariantFilter> variantFilters = getVariantFilterSteps(analysis);
        final List<VariantEvaluation> variantEvaluations = streamAndFilterVariantEvaluations(vcfPath, passedGenes, variantFilters);
        final List<Gene> genes = getGenesWithVariants(passedGenes);

        logger.info("Filtered {} genes containing {} filtered variants", genes.size(), variantEvaluations.size());

        //run all the other steps
        //run steps requiring fully filtered genes and variants (inheritanceFilter and OmimPrioritiser)
        runSteps(vcfPath, getInheritanceDependentSteps(analysis), genes, pedigree);
        scoreGenes(genes, analysis.getScoringMode(), analysis.getModeOfInheritance());

        sampleData.setGenes(genes);
        sampleData.setVariantEvaluations(variantEvaluations);

        long endAnalysisTimeMillis = System.currentTimeMillis();
        double analysisTimeSecs = (double) (endAnalysisTimeMillis - startAnalysisTimeMillis) / 1000;
        logger.info("Finished analysis in {} secs", analysisTimeSecs);
    }

    private SampleData makeSampleDataWithoutGenesOrVariants(Analysis analysis) {
        final SampleData sampleData = sampleDataFactory.createSampleDataWithoutVariantsOrGenes(analysis.getVcfPath(), analysis.getPedPath());
        analysis.setSampleData(sampleData);
        return sampleData;
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
                        variantFilters.stream()
                                .filter(filter -> variantEvaluation.passedFilters())
                                .forEach(filter -> variantFilterRunner.run(filter, variantEvaluation));
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
                .map(analysisStep -> {
                    logger.info("{}", analysisStep);
                    return (VariantFilter) analysisStep;
                })
                .collect(toList());
    }

    private List<AnalysisStep> getNonVariantNonInheritanceDependentSteps(Analysis analysis) {
        return analysis.getAnalysisSteps()
                .stream()
                .filter(analysisStep -> (!VariantFilter.class.isInstance(analysisStep)))
                .filter(analysisStep -> (!InheritanceFilter.class.isInstance(analysisStep)))
                .filter(analysisStep -> (!OMIMPriority.class.isInstance(analysisStep)))
                .collect(toList());
    }

    private List<AnalysisStep> getInheritanceDependentSteps(Analysis analysis) {
        return analysis.getAnalysisSteps()
                .stream()
                .filter(analysisStep -> (InheritanceFilter.class.isInstance(analysisStep) || OMIMPriority.class.isInstance(analysisStep)))
                .collect(toList());
    }
}
