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

package org.monarchinitiative.exomiser.core.model;

import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Chromosome {

    private static final Chromosome UNKNOWN_CHR = Chromosome.of(0, "na", 0, "", "");

    private final int id;
    private final String name;
    private final int length;
    private final String refSeqAccession;
    private final String genBankAccession;

    private Chromosome(int id, String name, int length, String refSeqAccession, String genBankAccession) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.length = length;
        this.refSeqAccession = Objects.requireNonNull(refSeqAccession);
        this.genBankAccession = genBankAccession;
    }

    public static Chromosome of(int id, String name, int length, String refSeqAccession, String genBankAccession) {
        return new Chromosome(id, name, length, refSeqAccession, genBankAccession);
    }

    public static Chromosome unknown() {
        return UNKNOWN_CHR;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public String getRefSeqAccession() {
        return refSeqAccession;
    }

    public String getGenBankAccession() {
        return genBankAccession;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chromosome)) return false;
        Chromosome that = (Chromosome) o;
        return id == that.id &&
                length == that.length &&
                name.equals(that.name) &&
                refSeqAccession.equals(that.refSeqAccession) &&
                genBankAccession.equalsIgnoreCase((that.genBankAccession));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, length, refSeqAccession, genBankAccession);
    }

    public int compareTo(Chromosome o) {
        return Integer.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return "Chromosome{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", length=" + length +
                ", refSeqAccession='" + refSeqAccession +
                ", genBankAccession='" + genBankAccession + '\'' +
                '}';
    }
}
