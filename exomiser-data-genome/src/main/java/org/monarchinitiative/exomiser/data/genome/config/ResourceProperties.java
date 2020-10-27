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

package org.monarchinitiative.exomiser.data.genome.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ResourceProperties {

    private final String fileName;
    private final Path fileDirectory;
    private final String fileUrl;

    public ResourceProperties(String fileName, Path fileDirectory, String fileUrl) {
        this.fileName = Objects.requireNonNull(fileName);
        this.fileDirectory = Objects.requireNonNull(fileDirectory);
        Objects.requireNonNull(fileUrl);
        if (!fileUrl.endsWith("/")) {
            this.fileUrl = fileUrl + "/";
        } else {
            this.fileUrl = fileUrl;
        }
    }

    public String getFileName() {
        return fileName;
    }

    public Path getFileDirectory() {
        return fileDirectory;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public Path getResourcePath() {
        return fileDirectory.resolve(fileName);
    }

    public URL getResourceUrl() {
        try {
            return new URL(fileUrl + fileName);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Incorrectly formatted allele resource URL", e);
        }
    }

    @Override
    public String toString() {
        return "ResourceProperties{" +
                "fileName='" + fileName + '\'' +
                ", fileDirectory=" + fileDirectory +
                ", fileUrl='" + fileUrl + '\'' +
                '}';
    }
}
