/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import htsjdk.variant.vcf.VCFHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

/**
 * Provides an entry point for getting a ResultsWriter for a specific format.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@Component
public class ResultsWriterFactory {

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Build {@link ResultsWriter} for the given {@link VCFReader} and {@link OutputFormat}.
     * 
     * @param header
     *            from the input file, to base output header upon
     * @param outputFormat
     *            the format to use for the output
     * @return the constructed {@link ResultsWriter} implementation
     */
    public ResultsWriter getResultsWriter(VCFHeader header, OutputFormat outputFormat) {
        switch (outputFormat){
            case HTML:
                return new HtmlResultsWriter(templateEngine);
            case TSV_GENE:
                return new TsvGeneResultsWriter();
            case TSV_VARIANT:
                return new TsvVariantResultsWriter();
            case VCF:
                return new VcfResultsWriter();
            case PHENOGRID:
                return new PhenogridWriter();
            default:
                return new HtmlResultsWriter(templateEngine);
        }
    }   
}
