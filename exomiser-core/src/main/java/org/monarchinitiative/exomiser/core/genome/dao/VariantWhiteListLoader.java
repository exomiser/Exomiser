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

package org.monarchinitiative.exomiser.core.genome.dao;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * Utility class for loading variant whitelist.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class VariantWhiteListLoader {

    private static final Logger logger = LoggerFactory.getLogger(VariantWhiteListLoader.class);

    private VariantWhiteListLoader() {
        // uninstantiable static utility class
    }

    /**
     * Loads variant whitelist data from the given path into an in-memory instance of a {@link VariantWhiteList}. It is
     * assumed that the whitelist will only contain a few tens of thousand variants.
     * <p>
     * The whitelist should be a gzipped TSV file containing a single allele on each line where the fields are as follows:
     * #CHR    POS REF ALT
     * 1    12345   A   T
     * <p>
     * It is permissible to add further fields, but they will be ignored.
     *
     * @param whiteListPath {@link Path) to the variant whitelist .gz file.
     * @return An in-memory instance of the {@link VariantWhiteList}
     */
    public static VariantWhiteList loadVariantWhiteList(Path whiteListPath) {
        Objects.requireNonNull(whiteListPath);
        logger.info("Loading variant whitelist from: {}", whiteListPath);
        // this should be a tabix-indexed gzip file
        ImmutableSet.Builder<AlleleProto.AlleleKey> whiteListBuilder = new ImmutableSet.Builder<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(whiteListPath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    // comment line
                    continue;
                }
                String[] tokens = line.split("\t");
                if (tokens.length < 4) {
                    logger.error("Error parsing variant whitelist. Require minimum 4 tokens, but got {} in line '{}'", tokens.length, line);
                    continue;
                }
                // Exomiser - simple VCF format
                AlleleProto.AlleleKey alleleKey = AlleleProto.AlleleKey.newBuilder()
                        .setChr(Contigs.parseId(tokens[0]))
                        .setPosition(Integer.parseInt(tokens[1]))
                        .setRef(tokens[2])
                        .setAlt(tokens[3])
                        .build();
                whiteListBuilder.add(alleleKey);
            }
        } catch (IOException e) {
            logger.error("AAARRRGH!", e);
            throw new RuntimeException("Unable to load variant whitelist", e);
        }

        ImmutableSet<AlleleProto.AlleleKey> whiteList = whiteListBuilder.build();
        logger.info("Loaded {} variants into whitelist", whiteList.size());
        return InMemoryVariantWhiteList.of(whiteList);
    }
}
