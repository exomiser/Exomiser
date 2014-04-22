/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ExternalResource;
import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a pipe delimited file of resource|version
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MetaDataParser implements Parser {
    
    private static final Logger logger = LoggerFactory.getLogger(MetaDataParser.class);
    
    private final Iterable<ExternalResource> externalResources;
    private ExternalResource metaDataResource;
    
    public MetaDataParser (ExternalResource metaDataResource, Iterable<ExternalResource> externalResources) {
        this.metaDataResource = metaDataResource;
        this.externalResources = externalResources;
    }

    @Override
    public ResourceOperationStatus parse(String inPath, String outPath) {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {     
            for (ExternalResource resource : externalResources) {
                String version = resource.getVersion();
                
                if (version.isEmpty()) {
                    Instant now = Instant.now();
                    version  = now.toString();
                }
                writer.write(String.format("%s|%s%n", resource.getName(), version));
            }
        } catch (IOException ex) {
            logger.error("Error parsing external resources MetaData {}", ex);
            return ResourceOperationStatus.FAILURE;
        }
        return ResourceOperationStatus.SUCCESS;
    }
    
    
}
