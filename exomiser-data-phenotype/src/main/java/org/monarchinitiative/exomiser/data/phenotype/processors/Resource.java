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

package org.monarchinitiative.exomiser.data.phenotype.processors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

/**
 * Central class used to describe a file-based resource which is used as source data for creating the database.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class Resource {

    private final String fileName; // extractedName
    private final Path fileDirectory; // the downloadDir
    private final URL remoteFileUrl; // url
    private final String remoteFileName;

    private Resource(Builder builder) {
        this.fileName = Objects.requireNonNull(builder.fileName);
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Resource filename cannot be empty");
        }
        this.fileDirectory = Objects.requireNonNull(builder.fileDirectory);
        this.remoteFileUrl = buildUrl(builder.remoteFileUrl, builder.remoteFileName);
        this.remoteFileName = builder.remoteFileName;
    }

    public static Resource of(String filePath) {
        Path localResource = Path.of(filePath);
        return new Builder().fileDirectory(localResource.getParent()).fileName(localResource.getFileName().toString()).build();
    }

    public static Resource of(Path fileDirectory, String filename) {
        return new Builder().fileDirectory(fileDirectory).fileName(filename).build();
    }

    private URL buildUrl(String remoteFileUrl, String remoteFileName) {
        if (remoteFileUrl.isEmpty() || remoteFileName.isEmpty()) {
            return null;
        }
        String url = !remoteFileUrl.endsWith("/") ? remoteFileUrl + "/" + remoteFileName : remoteFileUrl + remoteFileName;
        try {
            return new URL(url);
        } catch (MalformedURLException exception) {
            throw new RuntimeException("Incorrectly formatted resource URL: " + url, exception);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public Path getResourcePath() {
        return fileDirectory.resolve(fileName);
    }

    public BufferedReader newBufferedReader() throws IOException {
        if (fileName.endsWith(".zip")) {
            return new BufferedReader(new InputStreamReader(new ZipInputStream(Files.newInputStream(getResourcePath()))));
        }
        if (fileName.endsWith(".gz")) {
            return new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(getResourcePath()))));
        }
        return Files.newBufferedReader(getResourcePath());
    }

    public boolean hasRemoteResource() {
        return remoteFileUrl != null;
    }

    @Nullable
    public URL getRemoteResourceUrl() {
        return remoteFileUrl;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource that = (Resource) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(fileDirectory, that.fileDirectory) &&
                Objects.equals(remoteFileUrl, that.remoteFileUrl) &&
                Objects.equals(remoteFileName, that.remoteFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileDirectory, remoteFileUrl, remoteFileName);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "fileName='" + fileName + '\'' +
                ", fileDirectory=" + fileDirectory +
                ", remoteFileUrl='" + remoteFileUrl + '\'' +
                ", remoteFileName='" + remoteFileName + '\'' +
                '}';
    }

    public static class Builder {

        private String fileName = null; // extractedName
        private Path fileDirectory = null; // the downloadDir
        private String remoteFileUrl = ""; // url
        private String remoteFileName = "";

        public Builder fileName(@Nonnull String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder fileDirectory(@Nonnull Path fileDirectory) {
            this.fileDirectory = fileDirectory;
            return this;
        }

        public Builder remoteFileUrl(@Nullable String fileUrl) {
            this.remoteFileUrl = Objects.requireNonNullElse(fileUrl, "");
            return this;
        }

        public Builder remoteFileName(@Nullable String remoteFileName) {
            this.remoteFileName = Objects.requireNonNullElse(remoteFileName, "");
            return this;
        }

        public Resource build(){
            return new Resource(this);
        }

    }

}
