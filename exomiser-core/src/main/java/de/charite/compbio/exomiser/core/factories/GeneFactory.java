/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import java.util.*;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

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
            int geneId = variantEvaluation.getEntrezGeneId();
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

    public List<Gene> createKnownGenes(JannovarData jannovarData ) {
        int approxKnownGenes = 23000;
        Set<Gene> knownGenes = new HashSet<>(approxKnownGenes);
        for (Map.Entry<String, TranscriptModel> geneModels : jannovarData.getTmByGeneSymbol().entries()) {
            TranscriptModel transcriptModel = geneModels.getValue();
            String geneSymbol = geneModels.getKey();
            if (geneSymbol == null) {
                geneSymbol = ".";
            }
            if (transcriptModel != null && transcriptModel.getGeneID() != null && !transcriptModel.getGeneID().equals("null")) {
                // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
                // for UCSC. At this point, there is a hard dependency on using the UCSC database.
                Gene gene = new Gene(geneSymbol, Integer.parseInt(transcriptModel.getGeneID().substring("ENTREZ".length())));
                knownGenes.add(gene);
            }
        }
        logger.info("Created {} known genes.", knownGenes.size());
        return knownGenes.stream().collect(toList());
    }

}
