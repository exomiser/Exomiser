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

package org.monarchinitiative.exomiser.data.genome.model.resource;

import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.archive.Archive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.AlleleParser;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;

import java.net.URL;
import java.util.Objects;

/**
 * Class for defining the resources required for processing an allele data set in the application.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
abstract class AbstractAlleleResource implements AlleleResource {

    private final String name;
    private final URL resourceUrl;
    private final Archive archive;
    private final AlleleParser alleleParser;

    AbstractAlleleResource(String name, URL resourceUrl, Archive archive, AlleleParser alleleParser) {
        this.name = name;
        this.resourceUrl = resourceUrl;
        this.archive = archive;
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

    public Archive getArchive() {
        return archive;
    }

    @Override
    public Parser<Allele> getParser() {
        return alleleParser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAlleleResource that = (AbstractAlleleResource) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(resourceUrl, that.resourceUrl) &&
                Objects.equals(archive, that.archive) &&
                Objects.equals(alleleParser, that.alleleParser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resourceUrl, archive, alleleParser);
    }

    @Override
    public String toString() {
        return "AlleleResource{" +
                "name='" + name + '\'' +
                ", resourceUrl=" + resourceUrl +
                ", alleleArchive=" + archive +
                ", alleleParser=" + alleleParser +
                '}';
    }
}
