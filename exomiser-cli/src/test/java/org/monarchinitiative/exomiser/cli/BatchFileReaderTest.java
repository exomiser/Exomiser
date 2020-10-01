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

package org.monarchinitiative.exomiser.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class BatchFileReaderTest {

    private List<Path> readPaths(String fileName) {
        return BatchFileReader.readPathsFromBatchFile(Paths.get(fileName));
    }

    @Test
    void testReadPathsFromBatchFileFileNotFound() {
        assertThat(readPaths("wibble.txt").isEmpty(), is(true));
    }

    @Test
    void testReadPathsFromBatchFile() {
        assertThat(readPaths("src/test/resources/testBatchFiles.txt").size(), equalTo(3));
    }
}