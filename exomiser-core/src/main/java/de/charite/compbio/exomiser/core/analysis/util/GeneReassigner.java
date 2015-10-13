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

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ds5
 */
public class GeneReassigner {

    private final VariantDataService variantDataService;
    private final PriorityType priorityType;

    private static final Logger logger = LoggerFactory.getLogger(GeneReassigner.class);

    public GeneReassigner(VariantDataService variantDataService, PriorityType priorityType) {
        this.variantDataService = variantDataService;
        this.priorityType = priorityType;
    }

    //this always runs, but only after variant filtering - note the REGULATORY_REGION_VARIANT, this is assigned by the
    public void reassignVariantsToMostPhenotypicallySimilarGeneInTad(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.getVariantEffect() == VariantEffect.REGULATORY_REGION_VARIANT) {
                assignVariantToGeneWithHighestPhenotypeScore(variantEvaluation, allGenes);
            }
        }
    }

    //check - -this is only run in the geneFilterPredicate of the PassOnlyAnalysisRunner
    public void reassignVariantToMostPhenotypicallySimilarGeneInTad(VariantEvaluation variantEvaluation, Map<String, Gene> allGenes) {
        if (variantEvaluation.getVariantEffect() == VariantEffect.INTERGENIC_VARIANT || variantEvaluation.getVariantEffect() == VariantEffect.UPSTREAM_GENE_VARIANT) {
            assignVariantToGeneWithHighestPhenotypeScore(variantEvaluation, allGenes);
        }
    }

    private void assignVariantToGeneWithHighestPhenotypeScore(VariantEvaluation variantEvaluation, Map<String, Gene> allGenes) {
        Gene geneWithHighestPhenotypeScore = null;
        float bestScore = 0;
        List<String> genesInTad = variantDataService.getGenesInTad(variantEvaluation);
        for (String geneSymbol : genesInTad) {
            Gene gene = allGenes.get(geneSymbol);
            if (gene != null && (gene.getPriorityResult(priorityType)) != null) {
                float geneScore = gene.getPriorityResult(priorityType).getScore();
                //logger.info("Gene " + geneSymbol + " in TAD " + "has score " + geneScore);
                if (geneScore > bestScore) {
                    bestScore = geneScore;
                    geneWithHighestPhenotypeScore = gene;
                }
            }
        }
        assignVariantToGene(variantEvaluation, geneWithHighestPhenotypeScore);
    }

    private void assignVariantToGene(VariantEvaluation variantEvaluation, Gene gene) {
        if (gene == null) {
            return;
        }
        //logger.info("Changing gene to " + geneSymbol);
        variantEvaluation.setEntrezGeneId(gene.getEntrezGeneID());
        variantEvaluation.setGeneSymbol(gene.getGeneSymbol());
        //given the physical ranges of topologically associated domains, the annotations are likely to be meaningless once reassigned
        //TODO: check if the reassigned gene is actually in the annotation list and include this.
        variantEvaluation.setAnnotations(Collections.emptyList());
    }

    //Ignore this - this is an attempt to fix Jannovar/Annotation issues.
    public void reassignGeneToMostPhenotypicallySimilarGeneInAnnotations(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
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
                    float geneScore = gene.getPriorityResult(priorityType).getScore();
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
