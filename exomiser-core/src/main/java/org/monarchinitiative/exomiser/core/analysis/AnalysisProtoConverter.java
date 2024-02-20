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

import org.monarchinitiative.exomiser.api.v1.AnalysisProto;
import org.monarchinitiative.exomiser.api.v1.FiltersProto;
import org.monarchinitiative.exomiser.api.v1.PrioritisersProto;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.prioritisers.*;
import org.monarchinitiative.exomiser.core.proto.ProtoConverter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalysisProtoConverter implements ProtoConverter<Analysis, AnalysisProto.Analysis> {

    @Override
    public AnalysisProto.Analysis toProto(Analysis analysis) {
        return AnalysisProto.Analysis.newBuilder()
                .setAnalysisMode(analysis.getAnalysisMode() == AnalysisMode.PASS_ONLY ? AnalysisProto.AnalysisMode.PASS_ONLY : AnalysisProto.AnalysisMode.FULL)
                .putAllInheritanceModes(analysis.getInheritanceModeOptions().getMaxFreqs().entrySet().stream().collect(Collectors.toMap(subModeOfInheritanceFloatEntry -> subModeOfInheritanceFloatEntry.getKey().toString(), Map.Entry::getValue)))
                .addAllFrequencySources(analysis.getFrequencySources().stream().map(Objects::toString).toList())
                .addAllPathogenicitySources(analysis.getPathogenicitySources().stream().map(Objects::toString).toList())
                .addAllSteps(analysis.getAnalysisSteps().stream().map(analysisStepToProto()).filter(Objects::nonNull).toList())
                .build();
    }

    private Function<AnalysisStep, AnalysisProto.AnalysisStep> analysisStepToProto() {
        return analysisStep -> {
            if (analysisStep instanceof Filter) {
                return buildFilterProto(analysisStep);
            }
            if (analysisStep instanceof Prioritiser) {
                return buildPrioritiserProto(analysisStep);
            }
            return null;
        };
    }

    private AnalysisProto.AnalysisStep buildFilterProto(AnalysisStep analysisStep) {
        AnalysisProto.AnalysisStep.Builder stepBuilder = AnalysisProto.AnalysisStep.newBuilder();
        if (analysisStep instanceof VariantEffectFilter variantEffectFilter) {
            return stepBuilder
                    .setVariantEffectFilter(FiltersProto.VariantEffectFilter.newBuilder()
                            .addAllRemove(variantEffectFilter.getOffTargetVariantTypes().stream().map(Objects::toString).toList()))
                    .build();
        }
        if (analysisStep instanceof FrequencyFilter frequencyFilter) {
            return stepBuilder
                    .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder().setMaxFrequency(frequencyFilter.getMaxFreq()))
                    .build();
        }
        if (analysisStep instanceof PathogenicityFilter pathogenicityFilter) {
            return stepBuilder
                    .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder().setKeepNonPathogenic(pathogenicityFilter.keepNonPathogenic()))
                    .build();
        }
        if (analysisStep instanceof FailedVariantFilter) {
            return stepBuilder
                    .setFailedVariantFilter(FiltersProto.FailedVariantFilter.newBuilder())
                    .build();
        }
        if (analysisStep instanceof KnownVariantFilter) {
            return stepBuilder
                    .setKnownVariantFilter(FiltersProto.KnownVariantFilter.newBuilder())
                    .build();
        }
        if (analysisStep instanceof QualityFilter qualityFilter) {
            return stepBuilder
                    .setQualityFilter(FiltersProto.QualityFilter.newBuilder().setMinQuality((float) qualityFilter.getMimimumQualityThreshold()))
                    .build();
        }
        if (analysisStep instanceof IntervalFilter intervalFilter) {
            return stepBuilder
                    .setIntervalFilter(FiltersProto.IntervalFilter.newBuilder()
                            .addAllIntervals(intervalFilter.getChromosomalRegions().stream().map(region -> region.contigId() + ":" + region.start() + "-" + region.end()).toList()))
                    .build();
        }
        if (analysisStep instanceof GeneSymbolFilter geneSymbolFilter) {
            return stepBuilder
                    .setGenePanelFilter(FiltersProto.GenePanelFilter.newBuilder().addAllGeneSymbols(geneSymbolFilter.getGeneSymbols()))
                    .build();
        }

        if (analysisStep instanceof GeneBlacklistFilter geneBlacklistFilter) {
           return stepBuilder
                   .setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.newBuilder())
                   .build();
        }

        if (analysisStep instanceof InheritanceFilter) {
            return stepBuilder
                    .setInheritanceFilter(FiltersProto.InheritanceFilter.newBuilder().build())
                    .build();
        }
        if (analysisStep instanceof RegulatoryFeatureFilter) {
            return stepBuilder.setRegulatoryFeatureFilter(FiltersProto.RegulatoryFeatureFilter.newBuilder()).build();
        }
        if (analysisStep instanceof PriorityScoreFilter priorityScoreFilter) {
            return stepBuilder
                    .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                            .setMinPriorityScore((float) priorityScoreFilter.getMinPriorityScore())
                            .setPriorityType(priorityScoreFilter.getPriorityType().toString()))
                    .build();
        }
        return null;
    }

    private AnalysisProto.AnalysisStep buildPrioritiserProto(AnalysisStep analysisStep) {
        AnalysisProto.AnalysisStep.Builder stepBuilder = AnalysisProto.AnalysisStep.newBuilder();
        if (analysisStep instanceof OmimPriority) {
            return stepBuilder
                    .setOmimPrioritiser(PrioritisersProto.OmimPrioritiser.newBuilder())
                    .build();
        }
        if (analysisStep instanceof HiPhivePriority hiPhivePriority) {
            HiPhiveOptions hiPhiveOptions = hiPhivePriority.getOptions();
            return stepBuilder
                    .setHiPhivePrioritiser(PrioritisersProto.HiPhivePrioritiser.newBuilder()
                            .setRunParams(hiPhiveOptions.getRunParams())
                            .setDiseaseId(hiPhiveOptions.getDiseaseId())
                            .setCandidateGeneSymbol(hiPhiveOptions.getCandidateGeneSymbol()))
                    .build();
        }
        if (analysisStep instanceof PhenixPriority) {
            return stepBuilder
                    .setPhenixPrioritiser(PrioritisersProto.PhenixPrioritiser.newBuilder())
                    .build();
        }
        if (analysisStep instanceof PhivePriority) {
            return stepBuilder
                    .setPhivePrioritiser(PrioritisersProto.PhivePrioritiser.newBuilder())
                    .build();
        }
        if (analysisStep instanceof ExomeWalkerPriority exomeWalkerPriority) {
            return stepBuilder
                    .setExomeWalkerPrioritiser(PrioritisersProto.ExomeWalkerPrioritiser.newBuilder().addAllSeedGeneIds(exomeWalkerPriority.getSeedGenes()))
                    .build();
        }
        return null;
    }

    @Override
    public Analysis toDomain(AnalysisProto.Analysis analysisProto) {
        throw new IllegalStateException("Not yet implemented");
    }
}
