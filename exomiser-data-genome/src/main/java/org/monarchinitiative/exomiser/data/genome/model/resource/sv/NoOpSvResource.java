package org.monarchinitiative.exomiser.data.genome.model.resource.sv;

import org.monarchinitiative.exomiser.data.genome.indexers.Indexer;
import org.monarchinitiative.exomiser.data.genome.indexers.OutputFileIndexer;
import org.monarchinitiative.exomiser.data.genome.model.OutputLine;
import org.monarchinitiative.exomiser.data.genome.model.archive.Archive;
import org.monarchinitiative.exomiser.data.genome.model.archive.FileArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * A non-operational SvResource. This is used to provide empty files for the flyway to 'load' into the database for
 * resources (e.g. gnomad-sv and gonl) which are not available for certain genome assemblies.
 * <p>
 * The class will provide the bare-bones required of an {@link SvResource} to allow slotting-into the main download-then-index loop for
 * SV resources. It will create and download an empty file to the {@link Archive} file path and then create an empty output file when
 * indexing the resource.
 */
public class NoOpSvResource implements SvResource<OutputLine> {

    private final String name;
    private final FileArchive archive;
    private final Path outputFile;

    public NoOpSvResource(String name, FileArchive archive, Path outputFile) {
        this.name = name;
        this.archive = archive;
        this.outputFile = outputFile;
    }

    @Override
    public Indexer<OutputLine> indexer() {
        return new OutputFileIndexer<>(outputFile);
    }

    @Override
    public void indexResource() {
        try {
            if (!Files.exists(outputFile)) {
                Files.createFile(outputFile);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getResourceUrl() {
        try {
            return Files.createTempFile(name, ".stub").toUri().toURL();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create temp file " + name + ".stub", e);
        }
    }

    @Override
    public Archive getArchive() {
        return archive;
    }

    @Override
    public Parser<OutputLine> getParser() {
        return line -> List.of();
    }

    @Override
    public Stream<OutputLine> parseResource() {
        return Stream.empty();
    }
}
