/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.filter.FilterFactory;
import de.charite.compbio.exomiser.core.filter.FilterReport;
import de.charite.compbio.exomiser.core.filter.FilterReportFactory;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.VariantType;
import jannovar.exome.VariantTypeCounter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterUtils {
    
    private static final FilterReportFactory filterReportFactory = new FilterReportFactory();
    
    /**
     * Determines the correct file extension for a file given what was specified in the {@link de.charite.compbio.exomiser.core.ExomiserSettings}.
     * @param outFileName
     * @param outputFormat
     * @return 
     */
    public static String determineFileExtension(String outFileName, OutputFormat outputFormat) {

        String specifiedFileExtension = outputFormat.getFileExtension();
        String outFileExtension = FilenameUtils.getExtension(outFileName);
        if (outFileExtension.isEmpty() || outFileName.endsWith("-results")) {
            //default filename will end in the build number and "-results"
            outFileName = String.format("%s.%s",outFileName, specifiedFileExtension);
        } else {
            outFileName = outFileName.replace(outFileExtension, specifiedFileExtension);
        }
        return outFileName;
    }
    
    /**
     * Make a {@code VariantTypeCounter} object from the
     * list of {@code VariantEvaluation}.
     * We use this to print out a table of variant class distribution.
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
    
}
