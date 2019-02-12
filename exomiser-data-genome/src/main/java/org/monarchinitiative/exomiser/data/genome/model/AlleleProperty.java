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

    GNOMAD_E_AFR,
    GNOMAD_E_AMR,
    GNOMAD_E_ASJ,
    GNOMAD_E_EAS,
    GNOMAD_E_FIN,
    GNOMAD_E_NFE,
    GNOMAD_E_OTH,
    GNOMAD_E_SAS,

    GNOMAD_G_AFR,
    GNOMAD_G_AMR,
    GNOMAD_G_ASJ,
    GNOMAD_G_EAS,
    GNOMAD_G_FIN,
    GNOMAD_G_NFE,
    GNOMAD_G_OTH,
//  There is no SAS in gnomad genomes.

    SIFT,
    POLYPHEN,
    MUT_TASTER,
    CADD,
    REMM,
    REVEL;

    public static final Set<AlleleProperty> FREQUENCY_PROPERTIES = ImmutableSet.copyOf(EnumSet.range(KG, GNOMAD_G_OTH));
    public static final Set<AlleleProperty> PATHOGENIC_PROPERTIES = ImmutableSet.copyOf(EnumSet.range(SIFT, REVEL));

}
