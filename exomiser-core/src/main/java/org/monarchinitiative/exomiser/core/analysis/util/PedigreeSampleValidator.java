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

import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PedigreeSampleValidator {

    private static final Logger logger = LoggerFactory.getLogger(PedigreeSampleValidator.class);

    private PedigreeSampleValidator() {
        //uninstantiable utility class
    }

    public static Pedigree validate(Pedigree pedigree, SampleIdentifier proband, List<String> sampleNames) {
        Objects.requireNonNull(pedigree);
        Objects.requireNonNull(proband);
        Objects.requireNonNull(sampleNames);

        if (sampleNames.isEmpty()) {
            if (!pedigree.isEmpty()) {
                throw new PedigreeValidationException("Pedigree present, but no sample names present in sampleData");
            }
            logger.info("No sample names present. Using default '{}'", proband.getId());
            return singleSamplePedigree(proband.getId());
        }

        if (sampleNames.size() == 1 && pedigree.isEmpty()) {
            logger.info("No pedigree provided for sample '{}'", proband.getId());
            return singleSamplePedigree(proband.getId());
        }

        if (sampleNames.size() > 1 && pedigree.isEmpty()) {
            logger.error("Multiple samples present - {}, but no pedigree provided for proband '{}'", sampleNames, proband.getId());
            throw new PedigreeValidationException("Multiple samples present, but no pedigree provided");
        }

        return createMultiSamplePedigree(pedigree, proband, sampleNames);
    }

    private static Pedigree singleSamplePedigree(String sampleName) {
        logger.info("Creating single-sample pedigree for {}", sampleName);
        Individual proband = Individual.builder()
                .familyId("family")
                .id(sampleName)
                .status(Status.AFFECTED)
                .build();
        return Pedigree.of(proband);
    }

    private static Pedigree createMultiSamplePedigree(Pedigree pedigree, SampleIdentifier proband, List<String> sampleNames) {
        List<String> familyNames = pedigree.getIndividuals().stream()
                .map(Individual::getFamilyId)
                .distinct()
                .collect(toList());
        if (familyNames.size() > 1) {
            String message = "Pedigree must contain only one family, found " + familyNames.size() + ": " + familyNames + ". Please provide a pedigree containing only the proband family matching supplied HPO terms.";
            logger.error(message);
            throw new PedigreeValidationException(message);
        }
        checkForUnrepresentedSamples(sampleNames, pedigree.getIdentifiers());
        checkProbandIsPresentInPedigree(proband, pedigree);
        return pedigree;
    }

    private static void checkProbandIsPresentInPedigree(SampleIdentifier proband, Pedigree pedigree) {
        if (!pedigree.containsId(proband.getId())) {
            String message = "Proband - pedigree mismatch. Proband identifier "  + proband.getId() + " not present in pedigree";
            logger.error(message);
            throw new PedigreeValidationException(message);
        }

        Individual probandIndividual = pedigree.getIndividualById(proband.getId());
        if (!probandIndividual.isAffected()) {
            String message = "Proband - pedigree mismatch. Proband "  + proband.getId() + " not marked as affected in pedigree";
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
