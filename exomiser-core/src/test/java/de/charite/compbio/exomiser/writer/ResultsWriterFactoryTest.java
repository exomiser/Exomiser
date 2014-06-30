/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.util.OutputFormat;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterFactoryTest {
    
    public ResultsWriterFactoryTest() {
    }

    @Test
    public void testGetHtmlResultsWriter() {
        OutputFormat outputFormat = OutputFormat.HTML;
        ResultsWriter result = ResultsWriterFactory.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(OriginalHtmlResultsWriter.class));

    }
    
    @Test
    public void testGetTsvlResultsWriter() {
        OutputFormat outputFormat = OutputFormat.TSV;
        ResultsWriter result = ResultsWriterFactory.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(TsvResultsWriter.class));
    }
    
    @Test
    public void testGetVcfResultsWriter() {
        OutputFormat outputFormat = OutputFormat.VCF;
        ResultsWriter result = ResultsWriterFactory.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(VcfResultsWriter.class));
    }
    
}
