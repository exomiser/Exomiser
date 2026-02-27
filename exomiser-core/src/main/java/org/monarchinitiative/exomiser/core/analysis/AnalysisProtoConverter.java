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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalysisProtoConverter implements ProtoConverter<Analysis, AnalysisProto.Analysis> {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisProtoConverter.class);

    @Override
    public AnalysisProto.Analysis toProto(Analysis analysis) {
        return AnalysisProto.Analysis.newBuilder()
                .setAnalysisMode(analysis.analysisMode() == AnalysisMode.PASS_ONLY ? AnalysisProto.AnalysisMode.PASS_ONLY : AnalysisProto.AnalysisMode.FULL)
                .putAllInheritanceModes(analysis.inheritanceModeOptions().getMaxFreqs().entrySet().stream().collect(Collectors.toMap(subModeOfInheritanceFloatEntry -> subModeOfInheritanceFloatEntry.getKey().toString(), Map.Entry::getValue)))
                .addAllFrequencySources(analysis.frequencySources().stream().map(Objects::toString).toList())
                .addAllPathogenicitySources(analysis.pathogenicitySources().stream().map(Objects::toString).toList())
                .addAllSteps(analysis.analysisSteps().stream().map(analysisStepToProto()).filter(Objects::nonNull).toList())
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
        return switch (analysisStep) {
            case VariantEffectFilter variantEffectFilter -> stepBuilder
                    .setVariantEffectFilter(FiltersProto.VariantEffectFilter.newBuilder()
                            .addAllRemove(variantEffectFilter.getOffTargetVariantTypes().stream().map(Objects::toString).toList()))
                    .build();
            case FrequencyFilter(float maxFreq) -> stepBuilder
                    .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder().setMaxFrequency(maxFreq))
                    .build();
            case PathogenicityFilter(boolean keepNonPathogenic) -> stepBuilder
                    .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder().setKeepNonPathogenic(keepNonPathogenic))
                    .build();
            case FailedVariantFilter failedVariantFilter -> stepBuilder
                    .setFailedVariantFilter(FiltersProto.FailedVariantFilter.newBuilder())
                    .build();
            case KnownVariantFilter knownVariantFilter -> stepBuilder
                    .setKnownVariantFilter(FiltersProto.KnownVariantFilter.newBuilder())
                    .build();
            case QualityFilter(double mimimumQualityThreshold) -> stepBuilder
                    .setQualityFilter(FiltersProto.QualityFilter.newBuilder().setMinQuality((float) mimimumQualityThreshold))
                    .build();
            case AlleleBalanceFilter alleleBalanceFilter -> stepBuilder
                    .setAlleleBalanceFilter(FiltersProto.AlleleBalanceFilter.newBuilder())
                    .build();
            case IntervalFilter intervalFilter -> stepBuilder
                    .setIntervalFilter(FiltersProto.IntervalFilter.newBuilder()
                            .addAllIntervals(intervalFilter.getChromosomalRegions().stream().map(region -> region.contigId() + ":" + region.start() + "-" + region.end()).toList()))
                    .build();
            case GeneSymbolFilter geneSymbolFilter -> stepBuilder
                    .setGenePanelFilter(FiltersProto.GenePanelFilter.newBuilder().addAllGeneSymbols(geneSymbolFilter.getGeneSymbols()))
                    .build();
            case GeneBlacklistFilter geneBlacklistFilter -> stepBuilder
                    .setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.newBuilder())
                    .build();
            case InheritanceFilter inheritanceFilter -> stepBuilder
                    .setInheritanceFilter(FiltersProto.InheritanceFilter.newBuilder())
                    .build();
            case RegulatoryFeatureFilter regulatoryFeatureFilter -> stepBuilder
                    .setRegulatoryFeatureFilter(FiltersProto.RegulatoryFeatureFilter.newBuilder())
                    .build();
            case PriorityScoreFilter(PriorityType priorityType, double minPriorityScore) -> stepBuilder
                    .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                            .setMinPriorityScore((float) minPriorityScore)
                            .setPriorityType(priorityType.toString()))
                    .build();
            default -> {
                logger.warn("Unknown analysis step type: {}", analysisStep);
                yield null;
            }
        };
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
                            .setDiseaseId(hiPhiveOptions.diseaseId())
                            .setCandidateGeneSymbol(hiPhiveOptions.candidateGeneSymbol()))
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
