/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.monarchinitiative.exomiser.data.genome.model.OutputLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OutputFileIndexer<T extends OutputLine> extends AbstractIndexer<T> {

    private static final Logger logger = LoggerFactory.getLogger(OutputFileIndexer.class);

    private final AtomicLong count = new AtomicLong(0);
    private BufferedWriter bufferedWriter;
    private final Path outFilePath;

    public OutputFileIndexer(Path outFilePath) {
        this.outFilePath = outFilePath;
//        logger.info("Writing to {}", outFilePath);
    }

    @Override
    public void write(T type) {
        if (bufferedWriter == null) {
            setUp();
        }
        try {
            bufferedWriter.write(type.toOutputLine());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            logger.error("Unable to write {} line to {}", type.toOutputLine(), outFilePath, e);
        }
        count.incrementAndGet();
    }

    private void setUp() {
        try {
            if (Files.notExists(outFilePath)) {
                Files.createFile(outFilePath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create outfile " + outFilePath, e);
        }
        try {
            this.bufferedWriter = Files.newBufferedWriter(outFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to access outfile " + outFilePath, e);
        }
    }

    @Override
    public long count() {
        return count.get();
    }

    @Override
    public void close() {
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            throw new IndexingException("Unable to close writer ", e);
        }
    }
}
