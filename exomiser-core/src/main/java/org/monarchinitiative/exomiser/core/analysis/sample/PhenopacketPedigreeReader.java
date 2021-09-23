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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.phenopackets.schema.v1.core.Pedigree;
import org.phenopackets.schema.v1.core.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for reading a PED file into a {@link Pedigree} object.
 */
public class PhenopacketPedigreeReader {

    private static final Logger logger = LoggerFactory.getLogger(PhenopacketPedigreeReader.class);

    private PhenopacketPedigreeReader() {
        //empty
    }

    public static Pedigree readPedFile(Path pedPath) {
        try (Stream<String> lines = Files.lines(pedPath)) {
            return parsePedigree(lines);
        } catch (Exception e) {
            throw new PedigreeReaderException(String.format("Error reading PED file %s", pedPath), e);
        }
    }

    public static Pedigree parsePedigree(Stream<String> lines) {
        Pedigree.Builder pedigreeBuilder = Pedigree.newBuilder();
        Set<Pedigree.Person> individuals = lines
                .filter(line -> !line.startsWith("#"))
                .map(toPerson())
                .collect(Collectors.toUnmodifiableSet());
        return pedigreeBuilder.addAllPersons(individuals).build();
    }

    /**
     * Based on this:
     * https://software.broadinstitute.org/gatk/documentation/article?id=11016
     */
    private static Function<String, Pedigree.Person> toPerson() {
        //    #Family_ID	Individual_ID	Paternal_ID	Maternal_ID	sex	Phenotype
        //    1	Eva	0	0	2	1
        //    1	Adam	0	0	1	1
        //    1	Seth	Adam	Eva	1	2
        //Strictly this should be a tab delimited format, but there have been instances where users input spaces.
        return line -> {
            String[] tokens = line.split("\\s+");
            if (tokens.length < 6) {
                throw new PedigreeReaderParseException(String.format("PED file must have at least 6 fields - found %d in line '%s'", tokens.length, line));
            }
            String familyId = tokens[0];
            String id = parseId(tokens[1]);
            String fatherId = parseParentId(tokens[2]);
            String motherId = parseParentId(tokens[3]);

            Sex sex = parseSex(tokens[4]);
            Pedigree.Person.AffectedStatus status = parseStatus(tokens[5]);
            return Pedigree.Person.newBuilder()
                    .setFamilyId(familyId)
                    .setIndividualId(id)
                    .setPaternalId(fatherId)
                    .setMaternalId(motherId)
                    .setSex(sex)
                    .setAffectedStatus(status)
                    .build();
        };
    }

    private static String parseId(String token) {
        if (token.isEmpty()) {
            throw new PedigreeReaderParseException("Individual id cannot be empty");
        }
        return token;
    }

    private static String parseParentId(String token) {
        if (token.isEmpty()) {
            throw new PedigreeReaderParseException("Parent id cannot be empty");
        }
        if ("0".equals(token)) {
            // despite empty not being a legal token in the PED file, it's ok for the us.
            return "";
        }
        return token;
    }

    private static Sex parseSex(String token) {
        switch (token) {
            case "1":
                return Sex.MALE;
            case "2":
                return Sex.FEMALE;
            default:
                return Sex.UNKNOWN_SEX;
        }
    }

    private static Pedigree.Person.AffectedStatus parseStatus(String token) {
        switch (token) {
            case "-9":
            case "0":
                return Pedigree.Person.AffectedStatus.MISSING;
            case "1":
                return Pedigree.Person.AffectedStatus.UNAFFECTED;
            case "2":
                return Pedigree.Person.AffectedStatus.AFFECTED;
            default:
                throw new PedigreeReaderParseException(String.format("Individual status must be one of -9 0, 1, 2. Found '%s'", token));
        }
    }


    private static class PedigreeReaderException extends RuntimeException {

        private PedigreeReaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class PedigreeReaderParseException extends RuntimeException {

        private PedigreeReaderParseException(String message) {
            super(message);
        }
    }
}
