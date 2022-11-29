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

import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.FiltersProto;
import org.monarchinitiative.exomiser.api.v1.PrioritisersProto;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriority;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriority;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.util.EnumSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AnalysisProtoConverterTest {

    @Test
    void testToProtoGlobalOptions() {
        Analysis analysis = Analysis.builder()
                .inheritanceModeOptions(
                        InheritanceModeOptions.of(Map.of(
                                SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f,
                                SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f)))
                .frequencySources(EnumSet.of(FrequencySource.GNOMAD_G_NFE, FrequencySource.THOUSAND_GENOMES, FrequencySource.TOPMED))
                .pathogenicitySources(EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.MVP, PathogenicitySource.REVEL))
                .build();
        AnalysisProto.Analysis result = new AnalysisProtoConverter().toProto(analysis);
        AnalysisProto.Analysis expected = AnalysisProto.Analysis.newBuilder()
                .putAllInheritanceModes(Map.of(
                        "AUTOSOMAL_DOMINANT", 0.1f,
                        "AUTOSOMAL_RECESSIVE_COMP_HET", 2.0f
                ))
                .addFrequencySources("THOUSAND_GENOMES")
                .addFrequencySources("TOPMED")
                .addFrequencySources("GNOMAD_G_NFE")
                .addPathogenicitySources("POLYPHEN")
                .addPathogenicitySources("REVEL")
                .addPathogenicitySources("MVP")
                .build();
        assertThat(result, equalTo(expected));
    }

    @Test
    void testToProtoExomeAnalysisSteps() {
        Analysis analysis = Analysis.builder()
                .addStep((new GeneBlacklistFilter()))
                .addStep(new FrequencyFilter(0.2f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new InheritanceFilter())
                .addStep(new OmimPriority(TestPriorityServiceFactory.stubPriorityService()))
                .addStep(new HiPhivePriority(HiPhiveOptions.defaults(), DataMatrix.empty(), TestPriorityServiceFactory.stubPriorityService()))
                .build();
        AnalysisProto.Analysis result = new AnalysisProtoConverter().toProto(analysis);
        AnalysisProto.Analysis expected = AnalysisProto.Analysis.newBuilder()
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder().setMaxFrequency(0.2f)))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder().setKeepNonPathogenic(true)))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setInheritanceFilter(FiltersProto.InheritanceFilter.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setOmimPrioritiser(PrioritisersProto.OmimPrioritiser.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setHiPhivePrioritiser(PrioritisersProto.HiPhivePrioritiser.newBuilder().setRunParams("human, mouse, fish, ppi")))
                .build();
        assertThat(result, equalTo(expected));
    }

    @Test
    void testToProtoGenomeAnalysisSteps() {
        Analysis analysis = Analysis.builder()
                .addStep(new HiPhivePriority(HiPhiveOptions.defaults(), DataMatrix.empty(), TestPriorityServiceFactory.stubPriorityService()))
                .addStep(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501f))
                .addStep(new FailedVariantFilter())
                .addStep(new RegulatoryFeatureFilter())
                .addStep(new GeneBlacklistFilter())
                .addStep(new FrequencyFilter(0.2f))
                .addStep(new PathogenicityFilter(true))
                .addStep(new InheritanceFilter())
                .addStep(new OmimPriority(TestPriorityServiceFactory.stubPriorityService()))
                .build();
        AnalysisProto.Analysis result = new AnalysisProtoConverter().toProto(analysis);
        AnalysisProto.Analysis expected = AnalysisProto.Analysis.newBuilder()
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setHiPhivePrioritiser(PrioritisersProto.HiPhivePrioritiser.newBuilder().setRunParams("human, mouse, fish, ppi")))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder().setPriorityType("HIPHIVE_PRIORITY").setMinPriorityScore(0.501f)))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setFailedVariantFilter(FiltersProto.FailedVariantFilter.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setRegulatoryFeatureFilter(FiltersProto.RegulatoryFeatureFilter.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder().setMaxFrequency(0.2f)))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder().setKeepNonPathogenic(true)))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setInheritanceFilter(FiltersProto.InheritanceFilter.newBuilder()))
                .addSteps(AnalysisProto.AnalysisStep.newBuilder().setOmimPrioritiser(PrioritisersProto.OmimPrioritiser.newBuilder()))
                .build();
        assertThat(result, equalTo(expected));
    }
}