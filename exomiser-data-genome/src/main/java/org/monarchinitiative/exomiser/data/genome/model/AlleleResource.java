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

package org.monarchinitiative.exomiser.data.genome.model;

import org.monarchinitiative.exomiser.data.genome.archive.AlleleArchive;
import org.monarchinitiative.exomiser.data.genome.parsers.AlleleParser;

import java.util.Objects;

/**
 * Data class for defining the resources required for processing an allele data set in the application.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleResource {

    private final String name;
    private final AlleleArchive alleleArchive;
    private final AlleleParser alleleParser;

    public AlleleResource(String name, AlleleArchive alleleArchive, AlleleParser alleleParser) {
        this.name = name;
        this.alleleArchive = alleleArchive;
        this.alleleParser = alleleParser;
    }

    public String getName() {
        return name;
    }

    public AlleleArchive getAlleleArchive() {
        return alleleArchive;
    }

    public AlleleParser getAlleleParser() {
        return alleleParser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlleleResource that = (AlleleResource) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(alleleArchive, that.alleleArchive) &&
                Objects.equals(alleleParser, that.alleleParser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, alleleArchive, alleleParser);
    }

    @Override
    public String toString() {
        return "AlleleResource{" +
                "name='" + name + '\'' +
                ", alleleArchive=" + alleleArchive +
                ", alleleParser=" + alleleParser +
                '}';
    }
}
