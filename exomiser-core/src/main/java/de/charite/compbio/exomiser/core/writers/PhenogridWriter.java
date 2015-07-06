/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.charite.compbio.exomiser.core.Analysis;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.prioritisers.HiPhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGrid;
import de.charite.compbio.exomiser.core.writers.phenogrid.PhenoGridAdaptor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenogridWriter implements ResultsWriter {

    private static final Logger logger = LoggerFactory.getLogger(PhenogridWriter.class);    
    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.PHENOGRID;
    
    @Override
    public void writeFile(Analysis analysis, OutputSettings settings) {
        String outFileName = ResultsWriterUtils.makeOutputFilename(analysis.getVcfPath(), settings.getOutputPrefix(), OUTPUT_FORMAT);
        Path outFile = Paths.get(outFileName);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {

            writer.write(writeString(analysis, settings));

        } catch (IOException ex) {
            logger.error("Unable to write results to file {}.", outFileName, ex);
        }
        logger.info("{} results written to file {}.", OUTPUT_FORMAT, outFileName);

    }

    @Override
    public String writeString(Analysis analysis, OutputSettings settings) {
        SampleData sampleData = analysis.getSampleData();
        List<Gene> passedGenes = ResultsWriterUtils.getMaxPassedGenes(sampleData.getGenes(), settings.getNumberOfGenesToShow());       
        List<HiPhivePriorityResult> hiPhiveResults = new ArrayList<>();
        for (Gene gene : passedGenes) {
            if (gene.getPriorityResults().containsKey(PriorityType.HI_PHIVE_PRIORITY)) {
               HiPhivePriorityResult hiPhiveResult = (HiPhivePriorityResult) gene.getPriorityResult(PriorityType.HI_PHIVE_PRIORITY);
               hiPhiveResults.add(hiPhiveResult);
            }
        }
        PhenoGridAdaptor phenoGridAdaptor = new PhenoGridAdaptor();
        PhenoGrid phenogrid = phenoGridAdaptor.makePhenoGridFromHiPhiveResults("hiPhive specified phenotypes", hiPhiveResults) ;
        return writePhenoGridAsJson(phenogrid);
    }

    private String writePhenoGridAsJson(PhenoGrid phenogrid) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        try {
            return mapper.writeValueAsString(phenogrid);
        } catch (JsonProcessingException ex) {
            logger.error("Error mapping PhenoGrid to json", ex);
        }
        return "Error mapping PhenoGrid to json";
    }

}
