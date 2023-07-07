/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import java.util.Objects;

/**
 * Class representing the HPO term HP:0003674 (Onset) and its child terms.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public enum Onset {

    // Root term of Onset branch
    // The age group in which disease manifestations appear.
    ONSET("HP:0003674", "Onset", AgeRange.under(Age.of(0, 0, 0))),

    // Onset prior to birth.
    ANTENATAL("HP:0030674", "Antenatal onset", AgeRange.under(Age.of(0, 0, 0))),
    // Onset of disease at up to 8 weeks following fertilization (corresponding to 10 weeks of gestation).
    EMBRYONAL("HP:0011460", "Embryonal onset", AgeRange.under(Age.gestational(10, 0))),
    // Onset prior to birth but after 8 weeks of embryonic development (corresponding to a gestational age of 10 weeks).
    FETAL("HP:0011461", "Fetal onset", AgeRange.over(Age.gestational(10, 0))),

    // A phenotypic abnormality that is present at birth.
    CONGENITAL("HP:0003577", "Congenital onset", AgeRange.under(Age.of(0, 0, 0))),
    // Onset of signs or symptoms of disease within the first 28 days of life.
    NEONATAL("HP:0003623", "Neonatal onset", AgeRange.under(Age.of(0, 0, 28))),
    // TODO Check what is 1M ? 31D or 28D? there could be a bug here - check other offsets too and consider using
    //  overlapping terms if within a period +/- x of a boundary e.g. Neonatal and Infantile for 26D or 1M3D
    // Onset of disease manifestations before adulthood, defined here as before the age of 15 years, but excluding neonatal or congenital onset.
    PEDIATRIC("HP:0410280", "Pediatric onset", AgeRange.between(Age.of(0, 0, 28), Age.of(15, 0, 0))),
    // Onset of signs or symptoms of disease between 28 days to one year of life.
    INFANTILE("HP:0003593", "Infantile onset", AgeRange.between(Age.of(0, 0, 28), Age.of(0, 11, 30))),
    // Onset of disease at the age of between 1 and 5 years.
    CHILDHOOD("HP:0011463", "Childhood onset", AgeRange.between(Age.of(1, 0, 0), Age.of(5, 0, 0))),
    // Onset of signs or symptoms of disease between the age of 5 and 15 years.
    JUVENILE("HP:0003621", "Juvenile onset", AgeRange.between(Age.of(5, 0, 0), Age.of(15, 0, 0))),

    //Onset of disease manifestations in adulthood, defined here as at the age of 16 years or later.
    ADULT("HP:0003581", "Adult onset", AgeRange.over(Age.of(16, 0, 0))),
    // Onset of disease at the age of between 16 and 40 years.
    YOUNG_ADULT("HP:0011462", "Young adult onset", AgeRange.between(Age.of(16, 0, 0), Age.of(40, 0, 0))),
    // A type of adult onset with onset of symptoms at the age of 40 to 60 years.
    MIDDLE_AGE("HP:0003596", "Middle age onset", AgeRange.between(Age.of(40, 0, 0), Age.of(60, 0, 0))),
    // A type of adult onset with onset of symptoms after the age of 60 years.,
    LATE("HP:0003584", "Late onset", AgeRange.over(Age.of(60, 0, 0)));

    private final String id;
    private final String label;
    private final AgeRange ageRange;

    Onset(String id, String label, AgeRange ageRange) {
        this.id = id;
        this.label = label;
        this.ageRange = ageRange;
    }

    public Onset parseHpoId(String hpoId) {
        Objects.requireNonNull(hpoId);
        for (Onset onset : Onset.values()) {
            if (onset.id.equalsIgnoreCase(hpoId)) {
                return onset;
            }
        }
        return Onset.ONSET;
    }

}
