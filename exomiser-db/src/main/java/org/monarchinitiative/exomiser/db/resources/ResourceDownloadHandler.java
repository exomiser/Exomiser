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
package org.monarchinitiative.exomiser.db.resources;

import org.monarchinitiative.exomiser.db.resources.io.FileDownloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Handles the business of downloading a set of ExternalResources
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceDownloadHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDownloadHandler.class);

    private ResourceDownloadHandler() {
        //static utility class
    }

    public static void downloadResources(Iterable<Resource> externalResources, Path downloadPath) {

        int numResources = 0;
        for (Resource resource : externalResources) {
            numResources++;
            downloadResource(resource, downloadPath);
        }

        logger.info("Transferred {} file(s) with the following statuses:", numResources);
        for (Resource resource : externalResources) {
            logger.info("{}", resource.getStatus());
        }

    }

    public static void downloadResource(Resource externalResource, Path downloadDir) {

        ResourceOperationStatus status;
        if (externalResource.getUrl() == null || externalResource.getUrl().isEmpty()) {
            logger.info("Resource {} has no URL set - skipping resource.", externalResource.getName());
            return;
        }
        try {

            URL resourceUrl = new URL(externalResource.getUrl() + externalResource.getRemoteFileName());
            logger.info("Resource: {}: Getting {} from {}", externalResource.getName(), externalResource.getRemoteFileName(), resourceUrl);
            Path downloadPath = downloadDir.resolve(externalResource.getRemoteFileName());
            status = FileDownloadUtils.fetchFile(resourceUrl, downloadPath.toFile());
            externalResource.setDownloadStatus(status);
            //if there is no version info for the resource, set a timestamp
            if (externalResource.getVersion() == null || externalResource.getVersion().isEmpty()) {
                externalResource.setVersion(Instant.now().toString());
            }

        } catch (MalformedURLException ex) {
            status = ResourceOperationStatus.FAILURE;
            logger.error(null, ex);
        }
        externalResource.setDownloadStatus(status);

        logger.info("{}", externalResource.getStatus());

    }
}
