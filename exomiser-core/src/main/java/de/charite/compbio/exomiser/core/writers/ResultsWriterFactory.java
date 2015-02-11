/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import htsjdk.variant.vcf.VCFHeader;

/**
 * Provides an entry point for getting a ResultsWriter for a specific format.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class ResultsWriterFactory {

    /**
     * Build {@link ResultsWriter} for the given {@link VCFReader} and {@link OutputFormat}.
     * 
     * @param header
     *            from the input file, to base output header upon
     * @param outputFormat
     *            the format to use for the output
     * @return the constructed {@link ResultsWriter} implementation
     */
    public static ResultsWriter getResultsWriter(VCFHeader header, OutputFormat outputFormat) {
        switch (outputFormat){
            case HTML:
                return getHtmlResultsWriter();
            case TSV_GENE:
                return getTsvGeneResultsWriter();
            case TSV_VARIANT:
                return getTsvVariantResultsWriter();
            case VCF:
                return getVcfResultsWriter(header);
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

    protected static ResultsWriter getVcfResultsWriter(VCFHeader header) {
        return new VcfResultsWriter(header);
    }
    
}
