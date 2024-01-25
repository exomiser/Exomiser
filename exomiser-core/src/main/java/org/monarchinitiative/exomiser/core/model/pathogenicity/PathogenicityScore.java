/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
 * @since 3.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface PathogenicityScore extends Comparable<PathogenicityScore> {

    //Higher scores are more likely pathogenic so this is the reverse of what's normal (we're using a probablility of pathogenicity)
    //comparable requires a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object
    static final int MORE_PATHOGENIC = -1;
    static final int EQUAL = 0;
    static final int LESS_PATHOGENIC = 1;

    /**
     * Static factory constructor to simplify creation of new PathogenicityScore objects.
     *
     * @since 12.0.0
     * @param source {@link PathogenicitySource} of the score
     * @param score the *raw* score, as reported in the {@link PathogenicitySource}
     * @return a typed {@link PathogenicityScore}
     */
    public static PathogenicityScore of(PathogenicitySource source, float score) {
        return switch (source) {
            case POLYPHEN -> PolyPhenScore.of(score);
            case MUTATION_TASTER -> MutationTasterScore.of(score);
            case SIFT -> SiftScore.of(score);
            case CADD -> CaddScore.of(score);
            case REMM -> RemmScore.of(score);
            case REVEL -> RevelScore.of(score);
            case SPLICE_AI -> SpliceAiScore.of(score);
            default -> new BasePathogenicityScore(source, score);
        };
    }

    /**
     * @since 7.0.0
     * @return the {@link PathogenicitySource} the score was derived from
     */
    public PathogenicitySource getSource();

    /**
     * A score in the range of 0-1 where 0 is considered benign and 1 to be pathogenic. The precise nature of the score
     * should be considered on the bases of the source as not all scores will scale in an equivalent way. In cases where
     * the natural range of a score is not 0-1 it is required that the implementation handles the scaling.
     *
     * @return a 0-1 scaled score for this object
     */
    public float getScore();

    /**
     * In cases such as SIFT or CADD the raw scores as contained in that data source require scaling in order to fit
     * the 0-1 scale used in exomiser. In these cases the result of getScore and getRawScore will not be equal.
     *
     * @since 12.0.0
     * @return the raw score for this object
     */
    public float getRawScore();

    /**
     * For the purposes of this comparator scores are ranked on a scale of 0 to
     * 1 where 0 is considered probably non-pathogenic and 1 probably
     * pathogenic. This comparator will rank the more pathogenic
     * PathogenicityScore higher than a less pathogenic score such that in a
     * sorted list the most pathogenic score will come first.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     * @since 12.0.0
     * @param o the other score against which to compare this instance
     */
    @Override
    public default int compareTo(PathogenicityScore o) {
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
     * @since 12.0.0
     * @return  the value {@code 0} if the scaled score of {@code o1} is numerically equal to the scaled score of {@code o2};
     *          the value {@code 1} if {@code o1} is numerically less than {@code o2};
     *          and the value {@code -1} if {@code o1} is numerically greater than {@code o2}.
     */
    public static int compare(PathogenicityScore o1, PathogenicityScore o2) {
        // Higher scores are considered more pathogenic, so use the reverse of the standard Float.compare
        return - Float.compare(o1.getScore(), o2.getScore());
    }
}
