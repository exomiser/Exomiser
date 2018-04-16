/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Class for Parsing/Streaming files in BED format. Caution: The start and end coordinates are expected to be 0-based as
 * per the BED spec, however VCF is 1-based. This class will convert from 0 to 1-based intervals.
 *
 * See https://genome.ucsc.edu/FAQ/FAQformat.html#format1
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.1.0
 */
public class BedFiles {

    private static final Logger logger = LoggerFactory.getLogger(BedFiles.class);

    private static final ReferenceDictionary referenceDictionary = HG19RefDictBuilder.build();

    private BedFiles() {
    }

    /**
     * Parses the argument file into {@link ChromosomalRegion}. This is a super-simple implementation which will only
     * take into account the first three columns of the file (mandatory according to the spec) and ignore anything after
     * that. Consequently, THE STRAND INFORMATION, IF SUPPLIED, WILL BE IGNORED.
     *
     * @param bedPath path to the BED formatted file.
     * @return a stream of {@code ChromosomalRegion} parsed from the file.
     */
    public static Stream<ChromosomalRegion> readChromosomalRegions(Path bedPath) {
        try {
            return Files.lines(bedPath)
                    .filter(nonHeaderLine())
                    .map(toChromosomalRegion());
        } catch (IOException ex) {
            throw new BedFileParseException("Unable to read file "  + bedPath, ex);
        }
    }

    private static Predicate<String> nonHeaderLine() {
        return line -> !line.startsWith("#") && !line.startsWith("browser") && !line.startsWith("track");
    }

    private static Function<String, ChromosomalRegion> toChromosomalRegion() {
        return line -> {
                String[] tokens = line.split("\t");
                if (tokens.length < 3) {
                    throw new BedFileParseException("BED file requires at least 3 columns invalid line: '" + line + "'");
                }
                if (tokens.length > 3) {
                    logger.warn("Line contains more than 3 columns - ignoring optional columns 4+. Therefore STRAND will all be +");
                }
                int chr = referenceDictionary.getContigNameToID().get(tokens[0]);
                //BED format is 0-based - we use 1-based in the exomiser.
                int start = Integer.parseInt(tokens[1]) + 1;
                int end = Integer.parseInt(tokens[2]);
                return new GeneticInterval(chr, start, end);
            };
    }

    private static class BedFileParseException extends RuntimeException {
        public BedFileParseException(String message, Exception ex) {
            super();
        }

        public BedFileParseException(String message) {
            super(message);
        }

        public BedFileParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public BedFileParseException(Throwable cause) {
            super(cause);
        }
    }
}
