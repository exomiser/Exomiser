/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.analysis.AbstractAnalysisRunner;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ds5
 */
public class TadGeneReassigner {

    private final VariantDataService variantDataService;

    private static final Logger logger = LoggerFactory.getLogger(TadGeneReassigner.class);

    public TadGeneReassigner(VariantDataService variantDataService) {
        this.variantDataService = variantDataService;
    }

    public void reassignGeneToMostPhenotypicallySimilarGeneInTad(List<VariantEvaluation> variantEvaluations, Map<String, Gene> allGenes) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.getVariantEffect() == VariantEffect.REGULATORY_REGION_VARIANT) {//Should this TAD check be only run for intergenic/up/downstream variants in reg features table? 
                /* TODO
                 1. Need to pass correct PriorityType through as well
                 */
                logger.info("Found reg variant - time to see if gene needs reassigning from current closest gene, " + variantEvaluation.getGeneSymbol() + ", to best pheno gene in TAD");
                float score = 0;
                List<String> genesInTad = variantDataService.getGenesInTad(variantEvaluation);
                for (String geneSymbol : genesInTad) {
                    Gene gene = allGenes.get(geneSymbol);
                    if (gene != null) {
                        int entrezId = gene.getEntrezGeneID();
                        float geneScore = gene.getPriorityResult(PriorityType.HIPHIVE_PRIORITY).getScore();
                        logger.info("Gene " + geneSymbol + " in TAD " + "has score " + geneScore);
                        if (geneScore > score) {
                            logger.info("Changing gene to " + geneSymbol);
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

}
