/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OutputLineWriter<T extends OutputLine> {

    private static final Logger logger = LoggerFactory.getLogger(OutputLineWriter.class);

    private final Path outFile;

    public OutputLineWriter(Path outFile) {
        this.outFile = outFile;
    }

    public void write(Collection<T> rowObjects) {
        try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
            for (T rowObject : rowObjects) {
                writer.write(rowObject.toOutputLine());
                writer.newLine();
            }
        } catch (IOException ex) {
            logger.error("Unable to write to file {}", outFile, ex);
        }
        logger.info("Written {} lines to file {}", rowObjects.size(), outFile);
    }

}
