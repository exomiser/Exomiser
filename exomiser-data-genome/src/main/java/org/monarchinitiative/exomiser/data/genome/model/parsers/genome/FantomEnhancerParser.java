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

package org.monarchinitiative.exomiser.data.genome.model.parsers.genome;

import org.monarchinitiative.exomiser.data.genome.model.parsers.ChromosomeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FantomEnhancerParser {

    private static final Logger logger = LoggerFactory.getLogger(FantomEnhancerParser.class);

    private final Path dataPath;
    private final Path outputPath;

    //http://www.genome.ucsc.edu/FAQ/FAQformat.html#format1
    //BED format header
    //#chrom  chromStart  chromEnd    name    score   strand  thickStart  thickEnd    itemRgb blockCount  blockSizes  blockStarts
    //the 'name' column corresponds to the value in column 0 of the fantom
    public FantomEnhancerParser(Path dataPath, Path outputPath) {
        this.dataPath = dataPath;
        this.outputPath = outputPath;
    }

    public void parse() {

        Path inFile = dataPath;//.resolve("fantom_enhancers.bed");
        Path outFile = outputPath;//.resolve("fantom_enhancers.pg");
        //optional - we're not using the tissueIndexFile ("fantom_permissive_enhancer_tissue_index.tsv.gz")for the time being
        //format is line 0 = header tissue name
        //row col 0 = index value corresponding to col 3 in the bed file
        //row col 1- end = either "1" or "0" denoting whether the region is expressed in the tissue indicated in the header

        logger.info("Parsing Fantom enhancers BED file: {} Writing out to: {}", inFile, outFile);
        try (BufferedReader reader = Files.newBufferedReader(inFile);
             BufferedWriter bedFileWriter = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {

            //parse the remainder of the file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineParts = line.split("\t");
                //chr	start	end	index
                //13	111172874	111173635	11176
                //Y	59019812	59020026	43011
                String chr = parseChr(lineParts[0]);
                String start = lineParts[1];
                String end = lineParts[2];

                bedFileWriter.write(generateLine(chr, start, end));
                bedFileWriter.newLine();

            }

        } catch (IOException ex) {
            logger.error(null, ex);
        }
        logger.info("Done");
    }

    private String parseChr(String chr) {
        byte chrByte = ChromosomeParser.parseChr(chr);
        return Integer.toString(chrByte);
    }

    private String generateLine(String chr, String start, String end) {
        StringJoiner stringJoiner = new StringJoiner("|");
        stringJoiner.add(chr);
        stringJoiner.add(start);
        stringJoiner.add(end);
        stringJoiner.add("FANTOM permissive");
        return stringJoiner.toString();
    }
}
