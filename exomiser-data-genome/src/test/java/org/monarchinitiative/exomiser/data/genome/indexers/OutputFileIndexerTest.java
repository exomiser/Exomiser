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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.genome.model.OutputFileIndexingResource;
import org.monarchinitiative.exomiser.data.genome.model.OutputLine;
import org.monarchinitiative.exomiser.data.genome.model.archive.Archive;
import org.monarchinitiative.exomiser.data.genome.model.archive.FileArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class OutputFileIndexerTest {

    @Test
    public void name(@TempDir Path temp) throws IOException {
        Path inFilePath = Path.of("src/test/resources/test-archive-file.txt");
        Path outFilePath = temp.resolve("test-resource.txt");
        Archive archive = new FileArchive(inFilePath);
        Parser<TestOutputLine> parser = new TestParser();
        OutputFileIndexer<TestOutputLine> indexer = new OutputFileIndexer<>(outFilePath);
        OutputFileIndexingResource<TestOutputLine> linesResource = new TestIndexingResource("name", null, archive, parser, indexer);
        linesResource.indexResource();
        List<String> inLines = Files.readAllLines(inFilePath);
        List<String> outLines = Files.readAllLines(outFilePath);
        assertThat(outLines, equalTo(inLines));
    }


    private static class TestIndexingResource implements OutputFileIndexingResource<TestOutputLine> {

        private final String name;
        private final URL url;
        private final Archive archive;
        private final Parser<TestOutputLine> parser;
        private final Indexer<TestOutputLine> indexer;

        private TestIndexingResource(String name, URL url, Archive archive, Parser<TestOutputLine> parser, Indexer<TestOutputLine> indexer) {
            this.name = name;
            this.url = url;
            this.archive = archive;
            this.parser = parser;
            this.indexer = indexer;
        }


        @Override
        public String getName() {
            return name;
        }

        @Override
        public URL getResourceUrl() {
            return url;
        }

        @Override
        public Archive getArchive() {
            return archive;
        }

        @Override
        public Parser<TestOutputLine> getParser() {
            return parser;
        }

        @Override
        public Indexer<TestOutputLine> indexer() {
            return indexer;
        }
    }

    private static class TestParser implements Parser<TestOutputLine> {

        @Override
        public List<TestOutputLine> parseLine(String line) {
            return List.of(new TestOutputLine(line));
        }
    }

    private static class TestOutputLine implements OutputLine {

        private final String line;

        public TestOutputLine(String line) {
            this.line = line;
        }

        @Override
        public String toOutputLine() {
            return line;
        }
    }
}