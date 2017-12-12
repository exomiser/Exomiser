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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class EntrezParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(EntrezParser.class);

    /**
     * This function does the actual work of parsing the Entrez data.
     *
     * @param resource Resource containing the information about
     * @param inDir Directory path to string file.
     * @param outDir Directory where output file is to be written
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);

        Map<Integer, String> entrez2sym = new HashMap<>();
        ResourceOperationStatus status;

        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
             BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split("\t");
                if (split[0].equals("hgnc_id") || split[2].equals("entry withdrawn")
                        || split[3].equals("phenotype")
                        || split.length <= 18 || split[18].isEmpty())
                    continue;
                Integer entrez = null;

                try {
                    entrez = Integer.parseInt(split[18]);
                } catch (NumberFormatException e) {
                    logger.error("Malformed line: {} (could not parse entrez gene field: '{}')", line, split[1]);
                }
                if (split[1] == null || split[1].isEmpty()) {
                    logger.warn("Could not extract symbol, skipping line: {}", line);
                }
                String symbol = split[1];
                entrez2sym.put(entrez, symbol);
            }

            Iterator<Integer> it = entrez2sym.keySet().iterator();
            while (it.hasNext()) {
                Integer id = it.next();
                if (id == null) {
                    continue;
                }
                String sym = entrez2sym.get(id);
                writer.write(String.format("%s|%s", id, sym));
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
        logger.info("{}", status);
    }
}
