/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * Reads in Exomiser batch files and returns a list of Paths to the
 * settings/analysis files. The reader expects a single path per line.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BatchFileReader {

    private static final Logger logger = LoggerFactory.getLogger(BatchFileReader.class);

    public List<Path> readPathsFromBatchFile(Path batchFile) {
        logger.info("Processing batch file {}", batchFile);
        try (Stream<String> lines = Files.lines(batchFile, Charset.defaultCharset())) {
            return lines.filter(commentLines()).filter(emptyLines()).map(line -> Paths.get(line.trim())).collect(toList());
        } catch (IOException ex) {
            logger.error("Unable to read batch file {}", batchFile, ex);
        }
        return new ArrayList<>();
    }

    private Predicate<String> commentLines() {
        return line -> !line.startsWith("#");
    }

    private Predicate<String> emptyLines() {
        return line -> !line.isEmpty();
    }

}
