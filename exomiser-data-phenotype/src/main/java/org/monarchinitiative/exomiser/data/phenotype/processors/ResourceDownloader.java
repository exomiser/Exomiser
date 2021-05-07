/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ResourceDownloader {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDownloader.class);

    public static boolean downloadResource(Resource resource) {
        Path destination = resource.getResourcePath().toAbsolutePath();

        try {
            if (resource.getRemoteResourceUrl() != null) {
                logger.info("Transferring data from: {}", resource.getRemoteResourceUrl());
                FileUtils.copyURLToFile(resource.getRemoteResourceUrl(), destination.toFile(), 2500, 15000);
                // no exception? Hurrah!
                long sizeInBytes = Files.size(destination);
                logger.info("Success! Transferred {} bytes to {}", sizeInBytes, resource.getResourcePath());
                return true;
            }
        } catch (IOException ex) {
            logger.error("Unable to copy {} to {} due to error: ", resource.getRemoteResourceUrl(), destination, ex);
        }

        // clean-up empty files if transfer failed
        try {
            if (Files.size(destination) == 0) {
                logger.info("Deleting empty file {}", destination);
                Files.deleteIfExists(destination);
                // stop the entire build if resources failed to download
                throw new IllegalStateException("Failed to transfer resource " + resource.getFileName());
            }
        } catch (IOException e) {
            logger.error("Unable to delete empty file {}", destination);
        }

        return false;
    }
}
