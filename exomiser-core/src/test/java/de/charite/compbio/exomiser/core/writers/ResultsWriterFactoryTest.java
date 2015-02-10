/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultsWriterFactoryTest {
    
    @InjectMocks
    private ResultsWriterFactory instance;

    @Test
    public void testGetHtmlResultsWriter() {
        OutputFormat outputFormat = OutputFormat.HTML;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(HtmlResultsWriter.class));
    }
    
    @Test
    public void testGetTsvGeneResultsWriter() {
        OutputFormat outputFormat = OutputFormat.TSV_GENE;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(TsvGeneResultsWriter.class));
    }
    
    @Test
    public void testGetTsvVariantResultsWriter() {
        OutputFormat outputFormat = OutputFormat.TSV_VARIANT;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(TsvVariantResultsWriter.class));
    }
    
    @Test
    public void testGetVcfResultsWriter() {
        OutputFormat outputFormat = OutputFormat.VCF;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(VcfResultsWriter.class));
    }
    
}
