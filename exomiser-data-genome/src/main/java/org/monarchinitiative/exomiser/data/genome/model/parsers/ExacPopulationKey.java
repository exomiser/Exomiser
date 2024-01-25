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

import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.List;
import java.util.Objects;

import static org.monarchinitiative.exomiser.data.genome.model.AlleleProperty.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExacPopulationKey {

    public static final String ALLELE_COUNT_PREFIX = "AC";
    public static final String ALLELE_NUMBER_PREFIX = "AN";

    private static final String AC_AFR = "AC_AFR";
    private static final String AN_AFR = "AN_AFR";
    private static final String AC_AMR = "AC_AMR";
    private static final String AN_AMR = "AN_AMR";
    private static final String AC_EAS = "AC_EAS";
    private static final String AN_EAS = "AN_EAS";
    private static final String AC_FIN = "AC_FIN";
    private static final String AN_FIN = "AN_FIN";
    private static final String AC_NFE = "AC_NFE";
    private static final String AN_NFE = "AN_NFE";
    private static final String AC_OTH = "AC_OTH";
    private static final String AN_OTH = "AN_OTH";
    private static final String AC_SAS = "AC_SAS";
    private static final String AN_SAS = "AN_SAS";
    //ASJ is a gnomAD only population
    private static final String AC_ASJ = "AC_ASJ";
    private static final String AN_ASJ = "AN_ASJ";

    public static final List<ExacPopulationKey> EXAC_EXOMES = List.of(
            ExacPopulationKey.of(EXAC_AFR, AC_AFR, AN_AFR),
            ExacPopulationKey.of(EXAC_AMR, AC_AMR, AN_AMR),
            ExacPopulationKey.of(EXAC_EAS, AC_EAS, AN_EAS),
            ExacPopulationKey.of(EXAC_FIN, AC_FIN, AN_FIN),
            ExacPopulationKey.of(EXAC_NFE, AC_NFE, AN_NFE),
            ExacPopulationKey.of(EXAC_OTH, AC_OTH, AN_OTH),
            ExacPopulationKey.of(EXAC_SAS, AC_SAS, AN_SAS)
    );

    public static final List<ExacPopulationKey> GNOMAD_EXOMES = List.of(
            ExacPopulationKey.of(GNOMAD_E_AFR, AC_AFR, AN_AFR),
            ExacPopulationKey.of(GNOMAD_E_AMR, AC_AMR, AN_AMR),
            ExacPopulationKey.of(GNOMAD_E_ASJ, AC_ASJ, AN_ASJ),
            ExacPopulationKey.of(GNOMAD_E_EAS, AC_EAS, AN_EAS),
            ExacPopulationKey.of(GNOMAD_E_FIN, AC_FIN, AN_FIN),
            ExacPopulationKey.of(GNOMAD_E_NFE, AC_NFE, AN_NFE),
            ExacPopulationKey.of(GNOMAD_E_OTH, AC_OTH, AN_OTH),
            ExacPopulationKey.of(GNOMAD_E_SAS, AC_SAS, AN_SAS)
    );

    public static final List<ExacPopulationKey> GNOMAD_GENOMES = List.of(
            ExacPopulationKey.of(GNOMAD_G_AFR, AC_AFR, AN_AFR),
            ExacPopulationKey.of(GNOMAD_G_AMR, AC_AMR, AN_AMR),
            ExacPopulationKey.of(GNOMAD_G_ASJ, AC_ASJ, AN_ASJ),
            ExacPopulationKey.of(GNOMAD_G_EAS, AC_EAS, AN_EAS),
            ExacPopulationKey.of(GNOMAD_G_FIN, AC_FIN, AN_FIN),
            ExacPopulationKey.of(GNOMAD_G_NFE, AC_NFE, AN_NFE),
            ExacPopulationKey.of(GNOMAD_G_OTH, AC_OTH, AN_OTH)
            //there is no SAS in the gnomAD genomes set.
    );


    public final AlleleProperty alleleProperty;
    public final String AC;
    public final String AN;

    public static ExacPopulationKey of(AlleleProperty alleleProperty, String acKey, String anKey) {
        return new ExacPopulationKey(alleleProperty, acKey, anKey);
    }

    private ExacPopulationKey(AlleleProperty alleleProperty, String acKey, String anKey) {
        this.alleleProperty = alleleProperty;
        this.AC = acKey;
        this.AN = anKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExacPopulationKey that = (ExacPopulationKey) o;
        return alleleProperty == that.alleleProperty &&
                Objects.equals(AC, that.AC) &&
                Objects.equals(AN, that.AN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alleleProperty, AC, AN);
    }

    @Override
    public String toString() {
        return "PopulationKey{" +
                "alleleProperty=" + alleleProperty +
                ", AC='" + AC + '\'' +
                ", AN='" + AN + '\'' +
                '}';
    }
}
