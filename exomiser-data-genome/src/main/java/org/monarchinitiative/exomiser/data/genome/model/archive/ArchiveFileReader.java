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

package org.monarchinitiative.exomiser.data.genome.model.archive;

import htsjdk.samtools.util.BlockCompressedInputStream;
import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
abstract class ArchiveFileReader {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveFileReader.class);

    private final Path archivePath;
    private final String archiveFormat;
    protected final String dataFileFormat;

    protected ArchiveFileReader(Archive archive) {
        this.archivePath = archive.getPath();
        this.archiveFormat = archive.getArchiveFileFormat();
        this.dataFileFormat = archive.getDataFileFormat();
    }

    /**
     * Reads the the underlying {@link Archive} file and transforms the contents into a stream of lines to parse.
     */
    public Stream<String> lines() {
        return getArchiveFileObjects()
                .stream()
                .map(toBufferedReader())
                .flatMap(BufferedReader::lines);
    }

    /**
     * There could be several {@link FileObject} within an archive. Usually a {@link TabixArchive} contains a
     * single plaintext file inside the gz file. dbNSFP on the other hand is a zip file containing either plaintext (v3.x)
     * or gz (v4.x) files for each chromosome along with a bunch of other files.
     */
    private List<FileObject> getArchiveFileObjects() {
        List<FileObject> archiveFileObjects = new ArrayList<>();
        try {
            FileObject archive = getFileObject(archiveFormat, archivePath);
            for (FileObject fileObject : getArchiveFiles(archive)) {
                FileName fileName = fileObject.getName();
                if (isWanted(fileName)) {
                    logger.info("Including {} in archive file objects", fileName.getBaseName());
                    archiveFileObjects.add(fileObject);
                }
            }
        } catch (FileSystemException e) {
            logger.error("Unable to open {} archive file {}", archiveFormat, archivePath, e);
        }
        archiveFileObjects.sort(Comparator.comparing(FileObject::getName));
        return archiveFileObjects;
    }

    private FileObject getFileObject(String archiveFormat, Path archiveFileName) throws FileSystemException {
        FileSystemManager fileSystemManager = VFS.getManager();
        if (archiveFormat.isEmpty()) {
            return fileSystemManager.resolveFile(archiveFileName.toAbsolutePath().toString());
        }
        return fileSystemManager.resolveFile(archiveFormat + ":file://" + archiveFileName.toAbsolutePath());
    }

    private Iterable<FileObject> getArchiveFiles(FileObject archive) throws FileSystemException {
        return archive.getType().hasChildren() ? List.of(archive.getChildren()) : List.of(archive);
    }

    abstract boolean isWanted(FileName fileObjectName);

    private Function<FileObject, BufferedReader> toBufferedReader() {
        return fileObject -> {
            try {
                InputStream inputStream = readFileObject(fileObject);
                return new BufferedReader(new InputStreamReader(inputStream));
            } catch (IOException e) {
                logger.error("Error reading archive file {}", fileObject.getName(), e);
            }
            return null;
        };
    }

    // return a BufferedReader
    private InputStream readFileObject(FileObject fileObject) throws IOException {
        logger.info("Reading archive file {}", fileObject.getName());
        FileContent fileContent = fileObject.getContent();
        // hack for dbNSFP4 - most resources are plain text inside an archive
        if (fileObject.getName().getExtension().equals("gz")) {
            return new GZIPInputStream(fileContent.getInputStream());
        }
        // required for gnomad-genomes-v3 tar file containing vcf.bgz files
        if (fileObject.getName().getExtension().equals("bgz")) {
            return new BlockCompressedInputStream(fileContent.getInputStream());
        }
        return fileContent.getInputStream();
    }

}
