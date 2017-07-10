/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.parsers;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.exomiser.db.reference.Frequency;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceGroup;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

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

    //TODO: Wouldn't this be easier using a proper DI framework???  
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
        
        //doesn't matter which resource we choose the parsed file name from as they 
        //should all the the same
        Path outputFile = outDir.resolve(dbSnpResource.getParsedFileName());
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile, Charset.defaultCharset())) {
            // now do one chromosome at a time and then write out results as otherwise was taking ~20G
            byte[] chromosomes = new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};    
            for (byte chromosome : chromosomes) {
                /*
                 * First parseResource the dnSNP data.
                 */
                logger.info("Parsing chromosome {} dbSNP data", chromosome);
                //this is the Frequency List we're going to populate and the write out to file
                ArrayList<Frequency> frequencyList = new ArrayList<>();
                //provide it to the DbSnpFrequencyParser along with the UCSC data
                DbSnpFrequencyParser dbSnpParser = new DbSnpFrequencyParser(jannovarData, inDir, frequencyList, chromosome);
                dbSnpParser.parseResource(dbSnpResource, inDir, outDir);

                if (frequencyList.isEmpty()) {
                    logger.error("DbSnpFrequencyParser returned no Frequency data.");
                }

                // Now parseResource the ExAC data using the frequency information generated
                // from the dbSNP and UCSC known gene data.
                ExACFrequencyParser exacParser = new ExACFrequencyParser(jannovarData.getRefDict(), frequencyList, chromosome);
                logger.info("Parsing chromosome {} ExAC data", chromosome);
                exacParser.parseResource(exacResource, inDir, outDir);

                // Now parseResource the ESP data using the frequency information generated
                // from the dbSNP and UCSC known gene data.
                EspFrequencyParser espParser = new EspFrequencyParser(jannovarData.getRefDict(), frequencyList, chromosome);
                logger.info("Parsing chromosome {} ESP data", chromosome);
                espParser.parseResource(espResource, inDir, outDir);
                
                for (Frequency f : frequencyList) {
                    writer.write(f.getDumpLine());
                }
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
