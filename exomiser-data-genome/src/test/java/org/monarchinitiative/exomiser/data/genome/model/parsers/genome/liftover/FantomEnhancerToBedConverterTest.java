/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers.genome.liftover;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FantomEnhancerToBedConverterTest {

    @Test
    public void run(@TempDir Path tempDir) throws Exception {
        Path inputFile = Paths.get("src/test/resources/genome/liftover");
        Path expectedOutputFile = Paths.get("src/test/resources/genome/liftover/hg19_fantom_permissive_enhancer_usage.bed");

        FantomEnhancerToBedConverter instance = new FantomEnhancerToBedConverter(inputFile, tempDir);
        instance.run();

        List<String> expected = Files.readAllLines(expectedOutputFile);
        List<String> actual = Files.readAllLines(tempDir.resolve("hg19_fantom_permissive_enhancer_usage.bed"));

        assertThat(actual, equalTo(expected));
    }

}