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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.exomiser.autoconfigure.ExomiserAutoConfigurationException;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataProtoSerialiser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarDataSourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(JannovarDataSourceLoader.class);

    private JannovarDataSourceLoader() {
        //static utility class
    }

    public static JannovarData loadJannovarData(Path transcriptFilePath){
        //first try loading the file using the new protobuf-based format (larger file-size, but faster load-time)
        try {
            return JannovarDataProtoSerialiser.load(transcriptFilePath);
        } catch (Exception e) {
            logger.warn("Unable to load Jannovar data - {}", e.getMessage());
            logger.warn("Incorrect Jannovar data format? Will try the old version...");
        }
        try {
            // if we've got here, try again using the original java serializable-based format (small file-size, but slower load-time)
            return new JannovarDataSerializer(transcriptFilePath.toString()).load();
        } catch (SerializationException e) {
            throw new ExomiserAutoConfigurationException("Could not load Jannovar data from " + transcriptFilePath, e);
        }
    }
}
