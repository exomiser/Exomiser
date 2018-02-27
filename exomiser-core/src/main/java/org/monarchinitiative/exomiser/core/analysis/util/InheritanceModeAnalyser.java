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
package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toMap;

/**
 * This class allows us to do segregation analysis for the variants supplied to
 * it i.e., to determine if they are compatible with autosomal recessive,
 * autosomal dominant, or X-linked recessive inheritance.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InheritanceModeAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceModeAnalyser.class);

    private final Set<ModeOfInheritance> wantedModes;
    private final InheritanceModeAnnotator inheritanceAnnotator;

    public InheritanceModeAnalyser(InheritanceModeAnnotator inheritanceModeAnnotator) {
        Objects.requireNonNull(inheritanceModeAnnotator);
        this.wantedModes = inheritanceModeAnnotator.getDefinedModes();
        this.inheritanceAnnotator = inheritanceModeAnnotator;
    }

    /**
     * Analyses the compatibility of a list of {@link Gene} with the {@link ModeOfInheritance} used in the constructor
     * of this class according to the observed pattern of inheritance in the {@link Pedigree}. This will only be applied
     * to genes and the variants in the gene which have *PASSED* filtering.
     */
    public void analyseInheritanceModes(Collection<Gene> genes) {
        genes.forEach(analyseInheritanceModes());
    }

    /**
     * Analyses the compatibility of a {@link Gene} with the {@link ModeOfInheritance} used in the constructor
     * of this class according to the observed pattern of inheritance in the {@link Pedigree}. This will only be applied
     * to the variants in the gene which have *PASSED* filtering.
     */
    public void analyseInheritanceModes(Gene gene) {
        analyseInheritanceModes().accept(gene);
    }

    /**
     * Analyses the compatibility of a {@link Gene} with the {@link ModeOfInheritance} used in the constructor
     * of this class according to the observed pattern of inheritance in the {@link Pedigree}. This will only be applied
     * to the variants in the gene which have *PASSED* filtering.
     */
    public Consumer<Gene> analyseInheritanceModes() {
        return gene -> {
            if (gene.passedFilters()) {
                checkInheritanceCompatibilityOfPassedVariants(gene);
            }
        };
    }

    private void checkInheritanceCompatibilityOfPassedVariants(Gene gene) {
        //it is *CRITICAL* that only the PASSED variantEvaluations are taken into account here.
        List<VariantEvaluation> passedVariantEvaluations = gene.getPassedVariantEvaluations();
        if (passedVariantEvaluations.size() > 1000) {
            logger.info("Warning - this will take a while. Testing gene {} with {} passed variants for inheritance mode compatibility!", gene
                    .getGeneSymbol(), passedVariantEvaluations.size());
        }

        Map<ModeOfInheritance, List<VariantEvaluation>> compatibleInheritanceModes = inheritanceAnnotator
                .computeCompatibleInheritanceModes(passedVariantEvaluations);

        compatibleInheritanceModes.forEach((mode, variants) -> logger.debug("{} {}", mode, variants));

        Map<ModeOfInheritance, List<VariantEvaluation>> filteredModes = compatibleInheritanceModes.entrySet().stream()
                .filter(entry -> wantedModes.contains(entry.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!filteredModes.isEmpty()) {
            setCompatibleInheritanceModes(gene, filteredModes);
        }
    }

    private void setCompatibleInheritanceModes(Gene gene, Map<ModeOfInheritance, List<VariantEvaluation>> filteredModes) {
        logger.debug("Gene {} has variants compatible with {}:", gene.getGeneSymbol(), filteredModes.keySet());
        gene.setCompatibleInheritanceModes(filteredModes.keySet());
        Map<VariantEvaluation, Set<ModeOfInheritance>> variantCompatibilities = mapVariantsToCompatibleModes(filteredModes);
        variantCompatibilities.forEach(VariantEvaluation::setCompatibleInheritanceModes);
    }

    private Map<VariantEvaluation, Set<ModeOfInheritance>> mapVariantsToCompatibleModes(Map<ModeOfInheritance, List<VariantEvaluation>> compatibleMap) {
        Map<VariantEvaluation, Set<ModeOfInheritance>> variantsToCompatibleModes = new HashMap<>();
        for (Map.Entry<ModeOfInheritance, List<VariantEvaluation>> entry : compatibleMap.entrySet()){
            ModeOfInheritance currentMode = entry.getKey();
            if (wantedModes.contains(currentMode)) {
                List<VariantEvaluation> variants = entry.getValue();
                for (VariantEvaluation variant : variants) {
                    variantsToCompatibleModes.computeIfAbsent(variant, variantEvaluation -> new HashSet<>()).add(currentMode);
                }
            }
        }
        variantsToCompatibleModes.forEach((variantEvaluation, compatibleModes) -> logger.debug("{} {}", variantEvaluation, compatibleModes));
        return variantsToCompatibleModes;
    }

}
