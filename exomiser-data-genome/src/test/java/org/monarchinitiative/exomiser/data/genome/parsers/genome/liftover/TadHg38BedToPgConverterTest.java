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

package org.monarchinitiative.exomiser.data.genome.parsers.genome.liftover;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for cleaning up BED files converted using
 * http://www.ensembl.org/Homo_sapiens/Tools/AssemblyConverter?db=core
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TadHg38BedToPgConverterTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testMerge() throws Exception {
        Path inputFile = Paths.get("src/test/resources/genome/liftover/test_split_tad.bed");
        Path expectedOutputFile = Paths.get("src/test/resources/genome/liftover/test_expected_tad.bed");

        Path actualOutput = tempFolder.newFile().toPath();

        TadHg38BedToPgConverter instance = new TadHg38BedToPgConverter(inputFile, actualOutput);
        instance.run();

        List<String> expected = Files.readAllLines(expectedOutputFile);
        List<String> actual = Files.readAllLines(actualOutput);

        assertThat(actual, equalTo(expected));
    }

    /**
     * This was run as a one-off to create the hg38_tad.pg file.
     */
    public void createHg38TadPgFile() {
        Path inputFile = Paths.get("src/main/resources/hg19/hg38_tad.bed");
        Path outputFile = Paths.get("src/main/resources/hg19/hg38_tad.pg");

        TadHg38BedToPgConverter instance = new TadHg38BedToPgConverter(inputFile, outputFile);
        instance.run();
    }
}
