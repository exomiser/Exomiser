package org.monarchinitiative.exomiser.core.analysis.util;

import org.monarchinitiative.exomiser.core.analysis.SampleMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class SampleNameChecker {

    private static final Logger logger = LoggerFactory.getLogger(SampleNameChecker.class);

    private SampleNameChecker() {
        //deliberately empty - class should not be instantiated
    }

    public static String getProbandSampleName(String probandSampleName, List<String> sampleNames) {
        if (probandSampleName.isEmpty() && sampleNames.size() > 1) {
            String message = String.format("proband sample name not specified. Expected single sample VCF but got %d sample names - %s. Please check your sample and analysis files match.", sampleNames.size(), sampleNames);
            throw new SampleMismatchException(message);
        }
        if (probandSampleName.isEmpty() && sampleNames.size() == 1) {
            String vcfSampleName = sampleNames.get(0);
            logger.info("proband sample name not specified - using sample name '{}' from VCF", vcfSampleName);
            return vcfSampleName;
        }
        return probandSampleName;
    }

    public static int getProbandSampleId(String probandSampleName, List<String> sampleNames) {
        if (probandSampleName.isEmpty() && sampleNames.size() <= 1) {
            return 0;
        }
        return getMultiSampleProbandSampleId(probandSampleName, sampleNames);
    }

    private static int getMultiSampleProbandSampleId(String probandSampleName, List<String> sampleNames) {
        if (!sampleNames.contains(probandSampleName)) {
            String message = String.format("proband sample name '%s' is not found in the VCF sample. Expected one of %s. Please check your sample and analysis files match.", probandSampleName, sampleNames);
            throw new SampleMismatchException(message);
        }
        return sampleNames.indexOf(probandSampleName);
    }
}
