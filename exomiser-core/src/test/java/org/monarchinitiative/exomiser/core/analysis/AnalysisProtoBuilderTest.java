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
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.FiltersProto;
import org.monarchinitiative.exomiser.api.v1.PrioritisersProto;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AnalysisProtoBuilderTest {

    @Test
    void testApi() throws Exception {
        AnalysisProtoBuilder instance = AnalysisProtoBuilder.builder();

        AnalysisProto.Analysis protoAnalysis = instance
                .analysisMode(AnalysisMode.FULL)
                .frequencySources(FrequencySource.ALL_ESP_SOURCES)
                .pathogenicitySources(EnumSet.of(PathogenicitySource.REVEL, PathogenicitySource.MVP))
                .build();
    }

    @Test
    public void testAnalysisBuilderCanBuildCompleteAnalysis() {
        EnumSet<PathogenicitySource> pathogenicitySources = EnumSet.of(PathogenicitySource.REMM, PathogenicitySource.SIFT);
        EnumSet<FrequencySource> frequencySources = EnumSet.of(FrequencySource.ESP_AA, FrequencySource.EXAC_EAST_ASIAN);
        float frequencyCutOff = 2f;
        AnalysisProto.AnalysisStep frequencyFilter = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder().setMaxFrequency(frequencyCutOff))
                .build();

        AnalysisProto.AnalysisStep phivePrioritiser = AnalysisProto.AnalysisStep.newBuilder()
                .setPhivePrioritiser(PrioritisersProto.PhivePrioritiser.getDefaultInstance())
                .build();

        AnalysisProto.AnalysisStep blacklistFilter = AnalysisProto.AnalysisStep.newBuilder()
                .setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.getDefaultInstance())
                .build();

        PriorityType priorityType = PriorityType.PHIVE_PRIORITY;
        float minPriorityScore = 0.501f;

        AnalysisProto.AnalysisStep priorityScoreFilter = AnalysisProto.AnalysisStep.newBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .setPriorityType(priorityType.toString())
                        .setMinPriorityScore(minPriorityScore))
                .build();

        AnalysisProto.AnalysisStep regulatoryFeatureFilter = AnalysisProto.AnalysisStep.newBuilder()
                .setRegulatoryFeatureFilter(FiltersProto.RegulatoryFeatureFilter.getDefaultInstance())
                .build();

        AnalysisProto.AnalysisStep inheritanceFilter = AnalysisProto.AnalysisStep.newBuilder()
                .setInheritanceFilter(FiltersProto.InheritanceFilter.getDefaultInstance())
                .build();

        AnalysisProto.Analysis analysis = AnalysisProtoBuilder.builder()
                .inheritanceModes(InheritanceModeOptions.defaults())
                .analysisMode(AnalysisMode.FULL)
                .frequencySources(frequencySources)
                .pathogenicitySources(pathogenicitySources)
                .addPhivePrioritiser()
                .addPriorityScoreFilter(priorityType, minPriorityScore)
                .addRegulatoryFeatureFilter()
                .addGeneBlacklistFilter()
                .addFrequencyFilter(frequencyCutOff)
                .addInheritanceFilter()
                .build();

        Map<String, Float> inhertanceModeMaxFreqs = InheritanceModeOptions.defaults()
                .getMaxFreqs()
                .entrySet()
                .stream()
                .collect(toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
        assertThat(analysis.getInheritanceModesMap(), equalTo(inhertanceModeMaxFreqs));

        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisProto.AnalysisMode.FULL));

        List<String> freqSourceStrings = frequencySources.stream().map(FrequencySource::toString).collect(toList());
        assertThat(analysis.getFrequencySourcesList(), equalTo(freqSourceStrings));

        List<String> pathSourceStrings = pathogenicitySources.stream()
                .map(PathogenicitySource::toString)
                .collect(toList());
        assertThat(analysis.getPathogenicitySourcesList(), equalTo(pathSourceStrings));

        //check that the order of analysis steps is preserved
        assertThat(analysis.getStepsList(), equalTo(Arrays.asList(
                phivePrioritiser,
                priorityScoreFilter,
                regulatoryFeatureFilter,
                blacklistFilter,
                frequencyFilter,
                inheritanceFilter
        )));
    }
}