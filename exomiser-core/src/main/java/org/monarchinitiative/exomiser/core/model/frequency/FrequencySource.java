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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model.frequency;

import com.google.common.collect.Sets;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum describing where the frequency data has originated.
 *
 * @author Damian Smedley <damian.smedley@sanger.ac.uk>
 * @author Jules  Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum FrequencySource {

    UNKNOWN("unknown"),
    //Frequencies from a local datasource
    LOCAL("Local"),

    //Thousand genomes http://www.1000genomes.org/ 
    THOUSAND_GENOMES("1000Genomes"),
    //https://www.nhlbi.nih.gov/research/resources/nhlbi-precision-medicine-initiative/topmed
    TOPMED("TOPMed"),
    //http://www.uk10k.org/studies/cohorts.html
    UK10K("UK10K"),

    //ESP project http://evs.gs.washington.edu/EVS/
    ESP_AFRICAN_AMERICAN("ESP AA"),
    ESP_EUROPEAN_AMERICAN("ESP EA"),
    ESP_ALL("ESP All"),

    //ExAC project http://exac.broadinstitute.org/about
    EXAC_AFRICAN_INC_AFRICAN_AMERICAN("ExAC AFR"),
    EXAC_AMERICAN("ExAC AMR"),
    EXAC_EAST_ASIAN("ExAC EAS"),
    EXAC_FINNISH("ExAC FIN"),
    EXAC_NON_FINNISH_EUROPEAN("ExAC NFE"),
    EXAC_OTHER("ExAC OTH"),
    EXAC_SOUTH_ASIAN("ExAC SAS"),

    //http://gnomad.broadinstitute.org/about
    GNOMAD_E_AFR("gnomAD_E_AFR"),
    GNOMAD_E_AMR("gnomAD_E_AMR"),
    GNOMAD_E_ASJ("gnomAD_E_ASJ"),
    GNOMAD_E_EAS("gnomAD_E_EAS"),
    GNOMAD_E_FIN("gnomAD_E_FIN"),
    GNOMAD_E_NFE("gnomAD_E_NFE"),
    GNOMAD_E_OTH("gnomAD_E_OTH"),
    GNOMAD_E_SAS("gnomAD_E_SAS"),

    GNOMAD_G_AFR("gnomAD_G_AFR"),
    GNOMAD_G_AMR("gnomAD_G_AMR"),
    GNOMAD_G_ASJ("gnomAD_G_ASJ"),
    GNOMAD_G_EAS("gnomAD_G_EAS"),
    GNOMAD_G_FIN("gnomAD_G_FIN"),
    GNOMAD_G_NFE("gnomAD_G_NFE"),
    GNOMAD_G_OTH("gnomAD_G_OTH"),
    GNOMAD_G_SAS("gnomAD_G_SAS"),

    // structural variant frequency sources
    DBVAR("dbVar"),
    DECIPHER("DECIPHER"),
    DGV("DGV"),
    GONL("GoNL"),
    GNOMAD_SV("gnomAD_SV");

    public static final Set<FrequencySource> ALL_ESP_SOURCES = Sets.immutableEnumSet(EnumSet.range(ESP_AFRICAN_AMERICAN, ESP_ALL));

    public static final Set<FrequencySource> ALL_EXAC_SOURCES = Sets.immutableEnumSet(EnumSet.range(EXAC_AFRICAN_INC_AFRICAN_AMERICAN, EXAC_SOUTH_ASIAN));

    public static final Set<FrequencySource> ALL_GNOMAD_SOURCES = Sets.immutableEnumSet(EnumSet.range(GNOMAD_E_AFR, GNOMAD_G_SAS));

    public static final Set<FrequencySource> ALL_EXTERNAL_FREQ_SOURCES = Sets.immutableEnumSet(EnumSet.range(THOUSAND_GENOMES, GNOMAD_G_SAS));

    /**
     * Returns the set of non-founder/bottle-necked populations for use in frequency filtering.
     * Here we're using the populations included in the <a href="https://gnomad.broadinstitute.org/help/faf">gnomAD
     * filtering allele frequency</a>. Note that this is related to the ClinGen recommendations for populations to
     * consider for filtering against when considering the BA1 (Benign standAlone) evidence category. More explicitly,
     * this means any {@link FrequencySource} <b>excluding</b> Ashkenazi Jewish (ASJ), European Finnish (FIN), and
     * "Other" (OTH) populations (gnomAD v2) or Amish (AMI), Ashkenazi Jewish (ASJ), European Finnish (FIN),
     * Middle Eastern (MID), and "Other" (OTH) populations (gnomAD v3).
     * @since 13.3.0
     */
    public static final Set<FrequencySource> NON_FOUNDER_POPS = Sets.immutableEnumSet(
            EnumSet.of(
                    THOUSAND_GENOMES,
                    TOPMED,
                    UK10K,

                    ESP_AFRICAN_AMERICAN,
                    ESP_EUROPEAN_AMERICAN,
                    ESP_ALL,

                    EXAC_AFRICAN_INC_AFRICAN_AMERICAN,
                    EXAC_AMERICAN,
                    EXAC_EAST_ASIAN,
//                    EXAC_FINNISH,
                    EXAC_NON_FINNISH_EUROPEAN,
//                    EXAC_OTHER,
                    EXAC_SOUTH_ASIAN,

                    GNOMAD_E_AFR,
                    GNOMAD_E_AMR,
//                    GNOMAD_E_ASJ,
                    GNOMAD_E_EAS,
//                    GNOMAD_E_FIN,
                    GNOMAD_E_NFE,
//                    GNOMAD_E_OTH,
                    GNOMAD_E_SAS,

                    GNOMAD_G_AFR,
                    GNOMAD_G_AMR,
//                    GNOMAD_G_ASJ,
                    GNOMAD_G_EAS,
//                    GNOMAD_G_FIN,
                    GNOMAD_G_NFE,
//                    GNOMAD_G_OTH,
                    GNOMAD_G_SAS
            )
    );

    private final String source;

    FrequencySource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

}
