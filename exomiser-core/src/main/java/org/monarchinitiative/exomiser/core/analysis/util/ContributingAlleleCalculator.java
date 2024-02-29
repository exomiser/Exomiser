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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Non-public helper class for finding contributing alleles.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
class ContributingAlleleCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ContributingAlleleCalculator.class);

    private final String probandId;
    private final Sex probandSex;
    private final CompHetAlleleCalculator compHetAlleleCalculator;
    private final IncompletePenetranceAlleleCalculator incompletePenetranceAlleleCalculator;

    ContributingAlleleCalculator(String probandId, Sex probandSex, InheritanceModeAnnotator inheritanceModeAnnotator) {
        this.probandId = probandId;
        this.probandSex = probandSex;
        this.compHetAlleleCalculator = new CompHetAlleleCalculator(inheritanceModeAnnotator);
        this.incompletePenetranceAlleleCalculator = new IncompletePenetranceAlleleCalculator(inheritanceModeAnnotator.getPedigree());
    }

    /**
     * Calculates the total priority score for the {@code VariantEvaluation} of
     * the gene. Note that for assumed autosomal recessive variants, the mean of the worst two variants scores is
     * taken, and for other modes of inheritance, the worst (highest numerical) value is taken.
     * <p>
     * Note that we <b>cannot assume that genes have been filtered for mode of
     * inheritance before this function is called. This means that we
     * need to apply separate filtering for mode of inheritance here</b>. We also
     * need to watch out for is whether a variant is homozygous or
     * not (for autosomal recessive inheritance, these variants get counted
     * twice).
     */
    protected List<VariantEvaluation> findContributingVariantsForInheritanceMode(ModeOfInheritance modeOfInheritance, List<VariantEvaluation> variantEvaluations) {
        List<VariantEvaluation> variantsCompatibleWithMode = variantEvaluations.stream()
                //It is critical only the PASS variants are used in the scoring
                .filter(VariantEvaluation::passedFilters)
                .filter(variantEvaluation -> variantEvaluation.isCompatibleWith(modeOfInheritance))
                .toList();
        //note these need to be filtered for the relevant ModeOfInheritance before being checked for the contributing variants
        if (variantsCompatibleWithMode.isEmpty()) {
            return variantsCompatibleWithMode;
        }
        return switch (modeOfInheritance) {
            case AUTOSOMAL_RECESSIVE ->
                    findAutosomalRecessiveContributingVariants(modeOfInheritance, variantsCompatibleWithMode);
            case X_RECESSIVE -> findXRecessiveContributingVariants(modeOfInheritance, variantsCompatibleWithMode);
            case ANY -> findIncompletePenetranceContributingVariants(modeOfInheritance, variantsCompatibleWithMode);
            default -> findNonAutosomalRecessiveContributingVariants(modeOfInheritance, variantsCompatibleWithMode);
        };

    }

    /**
     * @since 13.0.0
     */
    private List<VariantEvaluation> findIncompletePenetranceContributingVariants(ModeOfInheritance modeOfInheritance, List<VariantEvaluation> variantEvaluations) {
        logger.debug("Checking ANY mode for {}", variantEvaluations);
        List<VariantEvaluation> compatibleVariants = incompletePenetranceAlleleCalculator.findCompatibleVariants(variantEvaluations);
        return findNonAutosomalRecessiveContributingVariants(modeOfInheritance, compatibleVariants);
    }

    /**
     * @since 13.0.0
     */
    private List<VariantEvaluation> findXRecessiveContributingVariants(ModeOfInheritance modeOfInheritance, List<VariantEvaluation> variantEvaluations) {
        logger.debug("Checking XR mode for {}", variantEvaluations);
        if (variantEvaluations.isEmpty()) {
            return Collections.emptyList();
        }
        if (probandSex == Sex.FEMALE) {
            logger.debug("Proband is female - finding AR compatible alleles");
            return findAutosomalRecessiveContributingVariants(modeOfInheritance, variantEvaluations);
        }
        logger.debug("Proband is male/unknown - finding AD compatible alleles");
        // male and unknown treat as dominant
        return findNonAutosomalRecessiveContributingVariants(modeOfInheritance, variantEvaluations);
    }

    private List<VariantEvaluation> findAutosomalRecessiveContributingVariants(ModeOfInheritance modeOfInheritance, List<VariantEvaluation> variantEvaluations) {
        if (variantEvaluations.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<CompHetPair> bestCompHetPair = compHetAlleleCalculator.findCompatibleCompHetAlleles(variantEvaluations)
                .stream()
                .map(pair -> new CompHetPair(pair.get(0), pair.get(1)))
                .max(Comparator.comparing(CompHetPair::getScore));
        logger.debug("Best CompHet: {}", bestCompHetPair);

        Optional<VariantEvaluation> bestHomozygousAlt = variantEvaluations.stream()
                .filter(variantIsHomozygousAlt(probandId))
                .max(Comparator.comparing(VariantEvaluation::getVariantScore));
        logger.debug("Best HomAlt: {}", bestHomozygousAlt);

        // Realised original logic allows a comphet to be calculated between a top scoring het and second place hom which is wrong
        // Jannovar seems to currently be allowing hom_ref variants through so skip these as well
        double bestCompHetScore = bestCompHetPair
                .map(CompHetPair::getScore)
                .orElse(0.0);

        double bestHomAltScore = bestHomozygousAlt
                .map(VariantEvaluation::getVariantScore)
                .orElse(0f);

        double bestScore = Double.max(bestHomAltScore, bestCompHetScore);

        if (Double.compare(bestScore, bestCompHetScore) == 0 && bestCompHetPair.isPresent()) {
            CompHetPair compHetPair = bestCompHetPair.get();
            compHetPair.setContributesToGeneScoreUnderMode(modeOfInheritance);
            logger.debug("Top scoring AR is comp het: {}", compHetPair);
            return bestCompHetPair.get().getAlleles();
        } else if (bestHomozygousAlt.isPresent()) {
            VariantEvaluation topHomAlt = bestHomozygousAlt.get();
            topHomAlt.setContributesToGeneScoreUnderMode(modeOfInheritance);
            logger.debug("Top scoring AR is hom alt: {}", topHomAlt);
            return List.of(topHomAlt);
        }
        logger.debug("No AR candidate alleles found");
        return Collections.emptyList();
    }

    private List<VariantEvaluation> findNonAutosomalRecessiveContributingVariants(ModeOfInheritance modeOfInheritance, List<VariantEvaluation> variantEvaluations) {
        if (variantEvaluations.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<VariantEvaluation> bestVariant = variantEvaluations.stream()
                .max(Comparator.comparing(VariantEvaluation::getVariantScore));

        bestVariant.ifPresent(variantEvaluation -> variantEvaluation.setContributesToGeneScoreUnderMode(modeOfInheritance));

        return bestVariant.map(Collections::singletonList).orElseGet(Collections::emptyList);
    }

    private Predicate<VariantEvaluation> variantIsHomozygousAlt(String probandId) {
        return ve -> ve.getSampleGenotype(probandId).isHomAlt();
    }

    /**
     * Data class for holding pairs of alleles which are compatible with AR compound heterozygous inheritance.
     */
    private static final class CompHetPair {

        private final double score;
        private final VariantEvaluation allele1;
        private final VariantEvaluation allele2;

        CompHetPair(VariantEvaluation allele1, VariantEvaluation allele2) {
            this.allele1 = allele1;
            this.allele2 = allele2;
            this.score = calculateScore(allele1, allele2);
        }

        double calculateScore(VariantEvaluation allele1, VariantEvaluation allele2) {
            double allele1Score = allele1 == null ? 0 : allele1.getVariantScore();
            double allele2Score = allele2 == null ? 0 : allele2.getVariantScore();
            return (allele1Score + allele2Score) / 2.0;
        }

        double getScore() {
            return score;
        }

        List<VariantEvaluation> getAlleles() {
            List<VariantEvaluation> alleles = new ArrayList<>();
            if (null != allele1) {
                alleles.add(allele1);
            }
            if (null != allele2) {
                alleles.add(allele2);
            }
            return alleles;
        }

        void setContributesToGeneScoreUnderMode(ModeOfInheritance modeOfInheritance) {
            if (allele1 != null) {
                allele1.setContributesToGeneScoreUnderMode(modeOfInheritance);
            }
            if (allele2 != null) {
                allele2.setContributesToGeneScoreUnderMode(modeOfInheritance);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompHetPair that = (CompHetPair) o;
            return Double.compare(that.score, score) == 0 &&
                    Objects.equals(allele1, that.allele1) &&
                    Objects.equals(allele2, that.allele2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(allele1, allele2, score);
        }

        @Override
        public String toString() {
            return "CompHetPair{" +
                    "score=" + score +
                    ", allele1=" + allele1 +
                    ", allele2=" + allele2 +
                    '}';
        }
    }
}
