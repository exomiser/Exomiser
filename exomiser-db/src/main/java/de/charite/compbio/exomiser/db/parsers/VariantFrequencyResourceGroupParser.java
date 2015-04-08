/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.parsers;

import de.charite.compbio.exomiser.db.reference.Frequency;
import de.charite.compbio.exomiser.db.resources.Resource;
import de.charite.compbio.exomiser.db.resources.ResourceGroup;
import de.charite.compbio.exomiser.db.resources.ResourceOperationStatus;
import de.charite.compbio.jannovar.io.JannovarData;
import de.charite.compbio.jannovar.io.JannovarDataSerializer;
import de.charite.compbio.jannovar.io.ReferenceDictionary;
import de.charite.compbio.jannovar.io.SerializationException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class wraps up the parsing of the resources required for creating the variant
 * frequency data in the frequency table.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class VariantFrequencyResourceGroupParser extends AbstractResourceGroupParser implements ResourceGroupParser {

    public static final String NAME = "FREQUENCY";

    private static final Logger logger = LoggerFactory.getLogger(VariantFrequencyResourceGroupParser.class);

    Resource dbSnpResource;
    Resource espResource;
    Resource jannovarResource;
    Resource exacResource;
    
    @Override
    public void parseResources(ResourceGroup resourceGroup, Path inDir, Path outDir) {

        logger.info("Parsing {} resources. Writing out to: {}", resourceGroup.getName(), outDir);

        //Check everything is present before trying to parse them
        if (!requiredResourcesPresent(resourceGroup)) {
            logger.error("Not parsing {} ResourceGroup resources as not all required resources are present.", resourceGroup.getName());
            return;
        }
        JannovarData jannovarData = extractKnownGenesFromJannovarResource(inDir);
        /*
         * First parseResource the dnSNP data.
         */
        logger.info("Parsing dbSNP data");
        //this is the Frequency List we're going to populate and the write out to file
        ArrayList<Frequency> frequencyList = new ArrayList<>();
        //provide it to the DbSnpFrequencyParser along with the UCSC data
        DbSnpFrequencyParser dbSnpParser = new DbSnpFrequencyParser(jannovarData.refDict, jannovarData, inDir, frequencyList);
        dbSnpParser.parseResource(dbSnpResource, inDir, outDir);

        if (frequencyList.isEmpty()) {
            logger.error("DbSnpFrequencyParser returned no Frequency data.");
        }
        
        // Now parseResource the ExAC data using the frequency information generated
        // from the dbSNP and UCSC known gene data.
        ExACFrequencyParser exacParser = new ExACFrequencyParser(jannovarData.refDict, frequencyList);
        logger.info("Parsing the ExAC data");
        exacParser.parseResource(exacResource, inDir, outDir);
        
        // Now parseResource the ESP data using the frequency information generated
        // from the dbSNP and UCSC known gene data.
        EspFrequencyParser espParser = new EspFrequencyParser(jannovarData.refDict, frequencyList);
        logger.info("Parsing the ESP data");
        espParser.parseResource(espResource, inDir, outDir);


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

    private JannovarData extractKnownGenesFromJannovarResource(Path inDir) throws RuntimeException {
        JannovarData jannovarData = null;
        Path jannovarSerialisedDataFile = inDir.resolve(jannovarResource.getExtractedFileName());
        try {
            jannovarData = new JannovarDataSerializer(jannovarSerialisedDataFile.toString()).load();
            jannovarResource.setExtractStatus(ResourceOperationStatus.SUCCESS);
            jannovarResource.setParseStatus(ResourceOperationStatus.SUCCESS);
        } catch (SerializationException e) {
            jannovarResource.setExtractStatus(ResourceOperationStatus.FAILURE);
            jannovarResource.setParseStatus(ResourceOperationStatus.FAILURE);
            throw new RuntimeException("Could not load Jannovar data from " + jannovarSerialisedDataFile, e);
        }
        return jannovarData;
    }

    @Override
    public boolean requiredResourcesPresent(ResourceGroup resourceGroup) {
        
        dbSnpResource = resourceGroup.getResource(DbSnpFrequencyParser.class);
        if (dbSnpResource == null) {
            logResourceMissing(resourceGroup.getName(), DbSnpFrequencyParser.class);
            return false;
        }
        
        exacResource = resourceGroup.getResource(ExACFrequencyParser.class);
        if (exacResource == null) {
            logResourceMissing(resourceGroup.getName(), ExACFrequencyParser.class);
            return false;
        }
        
        espResource = resourceGroup.getResource(EspFrequencyParser.class);
        if (espResource == null) {
            logResourceMissing(resourceGroup.getName(), EspFrequencyParser.class);
            return false;
        }
        
        jannovarResource = resourceGroup.getResource("UCSC_HG19");
        if (jannovarResource == null) {
            logger.error("MISSING RESOURCE for {} data required by {} - check this is defined in resource configuration class", NAME, "UCSC_HG19");
            return false;
        }
        
        return true; 
    }
    
}
