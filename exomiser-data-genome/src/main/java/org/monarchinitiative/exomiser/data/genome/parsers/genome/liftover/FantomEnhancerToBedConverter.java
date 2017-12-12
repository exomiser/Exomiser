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

package org.monarchinitiative.exomiser.data.genome.parsers.genome.liftover;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * One-off class used for parsing the FANTOM enhancer usage file and converting to BED format for manual assembly conversion using the
 * Ensembl Assembly Converter tool - http://www.ensembl.org/Homo_sapiens/Tools/AssemblyConverter?db=core
 * <p>
 * The output files are now present as static data in the src/main/resources directory.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FantomEnhancerToBedConverter {

    private static final Logger logger = LoggerFactory.getLogger(FantomEnhancerToBedConverter.class);

    private final Path dataPath;
    private final Path outputPath;

//    # FANTOM
//    fantom.url=http://enhancer.binf.ku.dk/presets/
//    fantom.remoteFile=hg19_permissive_enhancer_usage.csv.gz
//    fantom.version=
//    fantom.extractedName=hg19_permissive_enhancer_usage.csv
//    fantom.extractScheme=gz
//    fantom.parsedName=fantom.pg

    //http://www.genome.ucsc.edu/FAQ/FAQformat.html#format1
    //BED format header
    //#chrom  chromStart  chromEnd    name    score   strand  thickStart  thickEnd    itemRgb blockCount  blockSizes  blockStarts
    //http://fantom.gsc.riken.jp/5/datafiles/latest/extra/Enhancers/human_permissive_enhancers_phase_1_and_2.bed.gz

    public FantomEnhancerToBedConverter(Path dataPath, Path outputPath) {
        this.dataPath = dataPath;
        this.outputPath = outputPath;
    }

    public void download() {
        String urlString = "http://enhancer.binf.ku.dk/presets/hg19_permissive_enhancer_usage.csv.gz";
        Path destination = outputPath.resolve("hg19_permissive_enhancer_usage.csv.gz");
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

    public void run() {

        Path inFile = dataPath.resolve("hg19_permissive_enhancer_usage.csv.gz");
        Path outFile = outputPath.resolve("hg19_fantom_permissive_enhancer_usage.bed");
        Path tissueIndexFile = outputPath.resolve("fantom_permissive_enhancer_tissue_index.tsv.gz");

        logger.info("Parsing Fantom enhancers file: {} Writing out to: {}", inFile, outFile);
        try (BufferedReader reader = bufferedGZipReader(inFile);
             BufferedWriter bedFileWriter = Files.newBufferedWriter(outFile, Charset.defaultCharset());
             BufferedWriter tissueIndexWriter = bufferedGZipWriter(tissueIndexFile)) {

            // read in array of tissues from header
            String header = reader.readLine();
            String tissueHeader = header.replaceAll(",", "\t");
            tissueIndexWriter.write(tissueHeader);
            tissueIndexWriter.newLine();

            //parse the remainder of the file
            String line;
            int tissueIndex = 0;
            while ((line = reader.readLine()) != null) {
                tissueIndex++;
                //"chr13:111172874-111173635",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,1,1,0,0,0,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
                line = line.replaceAll("\n", "");// ? strip off newline first
                String[] lineParts = line.split(",");

                tissueIndexWriter.write(generateIndexLine(tissueIndex, lineParts));
                tissueIndexWriter.newLine();

                //"chr13:111172874-111173635" -> 13\t111172874\t111173635
                String chromosomalRegion = lineParts[0].replaceAll("\"", "");
                String[] chrRegionParts = chromosomalRegion.split(":");
                String chr = chrRegionParts[0].substring(3);
                String[] startEnd = chrRegionParts[1].split("-");
                String start = startEnd[0];
                String end = startEnd[1];

                if (tissueIsExpressed(lineParts)) {
                    //we're just checking that this region was expressed in at least one tissue type.
                    bedFileWriter.write(generateLine(chr, start, end, tissueIndex));
                    bedFileWriter.newLine();
                }
            }

        } catch (IOException ex) {
            logger.error(null, ex);
        }
        logger.info("Done");
    }

    private boolean tissueIsExpressed(String[] lineParts) {
        // now iterate through the remaining tokens - these represent the presence/absence of a tissue specific
        // enhancer in the chromosomal region we just parsed
        for (int i = 1; i < lineParts.length; i++) {
            String tissueFlag = lineParts[i];
            if (tissueFlag.equals("1")) {
                return true;
                // if want to reinstate tissue-specific chrRegionParts in table then add in the tissue by looking up the
                // type from the tissues in the header array at array[i] where the tissueFlag equals '1'
            }
        }
        return false;
    }

    private String generateIndexLine(int tissueIndex, String[] lineParts) {
        StringJoiner stringJoiner = new StringJoiner("\t");
        stringJoiner.add(Integer.toString(tissueIndex));
        for (int i = 1; i < lineParts.length; i++) {
            stringJoiner.add(lineParts[i]);
        }
        return stringJoiner.toString();
    }

    private BufferedReader bufferedGZipReader(Path inFile) throws IOException {
        GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(inFile));
        return new BufferedReader(new InputStreamReader(gzipInputStream));
    }

    private BufferedWriter bufferedGZipWriter(Path outFile) throws IOException {
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(Files.newOutputStream(outFile));
        return new BufferedWriter(new OutputStreamWriter(gzipOutputStream));
    }

    private String generateLine(String chr, String start, String end, int tissueIndex) {
        StringJoiner stringJoiner = new StringJoiner("\t");
        stringJoiner.add(chr);
        stringJoiner.add(start);
        stringJoiner.add(end);
        stringJoiner.add(Integer.toString(tissueIndex));
        return stringJoiner.toString();
    }
}
