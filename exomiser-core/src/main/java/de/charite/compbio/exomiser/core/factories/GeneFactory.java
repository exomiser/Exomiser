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

    /**
     * Returns a list of genes from the JannovarData TranscriptModels.
     * @param jannovarData
     * @return
     */
    public static List<Gene> createKnownGenes(JannovarData jannovarData ) {
        List<Gene> knownGenes = createKnownGeneIdentifiers(jannovarData).entrySet().stream()
                .map(entry -> {
                    String geneId = entry.getKey();
                    String geneSymbol = entry.getValue();
                    if (geneId.equals(geneSymbol)) {
                        return new Gene(geneSymbol, -1);
                    }
                    //we're assuming Entrez ids here.
                    return new Gene(geneSymbol, Integer.parseInt(geneId));
                })
                .collect(toList());

        logger.info("Created {} known genes.", knownGenes.size());
        return knownGenes;
    }

    /**
     * Creates a map of gene identifiers to gene symbols.
     * @param jannovarData
     * @return a map of gene identifiers to gene symbol. In cases where there is no valid geneId (i.e. the transcript is
     * in a non-coding region) the symbol and identifier will be the same. As an example {BC038731=BC038731, Mir_378=Mir_378}.
     * A valid gene id/symbol pair will depend in the underlying data used to create the JannovarData. Currently Exomiser
     * uses Entrez gene ids which are plain integers.
     *
     */
    public static Map<String, String> createKnownGeneIdentifiers(JannovarData jannovarData) {
        ImmutableMap.Builder<String, String> geneIdentifiers = ImmutableMap.builder();
        int identifiers = 0;
        int noEntrezId = 0;
        for (String geneSymbol : jannovarData.getTmByGeneSymbol().keySet()) {
            Collection<TranscriptModel> transcriptModels = jannovarData.getTmByGeneSymbol().get(geneSymbol);
            String geneId = transcriptModels.stream()
                    .filter(Objects::nonNull)
                    .filter(transcriptModel -> transcriptModel.getGeneID() != null)
                    .filter(transcriptModel -> !transcriptModel.getGeneID().equals("null"))
                    // The gene ID is of the form "${NAMESPACE}${NUMERIC_ID}" where "NAMESPACE" is "ENTREZ"
                    // for UCSC. At this point, there is a hard dependency on using the UCSC database.
                    .map(transcriptModel -> transcriptModel.getGeneID().substring("ENTREZ".length()))
                    .distinct()
                    .findFirst()
                    .orElse(geneSymbol);

            if (geneId.equals(geneSymbol)) {
                noEntrezId++;
                logger.debug("No geneId associated with gene symbol {} geneId set to {}", geneSymbol, geneId);
            }
            identifiers++;
            geneIdentifiers.put(geneId, geneSymbol);
        }
        int geneIds = identifiers - noEntrezId;
        logger.info("Created {} gene identifiers ({} genes, {} without EntrezId)", identifiers, geneIds, noEntrezId);
        return geneIdentifiers.build();
    }

}
