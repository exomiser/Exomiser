/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for checking the sample identifier in the VCF file.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class SampleIdentifiers {

    private static final Logger logger = LoggerFactory.getLogger(SampleIdentifiers.class);

    private static final String DEFAULT_ID = "sample";

    private SampleIdentifiers() {
    }

    /**
     * Default sample identifier for use with unspecified single-sample VCF files.
     *
     * @return a default sample identifier named 'sample'
     * @since 13.0.0
     */
    public static String defaultSample() {
        return DEFAULT_ID;
    }

    /**
     * Checks the input to ensure that the proband is in the input sampleNames. For cases where there is no specified
     * proband sample name, this method will return a {@code SampleIdentifier} from the sampleNames, assuming there is
     * only one name in the list. If both probandSampleName and sampleNames are empty a placeholder {@code SampleIdentifier}
     * will be returned containing default values.
     *
     * @param probandSampleName the name/identifier of the proband as it occurs in the VCF file.
     * @param sampleNames       the list of sample names in the genotype sample section of the VCF header.
     * @return a String corresponding to the proband sample identifier in the input arguments
     * @throws IllegalStateException if the proband sample name is not specified and there are more than one sample
     *                               names in the list or the proband sample name is not found in the list of sample names.
     * @since 13.0.0
     */
    public static String checkProbandIdentifier(String probandSampleName, List<String> sampleNames) {
        Objects.requireNonNull(probandSampleName);
        Objects.requireNonNull(sampleNames);
        requireNoDuplicates(sampleNames);

        logger.debug("Creating sample identifier for proband '{}'", probandSampleName);
        if (!probandSampleName.isEmpty() && sampleNames.isEmpty()) {
            return probandSampleName;
        }
        int numSamples = sampleNames.size();
        if (probandSampleName.isEmpty() && numSamples > 1) {
            String message = String.format("Proband sample name not specified. Expected single sample VCF but got %d sample names - %s. Please check your sample and analysis files match.", numSamples, sampleNames);
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (probandSampleName.isEmpty() && numSamples == 1) {
            String vcfSampleName = sampleNames.get(0);
            logger.info("Proband sample name not specified - using sample name '{}' from VCF", vcfSampleName);
            return vcfSampleName;
        }
        if (probandSampleName.isEmpty() && numSamples == 0) {
            logger.info("Proband sample name not specified and none found in sample names - using default sample name '{}'", DEFAULT_ID);
            return DEFAULT_ID;
        }
        requireProbandInSampleNames(probandSampleName, sampleNames);
        return probandSampleName;
    }

    private static void requireNoDuplicates(List<String> genotypeSamples) {
        Set<String> checked = new HashSet<>();
        Set<String> duplicates = genotypeSamples.stream()
                .filter(name -> !checked.add(name))
                .collect(Collectors.toSet());
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException("Found duplicate sample names in VCF sample genotypes - " + duplicates + ". Sample names must be unique! - ");
        }
    }

    private static void requireProbandInSampleNames(String probandSampleName, List<String> sampleNames) {
        for (int i = 0; i < sampleNames.size(); i++) {
            if (probandSampleName.equals(sampleNames.get(i))) {
                return;
            }
        }
        String message = String.format("Proband sample name '%s' is not found in the VCF sample. Expected one of %s. Please check your sample and analysis files match.", probandSampleName, sampleNames);
        logger.error(message);
        throw new IllegalStateException(message);
    }

    /**
     * Finds the position of the given sample name in the list.
     *
     * @param sampleName
     * @param sampleNames
     */
    public static int samplePosition(String sampleName, List<String> sampleNames) {
        Objects.requireNonNull(sampleName);
        Objects.requireNonNull(sampleNames);
        if (sampleNames.isEmpty()) {
            return 1;
        }
        for (int i = 0; i < sampleNames.size(); i++) {
            if (sampleName.equals(sampleNames.get(i))) {
                return i + 1;
            }
        }
        throw new IllegalStateException("Unable to find position of sample '" + sampleName + "' - not present in sample names " + sampleNames);
    }
}
