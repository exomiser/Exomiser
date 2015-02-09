/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.frequency.RsId;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for FilterReportFactory.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class FilterReportFactoryTest {

    private FilterReportFactory instance;

    private ExomiserSettings settings;
    private List<VariantEvaluation> variantEvaluations;
    private List<Gene> genes;
    private SampleData sampleData;

    @Mock
    private Variant mockVariant;

    @Before
    public void setUp() {
        instance = new FilterReportFactory();
        settings = new SettingsBuilder()
                .vcfFilePath(Paths.get("testVcf.vcf"))
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .build();
        variantEvaluations = new ArrayList<>();
        genes = new ArrayList<>();
        sampleData = new SampleData();
        sampleData.setVariantEvaluations(variantEvaluations);
        sampleData.setGenes(genes);
    }

    private VariantEvaluation makeFailedFilterVariantEvaluation(FilterType filterType) {
        VariantEvaluation failedFilterVariantEvaluation = new VariantEvaluation(mockVariant);
        failedFilterVariantEvaluation.addFilterResult(new GenericFilterResult(filterType, 0.0f, FilterResultStatus.FAIL));
        return failedFilterVariantEvaluation;
    }

    private VariantEvaluation makePassedFilterVariantEvaluation(FilterType filterType) {
        VariantEvaluation passedFilterVariantEvaluation = new VariantEvaluation(mockVariant);
        passedFilterVariantEvaluation.addFilterResult(new GenericFilterResult(filterType, 1.0f, FilterResultStatus.PASS));
        return passedFilterVariantEvaluation;
    }
    
    private Gene makeFailedFilterGene(FilterType filterType) {
        VariantEvaluation failedFilterVariantEvaluation = makeFailedFilterVariantEvaluation(filterType);
        Gene failedFilterGene = new Gene(failedFilterVariantEvaluation);
        return failedFilterGene;
    }

    private Gene makePassedFilterGene(FilterType filterType) {
        VariantEvaluation passedFilterVariantEvaluation = makePassedFilterVariantEvaluation(filterType);
        Gene passedFilterGene = new Gene(passedFilterVariantEvaluation);
        return passedFilterGene;
    }

    @Test
    public void testMakeFilterReportsNoTypesSpecifiedReturnsEmptyList() {
        List<FilterType> filterTypes = new ArrayList<>();
        List<FilterReport> emptyFilterReportList = new ArrayList<>();

        List<FilterReport> reports = instance.makeFilterReports(filterTypes, settings, sampleData);

        assertThat(reports, equalTo(emptyFilterReportList));
    }

    @Test
    public void testMakeFilterReportsFrequencyPathogenicityTypesSpecifiedReturnsListWithTwoReports() {
        List<FilterType> filterTypes = new ArrayList<>();
        filterTypes.add(FilterType.FREQUENCY_FILTER);
        filterTypes.add(FilterType.PATHOGENICITY_FILTER);

        List<FilterReport> reports = instance.makeFilterReports(filterTypes, settings, sampleData);

        assertThat(reports.size(), equalTo(filterTypes.size()));
    }

    @Test
    public void testMakeDefaultVariantFilterReportContainsCorrectNumberOfPassedAndFailedVariants() {
        FilterType filterType = FilterType.BED_FILTER;

        VariantEvaluation passedFilterVariantEvaluation = makePassedFilterVariantEvaluation(filterType);
        variantEvaluations.add(passedFilterVariantEvaluation);

        VariantEvaluation failedFilterVariantEvaluation = makeFailedFilterVariantEvaluation(filterType);
        variantEvaluations.add(failedFilterVariantEvaluation);

        FilterReport report = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(report.getPassed(), equalTo(1));
        assertThat(report.getFailed(), equalTo(1));
    }

    @Test
    public void testMakeDefaultGeneFilterReportContainsCorrectNumberOfPassedAndFailedGenes() {
        FilterType filterType = FilterType.INHERITANCE_FILTER;

        Gene passedFilterGene = makePassedFilterGene(filterType);
        genes.add(passedFilterGene);

        Gene failedFilterGene = makeFailedFilterGene(filterType);
        genes.add(failedFilterGene);

        FilterReport report = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(report.getPassed(), equalTo(1));
        assertThat(report.getFailed(), equalTo(1));
    }

    @Test
    public void testMakeTargetFilterReport() {
        FilterType filterType = FilterType.TARGET_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Removed a total of 0 off-target variants from further consideration");
        report.addMessage("Off target variants are defined as synonymous, intergenic, intronic but not in splice sequences");

        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeFrequencyFilterReportCanCopeWithNullFrequencyData() {
        FilterType filterType = FilterType.FREQUENCY_FILTER;

        VariantEvaluation variantEvalWithNullFrequencyData = makeFailedFilterVariantEvaluation(filterType);
        variantEvalWithNullFrequencyData.setFrequencyData(null);
        variantEvaluations.add(variantEvalWithNullFrequencyData);
        
        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, notNullValue());
    }
    
    @Test
    public void testMakeFrequencyFilterReportProducesCorrectStatistics() {
        FilterType filterType = FilterType.FREQUENCY_FILTER;

        VariantEvaluation completelyNovelVariantEval = makePassedFilterVariantEvaluation(filterType);
        completelyNovelVariantEval.setFrequencyData(new FrequencyData(null, null, null, null, null));
        variantEvaluations.add(completelyNovelVariantEval);
        
        VariantEvaluation mostCommonVariantEvalInTheWorld = makeFailedFilterVariantEvaluation(filterType);
        mostCommonVariantEvalInTheWorld.setFrequencyData(new FrequencyData(new RsId(123456), new Frequency(100f), new Frequency(100f), new Frequency(100f), new Frequency(100f)));
        variantEvaluations.add(mostCommonVariantEvalInTheWorld);
        
        settings = new SettingsBuilder().maximumFrequency(0.0f).build();
        FilterReport report = new FilterReport(filterType, 1, 1);
        
        report.addMessage("Allele frequency < 0.00 %");
        report.addMessage("Frequency Data available in dbSNP (for 1000 Genomes Phase I) for 1 variants (50.0%)");
        report.addMessage("dbSNP \"rs\" id available for 1 variants (50.0%)");
        report.addMessage("Data available in Exome Server Project for 1 variants (50.0%)");
                
        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeQualityFilterReport() {
        FilterType filterType = FilterType.QUALITY_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("PHRED quality 0.0");

        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePathogenicityFilterReportWhenRemovePathFilterCutOffIsTrue() {
        FilterType filterType = FilterType.PATHOGENICITY_FILTER;

        settings = new SettingsBuilder().removePathFilterCutOff(true).build();

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Retained all non-pathogenic variants of all types. Scoring was applied, but the filter passed all variants.");

        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakePathogenicityFilterReportWhenRemovePathFilterCutOffIsNotSpecified() {
        FilterType filterType = FilterType.PATHOGENICITY_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage("Retained all non-pathogenic missense variants");

        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeIntervalFilterReport() {
        FilterType filterType = FilterType.INTERVAL_FILTER;

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage(String.format("Restricted variants to interval: %s", settings.getGeneticInterval()));

        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

    @Test
    public void testMakeInheritanceFilterReport() {
        FilterType filterType = FilterType.INHERITANCE_FILTER;

        ModeOfInheritance expectedInheritanceMode = ModeOfInheritance.AUTOSOMAL_DOMINANT;
        settings = new SettingsBuilder().modeOfInheritance(expectedInheritanceMode).build();

        FilterReport report = new FilterReport(filterType, 0, 0);
        report.addMessage(String.format("Total of 0 genes were analyzed. 0 had genes with distribution compatible with %s inheritance.", expectedInheritanceMode));

        FilterReport result = instance.makeFilterReport(filterType, settings, sampleData);

        assertThat(result, equalTo(report));
    }

}
