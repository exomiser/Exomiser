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
package org.monarchinitiative.exomiser.data.phenotype.resources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;

/**
 * Handles the extraction of files from their downloaded state to an
 * unzipped/untarred state ready for parsing.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResourceExtractionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceExtractionHandler.class);

    private ResourceExtractionHandler() {
        //static utility class
    }

    public static void extractResources(Iterable<Resource> externalResources, Path inDir, Path outDir) {
        for (Resource externalResource : externalResources) {
            extractResource(externalResource, inDir, outDir);
        }
    }

    public static void extractResource(Resource externalResource, Path inDir, Path outDir) {
        logger.info("Resource: {} Extracting resource file {}...", externalResource.getName(), externalResource.getRemoteFileName());
        //make sure there is something to transfer... like the Metadata resource for instance 
        if (externalResource.getRemoteFileName().isEmpty() || externalResource.getExtractedFileName().isEmpty()) {
            logger.info("Nothing to extract for resource: {}", externalResource.getName());
            externalResource.setExtractStatus(ResourceOperationStatus.SUCCESS);
            return;
        }

        //make sure the output path exists
        outDir.toFile().mkdir();

        //expected types: .tar.gz, .gz, .zip, anything else
        File inFile = inDir.resolve(externalResource.getRemoteFileName()).toFile();
        File outFile = outDir.resolve(externalResource.getExtractedFileName()).toFile();

        ResourceOperationStatus status;
        String scheme = externalResource.getExtractionScheme();
        logger.info("Using resource extractionScheme: {}", scheme);
        switch (scheme) {
            case "gz":
                status = gunZipFile(inFile, outFile);
                break;
            case "tgz":
                status = extractTgzArchive(inFile, outFile);
                break;
            case "zip":
                status = extractArchive(inFile, outFile);
                break;
            case "copy":
                status = copyFile(inFile, outFile);
                break;
            case "none":
                status = ResourceOperationStatus.SUCCESS;
                break;
            default:
                status = copyFile(inFile, outFile);
        }

        externalResource.setExtractStatus(status);
        logger.info("{}", externalResource.getStatus());
    }

    private static ResourceOperationStatus gunZipFile(File inFile, File outFile) {
        logger.info("Unzipping file: {} to {}", inFile, outFile);

        try (FileInputStream fileInputStream = new FileInputStream(inFile);
             GZIPInputStream gZIPInputStream = new GZIPInputStream(fileInputStream);
             OutputStream out = new FileOutputStream(outFile)
        ) {
            //good for under 2GB otherwise IOUtils.copyLarge is needed
            IOUtils.copy(gZIPInputStream, out);

            return ResourceOperationStatus.SUCCESS;

        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        return ResourceOperationStatus.FAILURE;
    }

    /**
     * Will extract a .zip or .tar archive to the specified outFile location 
     * @param inFile
     * @param outFile
     * @return
     */
    private static ResourceOperationStatus extractArchive(File inFile, File outFile) {

        try {
            URI inFileUri = inFile.toURI();
            logger.info("Extracting archive: {}", inFileUri);
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject tarFile = fileSystemManager.resolveFile(inFileUri.toString());
            try {
                FileObject zipFileSystem = fileSystemManager.createFileSystem(tarFile);
                try {
                    fileSystemManager.toFileObject(outFile).copyFrom(zipFileSystem, new AllFileSelector());
                } finally {
                    zipFileSystem.close();
                }
            } finally {
                tarFile.close();
            }
            return ResourceOperationStatus.SUCCESS;

        } catch (FileSystemException ex) {
            logger.error(null, ex);
        }
        return ResourceOperationStatus.FAILURE;
    }

    private static ResourceOperationStatus extractTgzArchive(File inFile, File outFile) {
        logger.info("Extracting tar.gz file: {} to {}", inFile, outFile);
        File intermediateTarArchive = new File(outFile.getParentFile(), outFile.getName() + ".tar");

        //first unzip the file
        gunZipFile(inFile, intermediateTarArchive);
        //then extract the archive files
        ResourceOperationStatus returnStatus = extractArchive(intermediateTarArchive, outFile);
        //finally clean-up the intermediate file
        deleteFile(intermediateTarArchive);
        return returnStatus;
    }

    private static void deleteFile(File intermediateTarArchive) {
        try {
            Files.delete(intermediateTarArchive.toPath());
        } catch (IOException e) {
            logger.warn("Unable to delete file {}", intermediateTarArchive.getAbsolutePath());
        }
    }

    private static ResourceOperationStatus copyFile(File inFile, File outFile) {
        logger.info("Copying file {} to {}", inFile, outFile);

        try {
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            //update the last modified time to now so that it's easy to see when the file was 
            //copied over otherwise the last modified time will be when the file was downloaded.
            //this is merely a nicety for when manually inspecting the db build process. 
            outFile.setLastModified(System.currentTimeMillis());
            return ResourceOperationStatus.SUCCESS;
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        return ResourceOperationStatus.FAILURE;
    }
}
