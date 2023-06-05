/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis;

import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.KnownVariantFilter;
import org.monarchinitiative.exomiser.core.filters.PathogenicityFilter;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.Objects;

/**
 * Class to check the {@link Sample} and {@link Analysis} provided to the {@link AnalysisRunner} are in a
 * valid state to proceed.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class AnalysisSampleValidator {

    private AnalysisSampleValidator() {
        // Uninstantiable utility class.
    }

    /**
     * Checks that the supplied {@link Sample} and {@link Analysis} are capable of being run together.
     *
     * @param sample   the input sample
     * @param analysis the input analysis
     * @throws IllegalStateException if the {@link Sample} and/or {@link Analysis} are not correctly configured.
     */
    public static void validate(Sample sample, Analysis analysis) throws IllegalStateException {
        Objects.requireNonNull(sample);
        Objects.requireNonNull(analysis);
        if (analysis.getAnalysisSteps().isEmpty()) {
            throw new IllegalStateException("No analysis steps specified!");
        }
        // TODO: decide if VCF only filtering is OK
        // Guard against people running the new analysis.yml which has no sample information.
        // As of 13.0.0 it is possible to run a phenotype-only prioritisation, so we're only checking that the analysis
        // is in the correct state with respect to the input sample
        for (AnalysisStep analysisStep : analysis.getAnalysisSteps()) {
            if (analysisStep.isVariantFilter() && !sample.hasVcf()) {
                throw new IllegalStateException("No VCF has been specified! VCF file required to run " + analysisStep.getClass()
                        .getSimpleName());
            }
            if (isFrequencyDependent(analysisStep) && analysis.getFrequencySources().isEmpty()) {
                throw new IllegalStateException("Frequency sources have not been defined. Frequency sources required to run " + analysisStep
                        .getClass()
                        .getSimpleName());
            }
            if (isPathogenicityDependent(analysisStep) && analysis.getPathogenicitySources().isEmpty()) {
                throw new IllegalStateException("Pathogenicity sources have not been defined. Pathogenicity sources required to run " + analysisStep
                        .getClass()
                        .getSimpleName());
            }
            if (isHpoDependent(analysisStep) && sample.getHpoIds().isEmpty()) {
                throw new IllegalStateException("HPO IDs not defined. Define some sample phenotypes before adding prioritiser: " + analysisStep
                        .getClass()
                        .getSimpleName());
            }
        }
    }

    private static boolean isFrequencyDependent(AnalysisStep analysisStep) {
        return analysisStep instanceof KnownVariantFilter || analysisStep instanceof FrequencyFilter;
    }

    private static boolean isPathogenicityDependent(AnalysisStep analysisStep) {
        return analysisStep instanceof PathogenicityFilter;
    }

    private static boolean isHpoDependent(AnalysisStep analysisStep) {
        if (analysisStep instanceof Prioritiser<? extends PriorityResult> prioritiser) {
            PriorityType priorityType = prioritiser.getPriorityType();
            return switch (priorityType) {
                case HIPHIVE_PRIORITY, PHENIX_PRIORITY, PHIVE_PRIORITY -> true;
                case EXOMEWALKER_PRIORITY, OMIM_PRIORITY, NONE -> false;
            };
        }
        return false;
    }

}
