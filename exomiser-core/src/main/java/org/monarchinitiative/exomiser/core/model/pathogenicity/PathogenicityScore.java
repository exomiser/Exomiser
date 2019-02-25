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
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PathogenicityScore extends Comparable<PathogenicityScore> {

    //Higher scores are more likely pathogenic so this is the reverse of what's normal (we're using a probablility of pathogenicity)
    //comparable requires a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    static final int MORE_PATHOGENIC = -1;
    static final int EQUAL = 0;
    static final int LESS_PATHOGENIC = 1;

    public static PathogenicityScore of(PathogenicitySource source, float score) {
        switch (source) {
            case POLYPHEN:
                return PolyPhenScore.of(score);
            case MUTATION_TASTER:
                return MutationTasterScore.of(score);
            case SIFT:
                return SiftScore.of(score);
            case CADD:
                return CaddScore.of(score);
            case REMM:
                return RemmScore.of(score);
            case REVEL:
                return RevelScore.of(score);
            case MPC:
                return MpcScore.of(score);
                // TODO: Add MVP, ClinPred, PrimateAi, M-CAP,
            default:
                return new BasePathogenicityScore(source, score);
        }
    }

    public PathogenicitySource getSource();

    public float getScore();

    public float getRawScore();

    @Override
    default int compareTo(PathogenicityScore o) {
        return compare(this, o);
    }

    /**
     * For the purposes of this comparator scores are ranked on a scale of 0 to
     * 1 where 0 is considered probably non-pathogenic and 1 probably
     * pathogenic. This comparator will rank the more pathogenic
     * PathogenicityScore higher than a less pathogenic score such that in a
     * sorted list the most pathogenic score will come first.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    public static int compare(PathogenicityScore o1, PathogenicityScore o2) {
        // Higher scores are considered more pathogenic, so use the reverse of the standard Float.compare
        return - Float.compare(o1.getScore(), o2.getScore());
    }
}
