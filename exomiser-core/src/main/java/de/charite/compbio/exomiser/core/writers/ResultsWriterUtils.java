/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.filters.FilterFactory;
import de.charite.compbio.exomiser.core.filters.FilterReport;
import de.charite.compbio.exomiser.core.filters.FilterReportFactory;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.VariantType;
import jannovar.exome.VariantTypeCounter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResultsWriterUtils.class);

    private static final FilterReportFactory filterReportFactory = new FilterReportFactory();

    /**
     * Determines the correct file extension for a file given what was specified
     * in the {@link de.charite.compbio.exomiser.core.ExomiserSettings}.
     *
     * @param outFileName
     * @param outputFormat
     * @return
     */
    public static String determineFileExtension(String outFileName, OutputFormat outputFormat) {

        String specifiedFileExtension = outputFormat.getFileExtension();
        String outFileExtension = FilenameUtils.getExtension(outFileName);
        if (outFileExtension.isEmpty() || outFileName.endsWith("-results")) {
            //default filename will end in the build number and "-results"
            outFileName = String.format("%s.%s", outFileName, specifiedFileExtension);
        } else {
            outFileName = outFileName.replace(outFileExtension, specifiedFileExtension);
        }
        return outFileName;
    }

    /**
     * Make a {@code VariantTypeCounter} object from the list of
     * {@code VariantEvaluation}. We use this to print out a table of variant
     * class distribution.
     *
     * @param variantEvaluations
     * @return
     */
    public static List<VariantTypeCount> makeVariantTypeCounters(List<VariantEvaluation> variantEvaluations) {
        VariantTypeCounter variantTypeCounter = makeVariantTypeCounter(variantEvaluations);

        List<VariantTypeCount> variantTypeCounters = new ArrayList<>();

        Iterator<VariantType> iter = variantTypeCounter.getVariantTypeIterator();
        while (iter.hasNext()) {
            VariantType variantType = iter.next();
            List<Integer> typeSpecificCounts = variantTypeCounter.getTypeSpecificCounts(variantType);
            VariantTypeCount variantTypeCount = new VariantTypeCount(variantType, typeSpecificCounts);
            variantTypeCounters.add(variantTypeCount);
        }

        return variantTypeCounters;
    }

    protected static VariantTypeCounter makeVariantTypeCounter(List<VariantEvaluation> variantEvaluations) {

        if (variantEvaluations.isEmpty()) {
            return new VariantTypeCounter(0);
        }

        int numIndividuals = variantEvaluations.get(0).getNumberOfIndividuals();
        VariantTypeCounter vtypeCounter = new VariantTypeCounter(numIndividuals);

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            vtypeCounter.incrementCount(variantEvaluation.getVariant());
        }
        return vtypeCounter;
    }

    public static List<FilterReport> makeFilterReports(ExomiserSettings settings, SampleData sampleData) {
        //TODO: ExomiserSettings is really sticking it's nose into everything might be a good idea to scale
        //this back so that it's only really needed in to cli package as it is tightly coupled with that anyway.
        //For instance here it would be somewhat simpler to just supply the list of filters applied as they all
        //know what their required parameters were. Sure this will violate the 'Tell Don't Ask' principle but
        //the alternatives are worse
        List<FilterType> filtersApplied = FilterFactory.determineFilterTypesToRun(settings);
        return filterReportFactory.makeFilterReports(filtersApplied, settings, sampleData);

    }

    public static List<Gene> getMaxPassedGenes(List<Gene> genes, int maxGenes) {
        List<Gene> passedGenes = getPassedGenes(genes);
        if (maxGenes == 0) {
            logger.info("Maximum gene limit set to {} - Returning all {} genes which have passed filtering.", maxGenes, passedGenes.size());
            return passedGenes;
        }
        return getMaxGenes(passedGenes, maxGenes);
    }

    public static List<Gene> getPassedGenes(List<Gene> genes) {
        List<Gene> passedGenes = new ArrayList<>();
        for (Gene gene : genes) {
            if (gene.passedFilters()) {
                passedGenes.add(gene);
            }
        }
        logger.info("{} of {} genes have passed all filtering", passedGenes.size(), genes.size());
        return passedGenes;
    }

    private static List<Gene> getMaxGenes(List<Gene> genes, int maxGenes) {
        List<Gene> passedGenes = new ArrayList<>();
        int genesShown = 0;
        for (Gene gene : genes) {
            if (genesShown < maxGenes) {
                passedGenes.add(gene);
                genesShown++;
            }
        }
        logger.info("Maximum gene limit set to {} - Returning first {} of {} genes which have passed filtering.", maxGenes, maxGenes, genes.size());
        return passedGenes;
    }

}
