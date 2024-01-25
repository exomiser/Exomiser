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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.util.List;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;
import static org.monarchinitiative.exomiser.data.genome.model.parsers.GnomadPopulationKey.Population.*;

/**
 * https://gnomad.broadinstitute.org/help/what-populations-are-represented-in-the-gnomad-data
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public record GnomadPopulationKey(AlleleProto.FrequencySource frequencySource, String acPop, String anPop, String homPop) {

    public static final String ALLELE_COUNT_PREFIX = "AC";
    public static final String ALLELE_NUMBER_PREFIX = "AN";

    enum Population {
        AFR, AMR, EAS, FIN, NFE, OTH, SAS, ASJ, AMI, MID
    }

    public static final List<GnomadPopulationKey> GNOMAD_V2_0_EXOMES = List.of(
            GnomadPopulationKey.v1(GNOMAD_E_AFR, AFR),
            GnomadPopulationKey.v1(GNOMAD_E_AMR, AMR),
            GnomadPopulationKey.v1(GNOMAD_E_ASJ, ASJ),
            GnomadPopulationKey.v1(GNOMAD_E_EAS, EAS),
            GnomadPopulationKey.v1(GNOMAD_E_FIN, FIN),
            GnomadPopulationKey.v1(GNOMAD_E_NFE, NFE),
            GnomadPopulationKey.v1(GNOMAD_E_OTH, OTH),
            GnomadPopulationKey.v1(GNOMAD_E_SAS, SAS)
    );

    public static final List<GnomadPopulationKey> GNOMAD_V2_0_GENOMES = List.of(
            GnomadPopulationKey.v1(GNOMAD_G_AFR, AFR),
            GnomadPopulationKey.v1(GNOMAD_G_AMR, AMR),
            GnomadPopulationKey.v1(GNOMAD_G_ASJ, ASJ),
            GnomadPopulationKey.v1(GNOMAD_G_EAS, EAS),
            GnomadPopulationKey.v1(GNOMAD_G_FIN, FIN),
            GnomadPopulationKey.v1(GNOMAD_G_NFE, NFE),
            GnomadPopulationKey.v1(GNOMAD_G_SAS, SAS),
            GnomadPopulationKey.v1(GNOMAD_G_OTH, OTH)
    );

    public static final List<GnomadPopulationKey> GNOMAD_V2_1_EXOMES = List.of(
            GnomadPopulationKey.v2(GNOMAD_E_AFR, AFR),
            GnomadPopulationKey.v2(GNOMAD_E_AMR, AMR),
            GnomadPopulationKey.v2(GNOMAD_E_ASJ, ASJ),
            GnomadPopulationKey.v2(GNOMAD_E_EAS, EAS),
            GnomadPopulationKey.v2(GNOMAD_E_FIN, FIN),
            GnomadPopulationKey.v2(GNOMAD_E_NFE, NFE),
            GnomadPopulationKey.v2(GNOMAD_E_SAS, SAS),
            GnomadPopulationKey.v2(GNOMAD_E_OTH, OTH)
            );

    public static final List<GnomadPopulationKey> GNOMAD_V2_1_GENOMES = List.of(
            GnomadPopulationKey.v2(GNOMAD_G_AFR, AFR),
            GnomadPopulationKey.v2(GNOMAD_G_AMR, AMR),
            GnomadPopulationKey.v2(GNOMAD_G_ASJ, ASJ),
            GnomadPopulationKey.v2(GNOMAD_G_EAS, EAS),
            GnomadPopulationKey.v2(GNOMAD_G_FIN, FIN),
            GnomadPopulationKey.v2(GNOMAD_G_NFE, NFE),
            GnomadPopulationKey.v2(GNOMAD_G_SAS, SAS),
            GnomadPopulationKey.v2(GNOMAD_G_OTH, OTH)
    );

    // https://gnomad.broadinstitute.org/news/2020-10-gnomad-v3-1-new-content-methods-annotations-and-data-availability/#tweaks-and-updates
    public static final List<GnomadPopulationKey> GNOMAD_V3_1_GENOMES = List.of(
            GnomadPopulationKey.v2(GNOMAD_G_AFR, AFR),
            GnomadPopulationKey.v2(GNOMAD_G_AMI, AMI),
            GnomadPopulationKey.v2(GNOMAD_G_AMR, AMR),
            GnomadPopulationKey.v2(GNOMAD_G_ASJ, ASJ),
            GnomadPopulationKey.v2(GNOMAD_G_EAS, EAS),
            GnomadPopulationKey.v2(GNOMAD_G_FIN, FIN),
            GnomadPopulationKey.v2(GNOMAD_G_NFE, NFE),
            GnomadPopulationKey.v2(GNOMAD_G_MID, MID),
            GnomadPopulationKey.v2(GNOMAD_G_SAS, SAS),
            GnomadPopulationKey.v2(GNOMAD_G_OTH, OTH)
    );

    public static final List<GnomadPopulationKey> GNOMAD_V4_EXOMES = List.of(
            GnomadPopulationKey.v4(GNOMAD_E_AFR, AFR),
            GnomadPopulationKey.v4(GNOMAD_E_AMR, AMR),
            GnomadPopulationKey.v4(GNOMAD_E_ASJ, ASJ),
            GnomadPopulationKey.v4(GNOMAD_E_EAS, EAS),
            GnomadPopulationKey.v4(GNOMAD_E_FIN, FIN),
            GnomadPopulationKey.v4(GNOMAD_E_NFE, NFE),
            GnomadPopulationKey.v4(GNOMAD_E_MID, MID),
            GnomadPopulationKey.v4(GNOMAD_E_SAS, SAS),
            GnomadPopulationKey.v4(GNOMAD_E_OTH, OTH)
    );

    public static final List<GnomadPopulationKey> GNOMAD_V4_GENOMES = List.of(
            GnomadPopulationKey.v4(GNOMAD_G_AFR, AFR),
            GnomadPopulationKey.v4(GNOMAD_G_AMR, AMR),
            GnomadPopulationKey.v4(GNOMAD_G_AMI, AMI),
            GnomadPopulationKey.v4(GNOMAD_G_ASJ, ASJ),
            GnomadPopulationKey.v4(GNOMAD_G_EAS, EAS),
            GnomadPopulationKey.v4(GNOMAD_G_FIN, FIN),
            GnomadPopulationKey.v4(GNOMAD_G_MID, MID),
            GnomadPopulationKey.v4(GNOMAD_G_NFE, NFE),
            GnomadPopulationKey.v4(GNOMAD_G_SAS, SAS),
            GnomadPopulationKey.v4(GNOMAD_G_OTH, OTH)
    );

    /**
     * Returns a v1 population key where the population codes are of the form AN_AFR, AC_AFR, HOM_AFR.
     * @param frequencySource
     * @param population
     * @return
     */
    static GnomadPopulationKey v1(AlleleProto.FrequencySource frequencySource, Population population) {
        var acPop = ALLELE_COUNT_PREFIX + "_" + population;
        var anPop = ALLELE_NUMBER_PREFIX + "_" + population;
        var homPop = "HOM_" + population;
        return new GnomadPopulationKey(frequencySource, acPop, anPop, homPop);
    }

    /**
     * Returns a v2.1 population key where the population codes are of the form AN_afr, AC_afr, nhomalt_afr
     * @param frequencySource
     * @param population
     * @return
     */
    static GnomadPopulationKey v2(AlleleProto.FrequencySource frequencySource, Population population) {
        String popCode = population.toString().toLowerCase();
        var acPop = ALLELE_COUNT_PREFIX + "_" + popCode;
        var anPop = ALLELE_NUMBER_PREFIX + "_" + popCode;
        var homPop = "nhomalt_" + popCode;
        return new GnomadPopulationKey(frequencySource, acPop, anPop, homPop);
    }

    /**
     * Returns a gnomAD v4 population key where the population codes are of the form AN_afr, AC_afr, nhomalt_afr. This version
     * uses AN_remaining in place of AN_oth.
     * @param frequencySource
     * @param population
     * @return
     */
    static GnomadPopulationKey v4(AlleleProto.FrequencySource frequencySource, Population population) {
        // in gnomad v4 the 'oth' population was renamed as 'remain'
        // see https://gnomad.broadinstitute.org/news/2023-11-genetic-ancestry/
        String popCode = population == OTH ? "remaining" : population.toString().toLowerCase();
        var acPop = ALLELE_COUNT_PREFIX + "_" + popCode;
        var anPop = ALLELE_NUMBER_PREFIX + "_" + popCode;
        var homPop = "nhomalt_" + popCode;
        return new GnomadPopulationKey(frequencySource, acPop, anPop, homPop);
    }

}
