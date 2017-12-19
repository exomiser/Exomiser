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

package org.monarchinitiative.exomiser.data.genome.archive;

import org.apache.commons.vfs2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ArchiveFileReader {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveFileReader.class);

    private final Path archiveFileName;
    private final String archiveFormat;
    private final String dataFileFormat;

    public ArchiveFileReader(AlleleArchive alleleArchive) {
        this.archiveFileName = alleleArchive.getPath();
        this.archiveFormat = alleleArchive.getArchiveFileFormat();
        this.dataFileFormat = alleleArchive.getDataFileFormat();
    }

    public List<FileObject> getFileObjects() {
        List<FileObject> archiveFileInputStreams = new ArrayList<>();
        try {
            FileSystemManager fileSystemManager = VFS.getManager();
            FileObject archive = fileSystemManager.resolveFile(archiveFormat + ":file://" + archiveFileName.toAbsolutePath());
            for (FileObject fileObject : archive.getChildren()) {
                if (fileObject.getName().getExtension().startsWith(dataFileFormat)) {
                    archiveFileInputStreams.add(fileObject);
                }
            }
        } catch (FileSystemException e) {
            logger.error("{}", e);
        }
        return archiveFileInputStreams;
    }

    public InputStream readFileObject(FileObject fileObject) throws IOException {
        logger.info("Reading archive file {}", fileObject.getName());
        FileContent fileContent = fileObject.getContent();
        return fileContent.getInputStream();
    }

}
