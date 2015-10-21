/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.cli;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BatchFileReaderTest {

    private BatchFileReader instance;

    @Before
    public void setUp() throws Exception {
        instance = new BatchFileReader();
    }

    private List<Path> getPaths(String fileName) {
        return instance.readPathsFromBatchFile(Paths.get(fileName));
    }

    @Test
    public void testReadPathsFromBatchFile_FileNotFound() throws Exception {
        assertThat(getPaths("wibble.txt").isEmpty(), is(true));
    }

    @Test
    public void testReadPathsFromBatchFile() throws Exception {
        assertThat(getPaths("src/test/resources/testBatchFiles.txt").size(), equalTo(3));
    }
}