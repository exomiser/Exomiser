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

package org.monarchinitiative.exomiser.core.genome;

import java.util.Objects;

/**
 * genome build version - hg19/hg38
 */
public enum GenomeBuild {

    HG19("hg19"), HG38("hg38");


    private final String value;

    GenomeBuild(String value) {
        this.value = value;
    }

    public static GenomeBuild defaultBuild() {
        return GenomeBuild.HG19;
    }

    public static GenomeBuild fromValue(String value) {
        Objects.requireNonNull(value, "'null' is not a valid value for the genome build");
        switch (value.toLowerCase()) {
            case "hg19":
            case "hg37":
            case "grch37":
                return HG19;
            case "hg38":
            case "grch38":
                return HG38;
            default:
                return defaultBuild();
        }
    }

    @Override
    public String toString() {
        return value;
    }
}