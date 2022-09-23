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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.partitioningBy;

/**
 * Utility class for validating {@link Pedigree} objects against the proband sample identifier and the sample
 * names from the VCF to ensure that they match sufficiently for correct segregation analysis.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 11.0.0
 */
public class PedigreeSampleValidator {

    private static final Logger logger = LoggerFactory.getLogger(PedigreeSampleValidator.class);

    private PedigreeSampleValidator() {
        //uninstantiable utility class
    }

    /**
     * Checks and returns validated {@link Pedigree} objects against the supplied proband sample identifier and
     * the VCF file sample names to ensure that they match sufficiently for correct segregation analysis. Any errors will
     * result in a {@link PedigreeValidationException}. Note that the returned {@link Pedigree} may not be the same
     * instance as the input pedigree as an empty input pedigree may be be considered as valid in certain cases for single-
     * sample VCF files, depending on the supplied proband sample identifier.
     *
     * @param pedigree    the input {@link Pedigree} to be validated
     * @param proband     sample identifier of the proband
     * @param sampleNames the list of sample names contained in the VCF file to be analysed
     * @return a validated {@link Pedigree} object for use in an analysis.
     * @throws PedigreeValidationException for any invalid input.
     * @throws NullPointerException        for any null input value.
     */
    public static Pedigree validate(Pedigree pedigree, String proband, List<String> sampleNames) {
        Objects.requireNonNull(pedigree);
        Objects.requireNonNull(proband);
        Objects.requireNonNull(sampleNames);

        if (pedigree.isEmpty()) {
            return createAndValidateSingleSamplePedigree(proband, sampleNames);
        }
        return validatePedigree(pedigree, proband, sampleNames);
    }

    private static Pedigree createAndValidateSingleSamplePedigree(String probandId, List<String> sampleNames) {
        if (sampleNames.isEmpty()) {
            logger.info("No pedigree or sample names present. Using default '{}'", probandId);
            return singleSamplePedigree(probandId);
        } else if (sampleNames.size() == 1) {
            logger.info("No pedigree provided for sample '{}'", probandId);
            Pedigree singleSamplePedigree = singleSamplePedigree(probandId);
            return validatePedigree(singleSamplePedigree, probandId, sampleNames);
        } else {
            String message = String.format("No pedigree provided yet multiple samples present - %s for proband '%s'", sampleNames, probandId);
            logger.error(message);
            throw new PedigreeValidationException(message);
        }
    }

    private static Pedigree singleSamplePedigree(String sampleName) {
        logger.info("Creating single-sample pedigree for {}", sampleName);
        return Pedigree.justProband(sampleName);
    }

    private static Pedigree validatePedigree(Pedigree pedigree, String probandId, List<String> sampleNames) {
        checkOnlyOneFamilyIsPresent(pedigree);
        checkProbandIsPresentInPedigree(probandId, pedigree);
        checkForUnrepresentedSamples(sampleNames, pedigree.getIdentifiers());
        return pedigree;
    }

    private static void checkOnlyOneFamilyIsPresent(Pedigree pedigree) {
        List<String> familyNames = pedigree.getIndividuals().stream()
                .map(Individual::getFamilyId)
                .distinct()
                .toList();
        if (familyNames.size() > 1) {
            String message = "Pedigree must contain only one family, found " + familyNames.size() + ": " + familyNames + ". Please provide a pedigree containing only the proband family matching supplied HPO terms.";
            logger.error(message);
            throw new PedigreeValidationException(message);
        }
    }

    private static void checkProbandIsPresentInPedigree(String probandId, Pedigree pedigree) {
        if (!pedigree.containsId(probandId)) {
            String message = "Proband - pedigree mismatch. Proband identifier " + probandId + " not present in pedigree";
            logger.error(message);
            throw new PedigreeValidationException(message);
        }

        Individual probandIndividual = pedigree.getIndividualById(probandId);
        if (!probandIndividual.isAffected()) {
            String message = "Proband - pedigree mismatch. Proband " + probandId + " not marked as affected in pedigree";
            logger.error(message);
            throw new PedigreeValidationException(message);
        }
    }

    // A pedigree could contain unsequenced individuals (dead/unwilling/unable to be part of study) we just need to be
    // sure the sample names from the vcf are all present in the ped file.
    private static void checkForUnrepresentedSamples(List<String> sampleNames, Collection<String> pedNames) {
        List<String> unrepresentedSamples = findSampleNamesNotRepresentedInPedigree(pedNames, sampleNames);

        if (!unrepresentedSamples.isEmpty()) {
            unrepresentedSamples.forEach(name -> logger.error("Individual {} present in VCF but not in pedigree. Please ensure names in VCF are present in pedigree.", name));
            throw new PedigreeValidationException("VCF - pedigree mismatch. There are mismatched individuals in the pedigree and/or VCF file. Please ensure all VCF samples are represented in the pedigree.");
        }
    }

    private static List<String> findSampleNamesNotRepresentedInPedigree(Collection<String> pedNames, List<String> sampleNames) {
        if (sampleNames.isEmpty()) {
            return List.of();
        }
        Map<Boolean, List<String>> samples = sampleNames.stream().collect(partitioningBy(pedNames::contains));

        List<String> representedSamples = samples.get(true);
        if (representedSamples.isEmpty()) {
            String message = String.format("VCF - pedigree mismatch. None of the sample names in the VCF - %s match any of the individuals in the pedigree - %s", sampleNames, pedNames);
            logger.error(message);
            throw new PedigreeValidationException(message);
        }

        return samples.get(false);
    }

    private static class PedigreeValidationException extends RuntimeException {

        private PedigreeValidationException(String s) {
            super(s);
        }
    }
}
