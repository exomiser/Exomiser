/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
