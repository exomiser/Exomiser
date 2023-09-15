package org.monarchinitiative.exomiser.core.analysis.util.acmg;

/**
 * A points-based classification recommended by the ClinGen <a href = "https://clinicalgenome.org/working-groups/sequence-variant-interpretation/">Sequence Variant Interpretation (SVI)</a> group.
 * This implementation uses the points scales described in
 * <p>
 * Fitting a naturally scaled point system to the ACMG/AMP variant classification guidelines - Tavtigian et al. 2020,
 * DOI:<a href="https://doi.org/10.1002/humu.24088">10.1002/humu.24088</a>.
 * </p>
 * @since 13.3.0
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

}
