/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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
package de.charite.compbio.exomiser.core.factories;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Creates a {@code List} of {@code Gene} from a {@code List} of
 * {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneFactory {

    private static final Logger logger = LoggerFactory.getLogger(GeneFactory.class);

    private GeneFactory (){}

    /**
     * Returns a list of {@code Gene} objects created from the supplied list of
     * {@code VariantEvaluation}. This list will be complete and contain all
     * genes regardless of whether the {@code VariantEvaluation} has passed any
     * filters or not.
     *
     * @param variantEvaluations
     * @return
     */
    public static List<Gene> createGenes(List<VariantEvaluation> variantEvaluations) {
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

    public static List<Gene> createKnownGenes(JannovarData jannovarData ) {
        List<Gene> knownGenes = createKnownGeneIdentifiers(jannovarData).entrySet().stream()
                .map(entry -> new Gene(entry.getValue(), Integer.parseInt(entry.getKey())))
                .collect(toList());

        logger.info("Created {} known genes.", knownGenes.size());
        return knownGenes;
    }

    public static Map<String, String> createKnownGeneIdentifiers(JannovarData jannovarData) {
        ImmutableMap.Builder<String, String> geneIdentifiers = ImmutableMap.builder();
        for (String geneSymbol : jannovarData.getTmByGeneSymbol().keySet()) {
            String geneId = "-1";
            Collection<TranscriptModel> transcriptModels = jannovarData.getTmByGeneSymbol().get(geneSymbol);
            for (TranscriptModel transcriptModel : transcriptModels) {
                if (transcriptModel != null && transcriptModel.getGeneID() != null && !transcriptModel.getGeneID().equals("null")) {
                    // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
                    // for UCSC. At this point, there is a hard dependency on using the UCSC database.
                    geneId = transcriptModel.getGeneID().substring("ENTREZ".length());
                }
            }
            if (geneId.equals("-1")) {
                logger.debug("No geneId associated with gene symbol {} geneId set to {}", geneSymbol, geneId);
            }
            geneIdentifiers.put(geneId, geneSymbol);
        }
        return geneIdentifiers.build();
    }

}
