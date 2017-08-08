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

package org.monarchinitiative.exomiser.db.io;

import org.apache.commons.io.FileUtils;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FileDownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadUtils.class);

    private FileDownloadUtils() {
        //this class should not be instantiated
    }

    /**
     * Fetches the file from the source URL and writes it out to the destination
     * file. 
     * @param source
     * @param destination
     * @return
     */
    public static ResourceOperationStatus fetchFile(URL source, File destination) {
        ResourceOperationStatus status = ResourceOperationStatus.FAILURE;

        try {
            logger.info("Creating new file: {}", destination.getAbsolutePath());
            logger.info("Transferring data from: {}", source);
            destination.createNewFile();
            destination.setWritable(true);
//            logger.info("Protocol: {}", source.getProtocol());
//            SocketAddress socketAddress = new InetSocketAddress("wwwcache.sanger.ac.uk", 3128);
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, socketAddress);
            FileUtils.copyURLToFile(source, destination, 2500, 15000);
        } catch (IOException ex) {
            logger.error("Unable to copy file from external resource due to error: ", ex);
            return ResourceOperationStatus.FAILURE;
        }

        //always the optimist
        if (destination.length() == 0 ) {
            logger.info("{} is empty - deleting file.", destination.getAbsolutePath());
            destination.delete();
        } else {
            status = ResourceOperationStatus.SUCCESS;
        }

        return status;
    }
}
