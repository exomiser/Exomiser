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
package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Creates a {@code List} of {@code Gene} from a {@code List} of
 * {@code VariantEvaluation}.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GeneFactory {

    private static final Logger logger = LoggerFactory.getLogger(GeneFactory.class);

    private final JannovarData jannovarData;

    private Set<GeneIdentifier> geneIdentifiers;

    public GeneFactory(JannovarData jannovarData) {
        this.jannovarData = jannovarData;
    }

    /**
     * Returns a list of genes from the JannovarData TranscriptModels.
     * @return a mutable list of {@link Gene} objects. DO NOT SHARE THESE. If you need a new list, call this method again.
     */
    public List<Gene> createKnownGenes() {
        List<Gene> knownGenes = getGeneIdentifiers().stream()
                // We're assuming the GeneIdentifier includes Entrez ids here. They should be present.
                // If not the entire analysis will fail.
                .map(Gene::new)
                .collect(toList()); // MUST BE MUTABLE
        logger.debug("Created {} known genes.", knownGenes.size());
        return knownGenes;
    }

    /**
     * @return an immutable set of {@link GeneIdentifier} objects.
     */
    public Set<GeneIdentifier> getGeneIdentifiers() {
        return geneIdentifiers == null ? createKnownGeneIds() : geneIdentifiers;
    }

    private Set<GeneIdentifier> createKnownGeneIds() {
        ImmutableSet.Builder<GeneIdentifier> geneIdentifierBuilder = ImmutableSet.builder();
        int identifiers = 0;
        int noEntrezId = 0;
        for (String geneSymbol : jannovarData.getTmByGeneSymbol().keySet()) {
            Collection<TranscriptModel> transcriptModels = jannovarData.getTmByGeneSymbol().get(geneSymbol);
            GeneIdentifier geneIdentifier = transcriptModels.stream()
                    .filter(Objects::nonNull)
                    .filter(transcriptModel -> transcriptModel.getGeneID() != null)
                    .filter(transcriptModel -> !transcriptModel.getGeneID().equals("null"))
                    .map(toGeneIdentifier())
                    .distinct()
                    .findFirst()
                    .orElse(GeneIdentifier.builder().geneSymbol(geneSymbol).build());

            if (geneIdentifier.getEntrezId().isEmpty()) {
                noEntrezId++;
                logger.debug("No geneId associated with gene symbol {} geneId set to {}", geneSymbol, geneIdentifier);
            }
            identifiers++;
            geneIdentifierBuilder.add(geneIdentifier);
        }
        int geneIds = identifiers - noEntrezId;
        logger.debug("Created {} gene identifiers ({} genes, {} without EntrezId)", identifiers, geneIds, noEntrezId);
        geneIdentifiers = geneIdentifierBuilder.build();
        return geneIdentifiers;
    }

    private Function<TranscriptModel, GeneIdentifier> toGeneIdentifier() {
        //logger.info("{} {} {} {}", transcriptModel.getGeneSymbol(), transcriptModel.getGeneID(), transcriptModel.getAccession(), transcriptModel.getAltGeneIDs());
        //Using ucsc_hg19: LMOD1 ENTREZ25802 uc010ppu.2 null (pre-jannovar 0.19)
        //Using hg19_ucsc: LMOD1 25802 uc010ppu.2 {CCDS_ID=CCDS53457, COSMIC_ID=LMOD1, ENSEMBL_GENE_ID=ENSG00000163431, ENTREZ_ID=25802, HGNC_ALIAS=64kD|D1|1D, HGNC_ID=HGNC:6647, HGNC_PREVIOUS=, HGNC_SYMBOL=LMOD1, MGD_ID=MGI:2135671, OMIM_ID=602715, PUBMED_ID=, REFSEQ_ACCESSION=NM_012134, RGD_ID=RGD:1307236, UCSC_ID=uc057oju.1, UNIPROT_ID=P29536, VEGA_ID=OTTHUMG00000035802}
        //Using hg19_ensembl: LMOD1 ENSG00000163431 ENST00000367288 {CCDS_ID=CCDS53457, COSMIC_ID=LMOD1, ENSEMBL_GENE_ID=ENSG00000163431, ENTREZ_ID=25802, HGNC_ALIAS=64kD|D1|1D, HGNC_ID=HGNC:6647, HGNC_PREVIOUS=, HGNC_SYMBOL=LMOD1, MGD_ID=MGI:2135671, OMIM_ID=602715, PUBMED_ID=, REFSEQ_ACCESSION=NM_012134, RGD_ID=RGD:1307236, UCSC_ID=uc057oju.1, UNIPROT_ID=P29536, VEGA_ID=OTTHUMG00000035802}
        return transcriptModel -> {
            String geneId = transcriptModel.getGeneID();
            String geneSymbol = transcriptModel.getGeneSymbol();
            Map<String, String> altGeneIds = transcriptModel.getAltGeneIDs();
            return GeneIdentifier.builder()
                    .geneSymbol(geneSymbol)
                    .geneId((geneId == null || geneId.equals("null"))? "" : geneId)
                    .hgncId(altGeneIds.getOrDefault("HGNC_ID", ""))
                    .hgncSymbol(altGeneIds.getOrDefault("HGNC_SYMBOL", ""))
                    .entrezId(altGeneIds.getOrDefault("ENTREZ_ID", ""))
                    .ensemblId(altGeneIds.getOrDefault("ENSEMBL_GENE_ID", ""))
                    .ucscId(altGeneIds.getOrDefault("UCSC_ID", ""))
                    .build();
        };
    }

}
