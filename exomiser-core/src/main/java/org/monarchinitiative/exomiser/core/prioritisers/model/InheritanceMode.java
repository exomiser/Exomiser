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
    //n.b. This is tightly coupled to parsing the input files and inserting disease data into the database, so don't change the inheritanceCodes
    UNKNOWN("unknown", "U"),

    AUTOSOMAL_RECESSIVE("autosomal recessive", "R"),
    AUTOSOMAL_DOMINANT("autosomal dominant", "D"),
    AUTOSOMAL_DOMINANT_AND_RECESSIVE("autosomal dominant/recessive", "B"),

    X_LINKED("X-linked", "X"),
    X_RECESSIVE("X-linked recessive", "XR"),
    X_DOMINANT("X-linked dominant", "XD"),

    Y_LINKED("Y-linked", "Y"),

    SOMATIC("somatic", "S"),
    MITOCHONDRIAL("mitochondrial", "M"),
    POLYGENIC("polygenic", "P");

    private final String hpoTerm;
    //short form letter code for the inheritance mode
    private final String inheritanceCode;
    
    InheritanceMode(String hpoTerm, String inheritanceCode) {
        this.hpoTerm = hpoTerm;
        this.inheritanceCode = inheritanceCode;
    }
        
    public String getTerm() {
        return hpoTerm;
    }

    public String getInheritanceCode() {
        return inheritanceCode;
    }
    
    /**
     * Returns the InheritanceMode for a given inheritanceCode. Will return UNKNOWN
     * as a default.
     * @param inheritanceCode
     * @return 
     */
    public static InheritanceMode valueOfInheritanceCode(String inheritanceCode) {
        for (InheritanceMode inheritanceMode : InheritanceMode.values()) {
            if (inheritanceMode.inheritanceCode.equals(inheritanceCode)) {
                return inheritanceMode;
            }
        }
        return UNKNOWN;
    }

    public boolean isCompatibleWithDominant() {
        return switch (this) {
            case AUTOSOMAL_DOMINANT, AUTOSOMAL_DOMINANT_AND_RECESSIVE -> true;
            default -> false;
        };
    }

    public boolean isCompatibleWithRecessive() {
        return switch (this) {
            case AUTOSOMAL_RECESSIVE, AUTOSOMAL_DOMINANT_AND_RECESSIVE -> true;
            default -> false;
        };
    }

    public boolean isXlinked() {
        return switch (this) {
            case X_LINKED, X_DOMINANT, X_RECESSIVE -> true;
            default -> false;
        };
    }

    /**
     *
     * @return a set of {@code ModeOfInheritance} which are equivalent to the {@code InheritanceMode)
     */
    public Set<ModeOfInheritance> toModeOfInheritance() {
        return switch (this) {
            case AUTOSOMAL_DOMINANT -> EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);
            case AUTOSOMAL_RECESSIVE -> EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
            case AUTOSOMAL_DOMINANT_AND_RECESSIVE ->
                    EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
            case X_RECESSIVE -> EnumSet.of(ModeOfInheritance.X_RECESSIVE);
            case X_DOMINANT -> EnumSet.of(ModeOfInheritance.X_DOMINANT);
            case X_LINKED -> EnumSet.of(ModeOfInheritance.X_RECESSIVE, ModeOfInheritance.X_DOMINANT);
            case MITOCHONDRIAL -> EnumSet.of(ModeOfInheritance.MITOCHONDRIAL);
            default -> EnumSet.noneOf(ModeOfInheritance.class);
        };
    }

    public boolean isCompatibleWith(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.ANY) {
            return true;
        }
        return switch (this) {
            case AUTOSOMAL_DOMINANT -> modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case AUTOSOMAL_RECESSIVE -> modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case AUTOSOMAL_DOMINANT_AND_RECESSIVE ->
                    modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT || modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case X_RECESSIVE -> modeOfInheritance == ModeOfInheritance.X_RECESSIVE;
            case X_DOMINANT -> modeOfInheritance == ModeOfInheritance.X_DOMINANT;
            case X_LINKED ->
                    modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT;
            case MITOCHONDRIAL -> modeOfInheritance == ModeOfInheritance.MITOCHONDRIAL;
            default -> false;
        };
    }
}
