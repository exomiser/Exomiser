/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

/**
 * A deleteriousness prediction score for missense variants based on regional missense constraint.
 * The range of MPC score is 0 to 5. The larger the score, the more likely the variant is pathogenic. MPC scores greater
 * that or equal to 2.0 are significantly enriched in cases compared to controls.
 *
 * *CAUTION* This score will scale itself to the 0-1 range in order to be comparable with other {@link PathogenicityScore}
 *
 *
 * See: http://dx.doi.org/10.1101/148353
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class MpcScore extends ScaledPathogenicityScore {


    /**
     * Static factory for creating {@link MpcScore} objects.
     *
     * @param score the unscaled MPC score ranging from 0 to 5
     * @return  an MpcScore object containing scaled and raw scores.
     */
    public static MpcScore of(float score) {
        return new MpcScore(score, score * 0.2f);
    }

    private MpcScore(float rawScore, float scaledScore) {
        super(PathogenicitySource.MPC, rawScore, scaledScore);
    }
}
