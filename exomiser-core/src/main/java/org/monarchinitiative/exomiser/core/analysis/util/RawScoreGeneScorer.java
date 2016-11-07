/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class RawScoreGeneScorer implements GeneScorer {

    private static final Logger logger = LoggerFactory.getLogger(RawScoreGeneScorer.class);
    
    /**
     * Calculates the final ranks of all genes that have survived the filtering
     * and prioritising steps. The strategy is that for autosomal dominant
     * diseases, we take the single most pathogenic score of any variant
     * affecting the gene; for autosomal recessive diseases, we take the mean of
     * the two most pathogenic variants. X-linked diseases are filtered such
     * that only X-chromosomal genes are left over, and the single worst variant
     * is taken.
     * <P>
     * Once the scores have been calculated, we sort the array list of
     * {@link exomizer.exome.Gene Gene} objects according to the combined
     * filter/priority score.
     *
     * @param genes
     * @param modeOfInheritance
     */
    @Override
    public void scoreGenes(List<Gene> genes, ModeOfInheritance modeOfInheritance) {
        for (Gene gene : genes) {
            scoreGene(gene, modeOfInheritance);
        }
        Collections.sort(genes);
    }

    protected void scoreGene(Gene gene, ModeOfInheritance modeOfInheritance) {
        //It is critical only the PASS variants are used in the scoring
        float filterScore = calculateFilterScore(gene.getPassedVariantEvaluations(), modeOfInheritance);
        gene.setFilterScore(filterScore);
        
        float priorityScore = calculateGenePriorityScore(gene);
        gene.setPriorityScore(priorityScore);

        float combinedScore = calculateCombinedScore(filterScore, priorityScore, gene.getPriorityResults().keySet());
        gene.setCombinedScore(combinedScore);
    }


    /**
     * Calculates the total priority score for the {@code VariantEvaluation} of
     * the gene based on data stored in its associated
     * {@link jannovar.exome.Variant Variant} objects. Note that for assumed
     * autosomal recessive variants, the mean of the worst two variants is
     * taken, and for other modes of inheritance,the since worst value is taken.
     * <P>
     * Note that we <b>assume that genes have been filtered for mode of
     * inheritance before this function is called. This means that we do not
     * need to apply separate filtering for mode of inheritance here</b>. The
     * only thing we need to watch out for is whether a variant is homozygous or
     * not (for autosomal recessive inheritance, these variants get counted
     * twice).
     *
     * @param variantEvaluations from a gene
     * @param modeOfInheritance Autosomal recessive, dominant, or X chromosomal
     * recessive.
     * @return
     */
    private float calculateFilterScore(List<VariantEvaluation> variantEvaluations, ModeOfInheritance modeOfInheritance) {
        if (variantEvaluations.isEmpty()) {
            return 0f;
        }
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return calculateAutosomalRecessiveFilterScore(variantEvaluations);
        }
        return calculateNonAutosomalRecessiveFilterScore(variantEvaluations);
    }

    private float calculateGenePriorityScore(Gene gene) {
        if (gene.getPriorityResults().isEmpty()) {
            return 0f;
        }
        return calculatePriorityScore(gene.getPriorityResults().values());
    }

    /**
     * Calculate the combined priority score for the gene.
     *
     * @param priorityScores of the gene
     * @return
     */
    private float calculatePriorityScore(Collection<PriorityResult> priorityScores) {
        float finalPriorityScore = 1f;
        for (PriorityResult priorityScore : priorityScores) {
            finalPriorityScore *= priorityScore.getScore();
        }
        return finalPriorityScore;
    }


    /**
     * Calculate the combined score of this gene based on the relevance of the
     * gene (priorityScore) and the predicted effects of the variants
     * (filterScore).
     * <P>
     * Note that this method assumes we have calculate the scores, which is
     * depending on the function {@link #calculateGeneAndVariantScores} having
     * been called.
     *
     */
    private float calculateCombinedScore(float filterScore, float priorityScore, Set<PriorityType> prioritiesRun) {

        //TODO: what if we ran all of these? It *is* *possible* to do so. 
        if (prioritiesRun.contains(PriorityType.HIPHIVE_PRIORITY)) {
            double logitScore = 1 / (1 + Math.exp(-(-13.28813 + 10.39451 * priorityScore + 9.18381 * filterScore)));
            return (float) logitScore;
        } else if (prioritiesRun.contains(PriorityType.EXOMEWALKER_PRIORITY)) {
            //NB this is based on raw walker score
            double logitScore = 1 / (1 + Math.exp(-(-8.67972 + 219.40082 * priorityScore + 8.54374 * filterScore)));
            return (float) logitScore;
        } else if (prioritiesRun.contains(PriorityType.PHENIX_PRIORITY)) {
            double logitScore = 1 / (1 + Math.exp(-(-11.15659 + 13.21835 * priorityScore + 4.08667 * filterScore)));
            return (float) logitScore;
        } else {
            return (priorityScore + filterScore) / 2f;
        }
    }


    /**
     * For assumed autosomal recessive variants, this method calculates the mean
     * of the worst(highest numerical) two variants.
     *
     * @param variantEvaluations
     * @return
     */
    private float calculateAutosomalRecessiveFilterScore(List<VariantEvaluation> variantEvaluations) {

        List<VariantEvaluation> heterozygous = variantEvaluations.stream()
                .filter(variantIsHeterozygous())
                .sorted(Comparator.comparing(VariantEvaluation::getVariantScore).reversed())
                .limit(2)
                .collect(toList());
//
        Optional<VariantEvaluation> bestHomozygousAlt = variantEvaluations.stream()
                .filter(variantIsHomozygousAlt())
                .max(Comparator.comparing(VariantEvaluation::getVariantScore));

        // Realised original logic allows a comphet to be calculated between a top scoring het and second place hom which is wrong
        // Jannovar seems to currently be allowing hom_ref variants through so skip these as well
        double bestCompHetScore = heterozygous.stream()
                .mapToDouble(VariantEvaluation::getVariantScore)
                .average()
                .orElse(0f);

        double bestHomAltScore = bestHomozygousAlt
                .map(VariantEvaluation::getVariantScore)
                .orElse(0f);

        double bestScore = Double.max(bestHomAltScore, bestCompHetScore);

        if (BigDecimal.valueOf(bestScore).equals(BigDecimal.valueOf(bestCompHetScore))) {
            heterozygous.forEach(VariantEvaluation::setAsContributingToGeneScore);
        } else {
            bestHomozygousAlt.ifPresent(VariantEvaluation::setAsContributingToGeneScore);
        }

        return (float) bestScore;
    }
    
    private Predicate<VariantEvaluation> variantIsHomozygousAlt() {
        return ve -> ve.getVariantContext().getGenotype(0).isHomVar();
    }

    private Predicate<VariantEvaluation> variantIsHeterozygous() {
        return ve -> ve.getVariantContext().getGenotype(0).isHet();
    }

    /**
     * For other variants with non-autosomal recessive modes of inheritance, the
     * most deleterious variant (highest numerical variantScore value) is taken.
     *
     * @param variantEvaluations
     * @return
     */
    private float calculateNonAutosomalRecessiveFilterScore(List<VariantEvaluation> variantEvaluations) {
        //Otherwise for non-autosomal recessive, there is just one heterozygous mutation
        //thus return only the single highest score.
        Optional<VariantEvaluation> bestVariant = variantEvaluations
                .stream()
                .max(Comparator.comparing(VariantEvaluation::getVariantScore));

        bestVariant.ifPresent(VariantEvaluation::setAsContributingToGeneScore);

        return bestVariant.map(VariantEvaluation::getVariantScore).orElse(0f);
    }
}
