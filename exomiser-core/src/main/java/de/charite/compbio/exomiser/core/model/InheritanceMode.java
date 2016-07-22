/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

/**
 * Enum representing the different modes on inheritance for a disease.
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum InheritanceMode {
    //n.b. This is tightly coupled to parsing the input files and inserting disease data into the database, so don't change the inheritanceCodes
    UNKNOWN("unknown", "U"),

    RECESSIVE("autosomal recessive", "R"),
    DOMINANT("autosomal dominant", "D"),
    DOMINANT_AND_RECESSIVE("autosomal dominant/recessive", "B"),

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
    
    /**
     * Returns the InheritanceMode for a given HPO term. Will return UNKNOWN
     * as a default.
     * @param hpoTerm
     * @return 
     */
    public static InheritanceMode valueOfHpoTerm(String hpoTerm) {
        for (InheritanceMode inheritanceMode : InheritanceMode.values()) {
            if (inheritanceMode.hpoTerm.equals(hpoTerm)) {
                return inheritanceMode;
            }
        }
        return UNKNOWN;
    }
}
