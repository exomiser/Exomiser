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

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * ZPO parser
 */
public class ZPOntologyFileParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(ZPOntologyFileParser.class);

    private final Map<String, String> zpId2termMap;

    public ZPOntologyFileParser(Map<String, String> zpId2termMap) {
        this.zpId2termMap = zpId2termMap;
    }

    /**
     * This function does the actual work of parsing the HPO file.
     *
     * @param resource
     * @param inDir    Complete path to directory containing the zp.obo file.
     * @param outDir   Directory where output file is to be written
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
                String[] fields = line.split("\\t");
                if (fields[0].startsWith("ZP:")) {
                    zpId2termMap.put(fields[0], fields[1]);
                    writer.write(String.format("%s|%s", fields[0], fields[1]));
                    writer.newLine();
                }
            }
            status = ResourceOperationStatus.SUCCESS;
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }
        resource.setParseStatus(status);
        logger.info("{}", status);
    }
}