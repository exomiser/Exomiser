/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;

import java.util.Set;

/**
 * Interface for describing the heritability of genetic elements such as genes or
 * variants.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Inheritable {

    Set<ModeOfInheritance> getInheritanceModes();

    void setInheritanceModes(Set<ModeOfInheritance> inheritanceModes);

    /**
     * @param modeOfInheritance
     * @return true if the variants for this gene are compatible with the given
     * {@code ModeOfInheritance} otherwise false.
     */
    boolean isCompatibleWith(ModeOfInheritance modeOfInheritance);

}
