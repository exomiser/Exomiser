package org.monarchinitiative.exomiser.data.genome.model.resource;

import org.monarchinitiative.exomiser.core.genome.VcfFiles;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.archive.Archive;
import org.monarchinitiative.exomiser.data.genome.model.archive.TabixArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.AlfaAlleleParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;

import java.net.URL;
import java.nio.file.Path;

public class AlfaAlleleResource implements AlleleResource {

    private final String name;
    private final URL resourceUrl;
    private final Path resourcePath;
    private TabixArchive tabixArchive = null;
    private AlfaAlleleParser alfaAlleleParser = null;

    public AlfaAlleleResource(String name, URL resourceUrl, Path resourcePath) {
        this.name = name;
        this.resourceUrl = resourceUrl;
        this.resourcePath = resourcePath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getResourceUrl() {
        return resourceUrl;
    }

    @Override
    public Archive getArchive() {
        if (tabixArchive == null) {
            tabixArchive = new TabixArchive(resourcePath);
        }
        return tabixArchive;
    }

    @Override
    public Parser<Allele> getParser() {
        if (alfaAlleleParser == null) {
            alfaAlleleParser = new AlfaAlleleParser(VcfFiles.readVcfHeader(resourcePath));
        }
        return alfaAlleleParser;
    }

}

