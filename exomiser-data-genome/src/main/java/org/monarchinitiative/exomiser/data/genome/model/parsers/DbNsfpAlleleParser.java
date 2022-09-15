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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfpAlleleParser implements AlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(DbNsfpAlleleParser.class);

    private final DbNsfpColumnIndex columnIndex;

    //these fields are mutable as they are re-set each time a new chromosome is parsed.
    private int chrIndex;
    private int posIndex;
    private int rsIndex;
    private int refIndex;
    private int altIndex;

    private List<DbNsfpScoreParser> scoreParsers;

    public DbNsfpAlleleParser(DbNsfpColumnIndex columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            Map<String, Integer> index = makeHeaderIndex(line);
            setIndexFields(index);
            scoreParsers = makeScoreParsers(index);
            return List.of();
        }
        return parseAlleles(line);
    }

    private Map<String, Integer> makeHeaderIndex(String header) {
        Map<String, Integer> index = new HashMap<>();
        //remove the '#' prefix to the first column
        String[] fields = header.substring(1).split("\t");
        for (int i = 0; i < fields.length; i++) {
            String token = fields[i];
            if (token.startsWith(columnIndex.getRsPrefix())) {
                index.put(columnIndex.getRsPrefix(), i);
            }
            index.put(token, i);
        }
        return index;
    }

    private void setIndexFields(Map<String, Integer> index) {
        this.chrIndex = index.get(columnIndex.getChrHeader());
        this.posIndex = index.get(columnIndex.getPosHeader());
        this.rsIndex = index.get(columnIndex.getRsPrefix());
        this.refIndex = index.get(columnIndex.getRefHeader());
        this.altIndex = index.get(columnIndex.getAltHeader());
    }

    private List<DbNsfpScoreParser> makeScoreParsers(Map<String, Integer> index) {
        List<DbNsfpScoreParser> parsers = new ArrayList<>();
        // values
        for (Map.Entry<String, Integer> entry : index.entrySet()) {
            String key = entry.getKey();
            int pos = entry.getValue();
            //SIFT
            if (key.equals(columnIndex.getSiftHeader())) {
                parsers.add(new SiftScoreParser(pos));
            }
            // POLYPHEN
            else if (key.equals(columnIndex.getPolyPhen2HvarHeader())) {
                parsers.add(new PolyPhenScoreParser(pos));
            }
            // MUTATION_TASTER
            else if (key.equals(columnIndex.getMTasterPredHeader())) {
                int mTasterScorePos = index.get(columnIndex.getMTasterScoreHeader());
                parsers.add(new MutatationTasterScoreParser(mTasterScorePos, pos));
            }
            // REVEL
            else if (key.equals(columnIndex.getRevelScoreHeader())) {
                parsers.add(new RevelScoreParser(pos));
            }
            // These fields are new in version 4.0.
            // MCAP
            else if (key.equals(columnIndex.getMcapScoreHeader())) {
                parsers.add(new McapParser(pos));
            }
            // MPC
            else if (key.equals(columnIndex.getMpcScoreHeader())) {
                parsers.add(new MpcParser(pos));
            }
            // MVP
            else if (key.equals(columnIndex.getMvpScoreHeader())) {
                parsers.add(new MvpParser(pos));
            }
            // PRIMATE_AI
            else if (key.equals(columnIndex.getPrimateAiScoreHeader())) {
                parsers.add(new PrimateAiScoreParser(pos));
            }
        }
        return parsers;
    }

    private List<Allele> parseAlleles(String line) {
        String[] fields = line.split("\t");

        byte chr = ChromosomeParser.parseChr(fields[chrIndex]);
        if (chr == 0) {
            return List.of();
        }
        int pos = Integer.parseInt(fields[posIndex]);
        String rsId = RsIdParser.parseRsId(fields[rsIndex]);
        String ref = fields[refIndex];
        String alt = fields[altIndex];

        List<AlleleProto.PathogenicityScore> pathScores = parsePathScores(fields);

        if (pathScores.isEmpty()) {
            return List.of();
        }

        Allele allele = new Allele(chr, pos, ref, alt);
        allele.setRsId(rsId);
        allele.addAllPathogenicityScores(pathScores);
        return List.of(allele);
    }

    private List<AlleleProto.PathogenicityScore> parsePathScores(String[] fields) {
        var pathScores = new ArrayList<AlleleProto.PathogenicityScore>();
        for (DbNsfpScoreParser scoreParser : scoreParsers) {
            var score = scoreParser.parseScore(fields);
            if (score != null) {
                pathScores.add(score);
            }
        }
        return pathScores;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbNsfpAlleleParser that = (DbNsfpAlleleParser) o;
        return Objects.equals(columnIndex, that.columnIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnIndex);
    }

    /**
     * 24	SIFT_score: SIFT score (SIFTori). Scores range from 0 to 1. The smaller the score the
     * more likely the SNP has damaging effect.
     * Multiple scores separated by ";", corresponding to Ensembl_proteinid.
     */
    private record SiftScoreParser(int fieldPosition) implements TranscriptValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.SIFT;
        }

        @Override
        public AlleleProto.PathogenicityScore parseScore(String[] fields) {
            String field = getValueOrEmpty(fields, fieldPosition, EMPTY_VALUE);
            String[] transcriptPredictions = field.split(";");
            if (transcriptPredictions.length == 1) {
                return parseScore(transcriptPredictions[0]);
            }
            float minValue = 1;
            for (String score : transcriptPredictions) {
                if (!EMPTY_VALUE.equals(score)) {
                    float value = Float.parseFloat(score);
                    //The smaller the score the more likely the SNP has damaging effect.
                    minValue = Float.min(minValue, value);
                }
            }
            if (minValue < 1) {
                return pathScore(minValue);
            }
            return null;
        }

    }

    private record MutatationTasterScoreParser(int mTasterScorePos, int mTasterPredPos) implements DbNsfpScoreParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.MUTATION_TASTER;
        }

        @Override
        public AlleleProto.PathogenicityScore parseScore(String[] fields) {
            String scoreFields = getValueOrEmpty(fields, mTasterScorePos, EMPTY_VALUE);
            String predFields = getValueOrEmpty(fields, mTasterPredPos, EMPTY_VALUE);
            String[] scores = scoreFields.split(";");
            String[] predictions = predFields.split(";");
            if (scores.length == predictions.length) {
                float maxValue = 0;
                for (int i = 0; i < scores.length; i++) {
                    String score = scores[i];
                    // Note there are some entries such as ".;0.292" so catch them here
                    if (!score.equals(EMPTY_VALUE)) {
                        String p = predictions[i].trim();
                        if (p.equals("A") || p.equals("D")) {
                            float value = Float.parseFloat(score);
                            //The larger the score the more likely the SNP has damaging effect.
                            maxValue = Float.max(maxValue, value);
                        }
                    }
                }
                if (maxValue > 0) {
                    return pathScore(maxValue);
                }
            }
            return null;
        }
    }

    /**
     * 33	Polyphen2_HVAR_score: Polyphen2 score based on HumVar, i.e. hvar_prob.
     * The score ranges from 0 to 1.
     * Multiple entries separated by ";", corresponding to Uniprot_acc_Polyphen2.
     */
    private record PolyPhenScoreParser(int fieldPosition) implements TranscriptValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.POLYPHEN;
        }
    }

    /**
     * 63	REVEL_score: REVEL is an ensemble score based on 13 individual scores for predicting the
     * pathogenicity of missense variants. Scores range from 0 to 1. The larger the score the more
     * likely the SNP has damaging effect. "REVEL scores are freely available for non-commercial use.
     * For other uses, please contact Weiva Sieh" (weiva.sieh@mssm.edu)
     */
    private record RevelScoreParser(int fieldPosition) implements SingleValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.REVEL;
        }
    }

    /**
     * 70	M-CAP_score: M-CAP score (details in DOI: 10.1038/ng.3703). Scores range from 0 to 1. The larger
     * the score the more likely the SNP has damaging effect.
     */
    private record McapParser(int fieldPosition) implements SingleValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.M_CAP;
        }
    }

    /**
     * 80	MVP_score: A pathogenicity prediction score for missense variants using deep learning approach.
     * The range of MVP score is from 0 to 1. The larger the score, the more likely the variant is
     * pathogenic. The authors suggest thresholds of 0.7 and 0.75 for separating damaging vs tolerant
     * variants in constrained genes (ExAC pLI >=0.5) and non-constrained genes (ExAC pLI<0.5), respectively.
     * Details see doi: http://dx.doi.org/10.1101/259390
     * Multiple entries are separated by ";", corresponding to Ensembl_transcriptid.
     */
    private record MvpParser(int fieldPosition) implements TranscriptValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.MVP;
        }
    }

    /**
     * 82	MPC_score: A deleteriousness prediction score for missense variants based on regional missense
     * constraint. The range of MPC score is 0 to 5. The larger the score, the more likely the variant is
     * pathogenic. Details see doi: http://dx.doi.org/10.1101/148353.
     * Multiple entries are separated by ";", corresponding to Ensembl_transcriptid.
     */
    private record MpcParser(int fieldPosition) implements TranscriptValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.MPC;
        }
    }

    /**
     * 84	PrimateAI_score: A pathogenicity prediction score for missense variants based on common variants of
     * non-human primate species using a deep neural network. The range of PrimateAI score is 0 to 1.
     * The larger the score, the more likely the variant is pathogenic. The authors suggest a threshold
     * of 0.803 for separating damaging vs tolerant variants.
     * Details see https://doi.org/10.1038/s41588-018-0167-z
     **/
    private record PrimateAiScoreParser(int fieldPosition) implements SingleValueParser {

        @Override
        public AlleleProto.PathogenicitySource pathogenicitySource() {
            return AlleleProto.PathogenicitySource.PRIMATE_AI;
        }
    }

    private interface TranscriptValueParser extends DbNsfpScoreParser {

        public int fieldPosition();

        @Override
        default AlleleProto.PathogenicityScore parseScore(String[] fields) {
            String field = getValueOrEmpty(fields, fieldPosition(), EMPTY_VALUE);
            return parseTranscriptValues(field);
        }

        @Nullable
        private AlleleProto.PathogenicityScore parseTranscriptValues(String field) {
            String[] transcriptPredictions = field.split(";");
            if (transcriptPredictions.length == 1) {
                return parseScore(transcriptPredictions[0]);
            }
            float maxValue = getMaxValue(transcriptPredictions);
            if (maxValue > 0) {
                return pathScore(maxValue);
            }
            return null;
        }

        private float getMaxValue(String[] transcriptPredictions) {
            float maxValue = 0;
            for (String score : transcriptPredictions) {
                if (!EMPTY_VALUE.equals(score)) {
                    float value = Float.parseFloat(score);
                    //The larger the score the more likely the SNP has damaging effect.
                    maxValue = Float.max(maxValue, value);
                }
            }
            return maxValue;
        }
    }

    private interface SingleValueParser extends DbNsfpScoreParser {

        int fieldPosition();

        @Override
        default AlleleProto.PathogenicityScore parseScore(String[] fields) {
            String value = fields[fieldPosition()];
            return parseScore(value);
        }
    }

    private interface DbNsfpScoreParser {

        static final String EMPTY_VALUE = ".";

        AlleleProto.PathogenicitySource pathogenicitySource();

        @Nullable
        AlleleProto.PathogenicityScore parseScore(String[] fields);

        public default String getValueOrEmpty(String[] fields, int position, String empty) {
            String field = fields[position];
            if (field == null) {
                return empty;
            }
            return field;
        }

        private boolean isNullOrEmptyValue(String field) {
            return field == null || field.equals(EMPTY_VALUE);
        }

        @Nullable
        default AlleleProto.PathogenicityScore parseScore(String value) {
            return isNullOrEmptyValue(value) ? null : pathScore(Float.parseFloat(value));
        }

        default AlleleProto.PathogenicityScore pathScore(float score) {
            return Allele.buildPathScore(pathogenicitySource(), score);
        }
    }
}
