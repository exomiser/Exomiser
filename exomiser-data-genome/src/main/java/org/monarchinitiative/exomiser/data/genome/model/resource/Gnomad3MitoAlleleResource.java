package org.monarchinitiative.exomiser.data.genome.model.resource;

import org.monarchinitiative.exomiser.data.genome.model.parsers.Gnomad3MitoAlleleParser;

import java.net.URL;
import java.nio.file.Path;

public class Gnomad3MitoAlleleResource extends TabixAlleleResource {

    public Gnomad3MitoAlleleResource(String name, URL resourceUrl, Path resourcePath) {
        super(name, resourceUrl, resourcePath, new Gnomad3MitoAlleleParser());
    }
}
