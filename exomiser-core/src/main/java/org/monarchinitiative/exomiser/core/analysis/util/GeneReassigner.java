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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis.util;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.stream.Collectors.toSet;

/**
 * Reassigns regulatory non-coding variants to the gene with the best phenotype score in a topological domain
 * (doi:10.1038/nature11082). 'Recent research shows that high-order chromosome structures make an important contribution
 * to enhancer functionality by triggering their physical interactions with target genes.' (doi:10.1038/nature12753).
 *
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @since 7.0.0
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

    /**
     * @param variantEvaluation
     * @return
     * @since 13.0.0
     */
    public VariantEvaluation reassignRegulatoryAndNonCodingVariantAnnotations(VariantEvaluation variantEvaluation) {
        // Caution! This won't function correctly if run before a prioritiser has been run
        if (variantEvaluation.getVariantEffect() == VariantEffect.REGULATORY_REGION_VARIANT) {
            return assignVariantToGeneWithHighestPhenotypeScoreInTad(variantEvaluation);
        }
        if (variantEvaluation.isSymbolic() || variantEvaluation.isCodingVariant() || isInUnknownGene(variantEvaluation)) {
            // very rarely a variant just has a single annotation with no gene i.e. geneSymbol is .
            // we don't want to move structural or coding variants.
            return variantEvaluation;
        }
        return reassignNonCodingVariantToMostPhenotypicallySimilarGeneInAnnotations(variantEvaluation);
    }

    private VariantEvaluation reassignNonCodingVariantToMostPhenotypicallySimilarGeneInAnnotations(VariantEvaluation variantEvaluation) {

        // Prior to v13.0.0 genes containing a hyphen were split, however these are often:
        // IG V genes e.g. IGLV6-57, IGLVV-58, IGLVIV-59,
        // Pseudogenes e.g. LL22NC03-30E12.13, LL22NC03-88E1.17
        // others might be read-through proteins e.g. PRR5-ARHGAP8
        // so we'll leave them as is.

        // Given we're looking at non-coding variants which make up ~95% of a WGS sample we want to avoid new object
        // allocation where possible.
        List<TranscriptAnnotation> transcriptAnnotations = variantEvaluation.getTranscriptAnnotations();
        if (transcriptAnnotations.size() <= 1) {
            return variantEvaluation;
        }

        // This is the most common case in WGS samples where there are hundreds of thousands of these.
        // having this here is marginally faster than the other path.
        if (transcriptAnnotations.size() == 2) {
            // For UPSTREAM_GENE_VARIANT, DOWNSTREAM_GENE_VARIANT and INTERGENIC_VARIANT variants there are 2 transcripts
            // annotated - one up and one downstream.
            // in these cases pick the most phenotypically relevant (phenotype score > 0) and return that transcript only
            return variantEvaluationWithHighestNonZeroPhenotypeScore(variantEvaluation, transcriptAnnotations.get(0), transcriptAnnotations
                    .get(1));
        }

        // check there are transcripts for more than 1 gene which passes geneFilters
        // and has a prioritiser score greater than zero
        List<Gene> uniqueGenes = findUniqueScoringGenes(transcriptAnnotations);
        if (uniqueGenes.isEmpty()) {
            return variantEvaluation;
        }
        uniqueGenes.sort(Comparator.comparingDouble(this::prioritiserScore).reversed());

        Gene geneWithHighestPhenotypeScoreOverZero = uniqueGenes.get(0);
        List<TranscriptAnnotation> transcriptsWithHighestPhenotypeScoreOverZero = getTranscriptAnnotationsForGene(geneWithHighestPhenotypeScoreOverZero, transcriptAnnotations);
        if (transcriptsWithHighestPhenotypeScoreOverZero.isEmpty()) {
            return variantEvaluation;
        }
        VariantEffect topVariantEffect = transcriptsWithHighestPhenotypeScoreOverZero.get(0).getVariantEffect();
        return buildVariantEvaluationAssignedToGene(variantEvaluation, geneWithHighestPhenotypeScoreOverZero, topVariantEffect, transcriptsWithHighestPhenotypeScoreOverZero);
    }

    private boolean isInUnknownGene(VariantEvaluation variantEvaluation) {
        return !allGenes.containsKey(variantEvaluation.getGeneSymbol());
    }

    private VariantEvaluation variantEvaluationWithHighestNonZeroPhenotypeScore(VariantEvaluation variantEvaluation, TranscriptAnnotation transcriptAnnotationOne, TranscriptAnnotation transcriptAnnotationTwo) {
        if (transcriptAnnotationOne.getGeneSymbol().equals(transcriptAnnotationTwo.getGeneSymbol())) {
            // Nothing to change
            return variantEvaluation;
        }

        Gene geneOne = allGenes.get(transcriptAnnotationOne.getGeneSymbol());
        Gene geneTwo = allGenes.get(transcriptAnnotationTwo.getGeneSymbol());

        if (isNotNullPassesFiltersAndHasNonZeroScore(geneOne) && prioritiserScore(geneOne) > prioritiserScore(geneTwo)) {
            return buildVariantEvaluationAssignedToGene(variantEvaluation, geneOne, transcriptAnnotationOne.getVariantEffect(), ImmutableList
                    .of(transcriptAnnotationOne));
        } else if (isNotNullPassesFiltersAndHasNonZeroScore(geneTwo)) {
            return buildVariantEvaluationAssignedToGene(variantEvaluation, geneTwo, transcriptAnnotationTwo.getVariantEffect(), ImmutableList
                    .of(transcriptAnnotationTwo));
        }
        // do nothing - there will be two transcripts associated still, but there is no phenotypic evidence for the genes
        // associated with either transcript
        return variantEvaluation;
    }

    private List<Gene> findUniqueScoringGenes(List<TranscriptAnnotation> transcriptAnnotations) {
        List<Gene> uniqueGenes = new ArrayList<>(transcriptAnnotations.size());
        Set<String> uniqueValues = new HashSet<>(transcriptAnnotations.size());
        for (int i = 0; i < transcriptAnnotations.size(); i++) {
            TranscriptAnnotation transcriptAnnotation = transcriptAnnotations.get(i);
            String geneSymbol = transcriptAnnotation.getGeneSymbol();
            if (uniqueValues.add(geneSymbol)) {
                Gene gene = allGenes.get(geneSymbol);
                if (isNotNullPassesFiltersAndHasNonZeroScore(gene)) {
                    uniqueGenes.add(gene);
                }
            }
        }
        return uniqueGenes;
    }

    private VariantEvaluation assignVariantToGeneWithHighestPhenotypeScoreInTad(VariantEvaluation variantEvaluation) {
        Gene currentlyAssignedGene = getCurrentlyAssignedGene(variantEvaluation);
        Set<Gene> genesInTad = getGenesInTadForVariant(variantEvaluation);

        Gene geneWithHighestPhenotypeScore = highestPhenotypeScoringGeneInTad(currentlyAssignedGene, genesInTad);
        //assign this to the variant's current gene as we don't necessarily want ALL the regulatory region variants to clump into one gene.
        double currentScore = prioritiserScore(currentlyAssignedGene);
        double bestScore = prioritiserScore(geneWithHighestPhenotypeScore);
        if (currentScore >= bestScore) {
            //don't move the assignment if there is nowhere better to go...
            return variantEvaluation;
        }

        if (geneWithHighestPhenotypeScore != null) {
            //given the physical ranges of topologically associated domains, the annotations are likely to be meaningless once reassigned
            //but try to find any anything matching the new gene symbol.
            List<TranscriptAnnotation> matchingGeneAnnotations = getTranscriptAnnotationsForGene(geneWithHighestPhenotypeScore, variantEvaluation
                    .getTranscriptAnnotations());
            // n.b it is quite likely that there will be no matching gene annotations as the variant is likely to have been
            // re-assigned to a gene far from its original genomic position.
            return buildVariantEvaluationAssignedToGene(variantEvaluation, geneWithHighestPhenotypeScore, VariantEffect.REGULATORY_REGION_VARIANT, matchingGeneAnnotations);
        }
        return variantEvaluation;
    }

    @Nullable
    private Gene getCurrentlyAssignedGene(VariantEvaluation variantEvaluation) {
        return allGenes.get(variantEvaluation.getGeneSymbol());
    }

    private Set<Gene> getGenesInTadForVariant(VariantEvaluation variantEvaluation) {
        return tadIndex.getRegionsContainingVariant(variantEvaluation).stream()
                .map(TopologicalDomain::getGenes)
                .flatMap(geneMap -> geneMap.keySet().stream())
                .map(allGenes::get)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    /*
     * CAUTION! This can return NULL in cases where the input gene is null.
     */
    @Nullable
    private Gene highestPhenotypeScoringGeneInTad(Gene assigned, Set<Gene> genesInTad) {
        Gene topScoringGene = genesInTad.stream()
                .max(Comparator.comparingDouble(this::prioritiserScore))
                .orElse(assigned);
        return prioritiserScore(topScoringGene) > prioritiserScore(assigned) ? topScoringGene : assigned;
    }

    private List<TranscriptAnnotation> getTranscriptAnnotationsForGene(Gene gene, List<TranscriptAnnotation> transcriptAnnotations) {
        ImmutableList.Builder<TranscriptAnnotation> topTranscriptsBuilder = new ImmutableList.Builder<>();
        for (int i = 0; i < transcriptAnnotations.size(); i++) {
            TranscriptAnnotation transcriptAnnotation = transcriptAnnotations.get(i);
            if (transcriptAnnotation.getVariantEffect() != null && transcriptAnnotation.getGeneSymbol()
                    .equals(gene.getGeneSymbol())) {
                topTranscriptsBuilder.add(transcriptAnnotation);
            }
        }
        return topTranscriptsBuilder.build();
    }

    private VariantEvaluation buildVariantEvaluationAssignedToGene(VariantEvaluation variantEvaluation, Gene gene, VariantEffect variantEffect, List<TranscriptAnnotation> transcriptAnnotations) {
        return variantEvaluation.toBuilder()
                .geneSymbol(gene.getGeneSymbol())
                .geneId(gene.getGeneId())
                .annotations(transcriptAnnotations)
                // Only reassign variant effect if it has not already been flagged by RegFeatureDao as a regulatory region variant.
                // Otherwise TAD reassignment and subsequent reg feature filter fail to work as expected
                .variantEffect(variantEvaluation.getVariantEffect() != VariantEffect.REGULATORY_REGION_VARIANT ? variantEffect : variantEvaluation
                        .getVariantEffect())
                .build();
    }

    private boolean isNotNullPassesFiltersAndHasNonZeroScore(Gene gene) {
        return gene != null && prioritiserScore(gene) > 0 && gene.passedFilters();
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
}
