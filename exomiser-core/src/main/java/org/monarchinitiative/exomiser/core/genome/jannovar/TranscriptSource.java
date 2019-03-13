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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import de.charite.compbio.jannovar.data.JannovarData;

import java.util.Arrays;
import java.util.Objects;

/**
 * Enum representing the supported transcript sources in the {@link JannovarData} files.
 *
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum TranscriptSource {

    ENSEMBL("ensembl"),
    REFSEQ("refseq"),
    UCSC("ucsc");

    private final String value;

    TranscriptSource(String value) {
        this.value = value;
    }

    public static TranscriptSource parseValue(String value) {
        Objects.requireNonNull(value, "Transcript source cannot be null");
        switch (value.toLowerCase()) {
            case "ensembl":
                return ENSEMBL;
            case "refseq":
                return REFSEQ;
            case "ucsc":
                return UCSC;
            default:
                String message = String.format("'%s' is not a valid/supported transcript source. Valid sources are: %s", value, Arrays
                        .asList(TranscriptSource.values()));
                throw new InvalidTranscriptSourceException(message);
        }
    }

    @Override
    public String toString() {
        return value;
    }

    private static class InvalidTranscriptSourceException extends RuntimeException {

        private InvalidTranscriptSourceException(String message) {
            super(message);
        }

    }
}
