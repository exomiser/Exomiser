/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.resources;

import de.charite.compbio.exomiser.io.FileOperationStatus;
import de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser;
import de.charite.compbio.exomiser.parsers.DiseaseInheritanceCache;
import de.charite.compbio.exomiser.parsers.EspFrequencyParser;
import de.charite.compbio.exomiser.parsers.MimToGeneParser;
import de.charite.compbio.exomiser.parsers.MorbidMapParser;
import de.charite.compbio.exomiser.parsers.Parser;
import de.charite.compbio.exomiser.reference.Frequency;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles parsing of classes from the resource objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceParserHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceParserHandler.class.getName());

    public static void parseResources(Iterable<ExternalResource> externalResources, File ucscSerializedData, Path inPath, Path outPath) {

        ExternalResource dbSnpResource = null;
        ExternalResource espResource = null;
        //the ESP and dnSNP files are a special case where they are handled 
        //together to create a single output file. There will probably be more 
        //special cases at some point making this a bit of a mess...
        Map<String, ExternalResource> frequencyResources = new HashMap<>();
        Map<String, ExternalResource>  omimResources = new HashMap<>();
        
        for (ExternalResource resource : externalResources) {
            switch (resource.getParserGroup()) {
                case "frequency":
                    frequencyResources.put(resource.getName(), resource);
                    break;
                case "omim":
                    omimResources.put(resource.getName(), resource);
                    break;
                default:
                    parseResource(resource, inPath, outPath);
            }
        }

        if (dbSnpResource != null && espResource != null) {
            parseVariantFrequencyResources(ucscSerializedData, dbSnpResource, espResource, inPath, outPath);
        } else {
            logger.warn("Not parsing variant frequency resources as one or both resources have not been defined. Check the external-resources file.");
        }
        int requiredOmimResources = 3;
        if (omimResources.size() == requiredOmimResources) {
            parseOmimResources(omimResources, inPath, outPath);
        } else {
            logger.warn("Not parsing omim resources as only {} of {} required resources have been defined. Check the external-resources file.", omimResources.size(), requiredOmimResources);            
        }

    }

    public static void parseResource(ExternalResource externalResource, Path inPath, Path outPath) {
        try {
            logger.info("Parsing file: {} using parser: {}", externalResource.getExtractedFileName(), externalResource.getParser());
            if (externalResource.getParser().isEmpty()) {
                logger.error("No parser defined for resource: {}", externalResource.getExtractedFileName());
                return;
            }
            //this might be a bit too generic really as there are likely as many special cases as generic ones. We'll see...
            Class parserClass = Class.forName(externalResource.getParser());
            Parser parser = (Parser) parserClass.newInstance();
            //now do the actual parsing
            parseResourseFile(parser, externalResource, inPath, outPath);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.error("Error parsing resource {}", externalResource.getName(), ex);
        }
    }

    private static void parseVariantFrequencyResources(File ucscSerializedData, ExternalResource dbSnpResource, ExternalResource espResource, Path inPath, Path outPath) {
        logger.info("Parsing variant frequency files. Writing out to: {}", outPath);

        //doesn't matter
        File outputFile = new File(outPath.toFile(), dbSnpResource.getParsedFileName());

        /* First parse the dnSNP data. */
        logger.info("Parsing dbSNP data");
        //this is the Frequency List we're going to populate and the write out to file
        ArrayList<Frequency> frequencyList = new ArrayList<>();
        //provide it to the DbSnpFrequencyParser along with the UCSC data
        DbSnpFrequencyParser dbSnpParser = new DbSnpFrequencyParser(ucscSerializedData, frequencyList);
        parseResourseFile(dbSnpParser, dbSnpResource, inPath, outPath);

        if (frequencyList.isEmpty()) {
            logger.error("DbSnpFrequencyParser returned no Frequency data.");
        }
        // Now parse the ESP data.
        EspFrequencyParser espParser = new EspFrequencyParser(frequencyList);
        logger.info("Parsing the ESP data");
        parseResourseFile(espParser, espResource, inPath, outPath);
//        /* Remove duplicates */
//        if (frequencyList == null || frequencyList.isEmpty()) {
//            logger.error("Attempt to remove duplicates from null or empty frequencyList");
//        }

        try (FileWriter fwriter = new FileWriter(outputFile);
                BufferedWriter out = new BufferedWriter(fwriter)) {

            for (Frequency f : frequencyList) {
                out.write(f.getDumpLine());
            }
        } catch (IOException e) {
            logger.error("Error writing out frequency files", e);
        }
    }
       
    private static void parseOmimResources(Map<String, ExternalResource> omimResources, Path inPath, Path outPath) {
        logger.info("Parsing omim files. Writing out to: {}", outPath);
        ExternalResource morbidMapResource = omimResources.get("OMIM_morbidmap");
        ExternalResource mim2geneResource = omimResources.get("OMIM_mim2gene");
        ExternalResource hpoPhenotypeAnnotations = omimResources.get("HPO_phenotype_annotations");

        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        //first parse the mim2gene file
        MimToGeneParser mimParser = new MimToGeneParser(mim2geneMap);
        parseResourseFile(mimParser, mim2geneResource, inPath, outPath);
        
        //now parse the morbidmap file 
        File hpoAnnototaionsFile = new File(inPath.toFile(), hpoPhenotypeAnnotations.getExtractedFileName());
        //Need to make the cache for the morbidmap parser
        DiseaseInheritanceCache diseaseInheritanceCache = new DiseaseInheritanceCache(hpoAnnototaionsFile.getAbsolutePath());
        if (!diseaseInheritanceCache.isEmpty()) {
            hpoPhenotypeAnnotations.setParseStatus(FileOperationStatus.SUCCESS);
        }
        //make the MimList which morbid map will populate
        MorbidMapParser morbidParser = new MorbidMapParser(diseaseInheritanceCache, mim2geneMap);
        parseResourseFile(morbidParser, morbidMapResource, inPath, outPath);
    }

    /**
     * Handles the calling of <code>Parser.parse</code> for the parser of an 
     * <code>ExternalResource</code>.
     * 
     * @param parser
     * @param externalResource
     * @param inPath
     * @param outPath
     * @return The status from the <code>Parser.parse</code> 
     */
    private static FileOperationStatus parseResourseFile(Parser parser, ExternalResource externalResource, Path inPath, Path outPath) {
        File inputFile = new File(inPath.toFile(), externalResource.getExtractedFileName());
        File outputFile = new File(outPath.toFile(), externalResource.getParsedFileName());
        FileOperationStatus parseStatus;
        //check the file exists before trying to parse it!
        if (inputFile.exists()) {
            parseStatus = parser.parse(inputFile.getPath(), outputFile.getPath());
            logger.info("{} {}", parseStatus, parser.getClass().getCanonicalName());
        } else {
            parseStatus = FileOperationStatus.FILE_NOT_FOUND;
            logger.error("Did not try to parse file {} as it does not exist.", externalResource.getExtractedFileName());
        }
        //remember to set the status for later
        externalResource.setParseStatus(parseStatus);
        return parseStatus;
    }

}
