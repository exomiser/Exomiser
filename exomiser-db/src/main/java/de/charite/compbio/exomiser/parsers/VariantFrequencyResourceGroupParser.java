/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.reference.Frequency;
import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceGroup;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps up the parsing of the resources required for creating the variant
 * frequency data in the frequency table.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFrequencyResourceGroupParser implements ResourceGroupParser {

    private static final Logger logger = LoggerFactory.getLogger(VariantFrequencyResourceGroupParser.class);

    Resource dbSnpResource;
    Resource espResource;
    Resource ucscResource;
        
    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        dbSnpResource = resourceGroup.getResource(DbSnpFrequencyParser.class);
        espResource = resourceGroup.getResource(EspFrequencyParser.class);
        ucscResource = resourceGroup.getResource("UCSC_HG19");

        //first we need to prepare the serialized ucsc19 data file using Jannovar
        //this is required for parsing the dbSNP data where it is used as a filter to 
        // remove variants outside of exonic regions.
        Path ucscSerializedData = inDir.resolve(ucscResource.getExtractedFileName());
        if (!ucscSerializedData.isAbsolute()) {
            logger.warn("UCSC serialized data file is not present in the process path. Please add it here: {}", ucscSerializedData);
            //no useable API for Jannovar so we have to add it manually 
        }

        /*
         * First parseResource the dnSNP data.
         */
        logger.info("Parsing dbSNP data");
        //this is the Frequency List we're going to populate and the write out to file
        ArrayList<Frequency> frequencyList = new ArrayList<>();
        //provide it to the DbSnpFrequencyParser along with the UCSC data
        DbSnpFrequencyParser dbSnpParser = new DbSnpFrequencyParser(ucscSerializedData, frequencyList);
        dbSnpParser.parseResource(dbSnpResource, inDir, outDir);

        if (frequencyList.isEmpty()) {
            logger.error("DbSnpFrequencyParser returned no Frequency data.");
        }
        // Now parseResource the ESP data.
        EspFrequencyParser espParser = new EspFrequencyParser(frequencyList);
        logger.info("Parsing the ESP data");
        espParser.parseResource(espResource, inDir, outDir);
//        /* Remove duplicates */
//        if (frequencyList == null || frequencyList.isEmpty()) {
//            logger.error("Attempt to remove duplicates from null or empty frequencyList");
//        }

        //doesn't matter which resource we choose the parsed file name from as they 
        //should all the the same
        Path outputFile = outDir.resolve(dbSnpResource.getParsedFileName());
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, Charset.defaultCharset())) {

            for (Frequency f : frequencyList) {
                writer.write(f.getDumpLine());
            }
        } catch (IOException e) {
            logger.error("Error writing out frequency files", e);
        }
    }
    
}
