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

package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.OMIMPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * IMPORTANT: It is required that all genes have been filtered and the {@link InheritanceModeAnalyser} has been run.
 * Ideally the genes should also have been run through a {@link org.monarchinitiative.exomiser.core.prioritisers.Prioritiser}
 *
 *
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class GenePriorityScoreCalculator {

    /**
     * Calculates the gene's prioritser score under a given mode of inheritance. This will depend on the modes of
     * inheritance a disease is known have and the inheritance modes a gene is compatible with for this sample based on
     * the prior filtration and inheritance mode compatibility scoring.
     *
     * @param gene              the current, filtered and prioritised gene of interest
     * @param modeOfInheritance the current mode of inheritance the score is to be calculated for
     * @return a score between 0 and 1
     */
    double calculateGenePriorityScoreForMode(Gene gene, ModeOfInheritance modeOfInheritance) {
        // Don't take the OmimPrioritiser score into account here as this is dependent on the known disease inheritance
        // modes and the current inheritance mode model
        double phenotypePrioritiserScore = gene.getPriorityResults().values().stream()
                .filter(isNonOmimPriorityResult())
                //In the original implementation this used to average the scores of all the priority results
                //but here we're going to just take the first - there should only be one at ths stage
                .findFirst()
                .orElseGet(ZeroScorePrioritserResult::new)
                .getScore();

        // Calculate the modifier here to down-rank genes where the mode of inheritance of known diseases doesn't match
        // that of the gene, based on the filtered variants or the current inheritance mode.
        // This will be between 0 and 1
        double knownDiseaseInheritanceModeModifier = calculateknownDiseaseInheritanceModeModifier(gene, modeOfInheritance);
        // if running OMIM only, do want to always return 0 or a 1 or 0.5 score?
        return phenotypePrioritiserScore * knownDiseaseInheritanceModeModifier;
    }

    private Predicate<PriorityResult> isNonOmimPriorityResult() {
        return priorityResult -> !OMIMPriorityResult.class.isInstance(priorityResult);
    }


    private double calculateknownDiseaseInheritanceModeModifier(Gene gene, ModeOfInheritance modeOfInheritance) {
        if (gene.getInheritanceModes().isEmpty() || modeOfInheritance == ModeOfInheritance.ANY) {
            return 1;
        }

        if (!gene.isCompatibleWith(modeOfInheritance)) {
            return 0.5;
        }

        //if we're still here check the compatibility of the gene against the known modes for the disease
        List<Disease> knownAssociatedDiseases = getAssociatedDiseasesForGene(gene);
        return knownAssociatedDiseases.stream()
                .filter(disease -> disease.getInheritanceMode() != InheritanceMode.UNKNOWN)
                .map(Disease::getInheritanceMode)
                .mapToDouble(scoreInheritanceMode(gene))
                .max()
                .orElse(1);
    }

    private List<Disease> getAssociatedDiseasesForGene(Gene gene) {
        OMIMPriorityResult omimPriorityResult = (OMIMPriorityResult) gene.getPriorityResults().get(PriorityType.OMIM_PRIORITY);
        if (null == omimPriorityResult) {
            return Collections.emptyList();
        }
        return omimPriorityResult.getAssociatedDiseases();
    }

    /**
         * This function checks whether the mode of inheritance of the disease
         * matches the observed pattern of variants. That is, if the disease is
         * autosomal recessive and we have just one heterozygous mutation, then the
         * disease is probably not the correct diagnosis, and we assign it a factor
         * of 0.5. Note that hemizygous X chromosomal variants are usually called as
         * homozygous ALT in VCF files, and thus it is not reliable to distinguish
         * between X-linked recessive and dominant inheritance. Therefore, we return
         * 1 for any gene with X-linked inheritance if the disease in question is
         * listed as X chromosomal.
         */
        private ToDoubleFunction<InheritanceMode> scoreInheritanceMode(Gene gene){
            return inheritanceMode -> {
                // not likely a rare-disease
                // gene only associated with somatic mutations or is polygenic
                if (inheritanceMode == InheritanceMode.SOMATIC || inheritanceMode == InheritanceMode.POLYGENIC) {
                    return 0.5;
                }

                // Y chromosomal, rare.
                if (inheritanceMode == InheritanceMode.Y_LINKED) {
                    return 1;
                }

                // Gene compatible with any known mode of inheritance for this disease?
                // If yes, we're good, otherwise down-rank this gene.
                return geneCompatibleWithInheritanceMode(gene, inheritanceMode)? 1 : 0.5;
            };
        }

    private boolean geneCompatibleWithInheritanceMode(Gene gene, InheritanceMode inheritanceMode) {
        /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
        if (gene.getInheritanceModes().isEmpty() || inheritanceMode == InheritanceMode.UNKNOWN) {
            return true;
        }
        Set<ModeOfInheritance> compatibleDiseaseModes = toCompatibleModes(inheritanceMode);
        //as long as the gene is compatible with at least one of the known modes for the disease we'll return the
        //default score
        for (ModeOfInheritance mode : compatibleDiseaseModes) {
            if (gene.isCompatibleWith(mode)) {
                return true;
            }
        }
        return false;
    }

    private Set<ModeOfInheritance> toCompatibleModes(InheritanceMode inheritanceMode) {
        switch (inheritanceMode) {
            case AUTOSOMAL_DOMINANT:
                return EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);
            case AUTOSOMAL_RECESSIVE:
                return EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
            case AUTOSOMAL_DOMINANT_AND_RECESSIVE:
                return EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
            case X_RECESSIVE:
                return EnumSet.of(ModeOfInheritance.X_RECESSIVE);
            case X_DOMINANT:
                return EnumSet.of(ModeOfInheritance.X_DOMINANT);
            case X_LINKED:
                return EnumSet.of(ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT);
            case MITOCHONDRIAL:
                return EnumSet.of(ModeOfInheritance.MITOCHONDRIAL);
            default:
                return EnumSet.noneOf(ModeOfInheritance.class);
        }
    }

    /**
     * Private {@link PriorityResult} implementation to return a zero score if nothing is found
     */
    private static class ZeroScorePrioritserResult implements PriorityResult {
        @Override
        public int getGeneId() {
            return 0;
        }

        @Override
        public String getGeneSymbol() {
            return null;
        }

        @Override
        public double getScore() {
            return 0;
        }

        @Override
        public PriorityType getPriorityType() {
            return PriorityType.NONE;
        }
    }
}

