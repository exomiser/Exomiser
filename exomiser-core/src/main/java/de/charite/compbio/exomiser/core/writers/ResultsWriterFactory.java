/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

/**
 * Provides an entry point for getting a ResultsWriter for a specific format.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class ResultsWriterFactory {
    
    @Autowired
    private TemplateEngine templateEngine;
    
    public ResultsWriter getResultsWriter(OutputFormat outputFormat) {
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
    
    protected ResultsWriter getHtmlResultsWriter() {
        return new HtmlResultsWriter(templateEngine);
    }

    protected ResultsWriter getTsvGeneResultsWriter() {
        return new TsvGeneResultsWriter();
    }
    
    protected ResultsWriter getTsvVariantResultsWriter() {
        return new TsvVariantResultsWriter();
    }

    protected ResultsWriter getVcfResultsWriter() {
        return new VcfResultsWriter();
    }
    
}
