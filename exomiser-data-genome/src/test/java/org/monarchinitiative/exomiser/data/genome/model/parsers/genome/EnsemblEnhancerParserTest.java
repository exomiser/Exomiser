/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers.genome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EnsemblEnhancerParserTest {

    @Test
    @ExtendWith(TempDirectory.class)
    public void parse(@TempDir Path tempDir) throws Exception {
        Path inputFile = Paths.get("src/test/resources/genome/ensembl_enhancers.tsv");
        Path outputFile = tempDir.resolve("test_ensembl_enhancers.pg");
        Path expectedOutputFile = Paths.get("src/test/resources/genome/ensembl_enhancers.pg");

        EnsemblEnhancerParser instance = new EnsemblEnhancerParser(inputFile, outputFile);
        instance.parse();

        List<String> expected = Files.readAllLines(expectedOutputFile);
        List<String> actual = Files.readAllLines(outputFile);

        assertThat(actual, equalTo(expected));
    }

}