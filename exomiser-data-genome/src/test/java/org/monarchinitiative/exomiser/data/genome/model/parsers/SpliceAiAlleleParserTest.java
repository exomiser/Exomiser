package org.monarchinitiative.exomiser.data.genome.model.parsers;

import htsjdk.samtools.util.BlockCompressedOutputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SpliceAiAlleleParserTest {

    @Test
    void parseNonSexChrLine() {
        String line = "10\t12345\tA\tC\t1.0";
        List<Allele> result = new SpliceAiAlleleParser().parseLine(line);
        Allele expected = new Allele(10, 12345, "A", "C");
        expected.addPathogenicityScore(AlleleProto.PathogenicityScore.newBuilder().setPathogenicitySource(AlleleProto.PathogenicitySource.SPLICE_AI).setScore(1f).build());
        assertThat(result, equalTo(List.of(expected)));
    }

    @Test
    void parseSexChrLine() {
        String line = "X\t12345\tA\tC\t1.0";
        List<Allele> result = new SpliceAiAlleleParser().parseLine(line);
        Allele expected = new Allele(23, 12345, "A", "C");
        expected.addPathogenicityScore(AlleleProto.PathogenicityScore.newBuilder().setPathogenicitySource(AlleleProto.PathogenicitySource.SPLICE_AI).setScore(1f).build());
        assertThat(result, equalTo(List.of(expected)));
    }

    /**
     * This is a one-off method to prepare the SpliceAI data by selecting only the max score from the DL/DG/AL/AG scores
     * >= minScore. This is recommended as >= 0.2 for high recall, increasing to 0.5 as a default and 0.8 for high-precision.
     * confidence. <a href="https://doi.org/10.1016/j.cell.2018.12.015">Jaganathan et al, Cell 2019</a> and <a href="https://github.com/Illumina/SpliceAI">https://github.com/Illumina/SpliceAI</a>
     *
     * <blockquote>
     * Delta score of a variant, defined as the maximum of (DS_AG, DS_AL, DS_DG, DS_DL), ranges from 0 to 1 and can be interpreted as the probability of the variant being splice-altering. In the paper, a detailed characterization is provided for 0.2 (high recall), 0.5 (recommended), and 0.8 (high precision) cutoffs. Delta position conveys information about the location where splicing changes relative to the variant position (positive values are downstream of the variant, negative values are upstream).
     * </blockquote>
     * <cite>
     * These annotations are free for academic and not-for-profit use; other use requires a commercial license from Illumina, Inc.
     * </cite>
     **/
    @Disabled
    @Test
    void prepareSpliceAiData() {
        Path dataPath = Path.of("/home/hhx640/Documents/exomiser-build/");
        Path inFile = dataPath.resolve("spliceai_scores.masked.snv.hg19.vcf.gz");
        Path outFile = dataPath.resolve("spliceai_max_delta_scores_non_zero.masked.snv.hg19.vcf.gz");
        // the ACMG guidelines for use of SpliceAI scores recommend anything under 0.1 to be scored BP4 and over 0.2 PP3
        // so, we're using that cutoff here. This means that any SNP with no score in the database can be considered as
        // not impacting splicing.
        float minScore = 0.1f;
        SpliceAiMaxDeltaScoreParser.build(inFile, outFile, minScore);
    }

    private static class SpliceAiMaxDeltaScoreParser {

        private static final Logger logger = LoggerFactory.getLogger(SpliceAiMaxDeltaScoreParser.class);

        public static void build(Path spliceAiInputPath, Path outputBgZipPath, float minScore) {
            long count = 0;
            logger.info("Parsing SpliceAI scores from {} with a maximum delta score >= {}", spliceAiInputPath.getFileName(), minScore);
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(spliceAiInputPath))));
                 BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new BlockCompressedOutputStream(outputBgZipPath.toFile(), 6)));
            ) {
                for (String line; (line = bufferedReader.readLine()) != null; ) {
                    if (line.startsWith("##")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        continue;
                    }
                    if (line.startsWith("#")) {
                        bufferedWriter.write("#CHROM\tPOS\tREF\tALT\tSPLICE_AI_MAX_DELTA_SCORE");
                        bufferedWriter.newLine();
                        continue;
                    }
                    //##INFO=<ID=SpliceAI,Number=.,Type=String,Description="SpliceAIv1.3 variant annotation. These include delta scores (DS) and delta positions (DP) for acceptor gain (AG), acceptor loss (AL), donor gain (DG), and donor loss (DL). Format: ALLELE|SYMBOL|DS_AG|DS_AL|DS_DG|DS_DL|DP_AG|DP_AL|DP_DG|DP_DL">
                    //1       69092   .       T       A       .       .       SpliceAI=A|OR4F5|0.00|0.00|0.22|0.00|2|41|1|23
                    String[] fields = line.split("\t");
                    if (fields.length != 8) {
                        throw new IllegalArgumentException("Expected 8 fields but found " + fields.length + " in line " + line);
                    }
                    String contig = Contigs.toString(Contigs.parseId(fields[0])); // 1..22,X,Y,MT
                    int start = Integer.parseInt(fields[1]);
                    String ref = fields[3];
                    String alt = fields[4];
                    float score = parseSpliceAiScore(fields[7]);
                    // hg38 contains some oddly named unplaced contigs which end up with a contig of 0 and this leads to
                    // failed tabix indexing, so check and remove these.
                    if (!contig.equals("0") && score >= minScore) {
                        count++;
                        if (count % 1_000_000 == 0) {
                            logger.info("Current contig {} - written {} variants. Current: {}-{}-{}-{} SpliceAI={}", contig, count, contig, start, ref, alt, score);
                        }
                        String outLine = contig + "\t" + start + "\t" + ref + "\t"+ alt + "\t" + score;
                        bufferedWriter.write(outLine);
                        bufferedWriter.newLine();
                    }
                }
                logger.info("Done - written {} variants", count);
            } catch (IOException e) {
                logger.error("Unable to write file " + outputBgZipPath, e);
            }
        }

        static float parseSpliceAiScore(String info) {
            String[] fields = info.split("\\|");
            // SpliceAIv1.3 variant annotation. These include delta scores (DS) and delta positions (DP) for acceptor gain (AG),
            //   acceptor loss (AL), donor gain (DG), and donor loss (DL).
            //   Format: ALLELE|SYMBOL|DS_AG|DS_AL|DS_DG|DS_DL|DP_AG|DP_AL|DP_DG|DP_DL"
            // SpliceAI=A|OR4F5|0.00|0.00|0.22|0.00|2|41|1|23
            // we want the max delta score for gain or loss
            float max = 0f;
            for (int i = 2; i < 6; i++) {
                max = Math.max(max, Float.parseFloat(fields[i]));
            }
            return max;
        }

    }
}