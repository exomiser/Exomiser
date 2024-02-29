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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Factory class for producing {@code FilterReport} lists from the list of
 * filtered {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportFactory {

    private static final Logger logger = LoggerFactory.getLogger(FilterReportFactory.class);

    public FilterReportFactory() {
        Locale.setDefault(Locale.UK);
    }

    /**
     * Makes a List of {@code FilterReport} for the specified {@code Analysis}.
     *
     * @param analysis
     * @return a List of {@code FilterReport}
     */
    public List<FilterReport> makeFilterReports(Analysis analysis, AnalysisResults analysisResults) {
        return analysis.getAnalysisSteps().stream()
                .filter(Filter.class::isInstance)
                .map(Filter.class::cast)
                .map(filter -> makeFilterReport(filter, analysisResults))
                .collect(Collectors.toList());
    }

    /**
     * Returns a FilterReport for the AnalysisResults and the specified FilterType.
     * If the FilterType is not recognised or supported then this method will
     * return a default report with no messages.
     *
     * @param filter
     * @param analysisResults
     * @return
     */
    protected FilterReport makeFilterReport(Filter<?> filter, AnalysisResults analysisResults) {
        Filter<?> baseFilter = unWrapVariantFilterDataProvider(filter);
        FilterType filterType = filter.getFilterType();
        FilterResultCount filterResultCount = analysisResults.getFilterCount(filterType);
        return switch (filterType) {
            case FAILED_VARIANT_FILTER ->
                    filterReport(filterResultCount, failedVariantFilterMessages((FailedVariantFilter) baseFilter));
            case VARIANT_EFFECT_FILTER -> filterReport(filterResultCount, messages((VariantEffectFilter) baseFilter));
            case KNOWN_VARIANT_FILTER ->
                    filterReport(filterResultCount, messages((KnownVariantFilter) baseFilter, analysisResults.getVariantEvaluations()));
            case FREQUENCY_FILTER -> filterReport(filterResultCount, messages((FrequencyFilter) baseFilter));
            case QUALITY_FILTER -> filterReport(filterResultCount, messages((QualityFilter) baseFilter));
            case ENTREZ_GENE_ID_FILTER -> filterReport(filterResultCount, messages((GeneSymbolFilter) baseFilter));
            case PATHOGENICITY_FILTER -> filterReport(filterResultCount, messages((PathogenicityFilter) baseFilter));
            case INTERVAL_FILTER -> filterReport(filterResultCount, messages((IntervalFilter) baseFilter));
            case INHERITANCE_FILTER -> filterReport(filterResultCount, messages((InheritanceFilter) baseFilter));
            case BED_FILTER -> filterReport(filterResultCount, List.of());
            case PRIORITY_SCORE_FILTER -> filterReport(filterResultCount, messages((PriorityScoreFilter) baseFilter));
            case REGULATORY_FEATURE_FILTER ->
                    filterReport(filterResultCount, messages((RegulatoryFeatureFilter) baseFilter));
            case GENE_BLACKLIST_FILTER -> filterReport(filterResultCount, messages((GeneBlacklistFilter) baseFilter));
        };
    }

    private FilterReport filterReport(FilterResultCount filterResultCount, List<String> messages) {
        return new FilterReport(filterResultCount.filterType(), filterResultCount.passCount(), filterResultCount.failCount(), messages);
    }

    private Filter<?> unWrapVariantFilterDataProvider(Filter<?> filter) {
        return filter instanceof VariantFilterDataProvider decorator ? decorator.getDecoratedFilter() : filter;
    }

    private List<String> failedVariantFilterMessages(FailedVariantFilter baseFilter) {
        return List.of("Removed variants without PASS or . in VCF FILTER field");
    }

    private List<String> messages(VariantEffectFilter variantEffectFilter) {
        return List.of(String.format("Removed variants with effects of type: %s", variantEffectFilter.getOffTargetVariantTypes()));
    }

    private List<String> messages(KnownVariantFilter filter, List<VariantEvaluation> variantEvaluations) {
        int numNotInDatabase = 0;
        int numDbSnpFreqData = 0;
        int numDbSnpRsId = 0;
        int numEspFreqData = 0;
        int numExaCFreqData = 0;

        for (VariantEvaluation ve : variantEvaluations) {
            FrequencyData frequencyData = ve.getFrequencyData();

            if (!frequencyData.isRepresentedInDatabase()) {
                numNotInDatabase++;
            }
            if (frequencyData.hasDbSnpData()) {
                numDbSnpFreqData++;
            }
            if (frequencyData.hasDbSnpRsID()) {
                numDbSnpRsId++;
            }
            if (frequencyData.hasEspData()) {
                numEspFreqData++;
            }
            if (frequencyData.hasExacData()) {
                numExaCFreqData++;
            }
        }

        int total = variantEvaluations.size();

        List<String> messages = new ArrayList<>();
        messages.add(String.format("Removed %d variants with no RSID or frequency data (%.1f%%)", numNotInDatabase, asPercent(numNotInDatabase, total)));
        messages.add(String.format("dbSNP \"rs\" id available for %d variants (%.1f%%)", numDbSnpRsId, asPercent(numDbSnpRsId, total)));
        messages.add(String.format("Data available in dbSNP (for 1000 Genomes Phase I) for %d variants (%.1f%%)", numDbSnpFreqData, asPercent(numDbSnpFreqData, total)));
        messages.add(String.format("Data available in Exome Server Project for %d variants (%.1f%%)", numEspFreqData, asPercent(numEspFreqData, total)));
        messages.add(String.format("Data available from ExAC Project for %d variants (%.1f%%)", numExaCFreqData, asPercent(numExaCFreqData, total)));
        return List.copyOf(messages);
    }

    private double asPercent(double number, int total) {
        return 100f * number / total;
    }

    private List<String> messages(FrequencyFilter frequencyFilter) {
        return List.of(String.format("Variants filtered for maximum allele frequency of %.2f%%", frequencyFilter.getMaxFreq()));
    }

    private List<String> messages(QualityFilter qualityFilter) {
        return List.of(String.format("Variants filtered for mimimum PHRED quality of %.1f", qualityFilter.getMimimumQualityThreshold()));
    }

    private List<String> messages(PathogenicityFilter pathogenicityFilter) {
        if (pathogenicityFilter.keepNonPathogenic()) {
            return List.of("Retained all non-pathogenic variants of all types. Scoring was applied, but the filter passed all variants.");
        }
        return List.of("Retained all non-pathogenic missense variants");
    }

    private List<String> messages(RegulatoryFeatureFilter baseFilter) {
        return List.of("Variants found within a regulatory region or <= 20 Kb upstream of the nearest gene");
    }

    private List<String> messages(GeneSymbolFilter baseFilter) {
        return List.of(String.format("Removed variants in genes: %s", baseFilter.getGeneSymbols()));
    }

    private List<String> messages(GeneBlacklistFilter baseFilter) {
        return List.of(String.format("Removed variants in blacklisted genes including pseudogenes, HLA genes, and others that have a high degree of variants called in healthy individuals: %s", baseFilter.getBlacklist().stream().sorted().toList()));
    }

    private List<String> messages(IntervalFilter intervalFilter) {
        List<ChromosomalRegion> chromosomalRegions = intervalFilter.getChromosomalRegions();

        List<String> messages = new ArrayList<>();
        if (chromosomalRegions.size() == 1) {
            messages.add("Restricted variants to interval:");
        } else {
            messages.add("Restricted variants to intervals:");
        }

        int regionsToShow = 5;
        if (chromosomalRegions.size() <= regionsToShow) {
            for (ChromosomalRegion chromosomalRegion : chromosomalRegions) {
                messages.add(formatRegion(chromosomalRegion));
            }
        } else {
            for (int i = 0; i < regionsToShow - 2; i++) {
                ChromosomalRegion region = chromosomalRegions.get(i);
                messages.add(formatRegion(region));
            }
            messages.add("...");
            ChromosomalRegion finalRegion = chromosomalRegions.get(chromosomalRegions.size() - 1);
            messages.add(formatRegion(finalRegion));
        }
        return messages;
    }

    private String formatRegion(ChromosomalRegion region) {
        return String.format("%d:%d-%d", region.contigId(), region.start(), region.end());
    }

    private List<String> messages(InheritanceFilter inheritanceFilter) {
        String inheritanceModes = inheritanceFilter.getCompatibleModes()
                .stream()
                .map(ModeOfInheritance::toString)
                .collect(Collectors.joining(", "));

        return List.of(String.format("Variants filtered for compatibility with %s inheritance.", inheritanceModes));
    }

    private List<String> messages(PriorityScoreFilter priorityScoreFilter) {
        return List.of(String.format("Genes filtered for minimum %s score of %s", priorityScoreFilter.getPriorityType(), priorityScoreFilter.getMinPriorityScore()));
    }

}
