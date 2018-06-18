/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Utility class for creating {@code SampleIdentifier} objects from sample names extracted from a VCF file.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class SampleIdentifierUtil {

    private static final Logger logger = LoggerFactory.getLogger(SampleIdentifierUtil.class);
    private static final SampleIdentifier DEFAULT_SAMPLE_IDENTIFIER = SampleIdentifier.of("Sample", 0);

    private SampleIdentifierUtil() {
        //deliberately empty - class should not be instantiated
    }

    /**
     * Checks the input to ensure that the proband is in the input sampleNames. For cases where there is no specified
     * proband sample name, this method will return a {@code SampleIdentifier} from the sampleNames, assuming there is
     * only one name in the list. If both probandSampleName and sampleNames are empty a placeholder {@code SampleIdentifier}
     * will be returned containing default values.
     *
     * @param probandSampleName the name/identifier of the proband as it occurs in the VCF file.
     * @param sampleNames       the list of sample names in the genotype sample section of the VCF header.
     * @return a {@code SampleIdentifier} corresponding to the proband sample identifier in the input arguments and its
     * 0-based position in the genotypes list
     * @throws SampleMismatchException if the proband sample name is not specified and there are more than one sample
     *                                 names in the list or the proband sample name is not found in the list of sample names.
     */
    public static SampleIdentifier createProbandIdentifier(String probandSampleName, List<String> sampleNames) {
        logger.debug("Creating sample identifier for proband '{}'", probandSampleName);
        int numSamples = sampleNames.size();
        if (probandSampleName.isEmpty() && numSamples > 1) {
            String message = String.format("proband sample name not specified. Expected single sample VCF but got %d sample names - %s. Please check your sample and analysis files match.", numSamples, sampleNames);
            logger.error(message);
            throw new SampleMismatchException(message);
        }
        if (probandSampleName.isEmpty() && numSamples == 1) {
            String vcfSampleName = sampleNames.get(0);
            logger.info("proband sample name not specified - using sample name '{}' from VCF", vcfSampleName);
            return SampleIdentifier.of(vcfSampleName, 0);
        }
        if (probandSampleName.isEmpty() && numSamples == 0) {
            logger.info("proband sample name not specified and none found in sample names - using default sample name '{}'", DEFAULT_SAMPLE_IDENTIFIER
                    .getId());
            return DEFAULT_SAMPLE_IDENTIFIER;
        }
        return getMultiSampleProbandSampleIdentifier(probandSampleName, sampleNames);
    }

    private static SampleIdentifier getMultiSampleProbandSampleIdentifier(String probandSampleName, List<String> sampleNames) {
        for (int i = 0; i < sampleNames.size(); i++) {
            if (probandSampleName.equals(sampleNames.get(i))) {
                return SampleIdentifier.of(probandSampleName, i);
            }
        }
        String message = String.format("proband sample name '%s' is not found in the VCF sample. Expected one of %s. Please check your sample and analysis files match.", probandSampleName, sampleNames);
        throw new SampleMismatchException(message);
    }
}
