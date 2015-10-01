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
 *
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

    public void reassignGeneToMostPhenotypicallySimilarGeneInTad(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.getVariantEffect() == VariantEffect.REGULATORY_REGION_VARIANT) {//Should this TAD check be only run for intergenic/up/downstream variants in reg features table? 
                //logger.info("Found reg variant - time to see if gene needs reassigning from current closest gene, " + variantEvaluation.getGeneSymbol() + ", to best pheno gene in TAD");
                float score = 0;
                List<String> genesInTad = variantDataService.getGenesInTad(variantEvaluation);
                for (String geneSymbol : genesInTad) {
                    Gene gene = allGenes.get(geneSymbol);
                    if (gene != null && (gene.getPriorityResult(priorityType)) != null) {
                        int entrezId = gene.getEntrezGeneID();
                        float geneScore = gene.getPriorityResult(priorityType).getScore();
                        //logger.info("Gene " + geneSymbol + " in TAD " + "has score " + geneScore);
                        if (geneScore > score) {
                            //logger.info("Changing gene to " + geneSymbol);
                            variantEvaluation.setEntrezGeneId(entrezId);
                            variantEvaluation.setGeneSymbol(geneSymbol);
                            List<Annotation> alist = Collections.emptyList();;
                            variantEvaluation.setAnnotations(alist);
                            score = geneScore;
                        }
                    }
                }
            }
        }
    }

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
