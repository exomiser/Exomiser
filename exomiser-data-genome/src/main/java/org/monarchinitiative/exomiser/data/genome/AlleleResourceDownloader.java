/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome;

import org.apache.commons.io.FileUtils;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple downloader utility for {@link AlleleResource} data.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleResourceDownloader {

    private static final Logger logger = LoggerFactory.getLogger(AlleleResourceDownloader.class);

    private AlleleResourceDownloader() {
        // Static utility class
    }

    public static void download(AlleleResource alleleResource) {
        Path resourceFile = alleleResource.getAlleleArchive().getPath();

        if (!resourceFile.toFile().exists()) {
            downloadTabixFile(alleleResource);
            downloadResourceFile(alleleResource);
        } else {
            logger.info("{} resource file already present. Skipping download.", alleleResource.getName());
        }
    }

    private static void downloadTabixFile(AlleleResource alleleResource) {
        Path destination = alleleResource.getAlleleArchive().getPath();

        URL tabixUrl = createTabixFileUrl(alleleResource);
        Path tabixDestination = Paths.get(destination.toString() + ".tbi");
        String name = alleleResource.getName() + " tabix";
        try {
            logger.info("Downloading {} resource from: {}", name, tabixUrl);
            FileUtils.copyURLToFile(tabixUrl, tabixDestination.toFile(), 2500, 15000);
            logger.info("Finished downloading {} to {}", name, tabixDestination);
        } catch (IOException ex) {
            // this is not all that interesting - it's not critical to have the tabix index as it's only used for manual
            // inspection and not all resources (e.g. ESP and DBNSFP) have a corresponding index.
            logger.trace("Unable to download resource {} to {}", tabixUrl, tabixDestination, ex);
        }
    }

    private static URL createTabixFileUrl(AlleleResource alleleResource) {
        try {
            return new URL(alleleResource.getResourceUrl().toString() + ".tbi");
        } catch (MalformedURLException e) {
            logger.error("Unable to create tabix url for {}", alleleResource.getName(), e);
        }
        return null;
    }

    private static void downloadResourceFile(AlleleResource alleleResource) {
        URL resourceUrl = alleleResource.getResourceUrl();
        Path destination = alleleResource.getAlleleArchive().getPath();
        downloadResource(alleleResource.getName(), resourceUrl, destination);
    }

    private static void downloadResource(String name, URL resourceUrl, Path destination) {
        try {
            logger.info("Downloading {} resource from: {}", name, resourceUrl);
            FileUtils.copyURLToFile(resourceUrl, destination.toFile(), 2500, 15000);
            logger.info("Finished downloading {} to {}", name, destination);
        } catch (IOException ex) {
            logger.error("Unable to download resource {} to {}", resourceUrl, destination, ex);
            throw new ResourceDownloadException(ex);
        }
    }

}
