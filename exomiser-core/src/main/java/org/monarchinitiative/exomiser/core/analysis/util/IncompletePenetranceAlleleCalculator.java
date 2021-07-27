/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.SampleData;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * @since 13.0.0
 */
public class IncompletePenetranceAlleleCalculator {

    private final Set<String> affectedSampleIdentifiers;

    public IncompletePenetranceAlleleCalculator(Pedigree pedigree) {
        this.affectedSampleIdentifiers = pedigree.getIndividuals().stream()
                .filter(Pedigree.Individual::isAffected)
                .map(Pedigree.Individual::getId)
                .collect(toUnmodifiableSet());
    }

    public List<VariantEvaluation> findCompatibleVariants(List<VariantEvaluation> variantEvaluations) {
        return variantEvaluations.stream()
                .filter(inAllAffected())
                .collect(toUnmodifiableList());
    }

    private Predicate<VariantEvaluation> inAllAffected() {
        return variantEvaluation -> {
            boolean inAllAffected = true;
            for (SampleData sampleData : variantEvaluation.getSampleGenotypes()) {
                SampleGenotype genotype = sampleData.getSampleGenotype();
                // ALL affected MUST contain the variant to be compatible with incomplete penetrance. However, it can also be present in unaffected.
                if (affectedSampleIdentifiers.contains(sampleData.getId()) && (genotype.isNoCall() || genotype.isHomRef() || genotype.isEmpty())) {
                    inAllAffected = false;
                }
            }
            return inAllAffected;
        };
    }
}
