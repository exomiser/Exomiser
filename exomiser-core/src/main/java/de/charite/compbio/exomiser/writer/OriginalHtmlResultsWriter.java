/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.filter.TargetFilter;
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
 * @author jj8
 */
public class OriginalHtmlResultsWriter implements ResultsWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(OriginalHtmlResultsWriter.class);

    @Override
    public void writeFile(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList) {
        
        String outFileName = ResultsWriterUtils.determineFileExtension(settings);
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
            htmlWriter.writeHTMLFilterSummary(filterList, priorityList);
            VariantTypeCounter vtc = getVariantTypeCounter(filterList, sampleData.getVariantEvaluations());
            htmlWriter.writeVariantDistributionTable(vtc, sampleData.getSampleNames());
            logger.info("Writing HTML body with {} gene results", sampleData.getGeneList().size());
            htmlWriter.writeHTMLBody(sampleData.getPedigree(), sampleData.getGeneList());
            htmlWriter.writeAbout();
            htmlWriter.writeHTMLFooter();
        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("Results written to file {}.", outFileName);
        
    }
    
    /**
     * We are able to initilialize a VariantTypeCounter object either with a
     * list of Variant objects or to extract one from the TargetFilter object.
     * We use this object to print out a table of variant class distribution.
     * @param filterList
     * @param variantList
     * @return 
     */
    protected VariantTypeCounter getVariantTypeCounter(List<Filter> filterList, List<VariantEvaluation> variantList) {
        VariantTypeCounter vtc = null;
        for (Filter f : filterList) {
            if (f.getFilterType() == FilterType.TARGET_FILTER) {
                TargetFilter tf = (TargetFilter) f;
                vtc = tf.getVariantTypeCounter();
                break;
            }
        }
        if (vtc == null) {
            TargetFilter tf = new TargetFilter();
            tf.filterVariants(variantList);
            vtc = tf.getVariantTypeCounter();
        }
        return vtc;
    }

    @Override
    public String writeString(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
