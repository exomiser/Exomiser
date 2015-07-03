/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.Analysis;
import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for FilterReportFactory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FilterReportFactoryTest {

    private FilterReportFactory instance;

    private List<VariantEvaluation> variantEvaluations;
    private List<Gene> genes;
    private SampleData sampleData;

    private Analysis analysis;
    
    @Before
    public void setUp() {
        instance = new FilterReportFactory();

        variantEvaluations = new ArrayList<>();
        genes = new ArrayList<>();
        sampleData = new SampleData();
        sampleData.setVariantEvaluations(variantEvaluations);
        sampleData.setGenes(genes);
        
        analysis = new Analysis();
        analysis.setSampleData(sampleData);
    }

    private VariantEvaluation makeFailedFilterVariantEvaluation(FilterType filterType) {
        VariantEvaluation failedFilterVariantEvaluation = new VariantEvaluation.VariantBuilder(6, 1000000, "C", "T").build();
        failedFilterVariantEvaluation.addFilterResult(new FailFilterResult(filterType, 0.0f));
        return failedFilterVariantEvaluation;
    }

    private VariantEvaluation makePassedFilterVariantEvaluation(FilterType filterType) {
        VariantEvaluation passedFilterVariantEvaluation = new VariantEvaluation.VariantBuilder(6, 1000000, "C", "T").build();
        passedFilterVariantEvaluation.addFilterResult(new PassFilterResult(filterType, 1.0f));
        return passedFilterVariantEvaluation;
    }
    
    private Gene makeFailedFilterGene(FilterType filterType) {
        VariantEvaluation failedFilterVariantEvaluation = makeFailedFilterVariantEvaluation(filterType);
        Gene failedFilterGene = new Gene("GENE1", 12345);
        failedFilterGene.addVariant(failedFilterVariantEvaluation);
        return failedFilterGene;
    }

    private Gene makePassedFilterGene(FilterType filterType) {
        VariantEvaluation passedFilterVariantEvaluation = makePassedFilterVariantEvaluation(filterType);
        Gene passedFilterGene = new Gene("GENE2", 67890);
        passedFilterGene.addVariant(passedFilterVariantEvaluation);
        return passedFilterGene;
    }

    @Test
    public void testMakeFilterReportsNoTypesSpecifiedReturnsEmptyList() {
        List<FilterReport> emptyFilterReportList = new ArrayList<>();

        List<FilterReport> reports = instance.makeFilterReports(analysis);

        assertThat(reports, equalTo(emptyFilterReportList));
    }

    @Test
    public void testMakeFilterReportsFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        analysis.addStep(new FrequencyFilter(0.1f));
        analysis.addStep(new PathogenicityFilter(true));
        
        List<FilterReport> reports = instance.makeFilterReports(analysis);

        assertThat(reports.size(), equalTo(analysis.getAnalysisSteps().size()));
    }

    @Test
    public void testMakeDefaultVariantFilterReportContainsCorrectNumberOfPassedAndFailedVariants() {
        FilterType filterType = FilterType.BED_FILTER;

        VariantEvaluation passedFilterVariantEvaluation = makePassedFilterVariantEvaluation(filterType);
        variantEvaluations.add(passedFilterVariantEvaluation);

        VariantEvaluation failedFilterVariantEvaluation = makeFailedFilterVariantEvaluation(filterType);
        variantEvaluations.add(failedFilterVariantEvaluation);

        FilterReport report = instance.makeFilterReport(new BedFilter(null), sampleData);

        assertThat(report.getPassed(), equalTo(1));
        assertThat(report.getFailed(), equalTo(1));
    }

    @Test
    public void testMakeDefaultGeneFilterReportContainsCorrectNumberOfPassedAndFailedGenes() {
        Filter filter = new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_RECESSIVE);    
        FilterType filterType = filter.getFilterType();

        Gene passedFilterGene = makePassedFilterGene(filterType);
        genes.add(passedFilterGene);

        Gene failedFilterGene = makeFailedFilterGene(filterType);
        genes.add(failedFilterGene);

        FilterReport report = instance.makeFilterReport(filter, sampleData);

        assertThat(report.getPassed(), equalTo(1));
        assertThat(report.getFailed(), equalTo(1));
    }

    @Test
    public void testMakeTargetFilterReport() {
        VariantEffectFilter filter = new VariantEffectFilter(EnumSet.noneOf(VariantEffect.class));      

        FilterReport report = new FilterReport(filter.getFilterType(), 0, 0);
        report.addMessage("Removed a total of 0 off-target variants from further consideration");
        report.addMessage(String.format("Off target variants are defined as variants with effect: %s", filter.getOffTargetVariantTypes()));

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeFrequencyFilterReportCanCopeWithNullFrequencyData() {
        Filter filter = new FrequencyFilter(0.1f);
        FilterType filterType = filter.getFilterType();

        VariantEvaluation variantEvalWithNullFrequencyData = makeFailedFilterVariantEvaluation(filterType);
        variantEvalWithNullFrequencyData.setFrequencyData(null);
        variantEvaluations.add(variantEvalWithNullFrequencyData);
        
        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, notNullValue());
    }
    
    @Test
    public void testMakeFrequencyFilterReport() {
        Filter filter = new FrequencyFilter(0.0f);
        FilterType filterType = filter.getFilterType();

        VariantEvaluation completelyNovelVariantEval = makePassedFilterVariantEvaluation(filterType);
        completelyNovelVariantEval.setFrequencyData(new FrequencyData());
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedFilterVariantEvaluation(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency[]{new Frequency(100f, FrequencySource.THOUSAND_GENOMES), new Frequency(100f, FrequencySource.ESP_ALL), new Frequency(100f, FrequencySource.EXAC_OTHER)}));
        variantEvaluations.add(mostCommonVariantEvalInTheWorld);
        
        FilterReport report = new FilterReport(filterType, 1, 1);
        
        report.addMessage("Allele frequency < 0.00%");   
        FilterReport result = instance.makeFilterReport(filter, sampleData);
        System.out.println(result);
        assertThat(result, equalTo(report));
    }
    
    @Test
    public void testMakeKnownVariantFilterReportProducesCorrectStatistics() {
        Filter filter = new KnownVariantFilter();
        FilterType filterType = filter.getFilterType();

        VariantEvaluation completelyNovelVariantEval = makePassedFilterVariantEvaluation(filterType);
        completelyNovelVariantEval.setFrequencyData(new FrequencyData());
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedFilterVariantEvaluation(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency[]{new Frequency(100f, FrequencySource.THOUSAND_GENOMES), new Frequency(100f, FrequencySource.ESP_ALL), new Frequency(100f, FrequencySource.EXAC_OTHER)}));
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
        report.addMessage("PHRED quality 100.0");

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
        report.addMessage(String.format("Total of 0 genes were analyzed. 0 had genes with distribution compatible with %s inheritance.", expectedInheritanceMode));

        FilterReport result = instance.makeFilterReport(filter, sampleData);

        assertThat(result, equalTo(report));
    }

}
