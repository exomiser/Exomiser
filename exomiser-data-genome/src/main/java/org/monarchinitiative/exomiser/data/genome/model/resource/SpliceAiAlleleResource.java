package org.monarchinitiative.exomiser.data.genome.model.resource;

import org.monarchinitiative.exomiser.data.genome.model.archive.TabixArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.SpliceAiAlleleParser;

import java.net.URL;
import java.nio.file.Path;

public class SpliceAiAlleleResource extends AbstractAlleleResource {

    public SpliceAiAlleleResource(String name, URL resourceUrl, Path resourcePath) {
        super(name, resourceUrl, new TabixArchive(resourcePath), new SpliceAiAlleleParser());
    }
}
