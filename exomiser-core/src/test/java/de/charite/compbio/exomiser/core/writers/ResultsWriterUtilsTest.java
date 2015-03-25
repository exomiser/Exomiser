/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.filters.FilterReport;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.DEFAULT_OUTPUT_DIR;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
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

    SettingsBuilder settingsbuilder;

    @Mock
    Gene passedGeneOne;
    @Mock
    Gene passedGeneTwo;
    @Mock
    Gene failedGene;
    
    @Before
    public void before() {
        settingsbuilder = new ExomiserSettings.SettingsBuilder();
        settingsbuilder.vcfFilePath(Paths.get("wibble"));
        
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
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = String.format("%s/wibble-exomiser-results.%s", DEFAULT_OUTPUT_DIR, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), testedFormat);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedVcfFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.VCF;
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = String.format("%s/wibble-exomiser-results.%s", DEFAULT_OUTPUT_DIR, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), testedFormat);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedOutputFormatOverwritesGivenOutFileExtension() {
        OutputFormat testedFormat = OutputFormat.VCF;
        settingsbuilder.outFileName("/user/jules/exomes/analysis/slartibartfast.xml");
        ExomiserSettings settings = settingsbuilder.build();        
        String expResult = String.format("/user/jules/exomes/analysis/slartibartfast.%s", testedFormat.getFileExtension());
        String result = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), testedFormat);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testDefaultOutputFormatIsNotDestroyedByIncorrectFileExtensionDetection() {
        OutputFormat testedFormat = OutputFormat.HTML;
        settingsbuilder.buildVersion("2.1.0");
        ExomiserSettings settings = settingsbuilder.build();
        String expResult = DEFAULT_OUTPUT_DIR + "/wibble-exomiser-2.1.0-results.html";
        String result = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), testedFormat);
        assertThat(result, equalTo(expResult));
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
    public void canMakeFilterReportsFromSettingsAndVariantEvaluations(){
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().build();
        SampleData sampleData = new SampleData();
        sampleData.setVariantEvaluations(new ArrayList<VariantEvaluation>());
        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(settings, sampleData);
        
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
