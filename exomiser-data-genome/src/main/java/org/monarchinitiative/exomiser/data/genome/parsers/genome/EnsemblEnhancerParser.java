/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.parsers.genome;

import org.apache.commons.io.FileUtils;
import org.monarchinitiative.exomiser.data.genome.parsers.ChromosomeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EnsemblEnhancerParser {

    private static final Logger logger = LoggerFactory.getLogger(EnsemblEnhancerParser.class);

//    # Ensembl enhancers
//    ensembl_enhancers.url=http://grch37.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%221%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_regulatory_feature%22%20interface%20=%20%22default%22%20%3E%3CFilter%20name%20=%20%22regulatory_feature_type_name%22%20value%20=%20%22Enhancer%22/%3E%3CAttribute%20name%20=%20%22chromosome_name%22%20/%3E%3CAttribute%20name%20=%20%22chromosome_start%22%20/%3E%3CAttribute%20name%20=%20%22chromosome_end%22%20/%3E%3CAttribute%20name%20=%20%22feature_type_name%22%20/%3E%3C/Dataset%3E%3C/
//            #string2entrez.url=http://www.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%220%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_gene_ensembl%22%20interface%20=%20%22default%22%20%3E%20%3CAttribute%20name%20=%20%22ensembl_peptide_id%22%20/%3E%20%3CAttribute%20name%20=%20%22entrezgene%22%20/%3E%20%3CAttribute%20name%20=%20%22hgnc_symbol%22%20/%3E%20%3C/Dataset%3E%20%3C/
//
//    ensembl_enhancers.remoteFile=Query%3E
//    ensembl_enhancers.version=
//    ensembl_enhancers.extractedName=ensembl_enhancers.tsv
//    ensembl_enhancers.extractScheme=copy
//    ensembl_enhancers.parsedName=ensembl_enhancers.pg
//

    private final Path dataPath;
    private final Path outputPath;

    public EnsemblEnhancerParser(Path dataPath, Path outputPath) {
        this.dataPath = dataPath;
        this.outputPath = outputPath;
    }

    //TODO: fix the Assembly
    public void download() {
//        String urlString = "http://grch37.ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%221%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_regulatory_feature%22%20interface%20=%20%22default%22%20%3E%3CFilter%20name%20=%20%22regulatory_feature_type_name%22%20value%20=%20%22Enhancer%22/%3E%3CAttribute%20name%20=%20%22chromosome_name%22%20/%3E%3CAttribute%20name%20=%20%22chromosome_start%22%20/%3E%3CAttribute%20name%20=%20%22chromosome_end%22%20/%3E%3CAttribute%20name%20=%20%22feature_type_name%22%20/%3E%3C/Dataset%3E%3C/Query%3E";
        String urlString = "http://ensembl.org/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%20%3C!DOCTYPE%20Query%3E%20%3CQuery%20%20virtualSchemaName%20=%20%22default%22%20formatter%20=%20%22TSV%22%20header%20=%20%220%22%20uniqueRows%20=%20%221%22%20count%20=%20%22%22%20datasetConfigVersion%20=%20%220.6%22%20%3E%20%20%3CDataset%20name%20=%20%22hsapiens_regulatory_feature%22%20interface%20=%20%22default%22%20%3E%3CFilter%20name%20=%20%22regulatory_feature_type_name%22%20value%20=%20%22Enhancer%22/%3E%3CAttribute%20name%20=%20%22chromosome_name%22%20/%3E%3CAttribute%20name%20=%20%22chromosome_start%22%20/%3E%3CAttribute%20name%20=%20%22chromosome_end%22%20/%3E%3CAttribute%20name%20=%20%22feature_type_name%22%20/%3E%3C/Dataset%3E%3C/Query%3E";
        Path destination = outputPath.resolve("ensembl_enhancers.tsv");
        downloadResource(urlString, destination);
    }

    private void downloadResource(String urlString, Path destination) {
        try {
            URL source = new URL(urlString);
            logger.info("Downloading resource from: {}", source);

            FileUtils.copyURLToFile(source, destination.toFile(), 2500, 15000);

        } catch (IOException ex) {
            logger.error("Unable to download resource {} to {}", urlString, destination, ex);
        }
    }

    public void parse() {

        Path inFile = dataPath; //.resolve("ensembl_enhancers.tsv");
        Path outFile = outputPath; //.resolve("ensembl_enhancers.pg");


        logger.info("Parsing Ensembl enhancers file: {} Writing out to: {}", inFile, outFile);

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset());
             Stream<String> lines = Files.lines(inFile)) {
            lines
                    .filter(unplacedFragments())
                    .map(tabsToPipe())
                    .map(xTo23())
                    .map(yTo24())
                    .forEach(writeLineToOutFile(writer));

        } catch (IOException ex) {
            logger.error(null, ex);
        }
        logger.info("Done");
    }

    //1	35804401	35805200	Enhancer
    //KI270721.1	19801	20000	Enhancer
    //GL000195.1	65800	66000	Enhancer
    //X	11100201	11101001	Enhancer
    private Predicate<String> unplacedFragments() {
        return line -> {
            int firstTab = line.indexOf('\t');
            String chr = line.substring(0, firstTab);
            return ChromosomeParser.parseChr(chr) != (byte) 0;
        };
    }

    private Function<String, String> tabsToPipe() {
        return line -> line.replaceAll("\t", "|");
    }

    private Function<String, String> xTo23() {
        return line -> line.replace("X|", "23|");
    }

    private Function<String, String> yTo24() {
        return line -> line.replace("Y|", "24|");
    }

    private Consumer<String> writeLineToOutFile(BufferedWriter writer) {
        return line -> {
            try {
                writer.write(line);
                writer.newLine();
            } catch (IOException ex) {
                logger.error("Unable to write {}", line, ex);
            }
        };
    }
}
