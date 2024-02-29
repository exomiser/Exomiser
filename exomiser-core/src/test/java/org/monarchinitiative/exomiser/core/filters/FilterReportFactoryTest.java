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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for FilterReportFactory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportFactoryTest {

    private FilterReportFactory instance;

    private AnalysisResults analysisResults;
    private List<VariantEvaluation> variantEvaluations;
    private List<Gene> genes;

    @BeforeEach
    public void setUp() {
        instance = new FilterReportFactory();

        variantEvaluations = new ArrayList<>();
        genes = new ArrayList<>();
        analysisResults = analysisResults(List.of());
    }

    private AnalysisResults analysisResults(List<FilterResultCount> filterResultCounts) {
        return AnalysisResults.builder()
                .filterCounts(filterResultCounts)
                .variantEvaluations(variantEvaluations)
                .genes(genes)
                .build();
    }

    private VariantEvaluation makeFailedVariant(FilterType filterType) {
        return TestFactory.variantBuilder(6, 1000000, "C", "T")
                .filterResults(new FailFilterResult(filterType))
                .build();
    }

    private VariantEvaluation makePassedVariant(FilterType filterType) {
        return TestFactory.variantBuilder(6, 1000000, "C", "T")
                .filterResults(new PassFilterResult(filterType))
                .build();
    }
    
    private Gene makeFailedGene(FilterType filterType) {
        VariantEvaluation failedFilterVariantEvaluation = makeFailedVariant(filterType);
        Gene failedFilterGene = new Gene("GENE1", 12345);
        failedFilterGene.addVariant(failedFilterVariantEvaluation);
        return failedFilterGene;
    }

    private Gene makePassedGene(FilterType filterType) {
        VariantEvaluation passedFilterVariantEvaluation = makePassedVariant(filterType);
        Gene passedFilterGene = new Gene("GENE2", 67890);
        passedFilterGene.addVariant(passedFilterVariantEvaluation);
        return passedFilterGene;
    }

    @Test
    public void testMakeFilterReportsNoTypesSpecifiedReturnsEmptyList() {
        List<FilterReport> emptyFilterReportList = new ArrayList<>();

        List<FilterReport> reports = instance.makeFilterReports(Analysis.builder().build(), analysisResults);

        assertThat(reports, equalTo(emptyFilterReportList));
    }

    @Test
    public void testMakeFilterReportsFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        Analysis analysis = Analysis.builder()
            .addStep(new FrequencyFilter(0.1f))
            .addStep(new PathogenicityFilter(true))
            .build();
        
        List<FilterReport> reports = instance.makeFilterReports(analysis, analysisResults);

        assertThat(reports.size(), equalTo(analysis.getAnalysisSteps().size()));
    }
    
    @Test
    public void testMakeFilterReportsDecoratedFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        Analysis analysis = Analysis.builder()
                .addStep(new FrequencyDataProvider(null, Collections.emptySet(), new KnownVariantFilter()))
                .addStep(new FrequencyDataProvider(null, Collections.emptySet(), new FrequencyFilter(0.1f)))
                .addStep(new PathogenicityDataProvider(null, Collections.emptySet(), new PathogenicityFilter(true)))
                .build();
        
        List<FilterReport> reports = instance.makeFilterReports(analysis, analysisResults);

        assertThat(reports.size(), equalTo(analysis.getAnalysisSteps().size()));
        assertThat(reports.get(0).getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
        assertThat(reports.get(1).getFilterType(), equalTo(FilterType.FREQUENCY_FILTER));
        assertThat(reports.get(2).getFilterType(), equalTo(FilterType.PATHOGENICITY_FILTER));
    }

    @Test
    public void testMakeDefaultGeneFilterReportContainsCorrectNumberOfPassedAndFailedGenes() {
        Filter filter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);    
        FilterType filterType = filter.getFilterType();

        List<FilterResultCount> filterResultCounts = List.of(new FilterResultCount(filterType, 25, 100));
        FilterReport report = instance.makeFilterReport(filter, analysisResults(filterResultCounts));

        assertThat(report.getPassed(), equalTo(25));
        assertThat(report.getFailed(), equalTo(100));
    }

    @Test
    public void testMakeTargetFilterReport() {
        VariantEffectFilter filter = new VariantEffectFilter(EnumSet.noneOf(VariantEffect.class));      

        List<String> messages = List.of(String.format("Removed variants with effects of type: %s", filter.getOffTargetVariantTypes()));
        FilterReport report = new FilterReport(filter.getFilterType(), 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeFrequencyFilterReportCanCopeWithNullFrequencyData() {
        Filter filter = new FrequencyFilter(0.1f);
        FilterType filterType = filter.getFilterType();

        VariantEvaluation variantEvalWithNullFrequencyData = makeFailedVariant(filterType);
        variantEvalWithNullFrequencyData.setFrequencyData(null);
        variantEvaluations.add(variantEvalWithNullFrequencyData);
        
        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, notNullValue());
    }
    
    @Test
    public void testMakeFrequencyFilterReport() {
        Filter filter = new FrequencyFilter(0.0f);
        FilterType filterType = filter.getFilterType();

        VariantEvaluation completelyNovelVariantEval = makePassedVariant(filterType);
        completelyNovelVariantEval.setFrequencyData(FrequencyData.empty());
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedVariant(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(FrequencyData.of("rs123456", Frequency.of(FrequencySource.THOUSAND_GENOMES, 100f), Frequency
                .of(FrequencySource.ESP_ALL, 100f), Frequency.of(FrequencySource.EXAC_OTHER, 100f)));
        variantEvaluations.add(mostCommonVariantEvalInTheWorld);

        List<String> messages = List.of("Variants filtered for maximum allele frequency of 0.00%");
        FilterReport report = new FilterReport(filter.getFilterType(), 1, 1, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults(List.of(new FilterResultCount(filter.getFilterType(), 1, 1))));
        assertThat(result, equalTo(report));
    }
    
    @Test
    public void testMakeKnownVariantFilterReportProducesCorrectStatistics() {
        Filter filter = new KnownVariantFilter();
        FilterType filterType = filter.getFilterType();

        VariantEvaluation completelyNovelVariantEval = makePassedVariant(filterType);
        completelyNovelVariantEval.setFrequencyData(FrequencyData.empty());
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedVariant(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(
                FrequencyData.of("rs123456",
                        Frequency.of(FrequencySource.THOUSAND_GENOMES, 100f),
                        Frequency.of(FrequencySource.ESP_ALL, 100f),
                        Frequency.of(FrequencySource.EXAC_OTHER, 100f)
                ));
        variantEvaluations.add(mostCommonVariantEvalInTheWorld);

        List<String> messages = new ArrayList<>();
        messages.add("Removed 1 variants with no RSID or frequency data (50.0%)");
        messages.add("dbSNP \"rs\" id available for 1 variants (50.0%)");
        messages.add("Data available in dbSNP (for 1000 Genomes Phase I) for 1 variants (50.0%)");
        messages.add("Data available in Exome Server Project for 1 variants (50.0%)");
        messages.add("Data available from ExAC Project for 1 variants (50.0%)");

        FilterReport report = new FilterReport(filter.getFilterType(), 1, 1, messages);
        FilterReport result = instance.makeFilterReport(filter, analysisResults(List.of(new FilterResultCount(filter.getFilterType(), 1, 1))));

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeQualityFilterReport() {
        Filter filter = new QualityFilter(100.0f);
        FilterType filterType = filter.getFilterType();

        List<String> messages = List.of("Variants filtered for mimimum PHRED quality of 100.0");
        FilterReport report = new FilterReport(filterType, 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePathogenicityFilterReportWhenRemovePathFilterCutOffIsTrue() {
        Filter filter = new PathogenicityFilter(true);
        FilterType filterType = FilterType.PATHOGENICITY_FILTER;

        List<String> messages = List.of("Retained all non-pathogenic variants of all types. Scoring was applied, but the filter passed all variants.");
        FilterReport report = new FilterReport(filterType, 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePathogenicityFilterReportWhenRemovePathFilterCutOffIsNotSpecified() {
        Filter filter = new PathogenicityFilter(false);
        FilterType filterType = FilterType.PATHOGENICITY_FILTER;

        List<String> messages = List.of("Retained all non-pathogenic missense variants");
        FilterReport report = new FilterReport(filterType, 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeIntervalFilterReport() {
        GeneticInterval interval = new GeneticInterval(1, 2, 3);
        Filter filter = new IntervalFilter(interval);
        FilterType filterType = FilterType.INTERVAL_FILTER;

        List<String> messages = List.of("Restricted variants to interval:", "1:2-3");
        FilterReport report = new FilterReport(filterType, 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeIntervalFilterReportLotsOfIntervals() {
        List<ChromosomalRegion> intervals = new ArrayList<>();
        intervals.add(new GeneticInterval(1, 2, 3));
        intervals.add(new GeneticInterval(2, 2, 3));
        intervals.add(new GeneticInterval(3, 2, 3));
        intervals.add(new GeneticInterval(4, 2, 3));
        intervals.add(new GeneticInterval(5, 2, 3));
        intervals.add(new GeneticInterval(6, 2, 3));
        intervals.add(new GeneticInterval(7, 2, 3));

        Filter filter = new IntervalFilter(intervals);

        List<String> messages = List.of(
                "Restricted variants to intervals:",
                "1:2-3",
                "2:2-3",
                "3:2-3",
                "...",
                "7:2-3"
                );
        FilterReport expected = new FilterReport(filter.getFilterType(), 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testMakeInheritanceFilterReport() {
        Filter filter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        FilterType filterType = FilterType.INHERITANCE_FILTER;

        List<String> messages = List.of("Variants filtered for compatibility with AUTOSOMAL_DOMINANT, AUTOSOMAL_RECESSIVE inheritance.");
        FilterReport report = new FilterReport(filterType, 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePriorityScoreFilterReport() {
        float minimumPriorityScore = 0.5f;
        Filter filter = new PriorityScoreFilter(PriorityType.PHIVE_PRIORITY, minimumPriorityScore);
        FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;

        List<String> messages = List.of("Genes filtered for minimum PHIVE_PRIORITY score of 0.5");
        FilterReport report = new FilterReport(filterType, 0, 0, messages);

        FilterReport result = instance.makeFilterReport(filter, analysisResults);

        assertThat(result, equalTo(report));
    }
}
