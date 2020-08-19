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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import com.google.common.collect.ListMultimap;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Used to 'finish' the Orphanet {@link DiseaseGene} associations by trying to produce a ternary Disease-Gene-MOI
 * association based off the OMIM and HPO annotations. The issue being that Orphanet maps:
 *     Orpha - OMIM Diseases
 *     Orpha - OMIM genes
 *     Orpha - MOIs
 * but there is no link between them like in OMIM. This D-G-MOI link is needed by Exomiser to score the Disease-Gene-Phenotype
 * match based on the passed variant(s) in a gene and the known mode of inheritance for the Disease-Gene link.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OrphanetDiseaseGeneFactory {

    private static final Logger logger = LoggerFactory.getLogger(OrphanetDiseaseGeneFactory.class);

    private final List<DiseaseGene> omimDiseases;
    private final ListMultimap<String, OrphaOmimMapping> orphaOmimMappings;
    private final ListMultimap<String, DiseaseGene> orphaDiseaseGenes;
    private final ListMultimap<String, InheritanceMode> inheritanceModesMap;

    public OrphanetDiseaseGeneFactory(List<DiseaseGene> omimDiseases, ListMultimap<String, OrphaOmimMapping> orphaOmimMappings, ListMultimap<String, DiseaseGene> orphaDiseaseGenes, ListMultimap<String, InheritanceMode> inheritanceModesMap) {
        this.omimDiseases = Objects.requireNonNull(omimDiseases);
        this.orphaOmimMappings = Objects.requireNonNull(orphaOmimMappings);
        this.orphaDiseaseGenes = Objects.requireNonNull(orphaDiseaseGenes);
        this.inheritanceModesMap = Objects.requireNonNull(inheritanceModesMap);
    }

    public List<DiseaseGene> buildDiseaseGeneAssociations() {

        // Use the OMIM DiseaseGene MOI as the MOI for the Orphanet Disease.
        // to do this we'll need to get the omim disease id mappings from the orphanet disease - omimDiseaseId
        // then create a map of known omim disease_id + omim gene id keys to MOI
        // e.g. OMIM:618531_OMIM:606936
//        Map<String, DiseaseGene> omimDiseaseGenes = omimDiseases.stream().collect(toMap(diseaseGeneKey(), Function.identity()));

        Map<String, DiseaseGene> omimDiseaseGenes = new HashMap<>();
        for (DiseaseGene omimDisease : omimDiseases) {
            String diseaseGeneKey = diseaseGeneKey().apply(omimDisease);
            DiseaseGene currentDiseaseGene = omimDiseaseGenes.get(diseaseGeneKey);
            // allow duplicate keys
            if (currentDiseaseGene != null) {
                //TODO: Decide on most relevant entry- should be ordered by DISEASE with a known MOI
                logger.debug("{} already in omimDiseaseGenes", diseaseGeneKey);
                logger.debug("Current: {}", currentDiseaseGene);
                logger.debug("Updated: {}", omimDisease);
            }
            omimDiseaseGenes.put(diseaseGeneKey, omimDisease);
        }

        List<DiseaseGene> orphanetDiseaseGenes = new ArrayList<>(orphaOmimMappings.size());
        for (Map.Entry<String, Collection<OrphaOmimMapping>> entry : orphaOmimMappings.asMap().entrySet()) {
            // match the ORPHA disease gene with the OMIM disease gene
            // temp map of all possible OMIM disease and gene ids for this ORPHA entry
            String orphaNum = entry.getKey();
            logger.debug("{} OMIM mappings:", orphaNum);
            List<DiseaseGene> currentDiseaseGenes = orphaDiseaseGenes.get(orphaNum);
            logger.debug("Mapping {} Orphanet DiseaseGenes to OMIM MOI", currentDiseaseGenes.size());
            int mappedOrphaDiseaseGenes = 0;
            for (DiseaseGene orphaDiseaseGene : currentDiseaseGenes) {
                boolean hasMatch = false;
                String orphaDisorderId = orphaDiseaseGene.getDiseaseId();
                List<InheritanceMode> orphanetMois = inheritanceModesMap.get(orphaDisorderId);
                for (OrphaOmimMapping omimMapping : entry.getValue()) {
                    String omimKey = omimMapping.getId() + "_" + orphaDiseaseGene.getOmimGeneId();
                    DiseaseGene omimDiseaseGene = omimDiseaseGenes.get(omimKey);
                    if (omimDiseaseGene != null) {
                        hasMatch = true;
                        mappedOrphaDiseaseGenes++;
                        logger.debug("D-G match! {}", omimKey);
                        logger.debug("  {}", omimDiseaseGene);
                        logger.debug("  {}", orphaDiseaseGene);
                        // merge the OMIM annotations with the Orphanet ones and return a new DiseaseGene
                        InheritanceMode inheritanceMode = decideInheritanceMode(omimDiseaseGene.getInheritanceMode(), orphanetMois);
                        DiseaseGene merged = DiseaseGene.builder()
                                .diseaseId(orphaDisorderId)
                                .diseaseName(orphaDiseaseGene.getDiseaseName())
                                .diseaseType(orphaDiseaseGene.getDiseaseType())
                                .omimGeneId(orphaDiseaseGene.getOmimGeneId())
                                .hgncId(orphaDiseaseGene.getHgncId())
                                .geneSymbol(orphaDiseaseGene.getGeneSymbol())
                                .geneName(orphaDiseaseGene.getGeneName())
                                .entrezGeneId(omimDiseaseGene.getEntrezGeneId())
                                .inheritanceMode(inheritanceMode)
                                .build();
                        orphanetDiseaseGenes.add(merged);
                    }
                }
                if (!hasMatch) {
                    logger.debug("No OMIM D-G match found for " + orphaDiseaseGene + " - using Orpha MOI mapping, if present");
                    InheritanceMode inheritanceMode = decideInheritanceMode(InheritanceMode.UNKNOWN, orphanetMois);
                    // *IF* there is only one OMIM disease-Gene association
                    DiseaseGene merged = DiseaseGene.builder()
                            .diseaseId(orphaDisorderId)
                            .diseaseName(orphaDiseaseGene.getDiseaseName())
                            .diseaseType(orphaDiseaseGene.getDiseaseType())
                            .omimGeneId(orphaDiseaseGene.getOmimGeneId())
                            .hgncId(orphaDiseaseGene.getHgncId())
                            .geneSymbol(orphaDiseaseGene.getGeneSymbol())
                            .geneName(orphaDiseaseGene.getGeneName())
                            .entrezGeneId(orphaDiseaseGene.getEntrezGeneId())
                            .inheritanceMode(inheritanceMode)
                            .build();
                    if (orphaDiseaseGene.getOmimGeneId().isEmpty()) {
                        // These might have a salvageable Entrez ID, but only via the ORPHA HGNC ID or they could
                        //  be mapped to a locus in OMIM
                        logger.debug("Missing OMIM Gene ID {}", merged);
                    } else {
                        // add it if there is a OMIM gene ID
                        orphanetDiseaseGenes.add(merged);
                    }
                }
            }
            logger.debug("{} mapped {} out of {} Orphanet disease genes", orphaNum, mappedOrphaDiseaseGenes, currentDiseaseGenes.size());
        }
        return orphanetDiseaseGenes;
    }

    private Function<DiseaseGene, String> diseaseGeneKey() {
        return diseaseGene -> {
            if (!diseaseGene.getDiseaseId().startsWith("OMIM") || diseaseGene.getOmimGeneId().isEmpty()) {
                throw new IllegalStateException("Must have OMIM disease and gene ID");
            }
            return diseaseGene.getDiseaseId() + "_" + diseaseGene.getOmimGeneId();
        };
    }

    private InheritanceMode decideInheritanceMode(InheritanceMode omimInheritanceMode, List<InheritanceMode> orphanetMois) {
        // defer to OMIM unless UNKNOWN
        if (omimInheritanceMode == InheritanceMode.UNKNOWN) {
            if (orphanetMois == null || orphanetMois.isEmpty()) {
                return InheritanceMode.UNKNOWN;
            }
            if (orphanetMois.size() == 1) {
                return orphanetMois.get(0);
            }
            if (orphanetMois.contains(InheritanceMode.AUTOSOMAL_DOMINANT) && orphanetMois.contains(InheritanceMode.AUTOSOMAL_RECESSIVE)) {
                return InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE;
            }
            return orphanetMois.get(0);
        }
        return omimInheritanceMode;
    }
}
