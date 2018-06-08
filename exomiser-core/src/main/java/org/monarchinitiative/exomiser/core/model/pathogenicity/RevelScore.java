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
 * Class representing a REVEL pathogenicity predictor score.
 *
 * “REVEL: An ensemble method for predicting the pathogenicity of rare missense variants.”  American Journal of Human Genetics 2016; 99(4):877-885.
 * http://dx.doi.org/10.1016/j.ajhg.2016.08.016
 * https://sites.google.com/site/revelgenomics/
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class RevelScore extends BasePathogenicityScore {

    public static RevelScore valueOf(float score) {
        return new RevelScore(score);
    }

    private RevelScore(float score) {
        super(score, PathogenicitySource.REVEL);
    }

    @Override
    public String toString() {
        return String.format("REVEL: %.3f", score);
    }
}
