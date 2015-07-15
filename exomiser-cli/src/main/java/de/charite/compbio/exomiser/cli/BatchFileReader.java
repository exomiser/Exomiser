/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads in Exomiser batch files and returns a list of Paths to the
 * settings/analysis files. The reader expects a single path per line.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BatchFileReader {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchFileReader.class);
    
    public List<Path> readPathsFromBatchFile(Path batchFile) {
        List<Path> filePaths = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(batchFile, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                Path settingsFile = Paths.get(line);
                filePaths.add(settingsFile);
            }
        } catch (IOException ex) {
            logger.error("Unable to read batch file {}", batchFile, ex);
        }
        return filePaths;
    }

}
