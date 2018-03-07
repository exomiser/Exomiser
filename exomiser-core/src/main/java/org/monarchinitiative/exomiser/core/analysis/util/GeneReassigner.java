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
package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.TopologicalDomain;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * Reassigns regulatory non-coding variants to the gene with the best phenotype score in a topological domain
 * (doi:10.1038/nature11082). 'Recent research shows that high-order chromosome structures make an important contribution
 * to enhancer functionality by triggering their physical interactions with target genes.' (doi:10.1038/nature12753).
 *
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneReassigner {

    private static final Logger logger = LoggerFactory.getLogger(GeneReassigner.class);

    private final PriorityType priorityType;
    private final ChromosomalRegionIndex<TopologicalDomain> tadIndex;
    private final Map<String, Gene> allGenes;

    /**
     * @param priorityType
     * @param tadIndex
     */
    public GeneReassigner(PriorityType priorityType, Map<String, Gene> allGenes, ChromosomalRegionIndex<TopologicalDomain> tadIndex) {
        this.tadIndex = tadIndex;
        this.allGenes = allGenes;
        this.priorityType = priorityType;
        logger.debug("Made new GeneReassigner for {}", priorityType);
    }

    public void reassignRegulatoryRegionVariantToMostPhenotypicallySimilarGeneInTad(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getVariantEffect() == VariantEffect.REGULATORY_REGION_VARIANT) {
            logger.debug("Checking gene assignment for {} chr={} pos={}", variantEvaluation.getVariantEffect(), variantEvaluation
                    .getChromosome(), variantEvaluation.getPosition());
            assignVariantToGeneWithHighestPhenotypeScore(variantEvaluation);
        }
    }

    private void assignVariantToGeneWithHighestPhenotypeScore(VariantEvaluation variantEvaluation) {
        Gene currentlyAssignedGene = getCurrentlyAssignedGene(variantEvaluation);
        //assign this to the variant's current gene as we don't necessarily want ALL the regulatory region variants to clump into one gene.
        double bestScore = prioritiserScore(currentlyAssignedGene);
        List<Gene> genesInTad = getGenesInTadForVariant(variantEvaluation);

//        Optional<Gene> bestScoringGeneInTad = genesInTad.stream().max(Comparator.comparingDouble(this::prioritiserScore));
//        logger.info("bestScoringGeneInTad {}", bestScoringGeneInTad);

        Gene geneWithHighestPhenotypeScore = null;
        for (Gene gene : genesInTad) {
            double geneScore = prioritiserScore(gene);
//            logger.info("Gene {} in TAD has score {}", gene.getGeneSymbol(), geneScore);
            if (geneScore > bestScore) {
                bestScore = geneScore;
                geneWithHighestPhenotypeScore = gene;
            }
        }
        if (prioritiserScore(currentlyAssignedGene) == bestScore) {
            //don't move the assignment if there is nowhere better to go...
            return;
        }
        if (geneWithHighestPhenotypeScore != null) {
            //given the physical ranges of topologically associated domains, the annotations are likely to be meaningless once reassigned
            //but try to find any anything matching the new gene symbol.
            String geneSymbol = geneWithHighestPhenotypeScore.getGeneSymbol();
            List<TranscriptAnnotation> matchingGeneAnnotations = getTranscriptAnnotationsMatchingGene(variantEvaluation.getTranscriptAnnotations(), geneSymbol);
            assignVariantToGene(variantEvaluation, geneWithHighestPhenotypeScore, matchingGeneAnnotations);
        }
    }

    private List<Gene> getGenesInTadForVariant(VariantEvaluation variantEvaluation) {
        return tadIndex.getRegionsContainingVariant(variantEvaluation).stream()
                .map(TopologicalDomain::getGenes)
                .flatMap(geneMap -> geneMap.keySet().stream())
                .map(allGenes::get)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private double prioritiserScore(Gene gene) {
        //Fix for issue 224 - check everywhere for nulls!
        if (gene == null || prioritiserHasNotRun(gene)) {
            return 0d;
        }
        return gene.getPriorityResult(priorityType).getScore();
    }

    private boolean prioritiserHasNotRun(Gene gene) {
        return !gene.getPriorityResults().containsKey(priorityType);
    }

    private List<TranscriptAnnotation> getTranscriptAnnotationsMatchingGene(List<TranscriptAnnotation> annotations, String geneSymbol) {
        List<TranscriptAnnotation> matchingGeneAnnotations = new ArrayList<>();
        for (TranscriptAnnotation annotation : annotations) {
            if (annotation.getGeneSymbol().equals(geneSymbol)) {
                matchingGeneAnnotations.add(annotation);
            }
        }
        return matchingGeneAnnotations;
    }

    private void assignVariantToGene(VariantEvaluation variant, Gene gene, List<TranscriptAnnotation> matchingGeneAnnotations) {
        logger.debug("Reassigning {} {}:{} {}->{} from {} to {}", variant.getVariantEffect(), variant.getChromosome(), variant
                .getPosition(), variant.getRef(), variant.getAlt(), variant
                .getGeneSymbol(), gene.getGeneSymbol());

        variant.setGeneSymbol(gene.getGeneSymbol());
        variant.setGeneId(gene.getGeneId());
        variant.setAnnotations(matchingGeneAnnotations);
    }

    public void reassignGeneToMostPhenotypicallySimilarGeneInAnnotations(VariantEvaluation variantEvaluation) {

        if (isInUnknownGene(variantEvaluation)) {
            // very rarely a variant just has a single annotation with no gene i.e. geneSymbol is .
            return;
        }

        List<String> geneSymbols = new ArrayList<>();
        List<VariantEffect> variantEffects = new ArrayList<>();
        List<TranscriptAnnotation> newAnnotations = new ArrayList<>();
        for (TranscriptAnnotation annotation : variantEvaluation.getTranscriptAnnotations()) {
            String geneSymbol = annotation.getGeneSymbol();
            geneSymbols.add(geneSymbol);
            variantEffects.add(annotation.getVariantEffect());
            newAnnotations.add(annotation);
            // hack to deal with fusion protein Jannovar nonsense -
            // ? should the separate genes not be part of the annotation anyway - don't seem to be, should maybe not do this split
            if (isValidFusionProtein(geneSymbol)) {
                String[] separateGeneSymbols = geneSymbol.split("-");
                for (String separateGeneSymbol : separateGeneSymbols) {
                    geneSymbols.add(separateGeneSymbol);
                    // for - split entries we do not know effect
                    variantEffects.add(VariantEffect.CUSTOM);
                    newAnnotations.add(null);
                }
            }
        }

//        logger.info("variantEffects: {}", variantEffects);
//        logger.info("newAnnotations: {}", newAnnotations);

        Gene currentlyAssignedGene = getCurrentlyAssignedGene(variantEvaluation);
        double bestScore = prioritiserScore(currentlyAssignedGene);
        Gene geneWithHighestPhenotypeScore = null;
        VariantEffect variantEffectForTopHit = null;
        TranscriptAnnotation bestAnnotation = null;

        for (int i = 0; i < geneSymbols.size(); i++) {
            Gene gene = allGenes.get(geneSymbols.get(i));
            double geneScore = prioritiserScore(gene);
            if (geneScore > bestScore) {
                bestScore = geneScore;
                geneWithHighestPhenotypeScore = gene;
                variantEffectForTopHit = variantEffects.get(i);
                //bestAnnotation can be null here
                bestAnnotation = newAnnotations.get(i);
//                logger.info("Best annotation is {}", bestAnnotation);
            }
        }
        // Keep original annotation if possible - used in RegFilter later on and for display
        List<TranscriptAnnotation> finalAnnotations = new ArrayList<>();
        if (bestAnnotation != null) {
            finalAnnotations.add(bestAnnotation);
        }

        if (prioritiserScore(currentlyAssignedGene) == bestScore) {
            //don't move the assignment if there is nowhere better to go...
            return;
        }
        if (geneWithHighestPhenotypeScore == null) {
            return;
        }

        if (variantEffectForTopHit == null) {
            // had one variant where this happened in Genomiser runs and breaks a lot of downstream code
            return;
        }
        /* Only reassign variant effect if it has not already been flagged by RegFeatureDao as a regulatory region variant.
        Otherwise TAD reassignment and subsequent reg feature filter fail to work as expected
        */
        if (variantEvaluation.getVariantEffect() != VariantEffect.REGULATORY_REGION_VARIANT) {
            variantEvaluation.setVariantEffect(variantEffectForTopHit);
        }
        assignVariantToGene(variantEvaluation, geneWithHighestPhenotypeScore, finalAnnotations);
    }

    private Gene getCurrentlyAssignedGene(VariantEvaluation variantEvaluation) {
        return allGenes.get(variantEvaluation.getGeneSymbol());
    }

    private boolean isInUnknownGene(VariantEvaluation variantEvaluation) {
        return !allGenes.containsKey(variantEvaluation.getGeneSymbol());
    }

    // avoid RP11-489C13.1 type annotations
    private boolean isValidFusionProtein(String geneSymbol) {
        return geneSymbol.contains("-") && !geneSymbol.contains(".");
    }

}
