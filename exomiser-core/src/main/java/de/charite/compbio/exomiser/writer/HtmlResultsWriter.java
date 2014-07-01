/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.TemplateMode;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriter implements ResultsWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(HtmlResultsWriter.class);
    
    private static TemplateEngine templateEngine;

    public HtmlResultsWriter() {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML5);
        templateResolver.setPrefix("html/views/");
        templateResolver.setSuffix(".html");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }


    @Override
    public void write(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList) {
        
        String outFileName = settings.getOutFileName();
        Path outFile = Paths.get(outFileName);
        
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            
            Context context = new Context();
            context.setVariable("filters", filterList);
            writer.write(templateEngine.process("results", context));
            
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("Results written to file {}.", outFileName);

    }
}
