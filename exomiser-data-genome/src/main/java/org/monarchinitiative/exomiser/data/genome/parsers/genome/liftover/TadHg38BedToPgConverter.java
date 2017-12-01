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

import org.monarchinitiative.exomiser.data.genome.parsers.ChromosomeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringJoiner;

/**
 * Cleans-up the output of hg19_tad.bed from the ENSEMBL Assembly Converter tool.
 * <p>
 * http://www.ensembl.org/Homo_sapiens/Tools/AssemblyConverter?db=core
 * <p>
 * This was used in a one-off conversion of the src/test/resources/static/hg19_tad.bed to hg38 coordinates. The output
 * files are now present as static data in the src/main/resources directory.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TadHg38BedToPgConverter {

    private static final Logger logger = LoggerFactory.getLogger(TadHg38BedToPgConverter.class);


    private final Path inFile;
    private final Path outFile;

    public TadHg38BedToPgConverter(Path inFile, Path outFile) {
        this.inFile = inFile;
        this.outFile = outFile;
    }

    public void run() {

        try (BufferedReader bufferedReader = Files.newBufferedReader(inFile);
             BufferedWriter bufferedWriter = Files.newBufferedWriter(outFile)) {
            //read the first line
            String line = bufferedReader.readLine();
            TadLine tadLine = parseLine(line);
            //read the rest
            while ((line = bufferedReader.readLine()) != null) {
                TadLine currentTadLine = parseLine(line);
                if (tadLine.geneId.equals(currentTadLine.geneId)) {
                    //find the min and max of the start and end of the TAD
                    tadLine.addStart(currentTadLine.start);
                    tadLine.addEnd(currentTadLine.end);
                } else {
                    //got a new gene so..
                    //write the old tadLine
                    bufferedWriter.write(tadLine.writePg());
                    bufferedWriter.newLine();
                    //set the current tadLine to be the tadLine
                    tadLine = currentTadLine;
                }

            }
            //write the last one
            bufferedWriter.write(tadLine.writePg());
        } catch (IOException e) {
            logger.error("Unable to process file", e);
        }

    }

    private TadLine parseLine(String line) {
        String[] tokens = line.split("\t");
        String chr = tokens[0];
        int start = Integer.parseInt(tokens[1]);
        int end = Integer.parseInt(tokens[2]);
        String geneId = tokens[3];
        String geneSymbol = tokens[4];
        return new TadLine(chr, start, end, geneId, geneSymbol);
    }

    private class TadLine {
        byte chr;
        int start;
        int end;
        String geneId;
        String geneSymbol;

        public TadLine(String chr, int start, int end, String geneId, String geneSymbol) {
            this.chr = ChromosomeParser.parseChr(chr);
            this.start = start;
            this.end = end;
            this.geneId = geneId;
            this.geneSymbol = geneSymbol;
        }

        void addStart(int start) {
            this.start = Math.min(this.start, start);
        }

        void addEnd(int end) {
            this.end = Math.max(this.end, end);
        }

        String writePg() {
            StringJoiner stringJoiner = new StringJoiner("|");
            stringJoiner.add(Integer.toString(chr));
            stringJoiner.add(Integer.toString(start));
            stringJoiner.add(Integer.toString(end));
            stringJoiner.add(geneId);
            stringJoiner.add(geneSymbol);
            return stringJoiner.toString();
        }
    }
}
