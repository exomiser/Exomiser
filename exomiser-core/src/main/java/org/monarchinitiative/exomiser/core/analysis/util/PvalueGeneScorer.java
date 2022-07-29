package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.*;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @since 13.1.0
 */
public class PvalueGeneScorer implements GeneScorer {

    private static final Logger logger = LoggerFactory.getLogger(PvalueGeneScorer.class);
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
    public PvalueGeneScorer(String probandId, Pedigree.Individual.Sex probandSex, InheritanceModeAnnotator inheritanceModeAnnotator, CombinedScorePvalueCalculator pValueCalculator) {
        Objects.requireNonNull(probandId);
        Objects.requireNonNull(inheritanceModeAnnotator);
        this.inheritanceModes = inheritanceModeAnnotator.getDefinedModes();
        this.contributingAlleleCalculator = new ContributingAlleleCalculator(probandId, probandSex, inheritanceModeAnnotator);
        this.genePriorityScoreCalculator = new GenePriorityScoreCalculator();
        this.pValueCalculator = Objects.requireNonNull(pValueCalculator);
        AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner(probandId, inheritanceModeAnnotator.getPedigree());
        AcmgEvidenceClassifier acmgEvidenceClassifier = new Acgs2020Classifier();
        this.acmgAssignmentCalculator = new AcmgAssignmentCalculator(acmgEvidenceAssigner, acmgEvidenceClassifier);
    }

    @Override
    public List<Gene> scoreGenes(List<Gene> genes) {
        // TODO: v14.0.0 - return stream().toList() can't do this here as it breaks the sorting for the
        //  MOI-dependent writers. These will be removed in v14.0.0.
        return genes.stream()
                .parallel()
                .map(gene -> {
                    List<GeneScore> geneScores = scoreGene().apply(gene);
                    gene.addGeneScores(geneScores);
                    return gene;
                })
                .sorted()
                .collect(Collectors.toList());
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

        double variantScore = contributingVariants.stream()
                .mapToDouble(VariantEvaluation::getVariantScore)
                .average()
                .orElse(0);

        double combinedScore = GeneScorer.calculateCombinedScore(variantScore, priorityScore.getScore(), gene.getPriorityResults().keySet());

        double pValue = pValueCalculator.calculatePvalueFromCombinedScore(combinedScore);

        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = priorityScore.getCompatibleDiseaseMatches();

        List<AcmgAssignment> acmgAssignments = acmgAssignmentCalculator.calculateAcmgAssignments(modeOfInheritance, gene, contributingVariants, compatibleDiseaseMatches);

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
