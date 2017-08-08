/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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
package org.monarchinitiative.exomiser.core.writers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterReport;
import org.monarchinitiative.exomiser.core.filters.PassAllVariantEffectsFilter;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultsWriterUtilsTest {

    private static final String DEFAULT_OUTPUT_DIR = "results";
    private final Path vcfPath = Paths.get("wibble");
    
    @Mock
    Gene passedGeneOne;
    @Mock
    Gene passedGeneTwo;
    @Mock
    Gene failedGene;
    
    @Before
    public void before() {
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
        OutputSettings settings = OutputSettings.builder().build();
        String expResult = String.format("%s/wibble-exomiser-results.%s", DEFAULT_OUTPUT_DIR, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedVcfFileExtensionIsPresent() {
        OutputFormat testedFormat = OutputFormat.VCF;
        OutputSettings settings = OutputSettings.builder().build();
        String expResult = String.format("%s/wibble-exomiser-results.%s", DEFAULT_OUTPUT_DIR, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testThatSpecifiedOutputFormatDoesNotOverwriteGivenOutputPrefixFileExtension() {
        OutputFormat testedFormat = OutputFormat.VCF;
        String outputPrefix = "/user/jules/exomes/analysis/slartibartfast.xml";
        OutputSettings settings = OutputSettings.builder().outputPrefix(outputPrefix).build();
        String expResult = String.format("%s.%s", outputPrefix, testedFormat.getFileExtension());
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testDefaultOutputFormatIsNotDestroyedByIncorrectFileExtensionDetection() {
        OutputFormat testedFormat = OutputFormat.HTML;
        OutputSettings settings = OutputSettings.builder().build();
        String expResult = DEFAULT_OUTPUT_DIR + "/wibble-exomiser-results.html";
        String result = ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), testedFormat);
        assertThat(result, equalTo(expResult));
    }
    
    @Test
    public void testOutFileNameIsCombinationOfOutPrefixAndOutFormat() {
        OutputFormat outFormat = OutputFormat.TSV_GENE;
        String outFilePrefix = "user/subdir/geno/vcf/F0000009/F0000009.vcf";
        OutputSettings settings = OutputSettings.builder().outputPrefix(outFilePrefix).build();
        assertThat(ResultsWriterUtils.makeOutputFilename(vcfPath, settings.getOutputPrefix(), outFormat), equalTo(outFilePrefix + "." + outFormat.getFileExtension()));
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
        Analysis analysis = Analysis.builder().build();
        AnalysisResults analysisResults = AnalysisResults.builder().build();
        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(analysis, analysisResults);
        
        assertThat(results.isEmpty(), is(true));
    }
    
    @Test
    public void canMakeFilterReportsFromAnalysis(){
        Analysis analysis = Analysis.builder()
                .addStep(new PassAllVariantEffectsFilter())
                .build();
        AnalysisResults analysisResults = AnalysisResults.builder().build();
        List<FilterReport> results = ResultsWriterUtils.makeFilterReports(analysis, analysisResults);
        
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
    
}
