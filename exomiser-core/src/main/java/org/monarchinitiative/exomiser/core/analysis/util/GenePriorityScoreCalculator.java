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
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

/**
 * IMPORTANT: It is required that all genes have been filtered and the {@link InheritanceModeAnalyser} has been run.
 * Ideally the genes should also have been run through a {@link org.monarchinitiative.exomiser.core.prioritisers.Prioritiser}
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

        // Get the modifier from OmimPrioritiser to down-rank genes where the mode of inheritance of known diseases doesn't match
        // that of the gene, based on the filtered variants or the current inheritance mode.
        // This will be between 0 and 1
        Map<ModeOfInheritance, Double> diseaseScoresByMode = getOmimPriorityResultScoresOrEmpty(gene);
        double knownDiseaseInheritanceModeModifier = diseaseScoresByMode.getOrDefault(modeOfInheritance, 1d);
        return phenotypePrioritiserScore * knownDiseaseInheritanceModeModifier;
    }

    private Map<ModeOfInheritance, Double> getOmimPriorityResultScoresOrEmpty(Gene gene) {
        PriorityResult omimPrioritiserResult = gene.getPriorityResult(PriorityType.OMIM_PRIORITY);
        if (omimPrioritiserResult == null || !OmimPriorityResult.class.isInstance(omimPrioritiserResult)) {
            return Collections.emptyMap();
        }
        OmimPriorityResult omimPriorityResult = (OmimPriorityResult) omimPrioritiserResult;
        return omimPriorityResult.getScoresByMode();
    }

    private Predicate<PriorityResult> isNonOmimPriorityResult() {
        return priorityResult -> !OmimPriorityResult.class.isInstance(priorityResult);
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

