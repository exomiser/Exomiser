/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import htsjdk.variant.vcf.VCFHeader;
import de.charite.compbio.exomiser.core.writers.ResultsWriter;
import de.charite.compbio.exomiser.core.writers.ResultsWriterFactory;
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.core.writers.TsvGeneResultsWriter;
import de.charite.compbio.exomiser.core.writers.HtmlResultsWriter;
import de.charite.compbio.exomiser.core.writers.VcfResultsWriter;
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
        VCFHeader header = null;
        OutputFormat outputFormat = OutputFormat.HTML;
        ResultsWriter result = ResultsWriterFactory.getResultsWriter(header, outputFormat);
        assertThat(result, instanceOf(HtmlResultsWriter.class));

    }
    
    @Test
    public void testGetTsvlResultsWriter() {
        VCFHeader header = null;
        OutputFormat outputFormat = OutputFormat.TSV_GENE;
        ResultsWriter result = ResultsWriterFactory.getResultsWriter(header, outputFormat);
        assertThat(result, instanceOf(TsvGeneResultsWriter.class));
    }
    
    @Test
    public void testGetVcfResultsWriter() {
        VCFHeader header = null;
        OutputFormat outputFormat = OutputFormat.VCF;
        ResultsWriter result = ResultsWriterFactory.getResultsWriter(header, outputFormat);
        assertThat(result, instanceOf(VcfResultsWriter.class));
    }
    
}
