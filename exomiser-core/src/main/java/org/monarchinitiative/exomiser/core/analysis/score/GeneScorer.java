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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis.score;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.*;
import java.util.function.Function;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@FunctionalInterface
public interface GeneScorer {

    public Function<Gene, List<GeneScore>> scoreGene();

    default List<Gene> scoreGenes(List<Gene> genes) {
        genes.forEach(gene -> {
            List<GeneScore> geneScores = scoreGene().apply(gene);
            for (GeneScore score : geneScores) {
                gene.addGeneScore(score);
            }
        });
        Collections.sort(genes);
        return genes;
    }

    /**
     * Calculates the Exomiser score based on the gene phenotype score, the variant score and the ACMG posterior
     * probability of pathogenicity. <em>CAUTION!</em> the acmgPostProbPath will likely need adjusting to remove the
     * components included in the phenotype and variant scores.
     */
    static double calculateCombinedScore(double variantScore, double priorityScore, double acmgPostProbPath, Set<PriorityType> prioritiesRun) {
        if (variantScore == 0.0 && priorityScore == 0.0) {
            return 0.0;
        } else if (prioritiesRun.contains(PriorityType.HIPHIVE_PRIORITY)) {
            return hiPhiveWithAcmgLogitScore(variantScore, priorityScore, acmgPostProbPath);
        } else if (prioritiesRun.contains(PriorityType.EXOMEWALKER_PRIORITY)) {
            return walkerLogitScore(variantScore, priorityScore);
        } else if (prioritiesRun.contains(PriorityType.PHENIX_PRIORITY)) {
            return phenixLogitScore(variantScore, priorityScore);
        }
        return (priorityScore + variantScore) / 2.0;
    }

    // Newly trained score for use with acmgPostProbPath (WITH PP4, BS4 FILTERED OUT)
    private static double hiPhiveWithAcmgLogitScore(double variantScore, double priorityScore, double acmgPostProbPath) {
        return 1.0 / (1.0 + Math.exp(-(-16.64975766487485 + (10.04400174 * priorityScore) + (6.49073924 * variantScore) + (4.85807901 * acmgPostProbPath))));
    }

    // This is the original trained score used pre-15.0.0
    private static double hiPhiveLogitScore(double variantScore, double priorityScore) {
        return 1.0 / (1.0 + Math.exp(-(-13.28813 + 10.39451 * priorityScore + 9.18381 * variantScore)));
    }

    private static double walkerLogitScore(double variantScore, double priorityScore) {
        return 1.0 / (1.0 + Math.exp(-(-8.67972 + 219.40082 * priorityScore + 8.54374 * variantScore)));
    }

    private static double phenixLogitScore(double variantScore, double priorityScore) {
        return 1.0 / (1.0 + Math.exp(-(-11.15659 + 13.21835 * priorityScore + 4.08667 * variantScore)));
    }
}