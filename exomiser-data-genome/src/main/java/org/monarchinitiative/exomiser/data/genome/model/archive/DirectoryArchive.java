package org.monarchinitiative.exomiser.data.genome.model.archive;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @param archivePath    path of the original compressed archive file
 * @param archiveFormat  format of the original compressed archive file - tgz, gz or zip
 * @param dataFileFormat extension of the uncompressed data file inside the archive file - usually vcf, but dbNSFP uses .chr[1-22,X,Y,M]
 */
public record DirectoryArchive(Path archivePath, String archiveFormat, String dataFileFormat) implements Archive  {

    public DirectoryArchive {
        if (!Files.exists(archivePath)) {
            throw new IllegalArgumentException("Directory not found: " + archivePath);
        }
        if (!Files.isDirectory(archivePath)) {
            throw new IllegalArgumentException("ArchivePath must be a directory but a file was provided: " + archivePath);
        }
    }

    @Override
    public Path getPath() {
        return archivePath;
    }

    @Override
    public String getArchiveFileFormat() {
        return archiveFormat;
    }

    @Override
    public String getDataFileFormat() {
        return dataFileFormat;
    }

}
