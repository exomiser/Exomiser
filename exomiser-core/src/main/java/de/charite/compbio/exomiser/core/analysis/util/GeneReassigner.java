/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.TopologicalDomain;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    /**
     * @param tadIndex
     * @param priorityType
     */
    public GeneReassigner(ChromosomalRegionIndex<TopologicalDomain> tadIndex, PriorityType priorityType) {
        this.priorityType = priorityType;
        this.tadIndex = tadIndex;
        logger.info("Made new GeneReassigner for {}", priorityType);
    }

    //TODO: consider scenario for PrioritiserType.NONE
    public void reassignVariantToMostPhenotypicallySimilarGeneInTad(VariantEvaluation variantEvaluation, Map<String, Gene> allGenes) {
        if (variantEvaluation.getVariantEffect() == VariantEffect.REGULATORY_REGION_VARIANT) {
            logger.debug("Checking gene assignment for {} chr={} pos={}", variantEvaluation.getVariantEffect(), variantEvaluation.getChromosome(), variantEvaluation.getPosition());
            assignVariantToGeneWithHighestPhenotypeScore(variantEvaluation, allGenes);
        }
    }

    private void assignVariantToGeneWithHighestPhenotypeScore(VariantEvaluation variantEvaluation, Map<String, Gene> allGenes) {
        Gene geneWithHighestPhenotypeScore = null;
        Gene currentlyAssignedGene = allGenes.get(variantEvaluation.getGeneSymbol());
        //assign this to the variant's current gene as we don't necessarily want ALL the regulatory region variants to clump into one gene.
        float bestScore = prioritiserScore(currentlyAssignedGene);
        List<String> genesInTad = getGenesInTadForVariant(variantEvaluation);

        for (String geneSymbol : genesInTad) {
            Gene gene = allGenes.get(geneSymbol);
            logger.debug("Checking gene {}", gene);
            if (gene != null && (gene.getPriorityResult(priorityType)) != null) {
                float geneScore = prioritiserScore(gene);
                logger.debug("Gene {} in TAD has score {}", geneSymbol, geneScore);
                if (geneScore > bestScore) {
                    bestScore = geneScore;
                    geneWithHighestPhenotypeScore = gene;
                }
            }
        }
        if (prioritiserScore(currentlyAssignedGene) == bestScore) {
            //don't move the assignment if there is nowhere better to go...
            return;
        }
        if (geneWithHighestPhenotypeScore == null) {
            return;
        }
        assignVariantToGene(variantEvaluation, geneWithHighestPhenotypeScore);
    }

    private float prioritiserScore(Gene gene) {
        return gene.getPriorityResult(priorityType).getScore();
    }

    private List<String> getGenesInTadForVariant(VariantEvaluation variantEvaluation) {
        List<TopologicalDomain> tadsContainingVariant = tadIndex.getRegionsContainingVariant(variantEvaluation);

        return tadsContainingVariant.stream()
                .map(TopologicalDomain::getGenes)
                .flatMap(geneMap -> geneMap.keySet().stream())
                .collect(toList());
    }

    private void assignVariantToGene(VariantEvaluation variantEvaluation, Gene gene) {
        logger.debug("Reassigning {} {}:{} {}->{} from {} to {}", variantEvaluation.getVariantEffect(), variantEvaluation.getChromosome(), variantEvaluation.getPosition(), variantEvaluation.getRef(), variantEvaluation.getAlt(), variantEvaluation.getGeneSymbol(), gene.getGeneSymbol());
        variantEvaluation.setEntrezGeneId(gene.getEntrezGeneID());
        variantEvaluation.setGeneSymbol(gene.getGeneSymbol());
        //given the physical ranges of topologically associated domains, the annotations are likely to be meaningless once reassigned
        //TODO: check if the reassigned gene is actually in the annotation list and include this.
        variantEvaluation.setAnnotations(Collections.emptyList());
    }

    //Ignore this for the time being it should be public - this is an attempt to fix Jannovar/Annotation issues.
    private void reassignGeneToMostPhenotypicallySimilarGeneInAnnotations(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            List<Annotation> annotations = variantEvaluation.getAnnotations();
            float score = 0;
            Set<String> geneSymbols = new HashSet<>();
            for (Annotation a : annotations) {
                String geneSymbol = a.getGeneSymbol();
                //logger.info("Annotation to " + a.getGeneSymbol() + " has variantEffect " + a.getMostPathogenicVarType().toString());
                // hack to deal with fusion protein Jannovar nonsense
                String[] separateGeneSymbols = geneSymbol.split("-");
                for (String separateGeneSymbol : separateGeneSymbols) {
                    geneSymbols.add(separateGeneSymbol);
                }
            }
            for (String geneSymbol : geneSymbols) {
                Gene gene = allGenes.get(geneSymbol);
                if (gene != null && (gene.getPriorityResult(priorityType)) != null) {
                    int entrezId = gene.getEntrezGeneID();
                    float geneScore = prioritiserScore(gene);
                    //logger.info("Gene " + geneSymbol + " in possible annotations for variant " + variantEvaluation.getChromosomalVariant() + "has score " + geneScore);
                    if (geneScore > score) {
                        //logger.info("!!!!! Want to change gene from " + variantEvaluation.getGeneSymbol() + " to " + geneSymbol + " as has the better phenotype score");
                        // Doing the below will fix some of the Jannovar assignment issues seen in our benchmarking but currently breaks the tests as it reassigns one of the test variants! Not sure what to do
                        // Also we may be reassigning from a good MISSENSE variant in one gene to something dodgy in another - needs some more thought
                        //variantEvaluation.setEntrezGeneId(entrezId);
                        //variantEvaluation.setGeneSymbol(geneSymbol);
                        score = geneScore;
                    }
                }
            }
        }
    }

}
