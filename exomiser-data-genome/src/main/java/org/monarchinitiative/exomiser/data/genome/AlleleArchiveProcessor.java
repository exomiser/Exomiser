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

package org.monarchinitiative.exomiser.data.genome;

import org.apache.commons.vfs2.FileObject;
import org.monarchinitiative.exomiser.data.genome.archive.AlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.ArchiveFileReader;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.parsers.AlleleParser;
import org.monarchinitiative.exomiser.data.genome.writers.AlleleWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleArchiveProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AlleleArchiveProcessor.class);

    private final AlleleArchive alleleArchive;
    private final AlleleParser alleleParser;

    public AlleleArchiveProcessor(AlleleArchive alleleArchive, AlleleParser alleleParser) {
        this.alleleArchive = alleleArchive;
        this.alleleParser = alleleParser;
    }

    public void process(AlleleWriter alleleWriter) {
        ArchiveFileReader archiveFileReader = new ArchiveFileReader(alleleArchive);
        Instant startTime = Instant.now();
        AlleleLogger alleleLogger = new AlleleLogger(startTime);
        for (FileObject fileObject : archiveFileReader.getFileObjects()) {
            try (InputStream archiveFileInputStream = archiveFileReader.readFileObject(fileObject);
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(archiveFileInputStream))) {
                bufferedReader.lines()
                        .flatMap(toAlleleStream())
                        .peek(alleleLogger.logCount())
                        .forEach(alleleWriter::write);
            } catch (IOException e) {
                logger.error("Error reading archive file {}", fileObject.getName(), e);
            }
        }
        long seconds = Duration.between(startTime, Instant.now()).getSeconds();
        logger.info("Finished - processed {} variants total in {} sec", alleleWriter.count(), seconds);
    }

    private Function<String, Stream<Allele>> toAlleleStream() {
        return line -> alleleParser.parseLine(line).stream();
    }

    private class AlleleLogger {

        private final AtomicInteger counter;
        private final Instant startTime;

        public AlleleLogger(Instant startTime) {
            this.counter = new AtomicInteger();
            this.startTime = startTime;
        }

        public Consumer<Allele> logCount() {
            return allele -> {
                counter.incrementAndGet();
                if (counter.get() % 1000000 == 0) {
                    long seconds = Duration.between(startTime, Instant.now()).getSeconds();
                    logger.info("Processed {} variants total in {} sec", counter.get(), seconds);
                    logger.info("{}", allele);
                }
            };
        }
    }

}
