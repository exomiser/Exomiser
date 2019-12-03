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

package org.monarchinitiative.exomiser.core.model;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ChromosomalRegion {

    int getChromosome();

    int getStart();

    default int getStartMin() {
        return getStart();
    }

    default int getStartMax() {
        return getStart();
    }

    /**
     * The end chromosome of the region - this is usually the same as the start chromosome, apart from chromosomal
     * re-arrangements (<BND> type in VCF parlance) where this will be on a different chromosome, if known. Maps to
     * CHR2 in a VCF file.
     *
     * @return the int value of the end chromosome
     * @since 13.0.0
     */
    default int getEndChromosome() {
        return getChromosome();
    }

    int getEnd();

    default int getEndMin() {
        return getEnd();
    }

    default int getEndMax() {
        return getEnd();
    }

    default int getLength() {
        return (getEnd() - getStart()) + 1;
    }

    public static int compare(ChromosomalRegion c1, ChromosomalRegion c2) {
        //TODO: implement compare with new fields
        // Check out Guava ComparisonChain

        int chr = c1.getChromosome();
        int otherChr = c2.getChromosome();
        if (chr != otherChr) {
            return Integer.compare(chr, otherChr);
        }

        int start = c1.getStart();
        int otherStart = c2.getStart();
        if (start != otherStart) {
            return Integer.compare(start, otherStart);
        }

        return Integer.compare(c1.getEnd(), c2.getEnd());
    }
}
