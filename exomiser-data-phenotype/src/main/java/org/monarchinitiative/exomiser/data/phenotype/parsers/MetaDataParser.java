/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Creates a pipe delimited file of resource|version
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MetaDataParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MetaDataParser.class);

    private final Iterable<Resource> externalResources;
    private final Resource metaDataResource;

    public MetaDataParser(Resource metaDataResource, Iterable<Resource> externalResources) {
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
                    version = now.toString();
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
