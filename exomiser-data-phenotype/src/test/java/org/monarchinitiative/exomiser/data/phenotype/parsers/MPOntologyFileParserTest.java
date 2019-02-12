/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MPOntologyFileParserTest {

    @Test
    @ExtendWith(TempDirectory.class)
    public void testParseHPO(@TempDirectory.TempDir Path tempDir) throws Exception {
        Resource testResource = new Resource("MP");
        testResource.setExtractedFileName("mp.obo");
        testResource.setParsedFileName("mp.pg");

        Map<String, String> mpId2termMap = new HashMap<>();

        MPOntologyFileParser instance = new MPOntologyFileParser(mpId2termMap);
        instance.parseResource(testResource, Paths.get("src/test/resources/data"), tempDir);

        assertFalse(mpId2termMap.isEmpty());

        Path mpPath = tempDir.resolve("mp.pg");
        assertTrue(mpPath.toFile().exists());
        List<String> lines = Files.readAllLines(mpPath);
        assertFalse(lines.isEmpty());
    }

}