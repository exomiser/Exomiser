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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ReleaseFileSystem {

    private static final Logger logger = LoggerFactory.getLogger(ReleaseFileSystem.class);

    private final Path resourcesDir;
    private final Path processedDir;
    private final Path releaseDir;

    public static ReleaseFileSystem create(Path rootBuildDir, String buildVersion) {
        return new ReleaseFileSystem(rootBuildDir, buildVersion);
    }

    public ReleaseFileSystem(Path rootBuildDir, String buildVersion) {
        Objects.requireNonNull(rootBuildDir);
        Objects.requireNonNull(buildVersion);
        if (buildVersion.isEmpty()) {
            throw new IllegalArgumentException("buildVersion cannot be empty");
        }
        Path buildDir = rootBuildDir.toAbsolutePath();

        if (!Files.exists(buildDir)) {
            logger.info("Creating root build directory {}", buildDir);
            createDirectories(buildDir.toAbsolutePath());
        }

//        Path buildDir = createDirectories(rootBuildDir.toAbsolutePath());
        logger.info("Creating release filesystem under {}", buildDir);

        resourcesDir = createDirectories(buildDir.resolve("resources"));
        processedDir = createDirectories(buildDir.resolve("processed"));
        releaseDir = createDirectories(buildDir.resolve(buildVersion + "_phenotype"));
    }

    private Path createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create directories " + directory, e);
        }
        return directory;
    }

    /**
     * download -> downloads resources to 'resources'
     * buildDir/buildVersion/resources  -- 'resources'
     */
    public Path resourcesDir() {
        return resourcesDir;
    }

    /**
     * process -> processes downloaded resources to 'processedDir'
     * buildDir/buildVersion/processed -- 'processed'
     */
    public Path processedDir() {
        return processedDir;
    }

    /**
     * migrate -> reads from processedDir() into H2 database in buildVersion_phenotype
     * buildDir/buildVersion/buildVersion_phenotype
     */
    public Path releaseDir() {
        return releaseDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReleaseFileSystem)) return false;
        ReleaseFileSystem that = (ReleaseFileSystem) o;
        return resourcesDir.equals(that.resourcesDir) &&
                processedDir.equals(that.processedDir) &&
                releaseDir.equals(that.releaseDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourcesDir, processedDir, releaseDir);
    }

    @Override
    public String toString() {
        return "ReleaseFileSystem{" +
                "resourcesDir=" + resourcesDir +
                ", processedDir=" + processedDir +
                ", releaseDir=" + releaseDir +
                '}';
    }
}
