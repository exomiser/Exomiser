package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.genome.dao.ClinVarDao;
import org.monarchinitiative.exomiser.core.model.GeneStatistics;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;

class AcmgMissenseInFrameIndelEvidenceAssigner {

    private static final Logger logger = LoggerFactory.getLogger(AcmgMissenseInFrameIndelEvidenceAssigner.class);

    // e.g. p.(Lys567Thr)
    private static final Pattern MISSENSE_HGVS_P = Pattern.compile("p\\.\\((?<refPos>[A-Z][a-z]{2}\\d+)(?<alt>[A-Z][a-z]{2})\\)");

    private AcmgMissenseInFrameIndelEvidenceAssigner() {
        // static utility class
    }

    static void assignMissenseEvidence(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<Disease> knownDiseases, ClinVarDao clinVarDao) {
        // ignore non-missense, truncating, splice or mitochondrial variants
        if (isMissenseOrInframeIndel(variantEvaluation.getVariantEffect()) && variantEvaluation.contigId() != 25) {
            // ensure region is within contig bounds
            Contig contig = variantEvaluation.contig();
            var upStream = Math.max(1, variantEvaluation.start() - 25);
            var downStream = Math.min(contig.length(), variantEvaluation.start() + 25);
            GenomicInterval genomicInterval = GenomicInterval.of(contig, Strand.POSITIVE, Coordinates.oneBased(upStream, downStream));
            var localClinVarData = clinVarDao.findClinVarRecordsOverlappingInterval(genomicInterval);
            // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
            // PM5 "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before"
            assignPS1PM5(acmgEvidenceBuilder, variantEvaluation, localClinVarData);
            // PM1 "Located in a mutational hot spot and/or critical and well-established functional domain (e.g., active site of an enzyme) without benign variation"
            assignPM1(acmgEvidenceBuilder, variantEvaluation, localClinVarData);
            // TODO: BP3 "In-frame deletions/insertions in a repetitive region without a known function" - requires domain information.

            // PP2 "Missense variant in a gene that has a low rate of benign missense variation and in which missense variants are a common mechanism of disease."
            // BP1 "Missense variant in a gene for which primarily truncating variants are known to cause disease."
            assignPP2orBP1(acmgEvidenceBuilder, variantEvaluation, clinVarDao);
            // PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
            // BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
            assignPP3orBP4(acmgEvidenceBuilder, variantEvaluation.getPathogenicityData());
        }
    }

    // PP2 - Missense variant in a gene that has a low rate of benign missense variation and in which missense variants are a common mechanism of disease.
    // BP1 - Missense variant in a gene for which primarily truncating variants are known to cause disease.
    private static void assignPP2orBP1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ClinVarDao clinVarDao) {
        GeneStatistics geneStatistics = clinVarDao.getGeneStatistics(variantEvaluation.getGeneSymbol());
        boolean missenseOrInframeIndel = isMissenseOrInframeIndel(variantEvaluation.getVariantEffect());
        if (missenseOrInframeIndel && isPP2Gene(geneStatistics)) {
            acmgEvidenceBuilder.add(PP2);
        } else if (missenseOrInframeIndel && isBP1Gene(geneStatistics)) {
            acmgEvidenceBuilder.add(BP1);
        }
    }

    // a gene that has a low rate of benign missense variation and in which missense variants are a common mechanism of disease.
    private static boolean isPP2Gene(GeneStatistics geneStatistics) {
        int missensePathCount = geneStatistics.missensePathCount();
        int missenseVusCount = geneStatistics.missenseVusCount();
        int missenseBenignCount = geneStatistics.missenseBenignCount();
        return missensePathCount / (float) geneStatistics.pathCount() >= 0.75 && (missensePathCount / (float) missenseBenignCount >= 0.9) && ((float) missenseBenignCount / (missenseBenignCount + missenseVusCount + missensePathCount) <= 0.05);
    }

    // a gene for which primarily truncating variants are known to cause disease.
    private static boolean isBP1Gene(GeneStatistics geneStatistics) {
        return geneStatistics.lofPathCount() / (float) geneStatistics.pathCount() >= 0.75;
    }

    // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
    // PM5 "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before"
    private static void assignPS1PM5(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Map<GenomicVariant, ClinVarData> localClinVarData) {
        if (variantEvaluation.getTranscriptAnnotations().isEmpty()) {
            return;
        }
        String variantProteinChange = variantEvaluation.getTranscriptAnnotations().get(0).getHgvsProtein();
        Matcher queryVariantMatcher = MISSENSE_HGVS_P.matcher(variantProteinChange);
        boolean matches = queryVariantMatcher.matches();
        String aaRefPos = matches ? queryVariantMatcher.group("refPos") : "";
        String aaAlt = matches ? queryVariantMatcher.group("alt") : "";
        for (var entry : localClinVarData.entrySet()) {
            ClinVarData clinVarData = entry.getValue();
            ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
            if (isMissenseOrInframeIndel(clinVarData.getVariantEffect()) && isPathOrLikelyPath(primaryInterpretation)) {
                GenomicVariant clinVarVariant = entry.getKey();
                if (Math.abs(clinVarVariant.distanceTo(variantEvaluation)) <= 2 && GenomicVariant.compare(clinVarVariant, variantEvaluation) != 0) {
                    // within codon so check for same AA change or different AA change
                    Matcher clinvarMatcher = MISSENSE_HGVS_P.matcher(clinVarData.getHgvsProtein());
                    if (clinvarMatcher.matches()) {
                        String clnAaRefPos = clinvarMatcher.group("refPos");
                        String clnAaAlt = clinvarMatcher.group("alt");
                        if (aaRefPos.equals(clnAaRefPos)) {
                            if (aaAlt.equals(clnAaAlt)) {
                                // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
                                AcmgCriterion.Evidence evidence;
                                if (clinVarData.starRating() >= 2) {
                                    evidence = AcmgCriterion.Evidence.STRONG;
                                } else if (clinVarData.starRating() == 1) {
                                    evidence = AcmgCriterion.Evidence.MODERATE;
                                } else {
                                    evidence = AcmgCriterion.Evidence.SUPPORTING;
                                }
                                logger.debug("{} -> {}_{}", clinVarData.getHgvsProtein(), PS1, evidence);
                                acmgEvidenceBuilder.add(PS1, evidence);
                            } else {
                                AcmgCriterion.Evidence evidence = clinVarData.starRating() >= 2 ? AcmgCriterion.Evidence.MODERATE : AcmgCriterion.Evidence.SUPPORTING;
                                logger.debug("{} -> {}_{}", clinVarData.getHgvsProtein(), PM5, evidence);
                                // PM5 "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before"
                                acmgEvidenceBuilder.add(PM5, evidence);
                            }
                        }
                    }
                }
            }
        }
    }

    // PM1 "Located in a mutational hot spot and/or critical and well-established functional domain (e.g., active site of an enzyme) without benign variation"
    private static void assignPM1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Map<GenomicVariant, ClinVarData> localClinVarData) {
        // TODO - need UniProt domain / site info and clinvar counts
        //  can upgrade to STRONG
        // https://www.cell.com/ajhg/fulltext/S0002-9297(22)00461-X suggests to limit the combined evidence from PM1 and PP3 to strong
        int pathCount = 0;
        int vusCount = 0;
        int benignCount = 0;
        for (var entry : localClinVarData.entrySet()) {
            ClinVarData clinVarData = entry.getValue();
            ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
            if (isMissenseOrInframeIndel(clinVarData.getVariantEffect())) {
                if (isPathOrLikelyPath(primaryInterpretation)) {
                    pathCount++;
                }
                if (primaryInterpretation == ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE) {
                    vusCount++;
                }
                if (isBenignOrLikelyBenign(primaryInterpretation)) {
                    benignCount++;
                }
            }
        }
        logger.debug("PM1 evidence for region {} {}:{}-{} Paths: {} VUSs: {} Benigns: {}", variantEvaluation.getGenomeAssembly(), variantEvaluation.contigId(), variantEvaluation.start() - 25, variantEvaluation.end() + 25, pathCount, vusCount, benignCount);
        if (pathCount >= 4 && benignCount == 0) {
            // could do funkier thing to score other path variants by severity/star rating and distance to variant
            if (pathCount > vusCount) {
                acmgEvidenceBuilder.add(PM1);
                logger.debug("{} -> {}", variantEvaluation, PM1);
            } else {
                acmgEvidenceBuilder.add(PM1, Evidence.SUPPORTING);
                logger.debug("{} -> {}_Supporting", variantEvaluation, PM1);
            }
        }
    }

    private static boolean isMissenseOrInframeIndel(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.MISSENSE_VARIANT || variantEffect == VariantEffect.INFRAME_DELETION || variantEffect == VariantEffect.INFRAME_INSERTION;
    }

    private static boolean isBenignOrLikelyBenign(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.BENIGN || clinSig == ClinVarData.ClinSig.LIKELY_BENIGN || clinSig == ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN;
    }

    private static boolean isPathOrLikelyPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.PATHOGENIC || clinSig == ClinVarData.ClinSig.LIKELY_PATHOGENIC || clinSig == ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
    }


    /**
     * PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     * BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     */
    private static void assignPP3orBP4(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityData pathogenicityData) {
        // These approaches are broadly similar in recommending just using one predictor and REVEL is consistently
        // seen to out-perform other predictors. In our testing we also found that the REVEL-only approach worked best.

        // updated from "Evidence-based calibration of computational tools for missense variant pathogenicity
        //   classification and ClinGen recommendations for clinical use of PP3/BP4 criteria"
        //   https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
        //
        var revelScore = pathogenicityData.pathogenicityScore(PathogenicitySource.REVEL);
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
     * https://doi.org/10.1016/j.ajhg.2022.10.013
     *
     * This method provided much better
     */
    private static void assignRevelBasedPP3BP4Classification(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityScore revelScore) {
        var revel = revelScore.getRawScore();
        // Taken from table 2 of https://doi.org/10.1016/j.ajhg.2022.10.013
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
        // Cap at SUPPORTING as this leads to huge VUS -> LB
        else if (revel <= 0.290f) {
            acmgEvidenceBuilder.add(BP4, Evidence.SUPPORTING);
        }
//        else if (revel > 0.183f && revel <= 0.290f) {
//            acmgEvidenceBuilder.add(BP4, Evidence.SUPPORTING);
//        } else if (revel > 0.016f && revel <= 0.183f) {
//            acmgEvidenceBuilder.add(BP4, Evidence.MODERATE);
//        } else if (revel > 0.003f && revel <= 0.016f) {
//            acmgEvidenceBuilder.add(BP4, Evidence.STRONG);
//        } else if (revel <= 0.003f) {
//            acmgEvidenceBuilder.add(BP4, Evidence.VERY_STRONG);
//        }

        // PM1 & PP3 should only reach a max of STRONG i.e. 4 points
        // "Furthermore, it is challenging to separate these shared attributes for tools such as MutPred2 and VEST4 that
        // implicitly incorporate some notion of structural and functional importance to each variant position. To address
        // this potential overlap or double-counting of PP3/BP4 and PM1, we recommend that laboratories limit the sum of
        // the evidence strength of PP3 and PM1 to strong. This would allow PP3 to be invoked as supporting or moderate
        // along with PM1 to be invoked as moderate, which would be the same as limiting the sum of PP3 and PM1 to 4 points
        // in the Bayes points implementation."
        Evidence pm1Ev = acmgEvidenceBuilder.evidenceForCategory(PM1);
        Evidence pp3Ev = acmgEvidenceBuilder.evidenceForCategory(PP3);
        if (pp3Ev == Evidence.STRONG && pm1Ev != null) {
            // defer to PP3 Strong as this should be a lot more sophisticated than the PM1 assignment criteria
            logger.debug("Removing PM1_{} as PP3_{} is at threshold of {}", pm1Ev, pp3Ev, Evidence.STRONG);
            acmgEvidenceBuilder.remove(PM1);
        }
    }

    /*
     * Ensemble-based approach suggested in
     * See "Assessing performance of pathogenicity predictors using clinically relevant variant datasets"
     * http://dx.doi.org/10.1136/jmedgenet-2020-107003
     */
    private static void assignEnsembleBasedPP3BP4Classification(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityData pathogenicityData) {
        int numBenign = 0;
        int numPathogenic = 0;
        List<PathogenicityScore> pathogenicityScores = pathogenicityData.pathogenicityScores();
        for (PathogenicityScore pathogenicityScore : pathogenicityScores) {
            if (isPathogenic(pathogenicityScore)) {
                numPathogenic++;
            } else {
                numBenign++;
            }
        }
        if (pathogenicityScores.size() > 1 && numPathogenic > numBenign) {
            acmgEvidenceBuilder.add(PP3);
        }
        if (pathogenicityScores.size() > 1 && numBenign > numPathogenic) {
            acmgEvidenceBuilder.add(BP4);
        }
    }

    private static boolean isPathogenic(PathogenicityScore pathogenicityScore) {
        if (pathogenicityScore instanceof SiftScore score) {
            return score.getRawScore() < SiftScore.SIFT_THRESHOLD;
        }
        if (pathogenicityScore instanceof MutationTasterScore score) {
            return score.getScore() > MutationTasterScore.MTASTER_THRESHOLD;
        }
        if (pathogenicityScore instanceof PolyPhenScore score) {
            return score.getScore() > PolyPhenScore.POLYPHEN_PROB_DAMAGING_THRESHOLD;
        }
        if (pathogenicityScore instanceof CaddScore score) {
            // 95-99% most deleterious.
            return score.getRawScore() >= 13.0f;
        }
        return pathogenicityScore.getScore() > 0.5f;
    }

}
