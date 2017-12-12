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

package org.monarchinitiative.exomiser.core.prioritisers.util;

import java.util.List;

/**
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 */
public class ScoreDistribution {

    private double[] scores;
    private double[] pvalues;
    private double numberRandomizations;
    private int numberQueryTerms;
    private int mimNumber;

    public int getNumberQueryTerms() {
        return numberQueryTerms;
    }

    public int getMimNumber() {
        return mimNumber;
    }


    public void setDistribution(List<Double> scoresAL, List<Double> pvaluesAL, double numRandomizations) {

        this.scores = new double[scoresAL.size()];
        this.pvalues = new double[pvaluesAL.size()];

        for (int i = 0; i < scoresAL.size(); i++) {
            this.scores[i] = scoresAL.get(i);
            this.pvalues[i] = pvaluesAL.get(i);
        }

        this.numberRandomizations = numRandomizations;

    }

    public double getPvalue(double score, double roundFactor) {

        double rounded = round(score, roundFactor);

		/* maybe this value is too high */
        if (scores[scores.length - 1] < rounded)
            return (double) 1 / numberRandomizations;

		/* init with 1 */
        double pvalue = 1;

        for (int i = 0; i < scores.length; i++) {

            double scoreCand = scores[i];
            if (rounded <= scoreCand) {
                pvalue = pvalues[i];
                break;
            }

        }
        return pvalue;
    }

    private static double round(double d, double fact) {
        return Math.rint(d * fact) / fact;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("score dist for: " + mimNumber + ":\n");
        for (int i = 0; i < scores.length; i++) {
            stringBuilder.append(scores[i]);
            stringBuilder.append("-");
            stringBuilder.append(pvalues[i]);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

}



