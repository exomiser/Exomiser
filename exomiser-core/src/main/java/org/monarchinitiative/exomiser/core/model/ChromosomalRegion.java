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

package org.monarchinitiative.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Interface to represent a simple region on a chromosome. For a representation of variation over a region use the
 * {@link Variant} for biological annotations of variation over a region.
 *
// * @deprecated Clients should implement {@link org.monarchinitiative.svart.GenomicRegion} or extend {@link org.monarchinitiative.svart.BaseGenomicRegion}
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
//@Deprecated(forRemoval = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface ChromosomalRegion {

    public int contigId();

    public int start();

    public int end();

    public static int compare(ChromosomalRegion c1, ChromosomalRegion c2) {
        int chr = c1.contigId();
        int otherChr = c2.contigId();
        if (chr != otherChr) {
            return Integer.compare(chr, otherChr);
        }

        int start = c1.start();
        int otherStart = c2.start();
        if (start != otherStart) {
            return Integer.compare(start, otherStart);
        }

        return Integer.compare(c1.end(), c2.end());
    }
}
