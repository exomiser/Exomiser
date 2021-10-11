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

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriorityResult;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.monarchinitiative.exomiser.core.analysis.util.AcmgCriterion.*;

public class AcmgCriteriaAssigner {

    private final String probandId;
    private final Pedigree.Individual.Sex probandSex;
    private final Pedigree pedigree;

    public AcmgCriteriaAssigner(String probandId, Pedigree.Individual.Sex probandSex, Pedigree pedigree) {
        this.probandId = probandId;
        this.probandSex = probandSex;
        this.pedigree = pedigree;
    }

    public Set<AcmgCriterion> assignVariantAcmgCriteria(VariantEvaluation variantEvaluation, Gene gene) {
        Set<AcmgCriterion> acmgCriteria = EnumSet.noneOf(AcmgCriterion.class);
        // PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
        assignPVS1(acmgCriteria, variantEvaluation, gene);

        if (pedigree.containsId(probandId)) {
            Pedigree.Individual proband = pedigree.getIndividualById(probandId);
            // PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
            assignPS2(acmgCriteria, variantEvaluation, gene, proband);
            // PM6 "Assumed de novo, but without confirmation of paternity and maternity"
//        assignPM6(acmgCriteria, variantEvaluation, gene);
            // BS4 "Lack of segregation in affected members of a family"
            assignBS4(acmgCriteria, variantEvaluation, proband);
        }

        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        // PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        assignPM2(acmgCriteria, frequencyData);
        // BA1 "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        assignBA1(acmgCriteria, frequencyData);

        // PM3 "For recessive disorders, detected in trans with a pathogenic variant"
        assignPM3(acmgCriteria, variantEvaluation, gene);
        // PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
        assignPM4(acmgCriteria, variantEvaluation);

        // PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
        assignPP4(acmgCriteria, gene);

        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        ClinVarData clinVarData = variantEvaluation.getPathogenicityData().getClinVarData();
        if (!clinVarData.isEmpty()) {
            // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
            // PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignPS1orPP5(acmgCriteria, clinVarData);
            // BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignBP6(acmgCriteria, clinVarData);
        }
        // PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
        // BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
        // TODO: internalise in PathogenicityData and create proper cutoff values adding isPath/isbenign to PathogenicityScore
        assignPP3orBP4(acmgCriteria, pathogenicityData);

        return acmgCriteria;
    }

    /**
     * PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
     */
    private void assignPVS1(Set<AcmgCriterion> acmgCriteria, VariantEvaluation variantEvaluation, Gene gene) {
        // TODO: add new method - gene.getAssociatedDiseases()
        // also need ClinvarCache.getClinvarDataForGene(GeneIdentifier geneIdentifier)

        // Certain types of variants (e.g., nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single exon or multiexon
        // deletion) can often be assumed to disrupt gene function by leading to a complete absence of the gene product by lack of tran-
        // scription or nonsense-mediated decay of an altered transcript. One  must,  however,  exercise  caution  when  classifying  these
        // variants as pathogenic by considering the following principles:
        //  (i)  When  classifying  such  variants  as  pathogenic,  one  must ensure  that  null  variants  are  a  known  mechanism  of
        //  pathogenicity consistent with the established inheritance pattern  for  the  disease.  For  example,  there  are  genes  for
        //  which only heterozygous missense variants cause disease and null variants are benign in a heterozygous state (e.g.,
        //  many hypertrophic cardiomyopathy genes). A novel heterozygous  nonsense  variant  in  the  MYH7  gene  would
        //  not be considered pathogenic for dominant hypertrophic cardiomyopathy based solely on this evidence, whereas a
        //  novel  heterozygous  nonsense  variant  in  the  CFTR  gene would likely be considered a recessive pathogenic variant.
        // Caveats:
        // •  Beware of genes where LOF is not a known disease mechanism (e.g., GFAP, MYH7)  -
        // TODO: check LOEUF score here - pLOEUF < 0.35 can be considered LOF intolerant
        //  MYH7 pLOUF 0.575 has many AD diseases.
        //  GFAP pLOUF 1.026 has only one AD disease https://omim.org/entry/137780?search=gfap&highlight=gfap.
        //  ABCA2 pLOUF 0.1 but only 1 AR disease.
        //  USP9X pLOUF 0.051 XD and XR

        // •  Use caution interpreting LOF variants at the extreme 3′ end of a gene
        // TODO: need exon structure of gene so can check if variant lies in or disrupts last exon
        // •  Use caution with splice variants that are predicted to lead to exon skipping but leave the remainder of the protein intact
        // •  Use caution in the presence of multiple transcripts

        GeneContraint geneContraint = GeneConstraints.geneContraint(gene.getGeneSymbol());
        for (GeneScore geneScore : gene.getGeneScores()) {
            ModeOfInheritance modeOfInheritance = geneScore.getModeOfInheritance();
            if (compatibleWithRecessive(modeOfInheritance) && isLossOfFunctionEffect(variantEvaluation.getVariantEffect())) {
                acmgCriteria.add(PVS1);
            } else if (compatibleWithDominant(modeOfInheritance) && (geneContraint == null || geneContraint.isLossOfFunctionIntolerant()) && isLossOfFunctionEffect(variantEvaluation.getVariantEffect())) {
                acmgCriteria.add(PVS1);
            }
        }
    }

    private boolean compatibleWithRecessive(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return true;
        }
        return probandSex == Pedigree.Individual.Sex.FEMALE && modeOfInheritance == ModeOfInheritance.X_RECESSIVE;
    }

    private boolean compatibleWithDominant(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
            return true;
        }
        if (probandSex == Pedigree.Individual.Sex.MALE && (modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT)) {
            return true;
        }
        return probandSex == Pedigree.Individual.Sex.FEMALE && modeOfInheritance == ModeOfInheritance.X_DOMINANT;
    }

    private boolean isLossOfFunctionEffect(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.INITIATOR_CODON_VARIANT
                || variantEffect == VariantEffect.STOP_GAINED
                || variantEffect == VariantEffect.FRAMESHIFT_ELONGATION
                || variantEffect == VariantEffect.FRAMESHIFT_TRUNCATION
                || variantEffect == VariantEffect.FRAMESHIFT_VARIANT
                || variantEffect == VariantEffect.SPLICE_ACCEPTOR_VARIANT
                || variantEffect == VariantEffect.SPLICE_DONOR_VARIANT
                || variantEffect == VariantEffect.EXON_LOSS_VARIANT;
    }

    /**
     * PM3 "For recessive disorders, detected in trans with a pathogenic variant"
     * Note: This requires testing of parents (or offspring) to determine phase
     */
    private void assignPM3(Set<AcmgCriterion> acmgCriteria, VariantEvaluation variantEvaluation, Gene gene) {
        HiPhivePriorityResult hiPhiveResult = gene.getPriorityResult(HiPhivePriorityResult.class);
        GeneScore recessiveGeneScore = gene.getGeneScoreForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        SampleGenotype thisVariantGenotype = variantEvaluation.getSampleGenotype(probandId);
        if (hiPhiveResult != null
                && recessiveGeneScore.hasCompatibleDiseaseMatches()
                && recessiveGeneScore.getContributingVariants().size() >= 2
                && thisVariantGenotype.isPhased()
                && recessiveGeneScore.getContributingVariants().contains(variantEvaluation)) {
            for (VariantEvaluation otherVariant : recessiveGeneScore.getContributingVariants()) {
                SampleGenotype otherVariantGenotype = otherVariant.getSampleGenotype(probandId);
                ClinVarData otherClinVarData = otherVariant.getPathogenicityData().getClinVarData();
                if (otherClinVarData.getPrimaryInterpretation() == ClinVarData.ClinSig.PATHOGENIC
                        && !otherVariant.equals(variantEvaluation)
                        && otherVariantGenotype.isHet()
                        && !otherVariantGenotype.equals(thisVariantGenotype)) {
                    // Got here? We're in trans i.e. thisVariantGenotype = 0|1 and otherVariantGenotype = 1|0
                    acmgCriteria.add(PM3);
                }
            }
        }
    }

    /**
     * PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
     * PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignPS1orPP5(Set<AcmgCriterion> acmgCriteria, ClinVarData clinVarData) {
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        if (isPathOrLikelyPath(primaryInterpretation) && clinVarData.starRating() >= 2) {
            // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
            // TODO: can't quite do this fully as also need to know others with same AA change, if not identical
            acmgCriteria.add(PS1);
        } else if (isPathOrLikelyPath(primaryInterpretation) && clinVarData.starRating() >= 1) {
            acmgCriteria.add(PP5);
        }
    }

    private boolean isPathOrLikelyPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.PATHOGENIC || clinSig == ClinVarData.ClinSig.LIKELY_PATHOGENIC || clinSig == ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
    }

    /**
     * PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
     * see also PM6.
     */
    private void assignPS2(Set<AcmgCriterion> acmgCriteria, VariantEvaluation variantEvaluation, Gene gene, Pedigree.Individual proband) {
        GeneScore topGeneSore = gene.getTopGeneScore();
        if (topGeneSore.getModeOfInheritance().isDominant() && topGeneSore.hasCompatibleDiseaseMatches() && topGeneSore.getContributingVariants().contains(variantEvaluation)) {
            List<Pedigree.Individual> ancestors = pedigree.anscestorsOf(proband);
            boolean noFamilyHistoryOfDisease = true;
            boolean noFamilyHistoryOfAllele = true;
            if (ancestors.size() >= 2) {
                SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
                for (Pedigree.Individual ancestor : ancestors) {
                    SampleGenotype ancestorGenotype = variantEvaluation.getSampleGenotype(ancestor.getId());
                    // It may be the case that the pedigree contains a relative but the relative was not sequenced,
                    // so these are considered independently
                    if (ancestor.isAffected()) {
                        noFamilyHistoryOfDisease = false;
                    }
                    if ((ancestorGenotype.isHet() || ancestorGenotype.isHomAlt()) && (probandGenotype.isHet() || probandGenotype.isHomAlt())) {
                        noFamilyHistoryOfAllele = false;
                    }
                }
                if (noFamilyHistoryOfDisease && noFamilyHistoryOfAllele) {
                    acmgCriteria.add(PS2);
                }
            }
        }
    }

    /**
     * PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
     */
    private void assignPM2(Set<AcmgCriterion> acmgCriteria, FrequencyData frequencyData) {
        if (!frequencyData.hasEspData() && !frequencyData.hasExacData() && !frequencyData.hasDbSnpData()) {
            // TODO: this is dependent on the MOI for the model. Might end up calling PM2 and PM3 for AD when
            //  there are frequencies present due to variant also being present in AR result.
            acmgCriteria.add(PM2);
        }
    }

    /**
     * PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
     */
    private void assignPM4(Set<AcmgCriterion> acmgCriteria, VariantEvaluation variantEvaluation) {
        if (variantEvaluation.getVariantEffect() == VariantEffect.STOP_LOST) {
            acmgCriteria.add(PM4);
        }
    }

    /**
     * PM6 "Assumed de novo, but without confirmation of paternity and maternity"
     * See also PS2.
     */
    private void assignPM6(Set<AcmgCriterion> acmgCriteria, VariantEvaluation variantEvaluation, Gene gene) {
//        (iii) The phenotype in the patient matches the gene’s disease
//        association with reasonable specificity. For example, this
//        argument is strong for a patient with a de novo variant
//        in the NIPBL gene who has distinctive facial features,
//         hirsutism, and upper-limb defects (i.e., Cornelia de Lange syndrome),
//         whereas it would be weaker for a de
//        novo variant found by exome sequencing in a child with
//        nonspecific features such as developmental delay.
        GeneScore topGeneSore = gene.getTopGeneScore();
        if (topGeneSore.getModeOfInheritance().isDominant() && topGeneSore.hasCompatibleDiseaseMatches()
                && topGeneSore.getContributingVariants().contains(variantEvaluation)
                && variantEvaluation.getSampleGenotypes().size() == 1 && variantEvaluation.getSampleGenotype(probandId).isHet()) {
            // making an assumption that this could be a de novo
            acmgCriteria.add(PM6);
        }
    }

    /**
     * PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     * BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     */
    private void assignPP3orBP4(Set<AcmgCriterion> acmgCriteria, PathogenicityData pathogenicityData) {
        int numBenign = 0;
        int numPathogenic = 0;
        List<PathogenicityScore> predictedPathogenicityScores = pathogenicityData.getPredictedPathogenicityScores();
        for (PathogenicityScore pathogenicityScore : predictedPathogenicityScores) {
            if (isPathogenic(pathogenicityScore)) {
                numPathogenic++;
            } else {
                numBenign++;
            }
        }
        if (predictedPathogenicityScores.size() > 1 && numPathogenic > numBenign) {
            acmgCriteria.add(PP3);
        }
        if (predictedPathogenicityScores.size() > 1 && numBenign > numPathogenic) {
            acmgCriteria.add(BP4);
        }
    }

    /**
     * PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
     */
    private void assignPP4(Set<AcmgCriterion> acmgCriteria, Gene gene) {
        HiPhivePriorityResult hiPhiveResult = gene.getPriorityResult(HiPhivePriorityResult.class);
        if (hiPhiveResult != null && hiPhiveResult.getHumanScore() >= 0.6) {
            acmgCriteria.add(PP4);
        }
    }

    /**
     * BA1 "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
     */
    private void assignBA1(Set<AcmgCriterion> acmgCriteria, FrequencyData frequencyData) {
        if (frequencyData.getMaxFreq() >= 5.0) {
            acmgCriteria.add(BA1);
        }
    }

    /**
     * BS4 "Lack of segregation in affected members of a family"
     */
    private void assignBS4(Set<AcmgCriterion> acmgCriteria, VariantEvaluation variantEvaluation, Pedigree.Individual proband) {
        if (pedigree.size() >= 2) {
            List<Pedigree.Individual> affectedFamilyMembers = pedigree.getIndividuals().stream()
                    .filter(Pedigree.Individual::isAffected)
                    .filter(individual -> !individual.getId().equals(probandId))
                    .filter(individual -> individual.getFamilyId().equals(proband.getFamilyId()))
                    .collect(Collectors.toList());
            boolean segregatesWithAffectedInFamily = true;
            SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
            if (!affectedFamilyMembers.isEmpty()) {
                for (Pedigree.Individual affected : affectedFamilyMembers) {
                    SampleGenotype affectedGenotype = variantEvaluation.getSampleGenotype(affected.getId());
                    if ((affectedGenotype.isHomRef() || affectedGenotype.isNoCall() || affectedGenotype.isEmpty()) && (probandGenotype.isHet() || probandGenotype.isHomAlt())) {
                        segregatesWithAffectedInFamily = false;
                    }
                }
            }
            if (!segregatesWithAffectedInFamily) {
                acmgCriteria.add(BS4);
            }
        }
    }

    /**
     * BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignBP6(Set<AcmgCriterion> acmgCriteria, ClinVarData clinVarData) {
        if (clinVarData.getPrimaryInterpretation() == ClinVarData.ClinSig.BENIGN && clinVarData.starRating() >= 1) {
            acmgCriteria.add(BP6);
        }
    }

    private boolean isPathogenic(PathogenicityScore pathogenicityScore) {
        if (pathogenicityScore instanceof SiftScore) {
            SiftScore score = (SiftScore) pathogenicityScore;
            return score.getRawScore() < SiftScore.SIFT_THRESHOLD;
        }
        if (pathogenicityScore instanceof MutationTasterScore) {
            MutationTasterScore score = (MutationTasterScore) pathogenicityScore;
            return score.getScore() > MutationTasterScore.MTASTER_THRESHOLD;
        }
        if (pathogenicityScore instanceof PolyPhenScore) {
            PolyPhenScore score = (PolyPhenScore) pathogenicityScore;
            return score.getScore() > PolyPhenScore.POLYPHEN_PROB_DAMAGING_THRESHOLD;
        }
        return pathogenicityScore.getScore() > 0.5f;
    }
}
