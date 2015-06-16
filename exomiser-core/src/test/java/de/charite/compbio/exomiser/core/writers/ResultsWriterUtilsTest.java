/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.Analysis;
import de.charite.compbio.exomiser.core.filters.FilterReport;
import static de.charite.compbio.exomiser.core.ExomiserSettings.DEFAULT_OUTPUT_DIR;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.filters.TargetFilter;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultsWriterUtilsTest {

    SettingsBuilder settingsBuilder;

    @Mock
    Gene passedGeneOne;
    @Mock
    Gene passedGeneTwo;
    @Mock
    Gene failedGene;
    
    @Before
    public void before() {
        settingsBuilder = new SettingsBuilder();
        settingsBuilder.vcfFilePath(Paths.get("wibble"));
        
        Mockito.when(passedGeneOne.passedFilters()).thenReturn(Boolean.TRUE);
        Mockito.when(passedGeneTwo.passedFilters()).thenReturn(Boolean.TRUE);
        Mockito.when(failedGene.passedFilters()).thenReturn(Boolean.FALSE);
    }
    
    private List<Gene> getGenes() {
        List<Gene> genes = new ArrayList<>();
        genes.add(passedGeneOne);
        genes.add(passedGeneTwo);
        genes.add(failedGene);
        return genes;
    }

    @Test
    public void testThatSpecifiedTsvFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.TSV_GENE;
        OutputSettings settings = settingsBuilder.build();
        String expResult = String.format("%s/wibble-exomiser-results.%s", DEFAULT_OUTPUT_DIR, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.makeOutputFilename(settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedVcfFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.VCF;
        OutputSettings settings = settingsBuilder.build();
        String expResult = String.format("%s/wibble-exomiser-results.%s", DEFAULT_OUTPUT_DIR, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.makeOutputFilename(settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedOutputFormatDoesNotOverwriteGivenOutputPrefixFileExtension() {
        OutputFormat testedFormat = OutputFormat.VCF;
        String outputPrefix = "/user/jules/exomes/analysis/slartibartfast.xml";
        settingsBuilder.outputPrefix(outputPrefix);
        OutputSettings settings = settingsBuilder.build();        
        String expResult = String.format("%s.%s", outputPrefix, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.makeOutputFilename(settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testDefaultOutputFormatIsNotDestroyedByIncorrectFileExtensionDetection() {
        OutputFormat testedFormat = OutputFormat.HTML;
        settingsBuilder.buildVersion("2.1.0");
        OutputSettings settings = settingsBuilder.build();
        String expResult = DEFAULT_OUTPUT_DIR + "/wibble-exomiser-2.1.0-results.html";
        String result = ResultsWriterUtils.makeOutputFilename(settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testOutFileNameIsCombinationOfOutPrefixAndOutFormat() {
        OutputFormat outFormat = OutputFormat.TSV_GENE;
        String outFilePrefix = "user/subdir/geno/vcf/F0000009/F0000009.vcf";
        settingsBuilder.outputPrefix(outFilePrefix);
        OutputSettings settings = settingsBuilder.build();
        assertThat(ResultsWriterUtils.makeOutputFilename(settings.getOutputPrefix(), outFormat), equalTo(outFilePrefix + "." + outFormat.getFileExtension()));
    }
    
    @Test
    public void canMakeEmptyVariantTypeCounterFromEmptyVariantEvaluations() {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>();
        List<VariantEffectCount> variantTypeCounters = ResultsWriterUtils.makeVariantEffectCounters(variantEvaluations);
        assertThat(variantTypeCounters.isEmpty(), is(false));
        
        VariantEffectCount firstVariantTypeCount = variantTypeCounters.get(0);
        assertThat(firstVariantTypeCount.getVariantType(), notNullValue());
        assertThat(firstVariantTypeCount.getSampleVariantTypeCounts().isEmpty(), is(true));
    }
    
    @Test
    public void canMakeFilterReportsFromAnalysis_returnsEmptyListWhenNoFiltersAdded(){
        SampleData sampleData = new SampleData();
        sampleData.setVariantEvaluations(new ArrayList<VariantEvaluation>());
        Analysis analysis = new Analysis(sampleData);
        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(analysis);
        
        assertThat(results.isEmpty(), is(true));
    }
    
    @Test
    public void canMakeFilterReportsFromAnalysis(){
        SampleData sampleData = new SampleData();
        sampleData.setVariantEvaluations(new ArrayList<VariantEvaluation>());
        Analysis analysis = new Analysis(sampleData);
        analysis.addStep(new TargetFilter());
        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(analysis);
        
        for (FilterReport result : results) {
            System.out.println(result);
        }
        
        assertThat(results.isEmpty(), is(false));
    }

    @Test
    public void testMaxPassedGenesWhereMaxGenesIsZero() {
        List<Gene> allPassedGenes = new ArrayList<>();
        allPassedGenes.add(passedGeneOne);
        allPassedGenes.add(passedGeneTwo);
        assertThat(ResultsWriterUtils.getMaxPassedGenes(getGenes(), 0), equalTo(allPassedGenes));
    } 
    
    @Test
    public void testMaxPassedGenesWhereMaxGenesIsOne() {
        List<Gene> onePassed = new ArrayList<>();
        onePassed.add(passedGeneOne);
        assertThat(ResultsWriterUtils.getMaxPassedGenes(getGenes(), 1), equalTo(onePassed));
    } 
    @Test
    public void testMaxPassedGenesWhereMaxGenesIsGreaterThanInputSize() {
        List<Gene> allPassedGenes = new ArrayList<>();
        allPassedGenes.add(passedGeneOne);
        allPassedGenes.add(passedGeneTwo);
        assertThat(ResultsWriterUtils.getMaxPassedGenes(getGenes(), 100), equalTo(allPassedGenes));
    }
    
    @Test
    public void testPassedGenesReturnsAllPassedGenes() {
        List<Gene> allPassedGenes = new ArrayList<>();
        allPassedGenes.add(passedGeneOne);
        allPassedGenes.add(passedGeneTwo);
        assertThat(ResultsWriterUtils.getPassedGenes(getGenes()), equalTo(allPassedGenes));
    }
    
}
