/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a {@code List} of {@code Gene} from a {@code List} of
 * {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneFactory {

    private static final Logger logger = LoggerFactory.getLogger(GeneFactory.class);

    /**
     * Returns a list of {@code Gene} objects created from the supplied list of
     * {@code VariantEvaluation}. This list will be complete and contain all
     * genes regardless of whether the {@code VariantEvaluation} has passed any
     * filters or not.
     *
     * @param variantEvaluations
     * @return
     */
    public List<Gene> createGenes(List<VariantEvaluation> variantEvaluations) {
        //Record the genes we have seen before.
        Map<String, Gene> geneMap = new HashMap<>();

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            String geneSymbol = variantEvaluation.getGeneSymbol();
            int geneId = variantEvaluation.getEntrezGeneID();
            if (geneSymbol != null && !".".equals(geneSymbol)) {
                // Off target variants do not have gene-symbols.
                // This if avoids null pointers
                if (geneMap.containsKey(geneSymbol)) {
                    Gene gene = geneMap.get(geneSymbol);
                    gene.addVariant(variantEvaluation);
                } else {
                    Gene gene = new Gene(geneSymbol, geneId);
                    gene.addVariant(variantEvaluation);
                    geneMap.put(geneSymbol, gene);
                }
            }
        }
        logger.info("Made {} genes from {} variants", geneMap.values().size(), variantEvaluations.size());
        return new ArrayList<>(geneMap.values());
    }
    
}
