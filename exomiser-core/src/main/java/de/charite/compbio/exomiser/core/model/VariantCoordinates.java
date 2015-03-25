/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.jannovar.io.ReferenceDictionary;

/**
 * Bean for storing the chromosomal coordinates of a variant.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantCoordinates {
    /** int representation of the chromosome */
    public int getChromosome();
    /** Start position of the variant on the chromosome */
    public int getPos();
    /** Sequence (one or more nucleotides) of the reference */
    public String getRef();
    /** Sequence (one or more nucleotides) of the alt (variant)  sequence */
    public String getAlt();
       
    public ReferenceDictionary getRefDict();
}
