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

import org.monarchinitiative.exomiser.core.genome.Contig;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleVariantCoordinates implements VariantCoordinates {

    private final GenomeAssembly genomeAssembly;
    private final int chr;
    private final int start;
    private final String ref;
    private final String alt;

    public SimpleVariantCoordinates(GenomeAssembly genomeAssembly, int chr, int start, String ref, String alt) {
        this.genomeAssembly = genomeAssembly;
        this.chr = chr;
        this.start = start;
        this.ref = ref;
        this.alt = alt;
    }

    @Override
    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public int getChromosome() {
        return chr;
    }

    @Override
    public String getChromosomeName() {
        return Contig.toString(chr);
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return start;
    }

    @Override
    public String getRef() {
        return ref;
    }

    @Override
    public String getAlt() {
        return alt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleVariantCoordinates that = (SimpleVariantCoordinates) o;

        if (chr != that.chr) return false;
        if (start != that.start) return false;
        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;
        return !(alt != null ? !alt.equals(that.alt) : that.alt != null);

    }

    @Override
    public int hashCode() {
        int result = chr;
        result = 31 * result + start;
        result = 31 * result + (ref != null ? ref.hashCode() : 0);
        result = 31 * result + (alt != null ? alt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleVariantCoordinates{" +
                "chr=" + chr +
                ", start=" + start +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                '}';
    }
}
