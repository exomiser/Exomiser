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
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeAnalyser;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.ClinVarDao;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;

/**
 * @since 13.1.0
 */
public class Acmg2015EvidenceAssigner implements AcmgEvidenceAssigner {

    private static final Logger logger = LoggerFactory.getLogger(Acmg2015EvidenceAssigner.class);

    // Variants to be excluded from being assigned BA1 as specified by the ClinGen SVI working group in:
    //   https://www.clinicalgenome.org/site/assets/files/3460/ba1_exception_list_07_30_2018.pdf
    private static final Set<AlleleProto.AlleleKey> HG19_BA1_EXCLUSION_VARIANTS = Set.of(
            // ClinVar 1018 - 3: 128598490 (GRCh37) 128879647 (GRCh38) (SPDI: NC_000003.12:128879647:TAAG:TAAGTAAG)
            AlleleProtoAdaptor.toAlleleKey(3, 128_598_490, "C", "CTAAG"),
            // ClinVar 17023 - 13: 20763612 (GRCh37) 20189473 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 20_763_612, "C", "T"),
            // ClinVar 10 - 6: 26091179 (GRCh37) 26090951 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_091_179, "C", "G"),
            // ClinVar 9 - 6: 26093141 (GRCh37) 26092913 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_093_141, "G", "A"),
            // ClinVar 2551 - 16: 3299586 (GRCh37) 3249586 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_299_586, "G", "A"),
            // ClinVar 2552 - 16: 3299468 (GRCh37) 3249468 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_299_468, "C", "T"),
            // ClinVar 217689 - 13: 73409497 (GRCh37) 72835359 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 73_409_497, "G", "A"),
            // ClinVar 3830 - 12: 121175678 (GRCh37) 120737875 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(12, 121_175_678, "C", "T"),
            // ClinVar 1900 - 3: 15686693 (GRCh37) 15645186 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(3, 15_686_693, "G", "C")
    );
    private static final Set<AlleleProto.AlleleKey> HG38_BA1_EXCLUSION_VARIANTS = Set.of(
            // ClinVar 1018 - 3: 128598490 (GRCh37) 128879647 (GRCh38) (SPDI: NC_000003.12:128879647:TAAG:TAAGTAAG)
            AlleleProtoAdaptor.toAlleleKey(3, 128_879_647, "C", "CTAAG"),
            // ClinVar 17023 - 13: 20763612 (GRCh37) 20189473 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 20_189_473, "C", "T"),
            // ClinVar 10 - 6: 26091179 (GRCh37) 26090951 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_090_951, "C", "G"),
            // ClinVar 9 - 6: 26093141 (GRCh37) 26092913 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_092_913, "G", "A"),
            // ClinVar 2551 - 16: 3299586 (GRCh37) 3249586 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_249_586, "G", "A"),
            // ClinVar 2552 - 16: 3299468 (GRCh37) 3249468 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_249_468, "C", "T"),
            // ClinVar 217689 - 13: 73409497 (GRCh37) 72835359 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 72_835_359, "G", "A"),
            // ClinVar 3830 - 12: 121175678 (GRCh37) 120737875 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(12, 120_737_875, "C", "T"),
            // ClinVar 1900 - 3: 15686693 (GRCh37) 15645186 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(3, 15_645_186, "G", "C")
    );

    private final ClinVarDao clinVarDao;


    private final String probandId;
    private final Individual.Sex probandSex;
    private final Pedigree pedigree;

    public Acmg2015EvidenceAssigner(String probandId, Pedigree pedigree, ClinVarDao clinVarDao) {
        this.probandId = Objects.requireNonNull(probandId);
        this.pedigree = pedigree == null || pedigree.isEmpty() ? Pedigree.justProband(probandId) : pedigree;
        Individual proband = this.pedigree.getIndividualById(probandId);
        if (proband == null) {
            throw new IllegalArgumentException("Proband '" + probandId + "' not found in pedigree " + pedigree);
        }
        this.probandSex = proband.getSex();
        this.clinVarDao = clinVarDao;
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
        AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();

        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        // BA1 "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        // Updated recommendation: "Allele frequency is >0.05 in any general continental population dataset of at least
        // 2,000 observed alleles and found in a gene without a gene- or variant-specific BA1 modification." i.e. ExAC
        // African, East Asian, European [non-Finnish], Latino, and South Asian
        AlleleProto.AlleleKey alleleKey = variantEvaluation.alleleKey();
        boolean isBa1ExcludedVariant = variantEvaluation.getGenomeAssembly() == GenomeAssembly.HG19 ? HG19_BA1_EXCLUSION_VARIANTS.contains(alleleKey) : HG38_BA1_EXCLUSION_VARIANTS.contains(alleleKey);
        if (!isBa1ExcludedVariant && frequencyData.maxFreqForPopulation(FrequencySource.NON_FOUNDER_POPS) >= 5.0) {
            acmgEvidenceBuilder.add(BA1);
            // BA1 is supposed to be used as a filtering criterion where no other evidence need be considered.
            return acmgEvidenceBuilder.build();
        }
        // TODO: MT specific rules
        // PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        assignPM2(acmgEvidenceBuilder, frequencyData, modeOfInheritance);
        // BS1 "Allele frequency is greater than expected for disorder"
        assignBS1(acmgEvidenceBuilder, variantEvaluation, frequencyData, variantEvaluation.getPathogenicityData().clinVarData());
        // BS2 "Observed in a healthy adult individual for a recessive (homozygous), dominant (heterozygous), or X-linked (hemizygous) disorder, with full penetrance expected at an early age"
        assignBS2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, knownDiseases, frequencyData, variantEvaluation.getPathogenicityData().clinVarData());

        // PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
        // "Recommendations for interpreting the loss of function PVS1 ACMG/AMP variant criterion" https://doi.org/10.1002/humu.23626
        AcmgPVS1EvidenceAssigner.assignPVS1(acmgEvidenceBuilder, variantEvaluation, probandSex, modeOfInheritance, knownDiseases);

        // Apply PS1, PM5, PM1 to missense and inframe indels
        AcmgMissenseInFrameIndelEvidenceAssigner.assignMissenseEvidence(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, knownDiseases, clinVarDao);

        // TODO: MISSENSE_VARIANT should be assessed for splicing score if they are also a SPLICE_REGION_VARIANT then the max of these two outcomes should be used.
        //  Note that these can conflict e.g. a MISSENSE with a low REVEL score can have a BP4 assigned yet have a SPLICE_AI > 0.2 and therefore a PP3 if assessed
        //  as a SPLICE_REGION_VARIANT. This could also effect the PS1 assignment if a splice variant has been assigned a PP3 and there are other CLinVar variants in the region.
        //  e.g. 22-51019849-C-T missense_variant|splice_region_variant MODERATE NC_000022.10:g.51019849C>T ENST00000406938.2(CHKB):c.581G>A (CHKB)p.(Arg194Gln) 1638bp 395aa
        // apply PVS1, PS1, PP3, BP4, BP7 to splice region variants according to "Using the ACMG/AMP framework to capture
        // evidence related to predicted and observed impact on splicing: Recommendations from the ClinGen SVI Splicing Subgroup"
        // https://doi.org/10.1016/j.ajhg.2023.06.002
        AcmgSpliceEvidenceAssigner.assignSpliceEvidence(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, knownDiseases, clinVarDao);

        boolean hasCompatibleDiseaseMatches = !compatibleDiseaseMatches.isEmpty();
        if (pedigree.containsId(probandId)) {
            Individual proband = pedigree.getIndividualById(probandId);
            // PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
            assignPS2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches, proband);
            // PM6 "Assumed de novo, but without confirmation of paternity and maternity"
//            assignPM6(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches);
            // BS4 "Lack of segregation in affected members of a family"
            assignBS4(acmgEvidenceBuilder, variantEvaluation, proband);
        }

        // PM3 "For recessive disorders, detected in trans with a pathogenic variant"
        assignPM3orBP2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches);
        // PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
        assignPM4(acmgEvidenceBuilder, variantEvaluation);

        // PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
        assignPP4(acmgEvidenceBuilder, compatibleDiseaseMatches);

        ClinVarData clinVarData = variantEvaluation.getPathogenicityData().clinVarData();
        if (!clinVarData.isEmpty()) {
            // PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignPP5(acmgEvidenceBuilder, clinVarData);
            // BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignBP6(acmgEvidenceBuilder, clinVarData);
        }

        return acmgEvidenceBuilder.build();
    }

    private void assignBS1(AcmgEvidence.Builder acmgEvidenceBuilder, Variant variant, FrequencyData frequencyData, ClinVarData clinVarData) {
        boolean isReportedPorLP = clinVarData.starRating() >= 1 && isPathOrLikelyPath(clinVarData.getPrimaryInterpretation());
        if ((acmgEvidenceBuilder.contains(BA1) || acmgEvidenceBuilder.contains(PM2)) && !isReportedPorLP) {
            float maxNonFounderPopFreq = frequencyData.maxFreqForPopulation(FrequencySource.NON_FOUNDER_POPS);
            if (variant.contigId() == 25 && maxNonFounderPopFreq > 0.5) {
                // hard cutoff for MT based on AMP/ACMG "Specifications of the ACMG/AMP standards and guidelines
                //for mitochondrial DNA variant interpretation" doi:10.1002/humu.24107
                acmgEvidenceBuilder.add(BS1);
            } else if (maxNonFounderPopFreq > 1.5) {
                // InterVar uses a cutoff of 1% but this is half our default max for AR filtering (2%). More empirical
                // gene-specific cut-offs can be generated from the max frequencies seen for the most common P/LP clinvar
                // variant - i.e. GeneStats required
                acmgEvidenceBuilder.add(BS1);
            }
        }
    }

    // BS2 "Observed in a healthy adult individual for a recessive (homozygous), dominant (heterozygous), or X-linked (hemizygous) disorder, with full penetrance expected at an early age"
    private void assignBS2(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<Disease> diseases, FrequencyData frequencyData, ClinVarData clinVarData) {
        // TODO: need GENE MOI
        //We first determine the mode of inheritance of the gene, then compares the allele count (see allele frequency for quality checks) to the corresponding threshold:
        //
        //    recessive or X-linked genes: allele count greater than 2,
        //    dominant genes: allele count greater than 5.
        //
        //Rule BS2 is not evaluated if rule BA1 was triggered, to avoid double-counting the same evidence, and for performance we disable BS2 if rule PM2 triggered.
        int alleleCount = frequencyData.frequencies().stream().filter(frequency -> FrequencySource.NON_FOUNDER_POPS.contains(frequency.source())).mapToInt(Frequency::homs).sum();
        if (!(acmgEvidenceBuilder.contains(BA1) || acmgEvidenceBuilder.contains(PM2))) {
            if (alleleCount > 2 && (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT)) {
                acmgEvidenceBuilder.add(BS2);
            }
            if (alleleCount > 5 && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
                acmgEvidenceBuilder.add(BS2);
            }
        }
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
                    ClinVarData otherClinVarData = otherVariant.getPathogenicityData().clinVarData();
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
     * PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignPP5(AcmgEvidence.Builder acmgEvidenceBuilder, ClinVarData clinVarData) {
        // avoid double-counting by not assigning if PS1 already assigned
        if (acmgEvidenceBuilder.contains(PS1)) {
            return;
        }
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        boolean pathOrLikelyPath = isPathOrLikelyPath(primaryInterpretation);
        if (pathOrLikelyPath && clinVarData.starRating() == 1) {
            acmgEvidenceBuilder.add(PP5);
        } else if (pathOrLikelyPath && clinVarData.starRating() == 2) {
            // multiple submitters, no conflicts
            acmgEvidenceBuilder.add(PP5, Evidence.STRONG);
        } else if (pathOrLikelyPath && clinVarData.starRating() >= 3) {
            // expert panel or practice guidelines
            acmgEvidenceBuilder.add(PP5, Evidence.VERY_STRONG);
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
        } else if (benignOrLikelyBenign && clinVarData.starRating() == 2) {
            acmgEvidenceBuilder.add(BP6, Evidence.STRONG);
        } else if (benignOrLikelyBenign && clinVarData.starRating() >= 3) {
            // expert panel or practice guidelines
            acmgEvidenceBuilder.add(BP6, Evidence.VERY_STRONG);
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
     * PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes
     * Project, or Exome Aggregation Consortium"
     * See <a href=https://clinicalgenome.org/site/assets/files/5182/pm2_-_svi_recommendation_-_approved_sept2020.pdf>ClinGen SVI updated recommendations</a>.
     * This method uses the recommended frequency filtering populations from gnomAD for their <a href=https://gnomad.broadinstitute.org/help/faf>Filtering allele frequency</a>
     * which excludes the bottle-necked populations Ashkenazi Jewish (ASJ), European Finnish (FIN), Other (OTH), Amish (AMI)
     * and Middle Eastern (MID).
     */
    private void assignPM2(AcmgEvidence.Builder acmgEvidenceBuilder, FrequencyData frequencyData, ModeOfInheritance modeOfInheritance) {
        // allow local frequency occurrences as these are unverifiable as to their size or content. Also do not use isRepresentedInDatabase()
        // as this will exclude anything with an rsID which could be a ClinVar variant not seen in any population database.
        boolean absentFromDatabase = frequencyData.maxFreq() == 0f || (frequencyData.size() == 1 && frequencyData.containsFrequencySource(FrequencySource.LOCAL));
        boolean atVeryLowFrequencyIfRecessive = (modeOfInheritance.isRecessive() || modeOfInheritance == ModeOfInheritance.ANY) && frequencyData.maxFreqForPopulation(FrequencySource.NON_FOUNDER_POPS) < 0.01f;
        if (absentFromDatabase || atVeryLowFrequencyIfRecessive) {
            acmgEvidenceBuilder.add(PM2, Evidence.SUPPORTING);
        }
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
     * PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
     */
    private void assignPP4(AcmgEvidence.Builder acmgEvidenceBuilder, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        double humanGenePhenotypeScoreForMoi = 0;
        for (ModelPhenotypeMatch<Disease> diseaseModelPhenotypeMatch : compatibleDiseaseMatches) {
            humanGenePhenotypeScoreForMoi = Math.max(humanGenePhenotypeScoreForMoi, diseaseModelPhenotypeMatch.getScore());
        }
        if (humanGenePhenotypeScoreForMoi >= 0.7f) {
            acmgEvidenceBuilder.add(PP4, Evidence.MODERATE);
        } else if (humanGenePhenotypeScoreForMoi < 0.7f && humanGenePhenotypeScoreForMoi >= 0.51f) {
            acmgEvidenceBuilder.add(PP4, Evidence.SUPPORTING);
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
                    .toList();
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
