/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.db.build.io;

import de.charite.compbio.exomiser.db.build.resources.ResourceOperationStatus;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FileDownloadUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadUtils.class);
    
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
