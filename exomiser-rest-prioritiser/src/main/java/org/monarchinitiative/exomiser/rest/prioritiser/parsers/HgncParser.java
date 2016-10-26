package org.monarchinitiative.exomiser.rest.prioritiser.parsers;

import org.monarchinitiative.exomiser.core.model.HumanGeneIdentifier;
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

import static java.util.stream.Collectors.toList;
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

    public Stream<HumanGeneIdentifier> parseGeneIdentifiers() {
        return parseGeneIdentifiers(hgncTxt);
    }

    private Map<String, Integer> makeColumnIndex(Path hgncCompleteSetTxtPath) {
        List<String> columnHeaders = streamFile(hgncCompleteSetTxtPath)
                .limit(1)
                .flatMap(line -> Arrays.stream(line.split("\t")))
                .collect(toList());

        return IntStream.range(0, columnHeaders.size())
                .boxed()
                .collect(toMap(i -> columnHeaders.get(i), Function.identity()));

    }

    private Stream<String> streamFile(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException e) {
            logger.error("Unable to process file {}", path, e);
        }
        return Stream.empty();
    }

    private Stream<HumanGeneIdentifier> parseGeneIdentifiers(Path hgncCompleteSetTxtPath) {
        return streamFile(hgncCompleteSetTxtPath).skip(1).map(parseGeneIdentifier());
    }

    private Function<String, HumanGeneIdentifier> parseGeneIdentifier() {
        return line -> {
//            System.out.println(line);
            String[] tokens = line.split("\t");

            if ("Entry Withdrawn".equals(getField(tokens, "status"))) {
                return HumanGeneIdentifier.builder()
                        .hgncId(getField(tokens, "hgnc_id"))
                        .geneSymbol(getField(tokens, "symbol"))
                        .withdrawn(true)
                        .build();
            }

            return HumanGeneIdentifier.builder()
                    .hgncId(getField(tokens, "hgnc_id"))
                    .geneSymbol(getField(tokens, "symbol"))
                    .geneName(getField(tokens, "name"))
                    .locusGroup(getField(tokens, "locus_group"))
                    .locusType(getField(tokens, "locus_type"))
                    .location(getField(tokens, "location"))
                    .entrezId(getField(tokens, "entrez_id"))
                    .ensemblId(getField(tokens, "ensembl_gene_id"))
                    .ucscId(getField(tokens, "ucsc_id"))
                    .build();
        };
    }

    private String getField(String[] tokens, String fieldName) {
        int index = columnIndex.getOrDefault(fieldName, Integer.MAX_VALUE - 1);
        if (tokens.length < index + 1) {
            return HumanGeneIdentifier.EMPTY_FIELD;
        }
        return tokens[index];
    }

}
