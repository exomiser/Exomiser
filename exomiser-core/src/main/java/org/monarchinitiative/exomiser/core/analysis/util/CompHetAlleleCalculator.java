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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class CompHetAlleleCalculator {

    private final InheritanceModeAnnotator inheritanceAnnotator;

    CompHetAlleleCalculator(Pedigree pedigree) {
        this.inheritanceAnnotator = new InheritanceModeAnnotator(pedigree);
    }

    /**
     * Finds pairs of alleles compatible with autosomal recessive compound heterozygous inheritance according to the
     * pedigree supplied in the class constructor. This will work independently of the mode of inheritance specified in
     * the class constructor.
     *
     * @param passedVariantEvaluations
     * @return a list of allele pairs compatible with an autosomal recessive compound heterozygous inheritance pattern.
     */
    public List<List<VariantEvaluation>> findCompatibleCompHetAlleles(List<VariantEvaluation> passedVariantEvaluations) {
        //Cant't be comp het if there's only one allele.
        if (passedVariantEvaluations.size() <= 1) {
            return Collections.emptyList();
        }

        List<List<VariantEvaluation>> compatibleAllelePairs = new ArrayList<>();
        //don't do all vs all otherwise we'll get the reciprocal pairs being tested so only check one side of the diagonal
        for (int i = 0; i < passedVariantEvaluations.size(); i++) {
            for (int j = i + 1; j < passedVariantEvaluations.size(); j++) {
                VariantEvaluation ve1 = passedVariantEvaluations.get(i);
                VariantEvaluation ve2 = passedVariantEvaluations.get(j);
                if (!ve1.equals(ve2) && isCompHetCompatible(ve1, ve2)) {
                    compatibleAllelePairs.add(ImmutableList.of(ve1, ve2));
                }
            }
        }
        return ImmutableList.copyOf(compatibleAllelePairs);
    }

    private boolean isCompHetCompatible(VariantEvaluation ve1, VariantEvaluation ve2) {
        List<VariantEvaluation> pair = Arrays.asList(ve1, ve2);
        Map<SubModeOfInheritance, List<VariantEvaluation>> compatibleSubModesMap = inheritanceAnnotator
                .computeCompatibleInheritanceSubModes(pair);
        if (compatibleSubModesMap.containsKey(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET)) {
            List<VariantEvaluation> compHetPair = compatibleSubModesMap.get(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET);
            return compHetPair.size() == 2;
        } else if (compatibleSubModesMap.containsKey(SubModeOfInheritance.X_RECESSIVE_COMP_HET)) {
            List<VariantEvaluation> compHetPair = compatibleSubModesMap.get(SubModeOfInheritance.X_RECESSIVE_COMP_HET);
            return compHetPair.size() == 2;
        }
        return false;
    }
}
