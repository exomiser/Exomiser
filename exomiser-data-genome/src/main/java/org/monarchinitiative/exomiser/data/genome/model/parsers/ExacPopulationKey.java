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
import static org.monarchinitiative.exomiser.data.genome.model.parsers.ExacPopulationKey.Population.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public record ExacPopulationKey(AlleleProto.FrequencySource frequencySource, String acPop, String anPop, String homPop) {

    public static final String ALLELE_COUNT_PREFIX = "AC";
    public static final String ALLELE_NUMBER_PREFIX = "AN";

    enum Population {
        AFR, AMR, EAS, FIN, NFE, OTH, SAS, ASJ, AMI, MID
    }

    public static final List<ExacPopulationKey> EXAC_EXOMES = List.of(
            ExacPopulationKey.of(EXAC_AFR, AFR),
            ExacPopulationKey.of(EXAC_AMR, AMR),
            ExacPopulationKey.of(EXAC_EAS, EAS),
            ExacPopulationKey.of(EXAC_FIN, FIN),
            ExacPopulationKey.of(EXAC_NFE, NFE),
            ExacPopulationKey.of(EXAC_OTH, OTH),
            ExacPopulationKey.of(EXAC_SAS, SAS)
    );

    public static final List<ExacPopulationKey> GNOMAD_EXOMES = List.of(
            ExacPopulationKey.of(GNOMAD_E_AFR, AFR),
            ExacPopulationKey.of(GNOMAD_E_AMR, AMR),
            ExacPopulationKey.of(GNOMAD_E_ASJ, ASJ),
            ExacPopulationKey.of(GNOMAD_E_EAS, EAS),
            ExacPopulationKey.of(GNOMAD_E_FIN, FIN),
            ExacPopulationKey.of(GNOMAD_E_NFE, NFE),
            ExacPopulationKey.of(GNOMAD_E_OTH, OTH),
            ExacPopulationKey.of(GNOMAD_E_SAS, SAS)
    );

    public static final List<ExacPopulationKey> GNOMAD_GENOMES = List.of(
            ExacPopulationKey.of(GNOMAD_G_AFR, AFR),
            ExacPopulationKey.of(GNOMAD_G_AMR, AMR),
            ExacPopulationKey.of(GNOMAD_G_ASJ, ASJ),
            ExacPopulationKey.of(GNOMAD_G_EAS, EAS),
            ExacPopulationKey.of(GNOMAD_G_FIN, FIN),
            ExacPopulationKey.of(GNOMAD_G_NFE, NFE),
            ExacPopulationKey.of(GNOMAD_G_OTH, OTH)
            //there is no SAS in the gnomAD genomes set.
    );

    static ExacPopulationKey of(AlleleProto.FrequencySource frequencySource, Population population) {
        var acPop = ALLELE_COUNT_PREFIX + "_" + population;
        var anPop = ALLELE_NUMBER_PREFIX + "_" + population;
        var homPop = "Hom_" + population;
        return new ExacPopulationKey(frequencySource, acPop, anPop, homPop);
    }

}
