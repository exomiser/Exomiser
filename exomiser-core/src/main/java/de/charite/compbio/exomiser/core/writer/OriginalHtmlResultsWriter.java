/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.filter.FilterReport;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.io.html.HTMLWriter;
import de.charite.compbio.exomiser.priority.Priority;
import jannovar.exome.VariantTypeCounter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the original HTML generation code. 
 *
 * @deprecated This is now replaced by HtmlResultsWriter
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OriginalHtmlResultsWriter extends HtmlResultsWriter implements ResultsWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(OriginalHtmlResultsWriter.class);

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.HTML;

    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings, List<Priority> priorityList) {
        
        String outFileName = ResultsWriterUtils.determineFileExtension(settings.getOutFileName(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);
        
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            
            HTMLWriter htmlWriter = new HTMLWriter(writer);
            htmlWriter.writeHTMLHeaderAndCSS();
            //write in the settings used
            writer.write("<h2><a name=\"Settings\">Analysis Settings</a></h2>");
            writer.write("<p>Settings used in this analysis:</p>");
            writer.write("<p><pre>");
            ObjectMapper mapper = new ObjectMapper();
            //required for correct output of Path types
            mapper.registerModule(new Jdk7Module());
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
            
            String jsonSettings = mapper.writeValueAsString(settings);
            writer.write(jsonSettings);
            writer.write("</pre></p>");
            
            //add summary of filter results
            List<FilterReport> filterReports = makeFilterReports(settings, sampleData);

            htmlWriter.writeHTMLFilterSummary(filterReports, priorityList);
            VariantTypeCounter vtc = ResultsWriterUtils.makeVariantTypeCounter(sampleData.getVariantEvaluations());
            htmlWriter.writeVariantDistributionTable(vtc, sampleData.getSampleNames());

            htmlWriter.writeHTMLBody(sampleData.getPedigree(), sampleData.getGenes());
            htmlWriter.writeAbout();
            htmlWriter.writeHTMLFooter();
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
        
    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings, List<Priority> priorityList) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
