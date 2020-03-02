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

package org.monarchinitiative.exomiser.core.genome;

import de.charite.compbio.jannovar.reference.TranscriptModel;

/**
 * Utility class for safely extracting data from Jannovar {@link TranscriptModel} objects.
 */
final class TranscriptModelUtil {

    private TranscriptModelUtil() {
    }

    static String getTranscriptAccession(TranscriptModel transcriptModel) {
        if (transcriptModel == null) {
            return "";
        }
        return transcriptModel.getAccession();
    }

    static String getTranscriptGeneId(TranscriptModel transcriptModel) {
        if (transcriptModel == null || transcriptModel.getGeneID() == null) {
            return "";
        }
        //this will now return the id from the user-specified data source. Previously would only return the Entrez id.
        return transcriptModel.getGeneID();
    }

    static String getTranscriptGeneSymbol(TranscriptModel transcriptModel) {
        if (transcriptModel == null || transcriptModel.getGeneSymbol() == null) {
            return ".";
        } else {
            return transcriptModel.getGeneSymbol();
        }
    }
}