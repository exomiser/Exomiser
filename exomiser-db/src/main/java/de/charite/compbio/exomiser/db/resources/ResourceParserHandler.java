/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.db.resources;

import de.charite.compbio.exomiser.db.parsers.MetaDataParser;
import de.charite.compbio.exomiser.db.parsers.ResourceGroupParser;
import de.charite.compbio.exomiser.db.parsers.ResourceParser;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles parsing of classes from the resource objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceParserHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceParserHandler.class.getName());

    public static void parseResources(Iterable<Resource> externalResources, Path inDir, Path outDir) {

        //there are a lot of resources which need parsing together as a group
        //...like the ESP and dnSNP files
        //...and the OMIM files
        //...and the STRING DB files
        Map<Class, ResourceGroup> resourceGroupMap = new HashMap<>();
        //...and the MetaData 'file' this is really a special case
        Resource metaDataResource = null;
        
        logger.info("Parsing resources:");
        for (Resource resource : externalResources) {
            //we're using the resourseGroupParserClass as the key here as this is less likely to
            //be changed in an ambiguous way
            Class<? extends ResourceGroupParser> resourceGroupParserClass = resource.getResourceGroupParserClass();
            //the metadata needs 'parsing' after all the other resources as they
            //might have had their fileVersion set by their parsers 
            if (MetaDataParser.class.equals(resource.getParserClass())) {
                metaDataResource = resource;
                continue;
            }
            //resource is not parsed as part of a group so parse it now
            if (resourceGroupParserClass == null){
                logger.info("Resource {} has no declared resourceGroupParserClass. Attempting to parse as a single resource.", resource.getName());
                parseResource(resource, inDir, outDir);
            } else {
                logger.info("Resource {} is part of resourceGroup {} - this will be parsed by {}", resource.getName(), resource.getResourceGroupName(), resourceGroupParserClass);
                //resource is part of a group - add this to a ResourceGroup
                if (!resourceGroupMap.containsKey(resourceGroupParserClass)) {
                    ResourceGroup resourceGroup = new ResourceGroup(resource.getResourceGroupName(), resourceGroupParserClass);
                    resourceGroup.addResource(resource);
                    resourceGroupMap.put(resourceGroupParserClass, resourceGroup);
                } else {
                    resourceGroupMap.get(resourceGroupParserClass).addResource(resource);
                }
            }
        }
        
        //parse the ResourceGroups
        logger.info("Parsing resourceGroups:");
        for (ResourceGroup resourceGroup : resourceGroupMap.values()) {
            parseResourceGroup(resourceGroup, inDir, outDir);
        }
        //do the metadata
        logger.info("Parsing metadata:");
        if (metaDataResource != null) {
            parseMetaData(metaDataResource, externalResources, outDir);
        }
        
        //and we're done!
        logger.info("Done parsing.");
    }

    public static void parseResource(Resource resource, Path inDir, Path outDir) {
        try {
            logger.info("Parsing resource: {} file: {} using parser: {}", resource.getName(), resource.getExtractedFileName(), resource.getParserClass());
            if (resource.getParserClass() == null) {
                logger.error("No parser defined for resource: {}", resource.getExtractedFileName());
                resource.setParseStatus(ResourceOperationStatus.PARSER_NOT_FOUND);
                return;
            }
            //this might be a bit too generic really as there are likely as many special cases as generic ones. We'll see...
            Class<? extends ResourceParser> resourceParserClass = resource.getParserClass();
            ResourceParser resourceParser = resourceParserClass.newInstance();
            //now do the actual parsing
            resourceParser.parseResource(resource, inDir, outDir);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error("Error parsing resource {}", resource.getName(), ex);
            resource.setParseStatus(ResourceOperationStatus.FAILURE);
        }
    }

    public static void parseResourceGroup(ResourceGroup resourceGroup, Path inDir, Path outDir) {
        try {
            logger.info("Parsing resourceGroup: {} with parser: {}", resourceGroup.getName(), resourceGroup.getParserClass());
            if (resourceGroup.getParserClass() == null) {
                logger.error("No parser defined for resourceGroup: {}", resourceGroup.getName());
            }
            //We ought to be doing this with a more type-safe configuration. Like in config.ResourceConfig for instance 
            Class<? extends ResourceGroupParser> resourceGroupParserClass = resourceGroup.getParserClass();
            ResourceGroupParser resourceGroupParser = resourceGroupParserClass.newInstance();
            //now do the actual parsing
            resourceGroupParser.parseResources(resourceGroup, inDir, outDir);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error("Error parsing resource {}", resourceGroup.getName(), ex);
        }
    }
    
    /**
     * Parses out the file version info from the supplied ExternalResources and
     * dumps them out to a dump file.
     *
     * @param externalResources
     * @param outPath
     */
    private static void parseMetaData(Resource metaDataResource, Iterable<Resource> externalResources, Path outPath) {

        logger.info("Handling resource: {}", metaDataResource.getName());
                
        ResourceParser metaDataParser = new MetaDataParser(metaDataResource, externalResources);
        metaDataParser.parseResource(metaDataResource, null, outPath);
        logger.info("{} {}", metaDataResource.getStatus(), metaDataParser.getClass().getCanonicalName());
        
    }
}
