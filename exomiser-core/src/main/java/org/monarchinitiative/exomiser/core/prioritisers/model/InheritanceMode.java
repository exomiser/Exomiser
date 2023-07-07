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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.prioritisers.model;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enum representing the different modes on inheritance for a disease.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum InheritanceMode {
    UNKNOWN("HP:0000005", "unknown"),

    AUTOSOMAL_DOMINANT("HP:0000006", "autosomal dominant"),
    AUTOSOMAL_RECESSIVE("HP:0000007", "autosomal recessive"),

    /**
     * A mode of inheritance that is observed for traits related to a gene encoded on chromosomes in which a trait can
     * manifest in a monoallelic (e.g. heterozygotes) and biallelic (e.g. homozygotes, compound heterozygotes) state,
     * with similar or differing phenotype severity present dependent on the number of alleles affected.
     */
    SEMIDOMINANT("HP:0032113", "semidominant"),

    X_LINKED("HP:0001417", "X-linked"),
    X_DOMINANT("HP:0001423", "X-linked dominant"),
    X_RECESSIVE("HP:0001419", "X-linked recessive"),
    Y_LINKED("HP:0001450", "Y-linked"),

    MITOCHONDRIAL("HP:0001427", "mitochondrial"),

    SOMATIC("HP:0001442", "somatic"),
    POLYGENIC("HP:0010982", "polygenic");

    private final String id;
    private final String label;

    InheritanceMode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String id() {return id;}
    public String label() {
        return label;
    }

    public boolean isCompatibleWithDominant() {
        switch(this) {
            case AUTOSOMAL_DOMINANT:
            case SEMIDOMINANT:
                return true;
            default:
                return false;
        }
    }

    public boolean isCompatibleWithRecessive() {
        switch(this) {
            case AUTOSOMAL_RECESSIVE:
            case SEMIDOMINANT:
                return true;
            default:
                return false;
        }
    }

    public boolean isXlinked() {
        switch(this) {
            case X_LINKED:
            case X_DOMINANT:
            case X_RECESSIVE:
                return true;
            default:
                return false;
        }
    }

    /**
     *
     * @return a set of {@code ModeOfInheritance} which are equivalent to the {@code InheritanceMode)
     */
    public Set<ModeOfInheritance> toModeOfInheritance() {
        switch (this) {
            case AUTOSOMAL_DOMINANT:
                return EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);
            case AUTOSOMAL_RECESSIVE:
                return EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
            case SEMIDOMINANT:
                return EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
            case X_RECESSIVE:
                return EnumSet.of(ModeOfInheritance.X_RECESSIVE);
            case X_DOMINANT:
                return EnumSet.of(ModeOfInheritance.X_DOMINANT);
            case X_LINKED:
                return EnumSet.of(ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT);
            case MITOCHONDRIAL:
                return EnumSet.of(ModeOfInheritance.MITOCHONDRIAL);
            default:
                return EnumSet.noneOf(ModeOfInheritance.class);
        }
    }

    public boolean isCompatibleWith(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.ANY) {
            return true;
        }
        switch (this) {
            case AUTOSOMAL_DOMINANT:
                return modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case AUTOSOMAL_RECESSIVE:
                return modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case SEMIDOMINANT:
                return modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT || modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case X_RECESSIVE:
                return modeOfInheritance == ModeOfInheritance.X_RECESSIVE;
            case X_DOMINANT:
                return modeOfInheritance == ModeOfInheritance.X_DOMINANT;
            case X_LINKED:
                return modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT;
            case MITOCHONDRIAL:
                return modeOfInheritance == ModeOfInheritance.MITOCHONDRIAL;
            default:
                return false;
        }
    }
}
