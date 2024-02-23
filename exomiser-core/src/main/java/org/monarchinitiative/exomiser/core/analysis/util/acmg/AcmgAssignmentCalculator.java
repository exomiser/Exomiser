/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @since 13.1.0
 */
public class AcmgAssignmentCalculator {

    private static final Disease EMPTY_DISEASE = Disease.builder().build();

    private final AcmgEvidenceAssigner acmgEvidenceAssigner;
    private final AcmgEvidenceClassifier acmgEvidenceClassifier;

    public AcmgAssignmentCalculator(AcmgEvidenceAssigner acmgEvidenceAssigner, AcmgEvidenceClassifier acmgEvidenceClassifier) {
        this.acmgEvidenceAssigner = acmgEvidenceAssigner;
        this.acmgEvidenceClassifier = acmgEvidenceClassifier;
    }

    /**
     * @param modeOfInheritance
     * @param gene
     * @param contributingVariants
     * @param compatibleDiseaseMatches
     * @return
     */
    public List<AcmgAssignment> calculateAcmgAssignments(ModeOfInheritance modeOfInheritance, Gene gene, List<VariantEvaluation> contributingVariants, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        Disease disease = findTopDiseaseMatch(compatibleDiseaseMatches);

        List<Disease> knownDiseases = findKnownDiseasesCompatibleWithMoi(modeOfInheritance, gene);

        // TODO: implement criteria assignment and classification of structural/copy-number variants!
        return contributingVariants.stream()
                .sorted(VariantEvaluation::compareByRank)
                .map(assignVariantAcmg(modeOfInheritance, gene, contributingVariants, knownDiseases, compatibleDiseaseMatches, disease))
                .toList();
    }

    private Disease findTopDiseaseMatch(List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        return compatibleDiseaseMatches.stream()
                .sorted(ModelPhenotypeMatch::compareTo)
                .map(ModelPhenotypeMatch::getModel)
                .findFirst()
                .orElse(EMPTY_DISEASE);
    }

    private List<Disease> findKnownDiseasesCompatibleWithMoi(ModeOfInheritance modeOfInheritance, Gene gene) {
        return gene.getAssociatedDiseases().stream()
                .filter(disease -> disease.getInheritanceMode().isCompatibleWith(modeOfInheritance))
                .toList();
    }

    private Function<VariantEvaluation, AcmgAssignment> assignVariantAcmg(ModeOfInheritance modeOfInheritance, Gene gene, List<VariantEvaluation> contributingVariants, List<Disease> knownDiseases, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches, Disease disease) {
        return variantEvaluation -> {
            AcmgEvidence acmgEvidence = acmgEvidenceAssigner.assignVariantAcmgEvidence(variantEvaluation, modeOfInheritance, contributingVariants, knownDiseases, compatibleDiseaseMatches);
            AcmgClassification acmgClassification = acmgEvidenceClassifier.classify(acmgEvidence);
            return AcmgAssignment.of(variantEvaluation, gene.getGeneIdentifier(), modeOfInheritance, disease, acmgEvidence, acmgClassification);
        };
    }
}
