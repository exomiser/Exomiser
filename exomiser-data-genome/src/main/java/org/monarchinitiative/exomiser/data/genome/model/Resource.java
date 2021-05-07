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

package org.monarchinitiative.exomiser.data.genome.model;

import org.monarchinitiative.exomiser.data.genome.model.archive.Archive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;

import java.net.URL;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Resource<T> {

    String getName();

    URL getResourceUrl();

    Archive getArchive();

    Parser<T> getParser();

    default Stream<T> parseResource() {
        // wrap this in a try-with-resources to close the underlying file resources when the stream closes
        try (Stream<String> lines = getArchive().lines()) {
            return lines
//                       .peek(System.out::println)
                    .flatMap(line -> getParser().parseLine(line).stream());
        }
    }
}
