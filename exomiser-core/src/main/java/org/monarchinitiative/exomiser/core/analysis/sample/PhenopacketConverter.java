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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;

/**
 * Utility class providing methods for converting Phenopackets to Exomiser classes.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class PhenopacketConverter {

    private PhenopacketConverter() {
        // Uninstantiable utility class
    }

    public static Sample toSample(Family family) {
        return PhenopacketSampleConverter.toExomiserSample(family);
    }

    public static Sample toSample(Phenopacket phenopacket) {
        return PhenopacketSampleConverter.toExomiserSample(phenopacket);
    }

    public static Pedigree toExomiserPedigree(org.phenopackets.schema.v1.core.Pedigree pedigree) {
        return PhenopacketPedigreeConverter.toExomiserPedigree(pedigree);
    }

    public static Pedigree.Individual.Sex toExomiserSex(org.phenopackets.schema.v1.core.Sex sex) {
        return PhenopacketPedigreeConverter.toExomiserSex(sex);
    }

    public static Age toExomiserAge(org.phenopackets.schema.v1.core.Age age) {
        if (org.phenopackets.schema.v1.core.Age.getDefaultInstance().equals(age)) {
            return Age.unknown();
        }
        return Age.parse(age.getAge());
    }

    // toPhenopacket methods

    public static org.phenopackets.schema.v1.core.Pedigree toPhenopacketPedigree(Pedigree pedigree) {
        return PhenopacketPedigreeConverter.toPhenopacketPedigree(pedigree);
    }

    public static org.phenopackets.schema.v1.core.Sex toPhenopacketSex(Pedigree.Individual.Sex sex) {
        return PhenopacketPedigreeConverter.toPhenopacketSex(sex);
    }

    public static org.phenopackets.schema.v1.core.Age toPhenopacketAge(Age age) {
        if (age.isUnknown()) {
            return org.phenopackets.schema.v1.core.Age.getDefaultInstance();
        }
        return org.phenopackets.schema.v1.core.Age.newBuilder().setAge(age.toPeriod().toString()).build();
    }

}
