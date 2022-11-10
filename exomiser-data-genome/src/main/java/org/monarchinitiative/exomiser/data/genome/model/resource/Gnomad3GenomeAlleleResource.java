package org.monarchinitiative.exomiser.data.genome.model.resource;

import org.monarchinitiative.exomiser.data.genome.model.archive.DirectoryArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Gnomad3GenomeAlleleParser;

import java.net.URL;
import java.nio.file.Path;

public class Gnomad3GenomeAlleleResource extends AbstractAlleleResource {

    public Gnomad3GenomeAlleleResource(String name, URL resourceUrl, Path resourcePath) {
        // These files total ~4TB
        // Finished 'hg38.gnomad-genome' resource - processed 644435679 objects in 119525 sec. Total 644435679 objects written.
        // produces: 22G Oct 11 19:34 2210_hg38.gnomad-genome.mv.db
        super(name, resourceUrl, new DirectoryArchive(resourcePath, "", "bgz"), new Gnomad3GenomeAlleleParser());
    }
}
