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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.exomiser.core.proto.JannovarProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarDataProtoSerialiser {

    private static final Logger logger = LoggerFactory.getLogger(JannovarDataProtoSerialiser.class);

    private static final byte[] MAGIC_BYTES = {'J', 'T', 'P', 'B'};

    private JannovarDataProtoSerialiser() {
        //un-instantiable utility class
    }

    public static void save(Path outFilePath, JannovarData jannovarData) {
        logger.info("Serialising Jannovar data to {}", outFilePath);

        try (OutputStream outputStream = Files.newOutputStream(outFilePath)) {
            outputStream.write(MAGIC_BYTES);
            outputStream.flush();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
                JannovarProto.JannovarData protoJannovarData = JannovarProtoConverter.toJannovarProto(jannovarData);
                protoJannovarData.writeTo(gzipOutputStream);
            }
        } catch (Exception e) {
            logger.error("Unable to save file {}", outFilePath, e);
        }
        logger.info("Done");
    }

    public static JannovarData load(Path jannovarProtoPath) {
        logger.info("Deserialising Jannovar data from {}", jannovarProtoPath);
        Instant start = Instant.now();
        try (InputStream inputStream = Files.newInputStream(jannovarProtoPath)) {
            byte[] bytes = new byte[4];
            int bytesRead = inputStream.read(bytes);
            if (!Arrays.equals(bytes, MAGIC_BYTES) || bytesRead != bytes.length) {
                throw new InvalidFileFormatException(jannovarProtoPath + " not an Exomiser format Jannovar transcript database.");
            }
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
                JannovarProto.JannovarData protoJannovarData = JannovarProto.JannovarData.parseFrom(gzipInputStream);
                logger.info("Deserialisation took {} sec.", Duration.between(start, Instant.now()).toMillis() / 1000f);
                return JannovarProtoConverter.toJannovarData(protoJannovarData);
            }
        } catch (IOException e) {
            logger.error("Unable to deserialise data", e);
            throw new JannovarDataSerializerException(e);
        }
    }

    protected static class JannovarDataSerializerException extends RuntimeException {

        private JannovarDataSerializerException(Throwable cause) {
            super(cause);
        }

    }
}
