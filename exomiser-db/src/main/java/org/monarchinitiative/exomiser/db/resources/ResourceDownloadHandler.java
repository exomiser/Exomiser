/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.db.resources;

import org.joda.time.Instant;
import org.monarchinitiative.exomiser.db.io.FileDownloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Handles the business of downloading a set of ExternalResources
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceDownloadHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDownloadHandler.class);

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
