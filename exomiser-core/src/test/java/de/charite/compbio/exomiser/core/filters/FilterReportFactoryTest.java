/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for FilterReportFactory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportFactoryTest {

    private FilterReportFactory instance;

    private SampleData sampleData;
    private List<VariantEvaluation> variantEvaluations;
    private List<Gene> genes;

    @Before
    public void setUp() {
        instance = new FilterReportFactory();

        variantEvaluations = new ArrayList<>();
        genes = new ArrayList<>();
        sampleData = new SampleData();
        sampleData.setVariantEvaluations(variantEvaluations);
        sampleData.setGenes(genes);
        
    }

    private VariantEvaluation makeFailedVariant(FilterType filterType) {
        VariantEvaluation failedFilterVariantEvaluation = new VariantEvaluation.VariantBuilder(6, 1000000, "C", "T").build();
        failedFilterVariantEvaluation.addFilterResult(new FailFilterResult(filterType));
        return failedFilterVariantEvaluation;
    }

    private VariantEvaluation makePassedVariant(FilterType filterType) {
        VariantEvaluation passedFilterVariantEvaluation = new VariantEvaluation.VariantBuilder(6, 1000000, "C", "T").build();
        passedFilterVariantEvaluation.addFilterResult(new PassFilterResult(filterType));
        return passedFilterVariantEvaluation;
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

        List<FilterReport> reports = instance.makeFilterReports(Analysis.newBuilder().build(), sampleData);

        assertThat(reports, equalTo(emptyFilterReportList));
    }

    @Test
    public void testMakeFilterReportsFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        Analysis analysis = Analysis.newBuilder()
            .addStep(new FrequencyFilter(0.1f))
            .addStep(new PathogenicityFilter(true))
            .build();
        
        List<FilterReport> reports = instance.makeFilterReports(analysis, sampleData);

        assertThat(reports.size(), equalTo(analysis.getAnalysisSteps().size()));
    }
    
    @Test
    public void testMakeFilterReportsDecoratedFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        Analysis analysis = Analysis.newBuilder()
                .addStep(new FrequencyDataProvider(null, Collections.emptySet(), new KnownVariantFilter()))
                .addStep(new FrequencyDataProvider(null, Collections.emptySet(), new FrequencyFilter(0.1f)))
                .addStep(new PathogenicityDataProvider(null, Collections.emptySet(), new PathogenicityFilter(true)))
                .build();
        
        List<FilterReport> reports = instance.makeFilterReports(analysis, sampleData);

        assertThat(reports.size(), equalTo(analysis.getAnalysisSteps().size()));
        assertThat(reports.get(0).getFilterType(), equalTo(FilterType.KNOWN_VARIANT_FILTER));
        assertThat(reports.get(1).getFilterType(), equalTo(FilterType.FREQUENCY_FILTER));
        assertThat(reports.get(2).getFilterType(), equalTo(FilterType.PATHOGENICITY_FILTER));
    }

    @Test
    public void testMakeDefaultVariantFilterReportContainsCorrectNumberOfPassedAndFailedVariants() {
        FilterType filterType = FilterType.BED_FILTER;

        variantEvaluations.add(makePassedVariant(filterType));
        variantEvaluations.add(makeFailedVariant(filterType));

        FilterReport report = instance.makeFilterReport(new BedFilter(null), sampleData);

        assertThat(report.getPassed(), equalTo(1));
        assertThat(report.getFailed(), equalTo(1));
    }

    @Test
    public void testMakeDefaultGeneFilterReportContainsCorrectNumberOfPassedAndFailedGenes() {
        Filter filter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);    
        FilterType filterType = filter.getFilterType();

        genes.add(makePassedGene(filterType));
        genes.add(makeFailedGene(filterType));

        FilterReport report = instance.makeFilterReport(filter, sampleData);

        assertThat(report.getPassed(), equalTo(1));
        assertThat(report.getFailed(), equalTo(1));
    }

    @Test
    public void testMakeTargetFilterReport() {
        VariantEffectFilter filter = new VariantEffectFilter(EnumSet.noneOf(VariantEffect.class));      

        FilterReport report = new FilterReport(filter.getFilterType(), 0, 0);
        report.addMessage(String.format("Removed variants with effects of type: %s", filter.getOffTargetVariantTypes()));

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeFrequencyFilterReportCanCopeWithNullFrequencyData() {
        Filter filter = new FrequencyFilter(0.1f);
        FilterType filterType = filter.getFilterType();

        VariantEvaluation variantEvalWithNullFrequencyData = makeFailedVariant(filterType);
        variantEvalWithNullFrequencyData.setFrequencyData(null);
        variantEvaluations.add(variantEvalWithNullFrequencyData);
        
        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, notNullValue());
    }
    
    @Test
    public void testMakeFrequencyFilterReport() {
        Filter filter = new FrequencyFilter(0.0f);
        FilterType filterType = filter.getFilterType();

        VariantEvaluation completelyNovelVariantEval = makePassedVariant(filterType);
        completelyNovelVariantEval.setFrequencyData(new FrequencyData());
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedVariant(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency(100f, FrequencySource.THOUSAND_GENOMES), new Frequency(100f, FrequencySource.ESP_ALL), new Frequency(100f, FrequencySource.EXAC_OTHER)));
        variantEvaluations.add(mostCommonVariantEvalInTheWorld);
        
        FilterReport report = new FilterReport(filterType, 1, 1);
        
        report.addMessage("Variants filtered for maximum allele frequency of 0.00%");   
        FilterReport result = instance.makeFilterReport(filter, sampleData);
        System.out.println(result);
        assertThat(result, equalTo(report));
    }
    
    @Test
    public void testMakeKnownVariantFilterReportProducesCorrectStatistics() {
        Filter filter = new KnownVariantFilter();
        FilterType filterType = filter.getFilterType();

        VariantEvaluation completelyNovelVariantEval = makePassedVariant(filterType);
        completelyNovelVariantEval.setFrequencyData(new FrequencyData());
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedVariant(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency(100f, FrequencySource.THOUSAND_GENOMES), new Frequency(100f, FrequencySource.ESP_ALL), new Frequency(100f, FrequencySource.EXAC_OTHER)));
        variantEvaluations.add(mostCommonVariantEvalInTheWorld);
        
        FilterReport report = new FilterReport(filterType, 1, 1);
        
        report.addMessage("Removed 1 variants with no RSID or frequency data (50.0%)");
        report.addMessage("dbSNP \"rs\" id available for 1 variants (50.0%)");
        report.addMessage("Data available in dbSNP (for 1000 Genomes Phase I) for 1 variants (50.0%)");
        report.addMessage("Data available in Exome Server Project for 1 variants (50.0%)");
        report.addMessage("Data available from ExAC Project for 1 variants (50.0%)");        
        FilterReport result = instance.makeFilterReport(filter, sampleData);
        System.out.println(result);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeQualityFilterReport() {
        Filter filter = new QualityFilter(100.0f);
        FilterType filterType = filter.getFilterType();

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Variants filtered for mimimum PHRED quality of 100.0");

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePathogenicityFilterReportWhenRemovePathFilterCutOffIsTrue() {
        Filter filter = new PathogenicityFilter(true);
        FilterType filterType = FilterType.PATHOGENICITY_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Retained all non-pathogenic variants of all types. Scoring was applied, but the filter passed all variants.");

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePathogenicityFilterReportWhenRemovePathFilterCutOffIsNotSpecified() {
        Filter filter = new PathogenicityFilter(false);
        FilterType filterType = FilterType.PATHOGENICITY_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Retained all non-pathogenic missense variants");

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeIntervalFilterReport() {
        GeneticInterval interval = new GeneticInterval(1, 2, 3);
        Filter filter = new IntervalFilter(interval);
        FilterType filterType = FilterType.INTERVAL_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage(String.format("Restricted variants to interval: %s", interval));

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeInheritanceFilterReport() {
        ModeOfInheritance expectedInheritanceMode = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        Filter filter = new InheritanceFilter(expectedInheritanceMode);
        FilterType filterType = FilterType.INHERITANCE_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Genes filtered for compatibility with AUTOSOMAL_DOMINANT inheritance.");

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePriorityScoreFilterReport() {
        float minimumPriorityScore = 0.5f;
        Filter filter = new PriorityScoreFilter(PriorityType.PHIVE_PRIORITY, minimumPriorityScore);
        FilterType filterType = FilterType.PRIORITY_SCORE_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Genes filtered for minimum PHIVE_PRIORITY score of 0.5");
        
        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }
}
