/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.resource.sv;

import org.monarchinitiative.exomiser.data.genome.indexers.Indexer;
import org.monarchinitiative.exomiser.data.genome.model.OutputLine;
import org.monarchinitiative.exomiser.data.genome.model.archive.Archive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;

import java.net.URL;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class AbstractSvIndexingResource<T extends OutputLine> implements SvResource<T> {

    private final String name;
    private final URL resourceUrl;
    private final Archive archive;
    private final Parser<T> parser;
    private final Indexer<T> indexer;

    AbstractSvIndexingResource(String name, URL resourceUrl, Archive archive, Parser<T> parser, Indexer<T> indexer) {
        this.name = Objects.requireNonNull(name);
        this.resourceUrl = Objects.requireNonNull(resourceUrl);
        this.archive = Objects.requireNonNull(archive);
        this.parser = Objects.requireNonNull(parser);
        this.indexer = Objects.requireNonNull(indexer);
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
        return archive;
    }

    @Override
    public Parser<T> getParser() {
        return parser;
    }

    @Override
    public Indexer<T> indexer() {
        return indexer;
    }

}
