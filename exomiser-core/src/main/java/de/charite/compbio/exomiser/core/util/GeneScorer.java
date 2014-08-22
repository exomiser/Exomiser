/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.priority.PriorityScore;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.ScoringMode;
import jannovar.common.ModeOfInheritance;
import jannovar.genotype.GenotypeCall;
import jannovar.pedigree.Pedigree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneScorer {

    private static final Logger logger = LoggerFactory.getLogger(GeneScorer.class);

    /**
     * Calculates the final ranks of all genes that have been run through the filtering
     * and prioritising steps. The strategy is that for autosomal dominant diseases,
     * we take the single most pathogenic score of any variant affecting the
     * gene; for autosomal recessive diseases, we take the mean of the two most
     * pathogenic variants. X-linked diseases are filtered such that only
     * X-chromosomal genes are left over, and the single worst variant is taken.
     * <P>
     * Once the scores have been calculated, we sort the array list of
     * {@link exomizer.exome.Gene Gene} objects according to the combined
     * filter/priority score.
     * 
     * @param geneList
     * @param modeOfInheritance
     * @param pedigree
     * @param scoringMode
     */
    public static void scoreGenes(List<Gene> geneList, ModeOfInheritance modeOfInheritance, Pedigree pedigree, ScoringMode scoringMode) {
        logger.info("Scoring genes using mode {}", scoringMode);
        switch (scoringMode) {
            case RAW_SCORE:
                scoreGenes(geneList, modeOfInheritance, pedigree);
                break;
            case RANK_BASED:
                scoreGenesByRank(geneList, modeOfInheritance, pedigree);
                break;
            default:
                scoreGenes(geneList, modeOfInheritance, pedigree);
        }
        Collections.sort(geneList);
    }


    /**
     * Calculates the final ranks of all genes that have survived the filtering
     * and prioritising steps. The strategy is that for autosomal dominant diseases,
     * we take the single most pathogenic score of any variant affecting the
     * gene; for autosomal recessive diseases, we take the mean of the two most
     * pathogenic variants. X-linked diseases are filtered such that only
     * X-chromosomal genes are left over, and the single worst variant is taken.
     * <P>
     * Once the scores have been calculated, we sort the array list of
     * {@link exomizer.exome.Gene Gene} objects according to the combined
     * filter/priority score.
     */
    private static void scoreGenes(List<Gene> geneList, ModeOfInheritance modeOfInheritance, Pedigree pedigree) {
        logger.info("Scoring genes");
        for (Gene gene : geneList) {
            float filterScore = calculateFilterScore(gene, modeOfInheritance, pedigree);
            gene.setFilterScore(filterScore);

            float priorityScore = calculatePriorityScore(gene);
            gene.setPriorityScore(priorityScore);

            float combinedScore = calculateCombinedScore(gene);
            gene.setCombinedScore(combinedScore);
        }
    }

    /**
     * In the original implementation of the Exomiser, the genes were scored
     * according to various criteria that gave them scores between [0,1].
     * However, these scores tended not to be uniformly distributed. This
     * function implements an alternative scoring scheme that first ranks the
     * genes according to their score and then overwrites the original score
     * according to a uniform distribution based on the ranks of the genes.
     */
    private static void scoreGenesByRank(List<Gene> geneList, ModeOfInheritance modeOfInheritance, Pedigree pedigree) {
        //first of all score the genes according to their raw scores
        scoreGenes(geneList, modeOfInheritance, pedigree);

        //now reset the scores according to their rank
        logger.info("Scoring genes by RANK.");
        // Store all gene and variant scores in sortable map
        // The key is a Float representing the raw score.
        // The value is a list of one or more genes with this score.
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<>();
        for (Gene gene : geneList) {
            float geneScore = gene.getPriorityScore();
            //System.out.println("scoreCandidateGenesByRank, " + g.getGeneSymbol() + ": " + geneScore);
            if (geneScoreMap.containsKey(geneScore)) {
                List<Gene> geneScoreGeneList = geneScoreMap.get(geneScore);
                geneScoreGeneList.add(gene);
            } else {
                List<Gene> geneScoreGeneList = new ArrayList<>();
                geneScoreGeneList.add(gene);
                geneScoreMap.put(geneScore, geneScoreGeneList);
            }
        }
        /*
         * iterate through all gene scores in descending order calculating a
         * score between 1 and 0 depending purely on rank and overwrite gene
         * scores with these new scores
         */
        float rank = 1;
        Set<Float> set = geneScoreMap.descendingKeySet();
        Iterator<Float> i = set.iterator();
        while (i.hasNext()) {
            float score = i.next();
            List<Gene> geneScoreGeneList = geneScoreMap.get(score);
            int sharedHits = geneScoreGeneList.size();
            float adjustedRank = rank;
            if (sharedHits > 1) {
                adjustedRank = rank + (sharedHits / 2);
            }
            float newScore = 1f - (adjustedRank / geneList.size());
            rank = rank + sharedHits;
            for (Gene g : geneScoreGeneList) {
                // System.out.print(g.getGeneSymbol()+"\t");
                g.setPriorityScore(newScore);
            }
        }

    }

    /**
     * Calculates the total priority score for this gene based on data stored in
     * its associated {@link jannovar.exome.Variant Variant} objects. Note that
     * for assumed autosomal recessive variants, the mean of the worst two
     * variants is taken, and for other modes of inheritance,the since worst
     * value is taken.
     * <P>
     * Note that we <b>assume that genes have been filtered for mode of
     * inheritance before this function is called. This means that we do not
     * need to apply separate filtering for mode of inheritance here</b>. The
     * only thing we need to watch out for is whether a variant is homozygous or
     * not (for autosomal recessive inheritance, these variants get counted
     * twice).
     *
     * @param modeOfInheritance Autosomal recessive, dominant, or X chromosomal
     * recessive.
     * @param pedigree of the effected individual
     */
    private static float calculateFilterScore(Gene gene, ModeOfInheritance modeOfInheritance, Pedigree pedigree) {
        if (gene.getVariantList().isEmpty()) {
            return 0f;
        }
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return calculateAutosomalRecessiveFilterScore(gene.getVariantList(), pedigree);
        } // not autosomal recessive

        return calculateNonAutosomalRecessiveFilterScore(gene.getVariantList());

    }

    /**
     * Calculate the combined priority score for this gene.
     * @param gene
     * @return 
     */
    private static float calculatePriorityScore(Gene gene) {
        float priorityScore = 1f;
        for (PriorityScore r : gene.getPriorityScoreMap().values()) {
            priorityScore *= r.getScore();
        }
        return priorityScore;
    }

    /**
     * For assumed autosomal recessive variants, this method calculates the mean
     * of the worst(highest numerical) two variants.
     *
     * @param variantEvaluations
     * @param pedigree
     * @return
     */
    private static float calculateAutosomalRecessiveFilterScore(List<VariantEvaluation> variantEvaluations, Pedigree pedigree) {
        float filterScore = 0f;
        List<Float> filterScores = new ArrayList<>();

        for (VariantEvaluation ve : variantEvaluations) {
            filterScores.add(ve.getFilterScore());
            GenotypeCall gc = ve.getVariant().getGenotype();
            if (pedigree.containsCompatibleHomozygousVariant(gc)) {
                //Add the value a second time, it is homozygous
                filterScores.add(ve.getFilterScore());
            }
        }
        //Sort in descending order
        Collections.sort(filterScores, Collections.reverseOrder());
        if (filterScores.size() < 2) {
            //Less than two variants, cannot be AR
            return filterScores.get(0);
        }
        float x = filterScores.get(0);
        float y = filterScores.get(1);
        filterScore = (x + y) / (2f);
        return filterScore;
    }

    /**
     * For other variants with non-autosomal recessive modes of inheritance, the
     * worst (highest numerical) value is taken.
     *
     * @param variantEvaluations
     * @return
     */
    private static float calculateNonAutosomalRecessiveFilterScore(List<VariantEvaluation> variantEvaluations) {
        List<Float> filterScores = new ArrayList<>();

        for (VariantEvaluation ve : variantEvaluations) {
            filterScores.add(ve.getFilterScore());
        }
        //Sort in descending order
        Collections.sort(filterScores, Collections.reverseOrder());
        //Not autosomal recessive, there is just one heterozygous mutation
        //thus return only the single best score.
        return filterScores.get(0);
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
    private static float calculateCombinedScore(Gene gene) {
        float filterScore = gene.getFilterScore();
        float priorityScore = gene.getPriorityScore();

        Set<PriorityType> prioritiesRun = gene.getPriorityScoreMap().keySet();

        //TODO: what if we ran all of these? It *is* *possible* to do so. 
        if (prioritiesRun.contains(PriorityType.EXOMISER_ALLSPECIES_PRIORITY)) {
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

}
