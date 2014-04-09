/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.resources;

import de.charite.compbio.exomiser.io.FileDownloadUtils;
import de.charite.compbio.exomiser.io.FileOperationStatus;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the business of downloading a set of ExternalResources
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceDownloadHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceDownloadHandler.class);

    public static void downloadResources(Iterable<ExternalResource> externalResources, Path downloadPath) {

        int numResources = 0;
        for (ExternalResource resource : externalResources) {
            numResources++;
            downloadResource(resource, downloadPath);
        }

        logger.info("Transferred {} file(s) with the following statuses:", numResources);
        for (ExternalResource resource : externalResources) {
            logger.info("{}", resource.getStatus());
        }

    }

    public static void downloadResource(ExternalResource externalResource, Path downloadPath) {

        try {

            URL resourceUrl = new URL(externalResource.getUrl() + externalResource.getRemoteFileName());
            logger.info("Resource: {}: Getting {} from {}", externalResource.getName(), externalResource.getRemoteFileName(), resourceUrl);
            FileOperationStatus status = FileDownloadUtils.fetchFile(resourceUrl, new File(String.format("%s/%s", downloadPath, externalResource.getRemoteFileName())));
            externalResource.setDownloadStatus(status);

        } catch (MalformedURLException ex) {
            logger.error(null, ex);
        }

        logger.info("{}", externalResource.getStatus());

    }
}
