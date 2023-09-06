package org.monarchinitiative.exomiser.core.analysis.util.acmg;

/**
 * This is recommended by the ClinGen <a href = "https://clinicalgenome.org/working-groups/sequence-variant-interpretation/">Sequence Variant Interpretation (SVI)</a> group
 * <p>
 * Fitting a naturally scaled point system to the ACMG/AMP variant classification guidelines - Tavtigian et al. 2020,
 * (DOI:10.1002/humu.24088).
 * </p>
 */
public class Acmg2020PointsBasedClassifier implements AcmgEvidenceClassifier {

    @Override
    public AcmgClassification classify(AcmgEvidence acmgEvidence) {

        if (acmgEvidence.ba() == 1) {
            return AcmgClassification.BENIGN;
        }

        return classification(acmgEvidence.points());
    }

    protected AcmgClassification classification(double points) {
        if (points >= 10) {
            return AcmgClassification.PATHOGENIC;
        }
        if (points >= 6 && points <= 9) {
            return AcmgClassification.LIKELY_PATHOGENIC;
        }
        if (points >= 0 && points <= 5) {
            return AcmgClassification.UNCERTAIN_SIGNIFICANCE;
        }
        if (points >= -6 && points <= -1) {
            return AcmgClassification.LIKELY_BENIGN;
        }
        // points <= -7
        return AcmgClassification.BENIGN;
    }

    public double score(AcmgEvidence acmgEvidence) {
        if (acmgEvidence.ba() == 1) {
            return 0;
        }
        int maxPath = Math.min(scorePaths(acmgEvidence), 10);
        int minBenign = Math.max(scoreBenign(acmgEvidence), -4);

        double score = ((maxPath - minBenign) - -4) / (double) (10 - -4);
        return Math.max(Math.min(score, 1.0), 0.0);
    }

    private int scorePaths(AcmgEvidence acmgEvidence) {
        return acmgEvidence.pp() + (acmgEvidence.pm() * 2) + (acmgEvidence.ps() * 4) + (acmgEvidence.pvs() * 8);
    }

    private int scoreBenign(AcmgEvidence acmgEvidence) {
        return acmgEvidence.bp() + (acmgEvidence.bs() * 4);
    }

}
