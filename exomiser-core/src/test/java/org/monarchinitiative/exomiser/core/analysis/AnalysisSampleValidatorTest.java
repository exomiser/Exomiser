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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.PathogenicityFilter;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.*;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AnalysisSampleValidatorTest {

    @Test
    void getMainPrioritiserType() {
    }

    @Test
    public void throwsExceptionWhenNoVcfIsSetAndVariantFilterPresent() {
        Sample sample = Sample.builder()
                // no VCF defined here
                .build();

        Analysis analysis = Analysis.builder()
                .addStep(new PathogenicityFilter(true))
                .build();

        assertThrows(IllegalStateException.class, () ->
                AnalysisSampleValidator.validate(sample, analysis)
        );
    }

    @Test
    public void testParseAnalysisStepPathogenicityFilterNoPathSourcesDefined() {
        Analysis analysis = Analysis.builder()
                .pathogenicitySources(EnumSet.noneOf(PathogenicitySource.class))
                .addStep(new PathogenicityFilter(true))
                .build();

        assertThrows(IllegalStateException.class, () ->
                AnalysisSampleValidator.validate(sampleWithVcf(), analysis)
        );
    }

    private Sample sampleWithVcf() {
        return Sample.builder()
                .vcfPath(Path.of("test.vcf"))
                .build();
    }

    @Test
    public void testAddFrequencyFilterThrowsExceptionWhenFrequencySourcesAreNotDefined() {
        Analysis analysis = Analysis.builder()
                .frequencySources(EnumSet.noneOf(FrequencySource.class))
                .addStep(new FrequencyFilter(2f))
                .build();

        assertThrows(IllegalStateException.class, () ->
                AnalysisSampleValidator.validate(sampleWithVcf(), analysis)
        );
    }

    @Test
    public void testAddPathogenicityFilterStepThrowsExceptionWhenPathogenicitySourcesAreNotDefined() {
        Analysis analysis = Analysis.builder()
                .pathogenicitySources(EnumSet.noneOf(PathogenicitySource.class))
                .addStep(new PathogenicityFilter(true))
                .build();

        assertThrows(IllegalStateException.class, () ->
                AnalysisSampleValidator.validate(sampleWithVcf(), analysis)
        );
    }

    @Test
    public void testAddPhivePrioritiserThrowsExcptionWhenHpoIdsNotDefined() {
        Analysis analysis = Analysis.builder()
                .addStep(new PhivePriority(TestPriorityServiceFactory.stubPriorityService()))
                .build();
        assertThrows(IllegalStateException.class, () ->
                        AnalysisSampleValidator.validate(sampleWithVcf(), analysis),
                "HPO IDs not defined. Define some sample phenotypes before adding prioritiser: PhivePriority"
        );
    }

    @Test
    public void testAddHiPhivePrioritiserThrowsExceptionWhenNoHpoIdsDefined() {
        Analysis analysis = Analysis.builder()
                .addStep(new HiPhivePriority(HiPhiveOptions.defaults(), DataMatrix.empty(), TestPriorityServiceFactory.stubPriorityService()))
                .build();
        assertThrows(IllegalStateException.class, () ->
                        AnalysisSampleValidator.validate(sampleWithVcf(), analysis),
                "HPO IDs not defined. Define some sample phenotypes before adding prioritiser: HiPhivePriority"
        );
    }

    @Test
    public void testAddPhenixPrioritiserThrowsExceptionWhenNoHpoIdsDefined() {
        Analysis analysis = Analysis.builder()
                .addStep(new MockPrioritiser(PriorityType.PHENIX_PRIORITY, Map.of()))
                .build();
        assertThrows(IllegalStateException.class, () ->
                        AnalysisSampleValidator.validate(sampleWithVcf(), analysis),
                "HPO IDs not defined. Define some sample phenotypes before adding prioritiser: PhenixPriority"
        );
    }

}