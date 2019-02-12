/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.resource;

import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.archive.AlleleArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.AlleleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Class for defining the resources required for processing an allele data set in the application.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
abstract class AbstractAlleleResource implements AlleleResource {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAlleleResource.class);

    private final String name;
    private final URL resourceUrl;
    private final AlleleArchive alleleArchive;
    private final AlleleParser alleleParser;

    AbstractAlleleResource(String name, URL resourceUrl, AlleleArchive alleleArchive, AlleleParser alleleParser) {
        this.name = name;
        this.resourceUrl = resourceUrl;
        this.alleleArchive = alleleArchive;
        this.alleleParser = alleleParser;
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
    public AlleleArchive getAlleleArchive() {
        return alleleArchive;
    }

    @Override
    public AlleleParser getAlleleParser() {
        return alleleParser;
    }

    @Override
    public Stream<Allele> alleles() {
        // wrap this in a try-with-resources to close the underlying file resources when the stream closes
        try (Stream<String> lines = alleleArchive.lines()) {
               return lines
//                       .peek(line -> logger.info("{}", line))
                       .flatMap(line -> alleleParser.parseLine(line).stream());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAlleleResource that = (AbstractAlleleResource) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(resourceUrl, that.resourceUrl) &&
                Objects.equals(alleleArchive, that.alleleArchive) &&
                Objects.equals(alleleParser, that.alleleParser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resourceUrl, alleleArchive, alleleParser);
    }

    @Override
    public String toString() {
        return "AlleleResource{" +
                "name='" + name + '\'' +
                ", resourceUrl=" + resourceUrl +
                ", alleleArchive=" + alleleArchive +
                ", alleleParser=" + alleleParser +
                '}';
    }
}
