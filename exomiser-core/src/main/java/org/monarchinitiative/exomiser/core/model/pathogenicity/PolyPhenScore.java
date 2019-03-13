/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
 * PolyPhen (polymorphism phenotyping) score.
 * 
 * @link http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2855889/?report=classic
 * @link http://genetics.bwh.harvard.edu/pph2/
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PolyPhenScore extends BasePathogenicityScore {
    
    /**
     * Possibly damaging is > 0.446 with Polyphen2 (this is an intermediate
     * category, thus, we are not being extremely strict with the polyphen
     * filter).
     */
    public static final float POLYPHEN_THRESHOLD = 0.446f;

    /**
     * A polyphen2 score above this threshold is probably damaging
     */
    public static final float POLYPHEN_PROB_DAMAGING_THRESHOLD = 0.956f;

    public static PolyPhenScore of(float score) {
        return new PolyPhenScore(score);
    }

    private PolyPhenScore(float score) {
        super(PathogenicitySource.POLYPHEN, score);
    }

    @Override
    public String toString() {
        if (score > POLYPHEN_PROB_DAMAGING_THRESHOLD) {
            return String.format("Polyphen2: %.3f (D)", score);
        } else if (score > POLYPHEN_THRESHOLD) {
            return String.format("Polyphen2: %.3f (P)", score);
        } else {
            return String.format("Polyphen2: %.3f (B)", score);
        }
    }

}
