/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.parsers;

import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private int rsPos;
    private int refPos;
    private int altPos;

    private int siftPos;
    private int polyPhen2HvarPos;
    private int mTasterScorePos;
    private int mTasterPredPos;
    private int revelScorePos;

    public DbNsfpAlleleParser(DbNsfpColumnIndex columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            parseColumnIndex(line);
            return Collections.emptyList();
        }
        String[] fields = line.split("\t");

        return parseAllele(fields);
    }

    private void parseColumnIndex(String header) {
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

        this.chrIndex = index.get(columnIndex.getChrHeader());
        this.posIndex = index.get(columnIndex.getPosHeader());
        this.rsPos = index.get(columnIndex.getRsPrefix());
        this.refPos = index.get(columnIndex.getRefHeader());
        this.altPos = index.get(columnIndex.getAltHeader());
        //values
        this.siftPos = index.get(columnIndex.getSiftHeader());
        this.polyPhen2HvarPos = index.get(columnIndex.getPolyPhen2HvarHeader());
        this.mTasterScorePos = index.get(columnIndex.getMTasterScoreHeader());
        this.mTasterPredPos = index.get(columnIndex.getMTasterPredHeader());
        this.revelScorePos = index.get(columnIndex.getRevelScoreHeader());
    }

    private List<Allele> parseAllele(String[] fields) {
        byte chr = ChromosomeParser.parseChr(fields[chrIndex]);
        if (chr == 0) {
            return Collections.emptyList();
        }
        int pos = Integer.parseInt(fields[posIndex]);
        String rsId = RsIdParser.parseRsId(fields[rsPos]);
        String ref = fields[refPos];
        String alt = fields[altPos];

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
        parseSift(values, AlleleProperty.SIFT, fields[siftPos]);
        parsePolyPhen(values, AlleleProperty.POLYPHEN, fields[polyPhen2HvarPos]);
        parseMutationTaster(values, AlleleProperty.MUT_TASTER, fields[mTasterScorePos], fields[mTasterPredPos]);
        parseValue(values, AlleleProperty.REVEL, fields[revelScorePos]);
        return values;
    }

    //    24	SIFT_score: SIFT score (SIFTori). Scores range from 0 to 1. The smaller the score the
    //    more likely the SNP has damaging effect.
    //    Multiple scores separated by ";", corresponding to Ensembl_proteinid.
    private Map<AlleleProperty, Float> parseSift(Map<AlleleProperty, Float> values, AlleleProperty key, String field) {
        String[] transcriptPredictions = field.split(";");
        if (transcriptPredictions.length == 1) {
            return parseValue(values, key, transcriptPredictions[0]);
        }
        float maxValue = 1;
        for (int i = 0; i < transcriptPredictions.length; i++) {
            String score = transcriptPredictions[i];
            if (!EMPTY_VALUE.equals(score)) {
                float value = Float.parseFloat(score);
                //The smaller the score the more likely the SNP has damaging effect.
                maxValue = Float.min(maxValue, value);
            }
        }
        if (maxValue < 1) {
            values.put(key, maxValue);
        }
        return values;
    }

    //    33	Polyphen2_HVAR_score: Polyphen2 score based on HumVar, i.e. hvar_prob.
    //    The score ranges from 0 to 1.
    //    Multiple entries separated by ";", corresponding to Uniprot_acc_Polyphen2.
    private Map<AlleleProperty, Float> parsePolyPhen(Map<AlleleProperty, Float> values, AlleleProperty key, String field) {
        String[] transcriptPredictions = field.split(";");
        if (transcriptPredictions.length == 1) {
            return parseValue(values, key, transcriptPredictions[0]);
        }
        float maxValue = getMaxValue(transcriptPredictions);
        if (maxValue > 0) {
            values.put(key, maxValue);
        }
        return values;
    }


    //    63	REVEL_score: REVEL is an ensemble score based on 13 individual scores for predicting the
    //    pathogenicity of missense variants. Scores range from 0 to 1. The larger the score the more
    //    likely the SNP has damaging effect. "REVEL scores are freely available for non-commercial use.
    //    For other uses, please contact Weiva Sieh" (weiva.sieh@mssm.edu)

    private Map<AlleleProperty, Float> parseValue(Map<AlleleProperty, Float> values, AlleleProperty key, String value) {
        if (!EMPTY_VALUE.equals(value)) {
            values.put(key, Float.valueOf(value));
        }
        return values;
    }

    private float getMaxValue(String[] transcriptPredictions) {
        float maxValue = 0;
        for (int i = 0; i < transcriptPredictions.length; i++) {
            String score = transcriptPredictions[i];
            if (!EMPTY_VALUE.equals(score)) {
                float value = Float.parseFloat(score);
                //The larger the score the more likely the SNP has damaging effect.
                maxValue = Float.max(maxValue, value);
            }
        }
        return maxValue;
    }


    private Map<AlleleProperty, Float> parseMutationTaster(Map<AlleleProperty, Float> values, AlleleProperty key, String scoreFields, String predFields) {
//        MutationTaster_score: MutationTaster p-value (MTori), ranges from 0 to 1.
//        Multiple scores are separated by ";". Information on corresponding transcript(s) can
//        be found by querying http://www.mutationtaster.org/ChrPos.html

//        MutationTaster_pred: MutationTaster prediction, "A" ("disease_causing_automatic"),
//                "D" ("disease_causing"), "N" ("polymorphism") or "P" ("polymorphism_automatic"). The
//        score cutoff between "D" and "N" is 0.5 for MTnew and 0.31713 for the rankscore.

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
                values.put(key, maxValue);
            }
        }
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
}
