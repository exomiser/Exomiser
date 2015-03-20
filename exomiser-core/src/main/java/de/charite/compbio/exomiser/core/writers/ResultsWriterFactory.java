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
