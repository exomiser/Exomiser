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

package org.monarchinitiative.exomiser.rest.prioritiser.parsers;

import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;


/**
 * Parser for HGNC complete data set file. Requires a copy of the txt file from the
 * 'Complete HGNC dataset'.
 *
 * @link http://www.genenames.org/cgi-bin/statistics
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HgncParser {

    private static final Logger logger = LoggerFactory.getLogger(HgncParser.class);

    private final Path hgncTxt;
    private final Map<String, Integer> columnIndex;

    public HgncParser(Path hgncCompleteSetTxtPath) {
        hgncTxt = hgncCompleteSetTxtPath;
        columnIndex = makeColumnIndex(hgncTxt);
    }

    public Stream<GeneIdentifier> parseGeneIdentifiers() {
        return parseGeneIdentifiers(hgncTxt);
    }

    private Map<String, Integer> makeColumnIndex(Path hgncCompleteSetTxtPath) {
        List<String> columnHeaders = streamFile(hgncCompleteSetTxtPath)
                .limit(1)
                .flatMap(line -> Arrays.stream(line.split("\t")))
                .toList();

        return IntStream.range(0, columnHeaders.size())
                .boxed()
                .collect(toMap(columnHeaders::get, Function.identity()));

    }

    private Stream<String> streamFile(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            logger.error("Unable to process file {}", path, e);
        }
        return Stream.empty();
    }

    private Stream<GeneIdentifier> parseGeneIdentifiers(Path hgncCompleteSetTxtPath) {
        return streamFile(hgncCompleteSetTxtPath).skip(1).map(parseGeneIdentifier());
    }

    private Function<String, GeneIdentifier> parseGeneIdentifier() {
        return line -> {
//            System.out.println(line);
            String[] tokens = line.split("\t");

            if ("Entry Withdrawn".equals(getField(tokens, "status"))) {
                return GeneIdentifier.builder()
                        .hgncId(getField(tokens, "hgnc_id"))
                        .geneSymbol(getField(tokens, "symbol"))
                        .build();
            }

            return GeneIdentifier.builder()
                    .geneId(getField(tokens, "entrez_id"))
                    .geneSymbol(getField(tokens, "symbol"))
                    .hgncId(getField(tokens, "hgnc_id"))
                    .hgncSymbol(getField(tokens, "name"))
                    .entrezId(getField(tokens, "entrez_id"))
                    .ensemblId(getField(tokens, "ensembl_gene_id"))
                    .ucscId(getField(tokens, "ucsc_id"))
                    .build();
        };
    }

    private String getField(String[] tokens, String fieldName) {
        int index = columnIndex.getOrDefault(fieldName, Integer.MAX_VALUE - 1);
        if (tokens.length < index + 1) {
            return GeneIdentifier.EMPTY_FIELD;
        }
        return tokens[index];
    }

}
