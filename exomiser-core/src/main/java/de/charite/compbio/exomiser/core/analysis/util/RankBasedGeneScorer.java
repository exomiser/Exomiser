/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
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
public class RankBasedGeneScorer extends RawScoreGeneScorer {

    private static final Logger logger = LoggerFactory.getLogger(RankBasedGeneScorer.class);

    /**
     * Calculates the final ranks of all genes that have been run through the
     * filtering and prioritising steps. The strategy is that for autosomal
     * dominant diseases, we take the single most pathogenic score of any
     * variant affecting the gene; for autosomal recessive diseases, we take the
     * mean of the two most pathogenic variants. X-linked diseases are filtered
     * such that only X-chromosomal genes are left over, and the single worst
     * variant is taken.
     * <P>
     * Once the scores have been calculated, we sort the array list of
     * {@link exomizer.exome.Gene Gene} objects according to the combined
     * filter/priority score.
     *
     * In the original implementation of the Exomiser, the genes were scored
     * according to various criteria that gave them scores between [0,1].
     * However, these scores tended not to be uniformly distributed. This
     * function implements an alternative scoring scheme that first ranks the
     * genes according to their score and then overwrites the original score
     * according to a uniform distribution based on the ranks of the genes.
     * 
     * @param genes
     * @param modeOfInheritance
     */
    @Override
    public void scoreGenes(List<Gene> genes, Set<ModeOfInheritance> modesOfInheritance) {
        //first of all score the genes according to their raw scores
        for (Gene gene : genes) {
            scoreGene(gene, modesOfInheritance);
        }

        //now reset the scores according to their rank
        logger.info("Scoring genes by RANK.");
        // Store all gene and variant scores in sortable map
        // The key is a Float representing the raw score.
        // The value is a list of one or more genes with this score.
        TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<>();
        for (Gene gene : genes) {
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
            float newScore = 1f - (adjustedRank / genes.size());
            rank = rank + sharedHits;
            for (Gene g : geneScoreGeneList) {
                // System.out.print(g.getGeneSymbol()+"\t");
                g.setPriorityScore(newScore);
            }
        }
        
        Collections.sort(genes);
    }

}
