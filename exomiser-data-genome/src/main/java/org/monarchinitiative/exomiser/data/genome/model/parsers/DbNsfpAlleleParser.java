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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfpAlleleParser implements AlleleParser {

    private static Logger logger = LoggerFactory.getLogger(DbNsfpAlleleParser.class);

    private static final String EMPTY_VALUE = ".";

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
            return Collections.emptyList();
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
            return Collections.emptyList();
        }
        int pos = Integer.parseInt(fields[posIndex]);
        String rsId = RsIdParser.parseRsId(fields[rsIndex]);
        String ref = fields[refIndex];
        String alt = fields[altIndex];

        Map<AlleleProperty, Float> pathScores = parsePathScores(fields);

        if (rsId.isEmpty() && pathScores.isEmpty()) {
            return Collections.emptyList();
        }

        Allele allele = new Allele(chr, pos, ref, alt);
        allele.setRsId(rsId);
        allele.getValues().putAll(pathScores);
        return Collections.singletonList(allele);
    }

    private Map<AlleleProperty, Float> parsePathScores(String[] fields) {
        Map<AlleleProperty, Float> values = new EnumMap<>(AlleleProperty.class);

        scoreParsers.forEach(scoreParser -> {
            Float score = scoreParser.parse(fields);
            if (score != null) {
                values.put(scoreParser.getAlleleProperty(), score);
            }
        });

        return values;
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
    private static class SiftScoreParser extends TranscriptValueParser {

        SiftScoreParser(int fieldPosition) {
            super(AlleleProperty.SIFT, fieldPosition);
        }

        @Override
        public AlleleProperty getAlleleProperty() {
            return AlleleProperty.SIFT;
        }

        @Override
        public Float parse(String[] fields) {
            String field = getValueOrEmpty(fields, super.fieldPosition, EMPTY_VALUE);
            String[] transcriptPredictions = field.split(";");
            if (transcriptPredictions.length == 1) {
                return parseValue(transcriptPredictions[0]);
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
                return minValue;
            }
            return null;
        }

    }

    private static class MutatationTasterScoreParser implements DbNsfpScoreParser {

        private final int mTasterScorePos;
        private final int mTasterPredPos;

        public MutatationTasterScoreParser(int mTasterScorePos, int mTasterPredPos) {
            this.mTasterScorePos = mTasterScorePos;
            this.mTasterPredPos = mTasterPredPos;
        }

        @Override
        public AlleleProperty getAlleleProperty() {
            return AlleleProperty.MUT_TASTER;
        }

        @Override
        public Float parse(String[] fields) {
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
                    return maxValue;
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MutatationTasterScoreParser that = (MutatationTasterScoreParser) o;
            return mTasterScorePos == that.mTasterScorePos &&
                    mTasterPredPos == that.mTasterPredPos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mTasterScorePos, mTasterPredPos);
        }

        @Override
        public String toString() {
            return "MutatationTasterScoreParser{" +
                    "mTasterScorePos=" + mTasterScorePos +
                    ", mTasterPredPos=" + mTasterPredPos +
                    '}';
        }
    }

    /**
     * 33	Polyphen2_HVAR_score: Polyphen2 score based on HumVar, i.e. hvar_prob.
     * The score ranges from 0 to 1.
     * Multiple entries separated by ";", corresponding to Uniprot_acc_Polyphen2.
     */
    private static class PolyPhenScoreParser extends TranscriptValueParser {

        public PolyPhenScoreParser(int fieldPosition) {
            super(AlleleProperty.POLYPHEN, fieldPosition);
        }
    }

    /**
     * 63	REVEL_score: REVEL is an ensemble score based on 13 individual scores for predicting the
     * pathogenicity of missense variants. Scores range from 0 to 1. The larger the score the more
     * likely the SNP has damaging effect. "REVEL scores are freely available for non-commercial use.
     * For other uses, please contact Weiva Sieh" (weiva.sieh@mssm.edu)
     */
    private static class RevelScoreParser extends SingleValueParser {

        public RevelScoreParser(int fieldPosition) {
            super(AlleleProperty.REVEL, fieldPosition);
        }
    }

    /**
     * 70	M-CAP_score: M-CAP score (details in DOI: 10.1038/ng.3703). Scores range from 0 to 1. The larger
     * the score the more likely the SNP has damaging effect.
     */
    private static class McapParser extends SingleValueParser {

        public McapParser(int fieldPosition) {
            super(AlleleProperty.MCAP, fieldPosition);
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
    private static class MvpParser extends TranscriptValueParser {

        public MvpParser(int fieldPosition) {
            super(AlleleProperty.MVP, fieldPosition);
        }
    }

    /**
     * 82	MPC_score: A deleteriousness prediction score for missense variants based on regional missense
     * constraint. The range of MPC score is 0 to 5. The larger the score, the more likely the variant is
     * pathogenic. Details see doi: http://dx.doi.org/10.1101/148353.
     * Multiple entries are separated by ";", corresponding to Ensembl_transcriptid.
     */
    private static class MpcParser extends TranscriptValueParser {

        public MpcParser(int fieldPosition) {
            super(AlleleProperty.MPC, fieldPosition);
        }
    }

    /**
     * 84	PrimateAI_score: A pathogenicity prediction score for missense variants based on common variants of
     * non-human primate species using a deep neural network. The range of PrimateAI score is 0 to 1.
     * The larger the score, the more likely the variant is pathogenic. The authors suggest a threshold
     * of 0.803 for separating damaging vs tolerant variants.
     * Details see https://doi.org/10.1038/s41588-018-0167-z
     **/
    private class PrimateAiScoreParser extends SingleValueParser {

        public PrimateAiScoreParser(int fieldPosition) {
            super(AlleleProperty.PRIMATE_AI, fieldPosition);
        }
    }

    private static class TranscriptValueParser extends AbstractDbNsfpScoreParser {

        private final AlleleProperty alleleProperty;
        private final int fieldPosition;

        public TranscriptValueParser(AlleleProperty alleleProperty, int fieldPosition) {
            this.alleleProperty = alleleProperty;
            this.fieldPosition = fieldPosition;
        }

        @Override
        public AlleleProperty getAlleleProperty() {
            return alleleProperty;
        }

        @Override
        public Float parse(String[] fields) {
            String field = getValueOrEmpty(fields, fieldPosition, EMPTY_VALUE);
            return parseTranscriptValues(field);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranscriptValueParser that = (TranscriptValueParser) o;
            return fieldPosition == that.fieldPosition &&
                    alleleProperty == that.alleleProperty;
        }

        @Override
        public int hashCode() {
            return Objects.hash(alleleProperty, fieldPosition);
        }

        @Override
        public String toString() {
            return "TranscriptValueParser{" +
                    "alleleProperty=" + alleleProperty +
                    ", fieldPosition=" + fieldPosition +
                    '}';
        }
    }

    private static class SingleValueParser extends AbstractDbNsfpScoreParser {

        private final AlleleProperty alleleProperty;
        private final int fieldPosition;

        public SingleValueParser(AlleleProperty alleleProperty, int fieldPosition) {
            this.alleleProperty = alleleProperty;
            this.fieldPosition = fieldPosition;
        }

        @Override
        public AlleleProperty getAlleleProperty() {
            return alleleProperty;
        }

        @Override
        public Float parse(String[] fields) {
            String field = getValueOrEmpty(fields, fieldPosition, EMPTY_VALUE);
            return parseValue(field);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SingleValueParser that = (SingleValueParser) o;
            return fieldPosition == that.fieldPosition &&
                    alleleProperty == that.alleleProperty;
        }

        @Override
        public int hashCode() {
            return Objects.hash(alleleProperty, fieldPosition);
        }

        @Override
        public String toString() {
            return "SingleValueParser{" +
                    "alleleProperty=" + alleleProperty +
                    ", fieldPosition=" + fieldPosition +
                    '}';
        }
    }

    private abstract static class AbstractDbNsfpScoreParser implements DbNsfpScoreParser {

        protected static final String EMPTY_VALUE = ".";

        @Nullable
        protected Float parseTranscriptValues(String field) {
            String[] transcriptPredictions = field.split(";");
            if (transcriptPredictions.length == 1) {
                return parseValue(transcriptPredictions[0]);
            }
            float maxValue = getMaxValue(transcriptPredictions);
            if (maxValue > 0) {
                return maxValue;
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

        @Nullable
        protected Float parseValue(String value) {
            if (!EMPTY_VALUE.equals(value)) {
                return Float.valueOf(value);
            }
            return null;
        }
    }

    private interface DbNsfpScoreParser {

        public AlleleProperty getAlleleProperty();

        public Float parse(String[] fields);

        public default String getValueOrEmpty(String[] fields, int position, String empty) {
            String field = fields[position];
            if (field == null) {
                return empty;
            }
            return field;
        }
    }
}
