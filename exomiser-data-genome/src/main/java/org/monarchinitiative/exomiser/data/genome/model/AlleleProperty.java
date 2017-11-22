/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model;

import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum AlleleProperty {
    KG,
    TOPMED,
    UK10K,

    ESP_EA,
    ESP_AA,
    ESP_ALL,

    EXAC_AFR,
    EXAC_AMR,
    EXAC_EAS,
    EXAC_FIN,
    EXAC_NFE,
    EXAC_OTH,
    EXAC_SAS,

    SIFT,
    POLYPHEN,
    MUT_TASTER;

    public static final Set<AlleleProperty> FREQUENCY_PROPERTIES = ImmutableSet.copyOf(EnumSet.range(KG, EXAC_SAS));
    public static final Set<AlleleProperty> PATHOGENIC_PROPERTIES = ImmutableSet.copyOf(EnumSet.range(SIFT, MUT_TASTER));

}
