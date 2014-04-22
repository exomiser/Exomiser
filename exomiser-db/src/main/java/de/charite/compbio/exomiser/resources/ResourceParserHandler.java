/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.resources;

import de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser;
import de.charite.compbio.exomiser.parsers.DiseaseInheritanceCache;
import de.charite.compbio.exomiser.parsers.EntrezParser;
import de.charite.compbio.exomiser.parsers.EspFrequencyParser;
import de.charite.compbio.exomiser.parsers.MetaDataParser;
import de.charite.compbio.exomiser.parsers.MimToGeneParser;
import de.charite.compbio.exomiser.parsers.MorbidMapParser;
import de.charite.compbio.exomiser.parsers.Parser;
import de.charite.compbio.exomiser.parsers.StringParser;
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

    public static void parseResources(Iterable<ExternalResource> externalResources, Path inPath, Path outPath) {

        //the ESP and dnSNP files are a special case where they are handled 
        //together to create a single output file. There will probably be more 
        //special cases at some point making this a bit of a mess...
        Map<String, ExternalResource> frequencyResources = new HashMap<>();
        //...like the OMIM files for instance.
        Map<String, ExternalResource> omimResources = new HashMap<>();
        //...and the STRING DB files
        Map<String, ExternalResource> stringResources = new HashMap<>();
        //...and the MetaData 'file'
        ExternalResource metaDataResource = null;

        //these will be re-factored so that the resource file contains this information
        final String frequencyGroupName = "frequency";
        final String omimGroupName = "omim";
        final String stringGroupName = "string";
        final String metaDataGroupName = "metadata";

        for (ExternalResource resource : externalResources) {
            switch (resource.getParserGroup()) {
                case frequencyGroupName:
                    frequencyResources.put(resource.getName(), resource);
                    break;
                case omimGroupName:
                    omimResources.put(resource.getName(), resource);
                    break;
                case stringGroupName:
                    stringResources.put(resource.getName(), resource);
                    break;
                case metaDataGroupName:
                    //this is handled after all the others as the parsers could 
                    //set the file version in the ExternalResource 
                    metaDataResource = resource;
                    break;
                default:
                    parseResource(resource, inPath, outPath);
            }
        }
        int requiredNumFrequencyResources = 3;
        if (resourceParserGroupIsComplete(frequencyGroupName, requiredNumFrequencyResources, frequencyResources)) {
            parseVariantFrequencyResources(frequencyResources, inPath, outPath);
        }

        int requiredNumOmimResources = 3;
        if (resourceParserGroupIsComplete(omimGroupName, requiredNumOmimResources, omimResources)) {
            parseOmimResources(omimResources, inPath, outPath);
        }

        int requiredNumStringResources = 2;
        if (resourceParserGroupIsComplete(stringGroupName, requiredNumStringResources, stringResources)) {
            parseStringResources(stringResources, inPath, outPath);
        }
        
        if (metaDataResource != null) {
            parseMetaData(metaDataResource, externalResources, outPath);
        }
    }

    public static ResourceOperationStatus parseResource(ExternalResource externalResource, Path inPath, Path outPath) {
        try {
            logger.info("Parsing file: {} using parser: {}", externalResource.getExtractedFileName(), externalResource.getParser());
            if (externalResource.getParser() == null || externalResource.getParser().isEmpty()) {
                logger.error("No parser defined for resource: {}", externalResource.getExtractedFileName());
                externalResource.setParseStatus(ResourceOperationStatus.PARSER_NOT_FOUND);
                return ResourceOperationStatus.PARSER_NOT_FOUND;
            }
            //this might be a bit too generic really as there are likely as many special cases as generic ones. We'll see...
            Class parserClass = Class.forName(externalResource.getParser());
            Parser parser = (Parser) parserClass.newInstance();
            //now do the actual parsing
            return parseResourseFile(parser, externalResource, inPath, outPath);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            logger.error("Error parsing resource {}", externalResource.getName(), ex);
            externalResource.setParseStatus(ResourceOperationStatus.FAILURE);
        }
        return ResourceOperationStatus.FAILURE;
    }

    private static void parseVariantFrequencyResources(Map<String, ExternalResource> frequencyResources, Path inPath, Path outPath) {
        logger.info("Parsing variant frequency files. Writing out to: {}", outPath);

        ExternalResource dbSnpResource = frequencyResources.get("dbSNP");
        ExternalResource espResource = frequencyResources.get("ESP");
        ExternalResource ucscResource = frequencyResources.get("UCSC_HG19");

        //first we need to prepare the serialized ucsc19 data file using Jannovar
        //this is required for parsing the dbSNP data where it is used as a filter to 
        // remove variants outside of exonic regions.
        File ucscSerializedData = new File(inPath.toFile(), ucscResource.getExtractedFileName());
        if (!ucscSerializedData.exists()) {
            logger.warn("UCSC serialized data file is not present in the process path. Please add it here: {}", ucscSerializedData.getPath());
            //no useable API for Jannovar so we have to add it manually 
        }

        //doesn't matter
        File outputFile = new File(outPath.toFile(), dbSnpResource.getParsedFileName());

        /*
         * First parse the dnSNP data.
         */
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
        File hpoAnnotationsFile = new File(inPath.toFile(), hpoPhenotypeAnnotations.getExtractedFileName());
        //Need to make the cache for the morbidmap parser
        DiseaseInheritanceCache diseaseInheritanceCache = new DiseaseInheritanceCache(hpoAnnotationsFile.getAbsolutePath());
        if (!diseaseInheritanceCache.isEmpty()) {
            hpoPhenotypeAnnotations.setParseStatus(ResourceOperationStatus.SUCCESS);
        }
        //make the MimList which morbid map will populate
        MorbidMapParser morbidParser = new MorbidMapParser(diseaseInheritanceCache, mim2geneMap);
        parseResourseFile(morbidParser, morbidMapResource, inPath, outPath);
    }

    private static void parseStringResources(Map<String, ExternalResource> stringResources, Path inPath, Path outPath) {
        logger.info("Parsing omim files. Writing out to: {}", outPath);
        ExternalResource entrezResource = stringResources.get("String_entrez2sym");
        ExternalResource stringResource = stringResources.get("String_protein_links");

        HashMap<String, List<Integer>> ensembl2EntrezGene = new HashMap<>();
        //first parse the entrez gene to symbol and ensembl peptide biomart file
        EntrezParser entrezParser = new EntrezParser(ensembl2EntrezGene);
        parseResourseFile(entrezParser, entrezResource, inPath, outPath);

        //now parse the STRING DB file
        StringParser stringParser = new StringParser(ensembl2EntrezGene);
        parseResourseFile(stringParser, stringResource, inPath, outPath);
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
    private static ResourceOperationStatus parseResourseFile(Parser parser, ExternalResource externalResource, Path inPath, Path outPath) {
        File inputFile = new File(inPath.toFile(), externalResource.getExtractedFileName());
        File outputFile = new File(outPath.toFile(), externalResource.getParsedFileName());
        ResourceOperationStatus parseStatus;
        //check the file exists before trying to parse it!
        if (inputFile.exists()) {
            parseStatus = parser.parse(inputFile.getPath(), outputFile.getPath());
            logger.info("{} {}", parseStatus, parser.getClass().getCanonicalName());
        } else {
            parseStatus = ResourceOperationStatus.FILE_NOT_FOUND;
            logger.error("Did not try to parse file {} as it does not exist.", externalResource.getExtractedFileName());
        }
        //remember to set the status for later
        externalResource.setParseStatus(parseStatus);
        return parseStatus;
    }

    /**
     * Checks that the expected number of resources for a particular parser
     * group have been provided. This is hard-coded in the
     * <code>parseResources</code> method, but serves to verify everything has
     * been correctly defined before continuing otherwise the resources will be
     * skipped.
     *
     * @param groupName
     * @param requiredResourcesMapSize
     * @param resourcesMap
     * @return
     */
    private static boolean resourceParserGroupIsComplete(String groupName, int requiredResourcesMapSize, Map<String, ExternalResource> resourcesMap) {
        if (resourcesMap.size() == requiredResourcesMapSize) {
            return true;
        } else {
            logger.warn("Not parsing '{}' parserGroup group as only {} of the {} required resources following resources have been defined:", groupName, resourcesMap.size(), requiredResourcesMapSize);
            int resourceCounter = 1;
            for (ExternalResource resource : resourcesMap.values()) {
                logger.warn("{} - {}", resourceCounter, resource);
                resourceCounter++;
            }
            logger.warn("Check the external-resources file for resources with parserGroup '{}'", groupName);
        }
        return false;
    }

    /**
     * Parses out the file version info from the supplied ExternalResources and
     * dumps them out to a dump file.
     *
     * @param externalResources
     * @param outPath
     */
    private static ResourceOperationStatus parseMetaData(ExternalResource metaDataResource, Iterable<ExternalResource> externalResources, Path outPath) {

        logger.info("Handling resource: {}", metaDataResource.getName());
                
        File outputFile = new File(outPath.toFile(), metaDataResource.getParsedFileName());
        ResourceOperationStatus parseStatus;

        Parser metaDataParser = new MetaDataParser(metaDataResource, externalResources);
        parseStatus = metaDataParser.parse(null, outputFile.getPath());
        logger.info("{} {}", parseStatus, metaDataParser.getClass().getCanonicalName());

        //remember to set the status for later
        metaDataResource.setParseStatus(parseStatus);
        return parseStatus;

    }
}
