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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.model.Filterable;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
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

        List<Filter> filters = getFiltersFromAnalysis(analysis);

        return filters.stream().map(filter -> makeFilterReport(filter, analysisResults)).collect(Collectors.toList());
    }

    private List<Filter> getFiltersFromAnalysis(Analysis analysis) {
        return analysis.getAnalysisSteps().stream().filter(Filter.class::isInstance).map(step -> (Filter) step).collect(Collectors.toList());
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
    protected FilterReport makeFilterReport(Filter filter, AnalysisResults analysisResults) {
        FilterType filterType = filter.getFilterType();
        Filter baseFilter = unWrapVariantFilterDataProvider(filter);
        switch (filterType) {
            case VARIANT_EFFECT_FILTER:
                return makeTargetFilterReport((VariantEffectFilter) baseFilter, analysisResults.getVariantEvaluations());
            case KNOWN_VARIANT_FILTER:
                return makeKnownVariantFilterReport((KnownVariantFilter) baseFilter, analysisResults.getVariantEvaluations());
            case FREQUENCY_FILTER:
                return makeFrequencyFilterReport((FrequencyFilter) baseFilter, analysisResults.getVariantEvaluations());
            case QUALITY_FILTER:
                return makeQualityFilterReport((QualityFilter) baseFilter, analysisResults.getVariantEvaluations());
            case PATHOGENICITY_FILTER:
                return makePathogenicityFilterReport((PathogenicityFilter) baseFilter, analysisResults.getVariantEvaluations());
            case INTERVAL_FILTER:
                return makeIntervalFilterReport((IntervalFilter) baseFilter, analysisResults.getVariantEvaluations());
            case INHERITANCE_FILTER:
                return makeInheritanceFilterReport((InheritanceFilter) baseFilter, analysisResults.getGenes());
            case PRIORITY_SCORE_FILTER:
                return makePriorityScoreFilterReport((PriorityScoreFilter) baseFilter, analysisResults.getGenes());
            default:
                return makeDefaultVariantFilterReport(filterType, analysisResults.getVariantEvaluations());
        }
    }
    
    private Filter unWrapVariantFilterDataProvider(Filter filter) {
        if (VariantFilterDataProvider.class.isInstance(filter) ) {
            VariantFilterDataProvider decorator = (VariantFilterDataProvider) filter;
            return decorator.getDecoratedFilter();
        }
        return filter;
    } 

    private FilterReport makeTargetFilterReport(VariantEffectFilter filter, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.VARIANT_EFFECT_FILTER, variantEvaluations);
        report.addMessage(String.format("Removed variants with effects of type: %s", filter.getOffTargetVariantTypes()));
        return report;
    }

    private FilterReport makeKnownVariantFilterReport(KnownVariantFilter filter, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.KNOWN_VARIANT_FILTER, variantEvaluations);

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

        int total = report.getPassed() + report.getFailed();

        report.addMessage(String.format("Removed %d variants with no RSID or frequency data (%.1f%%)", numNotInDatabase, asPercent(numNotInDatabase, total)));
        report.addMessage(String.format("dbSNP \"rs\" id available for %d variants (%.1f%%)", numDbSnpRsId, asPercent(numDbSnpRsId, total)));
        report.addMessage(String.format("Data available in dbSNP (for 1000 Genomes Phase I) for %d variants (%.1f%%)", numDbSnpFreqData, asPercent(numDbSnpFreqData, total)));
        report.addMessage(String.format("Data available in Exome Server Project for %d variants (%.1f%%)", numEspFreqData, asPercent(numEspFreqData, total)));
        report.addMessage(String.format("Data available from ExAC Project for %d variants (%.1f%%)", numExaCFreqData, asPercent(numExaCFreqData, total)));
        return report;
    }

    private double asPercent(double number, int total) {
        return 100f * number / total;
    }

    private FilterReport makeFrequencyFilterReport(FrequencyFilter filter, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.FREQUENCY_FILTER, variantEvaluations);

        report.addMessage(String.format("Variants filtered for maximum allele frequency of %.2f%%", filter.getMaxFreq()));
        return report;
    }

    private FilterReport makeQualityFilterReport(QualityFilter filter, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.QUALITY_FILTER, variantEvaluations);

        report.addMessage(String.format("Variants filtered for mimimum PHRED quality of %.1f", filter.getMimimumQualityThreshold()));
        return report;
    }

    private FilterReport makePathogenicityFilterReport(PathogenicityFilter filter, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.PATHOGENICITY_FILTER, variantEvaluations);

        if (filter.keepNonPathogenic()) {
            report.addMessage("Retained all non-pathogenic variants of all types. Scoring was applied, but the filter passed all variants.");
        } else {
            report.addMessage("Retained all non-pathogenic missense variants");
            //this is redundant as the defaut now is to keep all these anyway, but maybe somone will be interested in the cutoffs used for the categories?
            // Set up the message - these scores ought to belong to the score itself thather than being hard-coded here...
//            report.addMessage("Pathogenicity predictions are based on the dbNSFP-normalized values");
//            report.addMessage("Mutation Taster: >0.95 assumed pathogenic, prediction categories not shown");
//            report.addMessage("Polyphen2 (HVAR): \"D\" (> 0.956,probably damaging), \"P\": [0.447-0.955], "
//                    + "possibly damaging, and \"B\", <0.447, benign.");
//            report.addMessage("SIFT: \"D\"<0.05, damaging and \"T\"&ge;0.05, tolerated");
        }
        return report;
    }

    private FilterReport makeIntervalFilterReport(IntervalFilter filter, List<VariantEvaluation> variantEvaluations) {
        FilterReport report = makeDefaultVariantFilterReport(FilterType.INTERVAL_FILTER, variantEvaluations);

        report.addMessage(String.format("Restricted variants to interval: %s", filter.getGeneticInterval()));

        return report;
    }

    private FilterReport makeInheritanceFilterReport(InheritanceFilter filter, List<Gene> genes) {
        FilterReport report = makeDefaultGeneFilterReport(FilterType.INHERITANCE_FILTER, genes);

        StringJoiner stringJoiner = new StringJoiner(",");
        filter.getCompatibleModes().forEach(modeOfInheritance -> stringJoiner.add(modeOfInheritance.toString()));

        report.addMessage(String.format("Genes filtered for compatibility with %s inheritance.", stringJoiner.toString()));

        return report;
    }

    private FilterReport makePriorityScoreFilterReport(PriorityScoreFilter filter, List<Gene> genes) {
        FilterReport report = makeDefaultGeneFilterReport(FilterType.PRIORITY_SCORE_FILTER, genes);

        report.addMessage(String.format("Genes filtered for minimum %s score of %s",
                filter.getPriorityType(), filter.getMinPriorityScore()));

        return report;
    }

    /**
     *
     * @param filterType
     * @param variantEvaluations
     * @return
     */
    private FilterReport makeDefaultVariantFilterReport(FilterType filterType, List<VariantEvaluation> variantEvaluations) {
        int passed = countVariantsPassingFilter(variantEvaluations, filterType);
        int failed = variantEvaluations.size() - passed;
        return new FilterReport(filterType, passed, failed);
    }

    private int countVariantsPassingFilter(List<VariantEvaluation> variantEvaluations, FilterType filterType) {
        int passed = 0;
        for (VariantEvaluation ve : variantEvaluations) {
            if (ve.passedFilter(filterType)) {
                passed++;
            }
        }
        return passed;
    }

    private FilterReport makeDefaultGeneFilterReport(FilterType filterType, List<Gene> genes) {
        int passed = countGenesPassingFilter(genes, filterType);
        int failed = genes.size() - passed;
        return new FilterReport(filterType, passed, failed);
    }

    private int countGenesPassingFilter(List<Gene> genes, FilterType filterType) {
        int passed = 0;
        for (Filterable filterable : genes) {
            if (filterable.passedFilter(filterType)) {
                passed++;
            }
        }
        return passed;
    }

}
