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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class wraps up the parsing of the resources required for creating the variant
 * frequency data in the frequency table.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class VariantFrequencyResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "VARIANT";

    private static final Logger logger = LoggerFactory.getLogger(VariantFrequencyResourceGroupParser.class);

    Resource dbSnpResource;
    Resource espResource;
    Resource ucscHgResource;

    public VariantFrequencyResourceGroupParser() {
    }
    
    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup.getName());
            return;
        }
        
        /*
         * First parseResource the dnSNP data.
         */
        logger.info("Parsing dbSNP data");
        //this is the Frequency List we're going to populate and the write out to file
        ArrayList<Frequency> frequencyList = new ArrayList<>();
        //provide it to the DbSnpFrequencyParser along with the UCSC data
        DbSnpFrequencyParser dbSnpParser = new DbSnpFrequencyParser(ucscHgResource, inDir, frequencyList);
        dbSnpParser.parseResource(dbSnpResource, inDir, outDir);

        if (frequencyList.isEmpty()) {
            logger.error("DbSnpFrequencyParser returned no Frequency data.");
        }
        // Now parseResource the ESP data using the frequency information generated
        // from the dbSNP and UCSC known gene data.
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

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
        
        dbSnpResource = resourceGroup.getResource(DbSnpFrequencyParser.class);
        if (dbSnpResource == null) {
            logResourceMissing(resourceGroup.getName(), DbSnpFrequencyParser.class);
            return false;
        }
        
        espResource = resourceGroup.getResource(EspFrequencyParser.class);
        if (espResource == null) {
            logResourceMissing(resourceGroup.getName(), EspFrequencyParser.class);
            return false;
        }
        
        ucscHgResource = resourceGroup.getResource("UCSC_HG19");
        if (ucscHgResource == null) {
            logger.error("MISSING RESOURCE for {} data required by {} - check this is defined in resource configuration class", NAME, "UCSC_HG19");
            return false;
        }
        
        return true; 
    }
    
}
