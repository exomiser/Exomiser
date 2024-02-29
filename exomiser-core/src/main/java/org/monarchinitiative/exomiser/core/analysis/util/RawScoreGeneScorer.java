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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.*;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

/**
 * Class for scoring Genes according to their phenotype similarity to the proband, the filtered variants and the
 * inheritance mode under which these would have an effect.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class RawScoreGeneScorer implements GeneScorer {

    private static final Logger logger = LoggerFactory.getLogger(RawScoreGeneScorer.class);
    private static final EnumSet<ModeOfInheritance> JUST_ANY = EnumSet.of(ModeOfInheritance.ANY);

    private final Set<ModeOfInheritance> inheritanceModes;

    private final ContributingAlleleCalculator contributingAlleleCalculator;
    private final GenePriorityScoreCalculator genePriorityScoreCalculator;

    private final CombinedScorePvalueCalculator pValueCalculator;
    private final AcmgAssignmentCalculator acmgAssignmentCalculator;

    /**
     * @param probandId                Sample id of the proband in the VCF.
     * @param inheritanceModeAnnotator An {@code InheritanceModeAnnotator} for the pedigree related to the proband.
     * @throws NullPointerException if any input arguments are null.
     * @since 10.0.0
     */
    public RawScoreGeneScorer(String probandId, Sex probandSex, InheritanceModeAnnotator inheritanceModeAnnotator, CombinedScorePvalueCalculator pValueCalculator, AcmgAssignmentCalculator acmgAssignmentCalculator) {
        Objects.requireNonNull(probandId);
        Objects.requireNonNull(inheritanceModeAnnotator);
        this.inheritanceModes = inheritanceModeAnnotator.getDefinedModes();
        this.contributingAlleleCalculator = new ContributingAlleleCalculator(probandId, probandSex, inheritanceModeAnnotator);
        this.genePriorityScoreCalculator = new GenePriorityScoreCalculator();
        this.pValueCalculator = Objects.requireNonNull(pValueCalculator);
        this.acmgAssignmentCalculator = Objects.requireNonNull(acmgAssignmentCalculator);
    }

    @Override
    public List<Gene> scoreGenes(List<Gene> genes) {
        return genes.stream()
                .parallel()
                .map(gene -> {
                    List<GeneScore> geneScores = scoreGene().apply(gene);
                    gene.addGeneScores(geneScores);
                    return gene;
                })
                .sorted()
                .toList();
    }

    /**
     * Calculates the final ranks of all genes that have survived the filtering
     * and prioritising steps. The strategy is that for autosomal dominant
     * diseases, we take the single most pathogenic score of any variant
     * affecting the gene; for autosomal recessive diseases, we take the mean of
     * the two most pathogenic variants. X-linked diseases are filtered such
     * that only X-chromosomal genes are left over, and the single worst variant
     * is taken.
     */
    @Override
    public Function<Gene, List<GeneScore>> scoreGene() {
        return gene -> {
            //Handle the scenario where no inheritance mode-dependent step was run
            if (inheritanceModes.isEmpty() || inheritanceModes.equals(JUST_ANY)) {
                GeneScore geneScore = calculateGeneScore(gene, ModeOfInheritance.ANY);
                logger.debug("{}", geneScore);
                return Collections.singletonList(geneScore);
            }

            List<GeneScore> geneScores = new ArrayList<>(inheritanceModes.size());
            for (ModeOfInheritance modeOfInheritance : inheritanceModes) {
                GeneScore geneScore = calculateGeneScore(gene, modeOfInheritance);
                logger.debug("{}", geneScore);
                // IMPORTANT: Do not skip score without variants!
                // A gene needs to have a score for each MOI as this will effect the overall ranks depending on the inheritance mode
                // the phenotype score and how omim dealt with the inheritance mode compatibility for known diseases affecting that gene.
                geneScores.add(geneScore);
            }
            return geneScores;
        };
    }

    private GeneScore calculateGeneScore(Gene gene, ModeOfInheritance modeOfInheritance) {
        //It is critical only the PASS variants are used in the scoring
        List<VariantEvaluation> contributingVariants = contributingAlleleCalculator.findContributingVariantsForInheritanceMode(modeOfInheritance, gene.getPassedVariantEvaluations());

        GenePriorityScoreCalculator.GenePriorityScore priorityScore = genePriorityScoreCalculator.calculateGenePriorityScore(gene, modeOfInheritance);

        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = priorityScore.getCompatibleDiseaseMatches();

        List<AcmgAssignment> acmgAssignments = acmgAssignmentCalculator.calculateAcmgAssignments(modeOfInheritance, gene, contributingVariants, compatibleDiseaseMatches);

//        double variantScore = acmgAssignments.stream()
//                .mapToDouble(acmgAssignment -> acmgAssignment.acmgEvidence().postProbPath())
//                .average()
//                .orElse(0.1); // 0.1 is the equivalent of a 0-point VUS

        double variantScore = contributingVariants.stream()
                .mapToDouble(VariantEvaluation::getVariantScore)
                .average()
                .orElse(0);

        double combinedScore = GeneScorer.calculateCombinedScore(variantScore, priorityScore.getScore(), gene.getPriorityResults().keySet());

        double pValue = pValueCalculator.calculatePvalueFromCombinedScore(combinedScore);

        return GeneScore.builder()
                .geneIdentifier(gene.getGeneIdentifier())
                .modeOfInheritance(modeOfInheritance)
                .variantScore(variantScore)
                .phenotypeScore(priorityScore.getScore())
                .combinedScore(combinedScore)
                .pValue(pValue)
                .contributingVariants(contributingVariants)
                // TODO this would be a good place to put a contributingModel
                //  i.e. from HiPhivePrioritiserResult see issue #363
//                .contributingModel()
                .compatibleDiseaseMatches(compatibleDiseaseMatches)
                .acmgAssignments(acmgAssignments)
                .build();
    }

}
