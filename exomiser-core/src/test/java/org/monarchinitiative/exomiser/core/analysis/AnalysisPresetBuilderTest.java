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

package org.monarchinitiative.exomiser.core.analysis;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.phenotype.service.TestOntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriority;
import org.monarchinitiative.exomiser.core.prioritisers.NoneTypePriorityFactoryStub;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriority;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;

class AnalysisPresetBuilderTest {

    private static final Set<FrequencySource> FREQUENCY_SOURCES = Set.copyOf(FrequencySource.NON_FOUNDER_POPS);

    private final AnalysisPresetBuilder instance = new AnalysisPresetBuilder(new GenomeAnalysisServiceProvider(TestFactory
            .buildDefaultHg19GenomeAnalysisService()), new NoneTypePriorityFactoryStub(), TestOntologyService.builder().build());

    // presets
    @Test
    void testBuildExomePreset() {
        Analysis analysis = instance.buildExomePreset();
        assertThat(analysis.analysisMode(), equalTo(AnalysisMode.PASS_ONLY));
        assertThat(analysis.inheritanceModeOptions(), equalTo(InheritanceModeOptions.defaults()));
        assertThat(analysis.frequencySources(), equalTo(FREQUENCY_SOURCES));
        assertThat(analysis.pathogenicitySources(), equalTo(Set.of(REVEL, MVP, ALPHA_MISSENSE, SPLICE_AI)));
        assertThat(analysis.analysisSteps().stream().map(AnalysisStep::getClass).toList(),
                equalTo(List.of(
                        GeneBlacklistFilter.class,
                        FailedVariantFilter.class,
                        AlleleBalanceFilter.class,
                        VariantEffectFilter.class,
                        FrequencyFilter.class,
                        PathogenicityFilter.class,
                        InheritanceFilter.class,
                        OmimPriority.class,
                        HiPhivePriority.class
                )));
    }

    @Test
    void testBuildGenomePreset() {
        Analysis analysis = instance.buildGenomePreset();
        assertThat(analysis.analysisMode(), equalTo(AnalysisMode.PASS_ONLY));
        assertThat(analysis.inheritanceModeOptions(), equalTo(InheritanceModeOptions.defaults()));
        assertThat(analysis.frequencySources(), equalTo(FREQUENCY_SOURCES));
        assertThat(analysis.pathogenicitySources(), equalTo(Set.of(REVEL, MVP, REMM, ALPHA_MISSENSE, SPLICE_AI)));
        assertThat(analysis.analysisSteps().stream().map(AnalysisStep::getClass).toList(),
                equalTo(List.of(
                        HiPhivePriority.class,
                        PriorityScoreFilter.class,
                        GeneBlacklistFilter.class,
                        FailedVariantFilter.class,
                        AlleleBalanceFilter.class,
                        RegulatoryFeatureFilter.class,
                        FrequencyFilter.class,
                        PathogenicityFilter.class,
                        InheritanceFilter.class,
                        OmimPriority.class
                )));
    }

}