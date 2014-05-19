/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.Resource;
import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a pipe delimited file of resource|version
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MetaDataParser implements ResourceParser {
    
    private static final Logger logger = LoggerFactory.getLogger(MetaDataParser.class);
    
    private final Iterable<Resource> externalResources;
    private final Resource metaDataResource;
    
    public MetaDataParser (Resource metaDataResource, Iterable<Resource> externalResources) {
        this.metaDataResource = metaDataResource;
        this.externalResources = externalResources;
    }

    @Override
    public void parseResource(Resource metaDataResource, Path inDir, Path outDir) {

        Path outFile = outDir.resolve(metaDataResource.getParsedFileName());

        logger.info("Parsing {}. Writing out to: {}", metaDataResource.getName(), outFile);
        ResourceOperationStatus status;
        try (
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {    
            
            for (Resource resource : externalResources) {
                String version = resource.getVersion();
                
                if (version == null || version.isEmpty()) {
                    Instant now = Instant.now();
                    version  = now.toString();
                }
                logger.info("Resource: {} Version: {}", resource.getName(), version);
                writer.write(String.format("%s|%s%n", resource.getName(), version));
            }
           status = ResourceOperationStatus.SUCCESS;
        } catch (IOException ex) {
            logger.error("Error parsing external resources MetaData", ex);
            status = ResourceOperationStatus.FAILURE;
        }
         metaDataResource.setParseStatus(status);
         logger.info("{}", status);
    }
    
    
}
