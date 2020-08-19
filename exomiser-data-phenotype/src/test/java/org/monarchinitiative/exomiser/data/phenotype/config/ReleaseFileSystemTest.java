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

package org.monarchinitiative.exomiser.data.phenotype.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ReleaseFileSystemTest {

    @Test
    void buildDirAlreadyExists(@TempDir Path tempDir) throws IOException {
        Path existingBuildDir = tempDir.resolve("2008-phenotype-build");
        Files.createDirectory(existingBuildDir);
        System.out.println("Created directory " + existingBuildDir);


        ReleaseFileSystem instance = ReleaseFileSystem.create(existingBuildDir, "2008");
        Path resourcesDir = instance.resourcesDir();
        assertThat(resourcesDir, equalTo(existingBuildDir.resolve("resources")));
        assertTrue(Files.exists(resourcesDir));
    }


    @Test
    void resourcesDir(@TempDir Path tempDir) {
        Path buildDir = tempDir.resolve("2008-phenotype-build");
        ReleaseFileSystem instance = ReleaseFileSystem.create(buildDir, "2008");

        Path resourcesDir = instance.resourcesDir();
        assertThat(resourcesDir, equalTo(buildDir.resolve("resources")));
        assertTrue(Files.exists(resourcesDir));
    }

    @Test
    void processedDir(@TempDir Path tempDir) {
        Path buildDir = tempDir.resolve("2008-phenotype-build");
        ReleaseFileSystem instance = ReleaseFileSystem.create(buildDir, "2008");
        Path processedDir = instance.processedDir();
        assertThat(processedDir, equalTo(buildDir.resolve("processed")));
        assertTrue(Files.exists(processedDir));
    }

    @Test
    void releaseDir(@TempDir Path tempDir) {
        Path buildDir = tempDir.resolve("2008-phenotype-build");

        ReleaseFileSystem instance = ReleaseFileSystem.create(buildDir, "2008");
        Path releaseDir = instance.releaseDir();
        assertThat(releaseDir, equalTo(buildDir.resolve("2008_phenotype")));
        assertTrue(Files.exists(releaseDir));
    }

}