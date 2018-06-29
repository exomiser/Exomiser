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
 * Enum representing the pathogenicity prediction method/database used to
 * calculate a given score.
 *
 * CAUTION! REVEL scores tend to be more nuanced and frequently lower thant either the default variant effect score
 * or the other predicted path scores, yet apparently are more concordant with ClinVar. However we do not yet recommend
 * using REVEL either in isolation of with other pathogenicity predictors as the REVEL scores tend to be overwhelmed by
 * the other scores. USE AT YOUR OWN RISK!!!
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum PathogenicitySource {
    //variant type is from Jannovar
    VARIANT_TYPE,
    //these guys are calculated from other sources
    POLYPHEN,
    MUTATION_TASTER,
    SIFT,
    CADD,
    REMM,
    REVEL

}
