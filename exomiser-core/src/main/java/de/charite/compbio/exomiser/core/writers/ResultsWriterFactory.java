/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

/**
 * Provides an entry point for getting a ResultsWriter for a specific format.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterFactory {

    public static ResultsWriter getResultsWriter(OutputFormat outputFormat) {
        switch (outputFormat){
            case HTML:
                return getHtmlResultsWriter();
            case TSV_GENE:
                return getTsvGeneResultsWriter();
            case TSV_VARIANT:
                return getTsvVariantResultsWriter();
            case VCF:
                return getVcfResultsWriter();
            default:
                return getHtmlResultsWriter();
        }
    }
    
    protected static ResultsWriter getHtmlResultsWriter() {
        return new HtmlResultsWriter();
    }

    protected static ResultsWriter getTsvGeneResultsWriter() {
        return new TsvGeneResultsWriter();
    }
    
    protected static ResultsWriter getTsvVariantResultsWriter() {
        return new TsvVariantResultsWriter();
    }

    protected static ResultsWriter getVcfResultsWriter() {
        return new VcfResultsWriter();
    }
    
}
