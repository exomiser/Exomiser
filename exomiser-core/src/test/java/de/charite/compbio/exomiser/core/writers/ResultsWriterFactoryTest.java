/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterFactoryTest {

    private ResultsWriterFactory instance;

    public ResultsWriterFactoryTest() {
    }

    @Before
    public void setUp() {
        instance = new ResultsWriterFactory();
    }

    @Test
    public void testGetHtmlResultsWriter() {
        OutputFormat outputFormat = OutputFormat.HTML;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(HtmlResultsWriter.class));

    }

    @Test
    public void testGetTsvGeneResults() {
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

    @Test
    public void testGetPhenoGridResultsWriter() {
        OutputFormat outputFormat = OutputFormat.PHENOGRID;
        ResultsWriter result = instance.getResultsWriter(outputFormat);
        assertThat(result, instanceOf(PhenogridWriter.class));
    }

}
