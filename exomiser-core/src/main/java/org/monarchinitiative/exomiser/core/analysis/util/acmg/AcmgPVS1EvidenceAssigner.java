package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.List;

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.PVS1;

class AcmgPVS1EvidenceAssigner {

    private AcmgPVS1EvidenceAssigner() {
    }

    /**
     * Applies PVS1 according to guidelines in <a href="https://doi.org/10.1002/humu.23626">Recommendations for interpreting the loss of function PVS1 ACMG/AMP variant criterion<a/>.
     *<p>
     * PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
     */
    static void assignPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Sex probandSex, ModeOfInheritance
            modeOfInheritance, List<Disease> knownDiseases) {
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

        GeneConstraint geneConstraint = GeneConstraints.geneConstraint(variantEvaluation.getGeneSymbol());
        // Should this be using the hasCompatibleDiseaseMatches variable?
        boolean inGeneWithKnownDiseaseAssociations = !knownDiseases.isEmpty();
        // TODO should modify the final strength according to ClinGen/GenCC known D2G validity - Table 1 of 10.1002/humu.23626
        VariantEffect variantEffect = variantEvaluation.getVariantEffect();
        if (inGeneWithKnownDiseaseAssociations
            && isLossOfFunctionEffect(variantEffect)
            && (geneConstraint != null && geneConstraint.isLossOfFunctionIntolerant())
            ){
            switch (variantEffect) {
                case STOP_LOST, STOP_GAINED, FRAMESHIFT_ELONGATION, FRAMESHIFT_TRUNCATION, FRAMESHIFT_VARIANT ->
                        assignNonsenseOrFrameshiftPVS1(acmgEvidenceBuilder, variantEvaluation);
                case SPLICE_DONOR_VARIANT, SPLICE_ACCEPTOR_VARIANT ->
                        assignCanonicalSpliceDonorOrAcceptorPVS1(acmgEvidenceBuilder, variantEvaluation);
                case TRANSCRIPT_ABLATION, EXON_LOSS_VARIANT ->
                        assignDeletionPVS1(acmgEvidenceBuilder, variantEvaluation);
                case START_LOST -> assignInitiationCodonPVS1(acmgEvidenceBuilder, variantEvaluation);
                default -> { // do not assign PVS1
                }
            }
        }
    }

    private static void assignNonsenseOrFrameshiftPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        if (predictedToLeadToNmd(variantEvaluation)) {
            acmgEvidenceBuilder.add(PVS1);
        } else {
            acmgEvidenceBuilder.add(PVS1, AcmgCriterion.Evidence.STRONG);
        }
    }

    private static void assignCanonicalSpliceDonorOrAcceptorPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        if (predictedToLeadToNmd(variantEvaluation)) {
            acmgEvidenceBuilder.add(PVS1);
        } else {
            acmgEvidenceBuilder.add(PVS1, AcmgCriterion.Evidence.STRONG);
        }
    }

    private static void assignDeletionPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        // full gene deletion -> PVS1 (Full gene deletion of haploinsufficient gene should be considered P (PVS1_StandAlone?) given absence of conflicting data)
        VariantEffect variantEffect = variantEvaluation.getVariantEffect();
        if (variantEffect == VariantEffect.TRANSCRIPT_ABLATION || variantEffect == VariantEffect.EXON_LOSS_VARIANT && predictedToLeadToNmd(variantEvaluation)) {
            // deletion of entire transcript
            acmgEvidenceBuilder.add(PVS1);
        } else {
            acmgEvidenceBuilder.add(PVS1, AcmgCriterion.Evidence.STRONG);
        }
    }

    private static void assignInitiationCodonPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        acmgEvidenceBuilder.add(PVS1, AcmgCriterion.Evidence.MODERATE);
    }

    private static boolean isLossOfFunctionEffect(VariantEffect variantEffect) {
        return isNonsenseOrFrameshift(variantEffect)
               || isCanonicalSpliceDonorOrAcceptor(variantEffect)
               || isGeneOrExonDeletion(variantEffect)
               || isStartLost(variantEffect);
    }

    private static boolean isNonsenseOrFrameshift(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.STOP_LOST
               || variantEffect == VariantEffect.STOP_GAINED
               || variantEffect == VariantEffect.FRAMESHIFT_ELONGATION
               || variantEffect == VariantEffect.FRAMESHIFT_TRUNCATION
               || variantEffect == VariantEffect.FRAMESHIFT_VARIANT;
    }

    private static boolean isCanonicalSpliceDonorOrAcceptor(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.SPLICE_ACCEPTOR_VARIANT || variantEffect == VariantEffect.SPLICE_DONOR_VARIANT;
    }

    private static boolean isGeneOrExonDeletion(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.EXON_LOSS_VARIANT || variantEffect == VariantEffect.TRANSCRIPT_ABLATION;
    }

    private static boolean isStartLost(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.START_LOST;
    }

    private static boolean predictedToLeadToNmd(VariantEvaluation variantEvaluation) {
        if (variantEvaluation.hasTranscriptAnnotations()) {
            TranscriptAnnotation transcriptAnnotation = variantEvaluation.getTranscriptAnnotations().get(0);
            return predictedToLeadToNmd(transcriptAnnotation);
        }
        return false;
    }

    // this is a crude method as we don't have the actual transcript coordinates here, so we'll only be able to apply the
    // last exon and single exon gene rules
    private static boolean predictedToLeadToNmd(TranscriptAnnotation transcriptAnnotation) {
        // predicted to lead to NMD if not in last exon or last 50bp of penultimate exon, or is in multi-exon transcript.
        // In other words a variant does NOT trigger NMD if it is located:
        //  - in the last exon
        //  - in the last 50 bases of the penultimate exon
        //  - in an exon larger than 400 bases
        //  - within the first 150 (CDS) bases of the transcription start site
        //  - in a single-exon gene
        boolean notInLastExon = transcriptAnnotation.getRank() < transcriptAnnotation.getRankTotal(); // will be false for single exon genes where rank == rankTotal
        VariantEffect variantEffect = transcriptAnnotation.getVariantEffect();
        boolean isExonicOrCanonicalSpliceSite = (transcriptAnnotation.getRankType() == TranscriptAnnotation.RankType.EXON) || variantEffect == VariantEffect.SPLICE_ACCEPTOR_VARIANT || variantEffect == VariantEffect.SPLICE_DONOR_VARIANT;
        return isExonicOrCanonicalSpliceSite && notInLastExon;
    }

    private static boolean compatibleWithRecessive(Sex probandSex, ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return true;
        }
        return probandSex == Sex.FEMALE && modeOfInheritance == ModeOfInheritance.X_RECESSIVE;
    }

    private static boolean compatibleWithDominant(Sex probandSex, ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
            return true;
        }
        if (probandSex == Sex.MALE && (modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT)) {
            return true;
        }
        return (probandSex == Sex.FEMALE || probandSex == Sex.UNKNOWN) && modeOfInheritance == ModeOfInheritance.X_DOMINANT;
    }
}
