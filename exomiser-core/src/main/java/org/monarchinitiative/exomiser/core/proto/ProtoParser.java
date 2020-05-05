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

package org.monarchinitiative.exomiser.core.proto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ProtoParser {

    private static final Logger logger = LoggerFactory.getLogger(ProtoParser.class);

    private ProtoParser() {
        // uninstantiable - static utility class
    }

    public static <U extends Message.Builder> U parseFromJsonOrYaml(U protoBuilder, Path path) {
        try (Reader fileReader = Files.newBufferedReader(path)) {
            return parseJson(protoBuilder, fileReader);
        } catch (IOException e) {
            logger.debug("Failed parsing data as JSON from file {}", path, e);
        }

        try (Reader fileReader = Files.newBufferedReader(path)) {
            return parseYaml(protoBuilder, fileReader);
        } catch (IOException e) {
            throw new ProtoParserException("Unable to parse file " + path, e);
        }
    }

    public static <U extends Message.Builder> U parseFromJsonOrYaml(U protoBuilder, String inputString) {
        try (Reader stringReader = new StringReader(inputString)) {
            return parseJson(protoBuilder, stringReader);
        } catch (IOException e) {
            logger.debug("Failed parsing data as JSON string", e);
        }

        try (Reader stringReader = new StringReader(inputString)) {
            return parseYaml(protoBuilder, stringReader);
        } catch (IOException e) {
            throw new ProtoParserException("Unable to parse input string", e);
        }
    }

    private static <U extends Message.Builder> U parseYaml(U protoBuilder, Reader reader) throws IOException {
        JsonNode tree = new YAMLMapper().readTree(reader);
        String jsonString = new ObjectMapper().writeValueAsString(tree);
        JsonFormat.parser()
                // should we be permissive or not?
                .ignoringUnknownFields()
                .merge(jsonString, protoBuilder);
        return protoBuilder;
    }

    private static <U extends Message.Builder> U parseJson(U protoBuilder, Reader reader) throws IOException {
        JsonFormat.parser()
                .ignoringUnknownFields()
                .merge(reader, protoBuilder);
        return protoBuilder;
    }

    public static class ProtoParserException extends RuntimeException {
        public ProtoParserException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
