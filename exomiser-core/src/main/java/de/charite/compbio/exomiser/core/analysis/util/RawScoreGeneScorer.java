/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        float filterScore = calculateFilterScore(gene.getPassedVariantEvaluations(), modeOfInheritance);
        gene.setFilterScore(filterScore);
        
        float priorityScore = setGenePriorityScore(gene);
        
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
    protected float calculateFilterScore(List<VariantEvaluation> variantEvaluations, ModeOfInheritance modeOfInheritance) {

        if (variantEvaluations.isEmpty()) {
            return 0f;
        }
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return calculateAutosomalRecessiveFilterScore(variantEvaluations);
        } // not autosomal recessive

        return calculateNonAutosomalRecessiveFilterScore(variantEvaluations);
    }

    private float setGenePriorityScore(Gene gene) {
        if (gene.getPriorityResults().isEmpty()) {
            return 0f;
        }
        float priorityScore = calculatePriorityScore(gene.getPriorityResults().values());
        gene.setPriorityScore(priorityScore);
        return priorityScore;
    }

    /**
     * Calculate the combined priority score for the gene.
     *
     * @param priorityScores of the gene
     * @return
     */
    protected float calculatePriorityScore(Collection<PriorityResult> priorityScores) {
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
    protected float calculateCombinedScore(float filterScore, float priorityScore, Set<PriorityType> prioritiesRun) {

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
    protected float calculateAutosomalRecessiveFilterScore(List<VariantEvaluation> variantEvaluations) {
        List<Float> hetFilterScores = new ArrayList<>();
        List<Float> homFilterScores = new ArrayList<>();

        for (VariantEvaluation ve : variantEvaluations) {
            // Realised original logic allows a cmphet to be calculated between a top scoring het and second place hom which is wrong 
            // Jannovar seems to currently be allowing hom_ref variants through so skip these as well
            if (variantIsHomozygousAlt(ve)){
                homFilterScores.add(ve.getVariantScore());
            }
            else if (variantIsHeterozygous(ve)){
                hetFilterScores.add(ve.getVariantScore());
            }
        }
        //maybe the variants were all crappy and nothing passed....
        if (hetFilterScores.isEmpty() && homFilterScores.isEmpty()) {
            return 0f;
        }
        sortFilterScoresInDecendingOrder(homFilterScores);
        sortFilterScoresInDecendingOrder(hetFilterScores);

        float bestCmpHetScore = 0f;
        float bestHomScore = 0f;
        if (hetFilterScores.size() >= 2) {
            bestCmpHetScore = calculateAverageOfFirstTwoScores(hetFilterScores);
        }
        if (!homFilterScores.isEmpty()){
            bestHomScore = homFilterScores.get(0);
        }
        return Float.max(bestHomScore, bestCmpHetScore);
    }
    
    private boolean variantIsHomozygousAlt(VariantEvaluation ve) {
        return ve.getVariantContext().getGenotype(0).isHomVar();
    }

    private boolean variantIsHomozygousRef(VariantEvaluation ve) {
        // below does not seem to work        
        //return ve.getVariantContext().getGenotype(0).isHomRef();
        if (ve.getGenotypeAsString().equals("0/0") || ve.getGenotypeAsString().equals("0|0")){
            return true;
        }
        return false;
    }
    
    private boolean variantIsHeterozygous(VariantEvaluation ve) {
        return ve.getVariantContext().getGenotype(0).isHet();
    }
    
    private void sortFilterScoresInDecendingOrder(List<Float> filterScores) {
        Collections.sort(filterScores, Collections.reverseOrder());
    }

    private float calculateAverageOfFirstTwoScores(List<Float> filterScores) {
        float x = filterScores.get(0);
        float y = filterScores.get(1);
        float filterScore = (x + y) / (2f);
        return filterScore;
    }

    /**
     * For other variants with non-autosomal recessive modes of inheritance, the
     * worst (highest numerical) value is taken.
     *
     * @param variantEvaluations
     * @return
     */
    protected float calculateNonAutosomalRecessiveFilterScore(List<VariantEvaluation> variantEvaluations) {
        List<Float> filterScores = new ArrayList<>();

        for (VariantEvaluation ve : variantEvaluations) {
            // TODO - should this check be used
            if (!variantIsHomozygousRef(ve)){
                filterScores.add(ve.getVariantScore());
            }
        }
        //maybe the variants were all crappy and nothing passed....
        if (filterScores.isEmpty()) {
            return 0f;
        }

        sortFilterScoresInDecendingOrder(filterScores);
        //Not autosomal recessive, there is just one heterozygous mutation
        //thus return only the single best score.
        return filterScores.get(0);
    }
}
