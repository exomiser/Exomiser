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

package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeAnalyser;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;

/**
 * @since 13.1.0
 */
public class Acmg2015EvidenceAssigner implements AcmgEvidenceAssigner {

    private final String probandId;
    private final Individual.Sex probandSex;
    private final Pedigree pedigree;

    public Acmg2015EvidenceAssigner(String probandId, Pedigree pedigree) {
        this.probandId = Objects.requireNonNull(probandId);
        this.pedigree = pedigree == null || pedigree.isEmpty() ? Pedigree.justProband(probandId) : pedigree;
        Individual proband = this.pedigree.getIndividualById(probandId);
        if (proband == null) {
            throw new IllegalArgumentException("Proband '" + probandId + "' not found in pedigree " + pedigree);
        }
        this.probandSex = proband.getSex();
    }

    /**
     * Assesses the provided {@link VariantEvaluation} against the ACMG criteria (Richards et al. 2015 doi:10.1038/gim.2015.30).
     * This class will assign the criteria codes and evidence strength based on the current {@link ModeOfInheritance}
     * being tested, the known diseases for this MOI, other contributing variants, the patients pedigree, and the phenotypic
     * similarity of the patient to the compatible diseases.
     *
     * <b>CAUTION! This method expects the variant evaluation to be compatible with the given mode of inheritance.</b> This
     * class is expected to be run downstream of an {@link InheritanceModeAnalyser} and accessed via the {@link AcmgAssignmentCalculator}. Failure to do this will result in
     * incorrect assignments.
     *
     * @param variantEvaluation
     * @param modeOfInheritance
     * @param contributingVariants
     * @param knownDiseases
     * @param compatibleDiseaseMatches
     * @return
     */
    // https://www.ncbi.nlm.nih.gov/clinvar/variation/464/ - check in ClinVar VCF if there is MOI information for a classification
    public AcmgEvidence assignVariantAcmgEvidence(VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, List<Disease> knownDiseases, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        // try strict ACMG assignments only if there are known disease-gene associations
        if (knownDiseases.isEmpty()) {
            return AcmgEvidence.empty();
        }

        AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();

        boolean hasCompatibleDiseaseMatches = !compatibleDiseaseMatches.isEmpty();

        // PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
        assignPVS1(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, knownDiseases);

        // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
        // Should NOT assign for PS1 for same base change. Unable to assign PS1 due to lack of AA change info in database
//        assignPS1(acmgEvidenceBuilder, variantEvaluation.getVariantEffect(), variantEvaluation.getPathogenicityData().getClinVarData());

        if (pedigree.containsId(probandId)) {
            Individual proband = pedigree.getIndividualById(probandId);
            // PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
            assignPS2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches, proband);
            // PM6 "Assumed de novo, but without confirmation of paternity and maternity"
//            assignPM6(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches);
            // BS4 "Lack of segregation in affected members of a family"
            assignBS4(acmgEvidenceBuilder, variantEvaluation, proband);
        }

        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        // PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        assignPM2(acmgEvidenceBuilder, frequencyData);
        // BA1 "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        assignBA1(acmgEvidenceBuilder, frequencyData);

        // PM3 "For recessive disorders, detected in trans with a pathogenic variant"
        assignPM3orBP2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches);
        // PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
        assignPM4(acmgEvidenceBuilder, variantEvaluation);
        // TODO: PM5 "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before

        // PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
        assignPP4(acmgEvidenceBuilder, compatibleDiseaseMatches);

        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        ClinVarData clinVarData = variantEvaluation.getPathogenicityData().getClinVarData();
        if (!clinVarData.isEmpty()) {
            // PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignPP5(acmgEvidenceBuilder, clinVarData);
            // BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignBP6(acmgEvidenceBuilder, clinVarData);
        }
        // PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
        // BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
        // TODO: internalise in PathogenicityData and create proper cutoff values adding isPath/isbenign to PathogenicityScore
        assignPP3orBP4(acmgEvidenceBuilder, pathogenicityData);

        return acmgEvidenceBuilder.build();
    }

    /**
     * PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
     */
    private void assignPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<Disease> knownDiseases) {
        // TODO: add new method - gene.getAssociatedDiseases()
        //  also need ClinvarCache.getClinvarDataForGene(GeneIdentifier geneIdentifier)

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
        // •  Beware of genes where LOF is not a known disease mechanism (e.g., GFAP, MYH7)
        // •  Use caution interpreting LOF variants at the extreme 3′ end of a gene
        // •  Use caution with splice variants that are predicted to lead to exon skipping but leave the remainder of the protein intact
        // •  Use caution in the presence of multiple transcripts

        GeneConstraint geneContraint = GeneConstraints.geneContraint(variantEvaluation.getGeneSymbol());
        // Should this be using the hasCompatibleDiseaseMatches variable?
        boolean inGeneWithKnownDiseaseAssociations = !knownDiseases.isEmpty();
        if (inGeneWithKnownDiseaseAssociations && isLossOfFunctionEffect(variantEvaluation.getVariantEffect())
                && (modeOfInheritance == ModeOfInheritance.ANY
                || compatibleWithRecessive(modeOfInheritance)
                || compatibleWithDominant(modeOfInheritance) && (geneContraint != null && geneContraint.isLossOfFunctionIntolerant())
        )
        ) {
            if (variantEvaluation.hasTranscriptAnnotations()) {
                TranscriptAnnotation transcriptAnnotation = variantEvaluation.getTranscriptAnnotations().get(0);
                if (predictedToLeadToNmd(transcriptAnnotation)) {
                    acmgEvidenceBuilder.add(PVS1);
                } else {
                    // Not predicted to lead to NMD? Downgrade to STRONG
                    acmgEvidenceBuilder.add(PVS1, Evidence.STRONG);
                }
            } else {
                // shouldn't happen that there are no transcript annotations, but just in case...
                acmgEvidenceBuilder.add(PVS1, Evidence.STRONG);
            }
        }
    }

    private boolean predictedToLeadToNmd(TranscriptAnnotation transcriptAnnotation) {
        // predicted to lead to NMD if in last exon or last 50bp of penultimate exon, or in single exon transcript
        boolean notInLastExon = transcriptAnnotation.getRank() < transcriptAnnotation.getRankTotal();
        boolean isSingleExonTranscript = transcriptAnnotation.getRankTotal() == 1;
        return transcriptAnnotation.getRankType() == TranscriptAnnotation.RankType.EXON && (notInLastExon || isSingleExonTranscript);
    }

    private boolean compatibleWithRecessive(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return true;
        }
        return probandSex == Individual.Sex.FEMALE && modeOfInheritance == ModeOfInheritance.X_RECESSIVE;
    }

    private boolean compatibleWithDominant(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
            return true;
        }
        if (probandSex == Individual.Sex.MALE && (modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT)) {
            return true;
        }
        return (probandSex == Individual.Sex.FEMALE || probandSex == Individual.Sex.UNKNOWN) && modeOfInheritance == ModeOfInheritance.X_DOMINANT;
    }

    private boolean isLossOfFunctionEffect(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.INITIATOR_CODON_VARIANT
                || variantEffect == VariantEffect.START_LOST
                || variantEffect == VariantEffect.STOP_LOST
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
    private void assignPM3orBP2(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, boolean hasCompatibleDiseaseMatches) {
        if (hasCompatibleDiseaseMatches) {
            SampleGenotype thisVariantGenotype = variantEvaluation.getSampleGenotype(probandId);
            if (contributingVariants.size() >= 2 && contributingVariants.contains(variantEvaluation)) {
                for (VariantEvaluation otherVariant : contributingVariants) {
                    SampleGenotype otherVariantGenotype = otherVariant.getSampleGenotype(probandId);
                    ClinVarData otherClinVarData = otherVariant.getPathogenicityData().getClinVarData();
                    if (otherClinVarData.getPrimaryInterpretation() == ClinVarData.ClinSig.PATHOGENIC && !otherVariant.equals(variantEvaluation)) {
                        // X = this variant, P = other pathogenic variant
                        boolean inTrans = inTrans(thisVariantGenotype, otherVariantGenotype);
                        boolean inCis = inCis(thisVariantGenotype, otherVariantGenotype);
                        if (inTrans && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
                            //     -------P- (AR)
                            //     ---X------
                            acmgEvidenceBuilder.add(PM3);
                        } else if (inCis && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
                            //     ---X---P- (AR)
                            //     ---------
                            acmgEvidenceBuilder.add(BP2);
                        } else if (inTrans && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
                            //     -------P-  (AD)
                            //     ---X------
                            acmgEvidenceBuilder.add(BP2);
                        }
                    }
                }
            }
        }
    }

    private boolean inTrans(SampleGenotype thisVariantGenotype, SampleGenotype otherVariantGenotype) {
        // GT in trans = (1|0 && 0|1) (variant in both copies of gene)
        // GT in cis   = (1|0 && 1|0) or (0|1 && 0|1) (variant in same copy of gene)
        return thisVariantGenotype.isPhased() && thisVariantGenotype.isHet()
                && otherVariantGenotype.isPhased() && otherVariantGenotype.isHet()
                && !thisVariantGenotype.equals(otherVariantGenotype);
    }

    private boolean inCis(SampleGenotype thisVariantGenotype, SampleGenotype otherVariantGenotype) {
        // GT in trans = (1|0 && 0|1) (variant in both copies of gene)
        // GT in cis   = (1|0 && 1|0) or (0|1 && 0|1) (variant in same copy of gene)
        return thisVariantGenotype.isPhased() && thisVariantGenotype.isHet()
                && otherVariantGenotype.isPhased() && otherVariantGenotype.isHet()
                && thisVariantGenotype.equals(otherVariantGenotype);
    }

    /**
     * PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
     */
    private void assignPS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEffect variantEffect, ClinVarData clinVarData) {
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        if (isMissense(variantEffect) && isPathOrLikelyPath(primaryInterpretation) && clinVarData.starRating() >= 2) {
            // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
            // TODO: can't quite do this fully as also need to know others with same AA change, if not identical
            acmgEvidenceBuilder.add(PS1);
        }
    }

    private boolean isMissense(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.MISSENSE_VARIANT;
    }

    /**
     * PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignPP5(AcmgEvidence.Builder acmgEvidenceBuilder, ClinVarData clinVarData) {
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        boolean pathOrLikelyPath = isPathOrLikelyPath(primaryInterpretation);
        if (pathOrLikelyPath && clinVarData.starRating() == 1) {
            acmgEvidenceBuilder.add(PP5);
        } else if (pathOrLikelyPath && clinVarData.starRating() >= 2) {
            acmgEvidenceBuilder.add(PP5, Evidence.STRONG);
        }
    }

    private boolean isPathOrLikelyPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.PATHOGENIC || clinSig == ClinVarData.ClinSig.LIKELY_PATHOGENIC || clinSig == ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
    }

    /**
     * BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignBP6(AcmgEvidence.Builder acmgEvidenceBuilder, ClinVarData clinVarData) {
        // these are likely never to be triggered as the variants will have been filtered out already
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        boolean benignOrLikelyBenign = isBenignOrLikelyBenign(primaryInterpretation);
        if (benignOrLikelyBenign && clinVarData.starRating() == 1) {
            acmgEvidenceBuilder.add(BP6);
        } else if (benignOrLikelyBenign && clinVarData.starRating() >= 2) {
            acmgEvidenceBuilder.add(BP6, Evidence.STRONG);
        }
    }

    private boolean isBenignOrLikelyBenign(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.BENIGN || clinSig == ClinVarData.ClinSig.LIKELY_BENIGN || clinSig == ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN;
    }

    /**
     * PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
     * see also PM6.
     */
    private void assignPS2(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, boolean hasCompatibleDiseaseMatches, Individual proband) {
        if (modeOfInheritance.isDominant() && hasCompatibleDiseaseMatches && contributingVariants.contains(variantEvaluation)) {
            List<Individual> ancestors = pedigree.anscestorsOf(proband);
            boolean noFamilyHistoryOfDisease = true;
            boolean noFamilyHistoryOfAllele = true;
            if (ancestors.size() >= 2) {
                SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
                for (Individual ancestor : ancestors) {
                    SampleGenotype ancestorGenotype = variantEvaluation.getSampleGenotype(ancestor.getId());
                    // It may be the case that the pedigree contains a relative but the relative was not sequenced,
                    // so these are considered independently
                    if (ancestor.isAffected()) {
                        noFamilyHistoryOfDisease = false;
                    }
                    if ((ancestorGenotype.isHet() || ancestorGenotype.isHomAlt()) && (probandGenotype.isHet() || probandGenotype.isHomAlt())) {
                        // See comment below about possible de novo
                        noFamilyHistoryOfAllele = false;
                    }
                }
                if (noFamilyHistoryOfDisease && noFamilyHistoryOfAllele) {
                    acmgEvidenceBuilder.add(PS2);
                }
            }
        }
    }

    // n.b. this method is a little more obvious about what it expects compared to the test for noFamilyHistoryOfAllele in the PS2 method
    // plus it doesn't assign a 0/1 or ./. genotypes in a parent as being a possible de novo in a 0/0 proband. This case shouldn't occur
    // however, as Exomiser removes 0/0 proband variants.
    private boolean possibleDeNovo(SampleGenotype ancestorGenotype, SampleGenotype probandGenotype) {
        return (ancestorGenotype.isNoCall() || ancestorGenotype.isHomRef()) && (probandGenotype.isHet() || probandGenotype.isHomAlt());
    }

    /**
     * PM1 "Located in a mutational hot spot and/or critical and well-established functional domain (e.g., active site of an enzyme) without benign variation"
     */
    private void assignPM1(Map<AcmgCriterion, Evidence> acmgEvidenceBuilder) {
        // TODO - need UniProt domain / site info and clinvar counts
        //  can upgrade to STRONG
    }

    /**
     * PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
     */
    private void assignPM2(AcmgEvidence.Builder acmgEvidenceBuilder, FrequencyData frequencyData) {
        if (!frequencyData.hasEspData() && !frequencyData.hasExacData() && !frequencyData.hasDbSnpData()) {
            acmgEvidenceBuilder.add(PM2);
        }
        // TODO: require disease incidence in carriers and penetrance to be able to calculate expected frequencies for AR
    }

    /**
     * PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
     */
    private void assignPM4(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        boolean isInFrameInDel = variantEvaluation.getVariantEffect() == VariantEffect.INFRAME_INSERTION || variantEvaluation.getVariantEffect() == VariantEffect.INFRAME_DELETION;
        // avoid double-counting same affects if PVS1 already applied
        if (!acmgEvidenceBuilder.contains(PVS1) && variantEvaluation.getVariantEffect() == VariantEffect.STOP_LOST || isInFrameInDel) {
            acmgEvidenceBuilder.add(PM4);
        }
        // TODO if can assess whether the variant is an inframe indel in an unconserved or repetitive region then can assign BP3
    }

    /**
     * PM6 "Assumed de novo, but without confirmation of paternity and maternity"
     * See also PS2.
     */
    private void assignPM6(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, boolean hasCompatibleDiseaseMatches) {
//        (iii) The phenotype in the patient matches the gene’s disease
//        association with reasonable specificity. For example, this
//        argument is strong for a patient with a de novo variant
//        in the NIPBL gene who has distinctive facial features,
//         hirsutism, and upper-limb defects (i.e., Cornelia de Lange syndrome),
//         whereas it would be weaker for a de
//        novo variant found by exome sequencing in a child with
//        nonspecific features such as developmental delay.
        if (hasCompatibleDiseaseMatches && modeOfInheritance.isDominant()
                && contributingVariants.contains(variantEvaluation)
                && variantEvaluation.getSampleGenotypes().size() == 1 && variantEvaluation.getSampleGenotype(probandId).isHet()) {
            // making an assumption that this could be a de novo
            acmgEvidenceBuilder.add(PM6);
        }
    }

    /**
     * PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     * BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     */
    private void assignPP3orBP4(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityData pathogenicityData) {
        // These approaches are broadly similar in recommending just using one predictor and REVEL is consistently
        // seen to out-perform other predictors. In our testing we also found that the REVEL-only approach worked best.

        // updated from "Evidence-based calibration of computational tools for missense variant pathogenicity
        //   classification and ClinGen recommendations for clinical use of PP3/BP4 criteria"
        //   https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
        //
        var revelScore = pathogenicityData.getPredictedScore(PathogenicitySource.REVEL);
        if (revelScore != null) {
            assignRevelBasedPP3BP4Classification(acmgEvidenceBuilder, revelScore);
        } else {
            // See "Assessing performance of pathogenicity predictors using clinically relevant variant datasets"
            // http://dx.doi.org/10.1136/jmedgenet-2020-107003
            assignEnsembleBasedPP3BP4Classification(acmgEvidenceBuilder, pathogenicityData);
        }
    }

    /*
     * Updated classification from "Evidence-based calibration of computational tools for missense variant pathogenicity
     * classification and ClinGen recommendations for clinical use of PP3/BP4 criteria"
     * https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
     *
     * This method provided much better
     */
    private void assignRevelBasedPP3BP4Classification(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityScore revelScore) {
        var revel = revelScore.getRawScore();
        // Taken from table 2 of https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
        // P_Strong   P_Moderate   P_Supporting       B_Supporting   B_Moderate     B_Strong      B_Very Strong
        // ≥ 0.932 [0.773, 0.932) [0.644, 0.773)    (0.183, 0.290] (0.016, 0.183] (0.003, 0.016] ≤ 0.003
        // PATHOGENIC categories

        if (revel >= 0.932f) {
            acmgEvidenceBuilder.add(PP3, Evidence.STRONG);
        } else if (revel < 0.932f && revel >= 0.773f) {
            acmgEvidenceBuilder.add(PP3, Evidence.MODERATE);
        } else if (revel < 0.773f && revel >= 0.644f) {
            acmgEvidenceBuilder.add(PP3, Evidence.SUPPORTING);
        }
        // BENIGN categories
        else if (revel > 0.183f && revel <= 0.290f) {
            acmgEvidenceBuilder.add(BP4, Evidence.SUPPORTING);
        } else if (revel > 0.016f && revel <= 0.183f) {
            acmgEvidenceBuilder.add(BP4, Evidence.MODERATE);
        } else if (revel > 0.003f && revel <= 0.016f) {
            acmgEvidenceBuilder.add(BP4, Evidence.STRONG);
        } else if (revel <= 0.003f) {
            acmgEvidenceBuilder.add(BP4, Evidence.VERY_STRONG);
        }
    }

    /*
     * Ensemble-based approach suggested in
     * See "Assessing performance of pathogenicity predictors using clinically relevant variant datasets"
     * http://dx.doi.org/10.1136/jmedgenet-2020-107003
     */
    private void assignEnsembleBasedPP3BP4Classification(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityData pathogenicityData) {
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
            acmgEvidenceBuilder.add(PP3);
        }
        if (predictedPathogenicityScores.size() > 1 && numBenign > numPathogenic) {
            acmgEvidenceBuilder.add(BP4);
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
        if (pathogenicityScore instanceof CaddScore) {
            CaddScore score = (CaddScore) pathogenicityScore;
            // 95-99% most deleterious.
            return score.getRawScore() >= 13.0f;
        }
        return pathogenicityScore.getScore() > 0.5f;
    }

    /**
     * PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
     */
    private void assignPP4(AcmgEvidence.Builder acmgEvidenceBuilder, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        double humanGenePhenotypeScoreForMoi = 0;
        for (ModelPhenotypeMatch<Disease> diseaseModelPhenotypeMatch : compatibleDiseaseMatches) {
            humanGenePhenotypeScoreForMoi = Math.max(humanGenePhenotypeScoreForMoi, diseaseModelPhenotypeMatch.getScore());
        }
        if (humanGenePhenotypeScoreForMoi >= 0.6) {
            acmgEvidenceBuilder.add(PP4);
        }
    }

    /**
     * BA1 "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
     */
    private void assignBA1(AcmgEvidence.Builder acmgEvidenceBuilder, FrequencyData frequencyData) {
        if (frequencyData.getMaxFreq() >= 5.0) {
            acmgEvidenceBuilder.add(BA1);
        }
    }

    /**
     * BS4 "Lack of segregation in affected members of a family"
     */
    //TODO: implement PP1 - Cosegregation with disease in multiple affected family members in a gene definitively known to cause the disease
    private void assignBS4(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Individual proband) {
        if (pedigree.size() >= 2) {
            List<Individual> affectedFamilyMembers = pedigree.getIndividuals().stream()
                    .filter(Individual::isAffected)
                    .filter(individual -> !individual.getId().equals(probandId))
                    .filter(individual -> individual.getFamilyId().equals(proband.getFamilyId()))
                    .collect(Collectors.toList());
            boolean segregatesWithAffectedInFamily = true;
            SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
            if (!affectedFamilyMembers.isEmpty()) {
                for (Individual affected : affectedFamilyMembers) {
                    SampleGenotype affectedGenotype = variantEvaluation.getSampleGenotype(affected.getId());
                    if ((affectedGenotype.isHomRef() || affectedGenotype.isNoCall() || affectedGenotype.isEmpty()) && (probandGenotype.isHet() || probandGenotype.isHomAlt())) {
                        segregatesWithAffectedInFamily = false;
                    }
                }
            }
            if (!segregatesWithAffectedInFamily) {
                acmgEvidenceBuilder.add(BS4);
            }
        }
    }

}
