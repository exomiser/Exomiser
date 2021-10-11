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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.monarchinitiative.exomiser.core.analysis.util.AcmgClassification.*;

public class AcmgClassifier {

    private AcmgClassifier() {
    }

    public static AcmgClassification classify(Set<AcmgCriterion> acmgCategories) {
        if (acmgCategories == null || acmgCategories.isEmpty()) {
            return AcmgClassification.UNCERTAIN_SIGNIFICANCE;
        }

        int pvs = 0;
        int ps = 0;
        int pm = 0;
        int pp = 0;

        int ba = 0;
        int bs = 0;
        int bp = 0;

        for (AcmgCriterion acmgCriterion : acmgCategories) {
            AcmgCriterion.Evidence evidence = acmgCriterion.evidence();
            if (acmgCriterion.isPathogenic()) {
                switch (evidence) {
                    case VERY_STRONG:
                        pvs++;
                        break;
                    case STRONG:
                        ps++;
                        break;
                    case MODERATE:
                        pm++;
                        break;
                    case SUPPORTING:
                        pp++;
                        break;
                    default:
                        // do nothing
                        break;
                }
            }

            if (acmgCriterion.isBenign()) {
                switch (evidence) {
                    case STAND_ALONE:
                        ba++;
                        break;
                    case STRONG:
                        bs++;
                        break;
                    case SUPPORTING:
                        bp++;
                        break;
                    default:
                        // do nothing
                        break;
                }
            }
        }

        return classifyCriteriaCounts(pvs, ps, pm, pp, ba, bs, bp);
    }

    private static AcmgClassification classifyCriteriaCounts(int pvs, int ps, int pm, int pp, int ba, int bs, int bp) {
        List<AcmgClassification> classifications = new ArrayList<>(2);
        // Table 5 of https://www.acmg.net/docs/Standards_Guidelines_for_the_Interpretation_of_Sequence_Variants.pdf
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
        if (pvs == 1 && (ps >= 1 || pm >= 2 || (pm == 1 && pp == 1) || pp >= 2)) {
            return true;
        }
        return ps >= 2 || ps == 1 && (pm >= 3 || pm == 2 && pp >= 2 || pm == 1 && pp >= 4);
    }

    private static boolean isLikelyPathogenic(int pvs, int ps, int pm, int pp) {
        if (pvs == 1 && pm == 1) {
            return true;
        }
        if (ps == 1 && (pm == 1 || pm == 2) || ps == 1 && pp >= 2) {
            return true;
        }
        return pm >= 3 || pm == 2 && pp >= 2 || pm == 1 && pp >= 4;
    }

    private static boolean isBenign(int ba, int bs) {
        return ba == 1 || bs >= 2;
    }

    private static boolean isLikelyBenign(int bs, int bp) {
        return bs == 1 && bp == 1 || bp >= 2;
    }

}
