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

import java.util.ArrayList;
import java.util.List;

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgClassification.*;

/**
 * Implementation of the ACGS v4 guidelines, Table 3 from
 * https://www.acgs.uk.com/media/11631/uk-practice-guidelines-for-variant-classification-v4-01-2020.pdf
 *
 * @since 13.1.0
 */
public final class Acgs2020Classifier implements AcmgEvidenceClassifier {

    public AcmgClassification classify(AcmgEvidence acmgEvidence) {
        if (acmgEvidence == null || acmgEvidence.isEmpty()) {
            return AcmgClassification.UNCERTAIN_SIGNIFICANCE;
        }

        int pvs = acmgEvidence.pvs();
        int ps = acmgEvidence.ps();
        int pm = acmgEvidence.pm();
        int pp = acmgEvidence.pp();

        int ba = acmgEvidence.ba();
        int bs = acmgEvidence.bs();
        int bp = acmgEvidence.bp();

        return classifyCriteriaCounts(pvs, ps, pm, pp, ba, bs, bp);
    }

    private static AcmgClassification classifyCriteriaCounts(int pvs, int ps, int pm, int pp, int ba, int bs, int bp) {
        List<AcmgClassification> classifications = new ArrayList<>(2);
        // Table 3 of https://www.acgs.uk.com/media/11631/uk-practice-guidelines-for-variant-classification-v4-01-2020.pdf
        if (isPathogenic(pvs, ps, pm, pp)) {
            classifications.add(PATHOGENIC);
        } else if (isLikelyPathogenic(pvs, ps, pm, pp)) {
            classifications.add(LIKELY_PATHOGENIC);
        }

        if (isBenign(ba, bs)) {
            classifications.add(BENIGN);
        } else if (isLikelyBenign(bs, bp)) {
            classifications.add(LIKELY_BENIGN);
        }
        // Uncertain significance:
        // (i)  Other criteria shown above are not met OR
        // (ii) the criteria for benign and pathogenic are contradictory
        return classifications.size() != 1 ? UNCERTAIN_SIGNIFICANCE : classifications.get(0);
    }

    private static boolean isPathogenic(int pvs, int ps, int pm, int pp) {
        if (pvs >= 2 || pvs == 1 && (ps >= 1 || pm >= 1 || pp >= 2)) {
            return true;
        }
        return ps >= 3 || ps == 2 && (pm >= 1 || pp >= 2) || ps == 1 && (pm >= 3 || (pm >= 2 && pp >= 2) || (pm >= 1 && pp >= 4));
    }

    private static boolean isLikelyPathogenic(int pvs, int ps, int pm, int pp) {
        if (pvs == 1 && pp == 1) {
            return true;
        }
        if (ps >= 2) {
            return true;
        }
        if ((ps == 1 && (pm == 1 || pm == 2)) || (ps == 1 && pp >= 2)) {
            return true;
        }
        // https://varsome.com/about/resources/acmg-implementation/#acmgverdict
        // "Since we can only automate 3 of the 5 pathogenic supporting rules, we have also adjusted the verdict so that
        //   1 Moderate + 3 Supporting Pathogenic rules is sufficient to trigger 'Likely Pathogenic' (the standard
        //   requires 1 Moderate + 4 Supporting)."
        // standard ACMG guidelines require pm = 1 && pp >= 4
//        return pm >= 3 || pm == 2 && pp >= 2 || pm == 1 && pp >= 4;
        return pm >= 3 || pm == 2 && pp >= 2 || pm == 1 && pp >= 3;
    }

    private static boolean isBenign(int ba, int bs) {
        return ba == 1 || bs >= 2;
    }

    private static boolean isLikelyBenign(int bs, int bp) {
        return bs == 1 && bp == 1 || bp >= 2;
    }

}
