package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parse the permissive enhancers file from Fantom
 *
 * @version 7.02 (14th December 2015)
 * @author Damian Smedley
 */
public class EnsemblEnhancerParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(EnsemblEnhancerParser.class);

    /**
     * @param conn COnnection to the Exomiser database.
     */
    public EnsemblEnhancerParser() {
    }

    /**
     * This function does the actual work of parsing the HPO file.
     *
     * @param resource
     * @param inDir Complete path to directory containing the
     * human-phenotype-ontology.obo or hp.obo file.
     * @param outDir Directory where output file is to be written
     * @return
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;

        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.forName("UTF-8"));
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("KI")){
                    continue;
                }
                line = line.replaceAll("\t", "|");
                line = line.replace("X|", "23|");
                line = line.replace("Y|", "24|");
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            reader.close();
            status = ResourceOperationStatus.SUCCESS;
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }

        resource.setParseStatus(status);

        logger.info(
                "{}", status);
    }

}
/* eof */
