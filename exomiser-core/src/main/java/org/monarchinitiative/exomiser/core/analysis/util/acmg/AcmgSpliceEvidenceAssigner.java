package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.genome.dao.ClinVarDao;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.svart.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;
import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;

class AcmgSpliceEvidenceAssigner {

    private AcmgSpliceEvidenceAssigner() {
    }

    /**
     * Applies PVS1, PS1, PP3, BP4, BP7 to splice region variants according to <a href="https://doi.org/10.1016/j.ajhg.2023.06.002">"Using the ACMG/AMP framework to capture
     *  evidence related to predicted and observed impact on splicing: Recommendations from the ClinGen SVI Splicing Subgroup"</a>
     *  https://doi.org/10.1016/j.ajhg.2023.06.002
     * @param acmgEvidenceBuilder
     * @param variantEvaluation
     * @param modeOfInheritance
     * @param knownDiseases
     * @param clinVarDao
     */
    static void assignSpliceEvidence(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<Disease> knownDiseases, ClinVarDao clinVarDao) {
        VariantEffect variantEffect = variantEvaluation.getVariantEffect();
        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        if (isSpliceDonorAcceptorSpliceVariant(variantEffect)) {
            // PVS1 decision tree - this should have been independently added by the PVS1EvidenceAssigner class
            // add PS1 modifier if PVS1 was assigned
            assignDonorAcceptorPS1(acmgEvidenceBuilder, variantEvaluation, clinVarDao);
        } else if (isNonDonorAcceptorSpliceRegionVariant(variantEffect)) {
            // PP3+PS1 or BP4+BP7
            // The database only contains SpliceAI scores > 0.1. This means that any SNP with no score in the database can be considered as
            // not impacting splicing.
            PathogenicityScore spliceAiScore = pathogenicityData.pathogenicityScore(PathogenicitySource.SPLICE_AI);
            if (variantEvaluation.variantType() == VariantType.SNV) {
                float score = spliceAiScore == null ? 0 : spliceAiScore.getScore();
                assignSpliceAiBasedPP3Classification(acmgEvidenceBuilder, score);
                if (acmgEvidenceBuilder.contains(PP3)) {
                    assignNonDonorAcceptorPS1(acmgEvidenceBuilder, variantEvaluation, clinVarDao);
                } else if (acmgEvidenceBuilder.contains(BP4)) {
                    assignSilentIntronicBP7(acmgEvidenceBuilder, variantEvaluation);
                }
            }
        }
        // Note - this is independent of the Splicing guidelines BP7 criteria which is for
        // BP7 "A synonymous (silent) variant for which splicing prediction algorithms predict no impact to the splice consensus sequence nor the creation of a new splice site AND the nucleotide is not highly conserved"
        ClinVarData clinVarData = pathogenicityData.clinVarData();
        assignSynonymousBP7(acmgEvidenceBuilder, variantEvaluation, pathogenicityData, clinVarData);
    }

    private static boolean assignSpliceAiBasedPP3Classification(AcmgEvidence.Builder acmgEvidenceBuilder, float spliceAiScore) {
        // "Using the ACMG/AMP framework to capture evidence related to predicted and observed impact on splicing: Recommendations from the ClinGen SVI Splicing Subgroup"
        // DOI: 10.1016/j.ajhg.2023.06.002
        // variants outside of the +/-1,2 canonical splice acceptor/donor with a spliceAI:
        //   score >= 0.2 can be assigned PP3
        //   score < 0.1 can be assigned BP4
        if (spliceAiScore >= 0.2) {
            acmgEvidenceBuilder.add(PP3);
            return true;
        } else if (spliceAiScore < 0.1) {
            acmgEvidenceBuilder.add(BP4);
            return true;
        }
        return false;
    }

    private static void assignNonDonorAcceptorPS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ClinVarDao clinVarDao) {
        var localClinVarData = getClinVarDataSurroundingVariant(variantEvaluation, clinVarDao);
        Set<Evidence> ps1Evidence = EnumSet.noneOf(Evidence.class);
        for (var entry : localClinVarData.entrySet()) {
            GenomicVariant clinVarVariant = entry.getKey();
            ClinVarData clinVarData = entry.getValue();
            ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
            if (isNonDonorAcceptorSpliceRegionVariant(clinVarData.getVariantEffect()) && isPathOrLikelyPath(primaryInterpretation)) {
                // same position in comparison to VUA
                if (clinVarVariant.contains(variantEvaluation)) {
                    if (isPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.STRONG);
                    } else if (isLikelyPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.MODERATE);
                    }
                } else {
                    // same splice region
                    if (isPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.MODERATE);
                    } else if (isLikelyPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.SUPPORTING);
                    }
                }
            }
        }
        addPs1Evidence(acmgEvidenceBuilder, ps1Evidence);
    }

    private static final Pattern CDS_INTRONIC_HGVS = Pattern.compile("c\\.\\d+(?<intronEnd>[+-])(?<intronPos>\\d+)[A-Z]+>[A-Z]+");
    private static final int DONOR_REGION_END = 6;
    private static final int ACCEPTOR_REGION_START = 20;
    private static void assignSilentIntronicBP7(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        // (C) Silent (excluding last 3 nucleotides of exon and first nucleotide of exon) and intronic variants
        // at or beyond the +7 and −21 positions (conservative designation for donor/acceptor splice region) or otherwise
        // at or beyond the +7 and −4 positions (less conservative designation for the minimal donor/acceptor splice region).

        // currently we can't use the actual transcript to calculate the variant's position in the intron/exon,
        // so we need to use the HGVS expression instead.
        if (variantEvaluation.hasTranscriptAnnotations()) {
            String hgvsc = variantEvaluation.getTranscriptAnnotations().get(0).getHgvsCdna();
            Matcher matcher = CDS_INTRONIC_HGVS.matcher(hgvsc);
            if (matcher.matches()) {
                String intronEnd = matcher.group("intronEnd");
                int position = Integer.parseInt(matcher.group("intronPos"));
                if ("+".equals(intronEnd) && position > DONOR_REGION_END || "-".equals(intronEnd) && position > ACCEPTOR_REGION_START) {
                    acmgEvidenceBuilder.add(BP7);
                }
            }
        }
    }

    // BP7 "A synonymous (silent) variant for which splicing prediction algorithms predict no impact to the splice consensus sequence nor the creation of a new splice site AND the nucleotide is not highly conserved"
    private static void assignSynonymousBP7(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, PathogenicityData pathogenicityData, ClinVarData clinVarData) {
        boolean isReportedPorLP = clinVarData.starRating() >= 1 && isPathOrLikelyPath(clinVarData.getPrimaryInterpretation());
        if (variantEvaluation.getVariantEffect() == SYNONYMOUS_VARIANT && !isReportedPorLP) {
            PathogenicityScore spliceAiScore = pathogenicityData.pathogenicityScore(PathogenicitySource.SPLICE_AI);
            if (spliceAiScore == null || spliceAiScore.getScore() < 0.1) {
                acmgEvidenceBuilder.add(BP7);
            }
        }
    }

    private static void assignDonorAcceptorPS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ClinVarDao clinVarDao) {
        Evidence pvs1Evidence = acmgEvidenceBuilder.evidenceForCategory(PVS1);
        if (pvs1Evidence == null) {
            return;
        }
        // find other clinvar variants in same splice region...
        var localClinVarData = getClinVarDataSurroundingVariant(variantEvaluation, clinVarDao);
        for (var entry : localClinVarData.entrySet()) {
            ClinVarData clinVarData = entry.getValue();
            ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
            // Table 2. PS1 code weights for variants with same predicted splicing event as known (likely) pathogenic variant
            VariantEffect clinVarVariantEffect = clinVarData.getVariantEffect();
            if (isSpliceVariant(clinVarVariantEffect) && isPathOrLikelyPath(primaryInterpretation)) {
                Set<Evidence> ps1Evidence = EnumSet.noneOf(Evidence.class);
                if (pvs1Evidence == Evidence.VERY_STRONG) {
                    // same position in comparison to VUA
                    if (isSpliceDonorAcceptorSpliceVariant(clinVarVariantEffect) && isPath(primaryInterpretation) ||
                        isNonDonorAcceptorSpliceRegionVariant(clinVarVariantEffect) && isLikelyPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.SUPPORTING);
                    }
                }
                else if (pvs1Evidence == Evidence.STRONG || pvs1Evidence == Evidence.MODERATE || pvs1Evidence == Evidence.SUPPORTING) {
                    // same splice region as VUA
                    if (isSpliceDonorAcceptorSpliceVariant(clinVarVariantEffect) && isPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.STRONG);
                    } else if (isNonDonorAcceptorSpliceRegionVariant(clinVarVariantEffect) && isPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.MODERATE);
                    } else if (isNonDonorAcceptorSpliceRegionVariant(clinVarVariantEffect) && isLikelyPath(primaryInterpretation)) {
                        ps1Evidence.add(Evidence.SUPPORTING );
                    }
                }
                addPs1Evidence(acmgEvidenceBuilder, ps1Evidence);
            }
        }
    }

    private static Map<GenomicVariant, ClinVarData> getClinVarDataSurroundingVariant(VariantEvaluation variantEvaluation, ClinVarDao clinVarDao) {
        // ensure region is within contig bounds
        Contig contig = variantEvaluation.contig();
        var upStream = Math.max(1, variantEvaluation.start() - 25);
        var downStream = Math.min(contig.length(), variantEvaluation.start() + 25);
        GenomicInterval genomicInterval = GenomicInterval.of(contig, Strand.POSITIVE, Coordinates.oneBased(upStream, downStream));
        return clinVarDao.findClinVarRecordsOverlappingInterval(genomicInterval);
    }

    private static boolean isPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.PATHOGENIC || clinSig == ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
    }

    private static boolean isLikelyPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.LIKELY_PATHOGENIC;
    }

    private static boolean isPathOrLikelyPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.PATHOGENIC || clinSig == ClinVarData.ClinSig.LIKELY_PATHOGENIC || clinSig == ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
    }

    /**
     * Returns true if the variantEffect is a SPLICE_REGION, SPLICE_DONOR or SPLICE_ACCEPTOR type.
     *
     * @param variantEffect
     * @return
     */
    private static boolean isSpliceVariant(VariantEffect variantEffect) {
        return isSpliceDonorAcceptorSpliceVariant(variantEffect) || isNonDonorAcceptorSpliceRegionVariant(variantEffect);
    }

    private static boolean isNonDonorAcceptorSpliceRegionVariant(VariantEffect variantEffect) {
        return variantEffect == SPLICE_REGION_VARIANT;
    }

    private static boolean isSpliceDonorAcceptorSpliceVariant(VariantEffect variantEffect) {
        return variantEffect == SPLICE_DONOR_VARIANT || variantEffect == SPLICE_ACCEPTOR_VARIANT;
    }

    private static void addPs1Evidence(AcmgEvidence.Builder acmgEvidenceBuilder, Set<Evidence> ps1Evidence) {
        Evidence maxPs1Strength = null;
        if (ps1Evidence.contains(Evidence.STRONG)) {
            maxPs1Strength = Evidence.STRONG;
        } else if (ps1Evidence.contains(Evidence.MODERATE)) {
            maxPs1Strength = Evidence.MODERATE;
        } else if (ps1Evidence.contains(Evidence.SUPPORTING)) {
            maxPs1Strength = Evidence.SUPPORTING;
        }

        if (maxPs1Strength != null) {
            acmgEvidenceBuilder.add(PS1, maxPs1Strength);
        }
    }

}
