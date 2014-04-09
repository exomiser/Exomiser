/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core;

/**
 * Bean for storing the chromosomal coordinates of a variant.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantCoordinates {
    
/** Byte representation of the chromosome */
    private byte chromosome;
    /** Start position of the variant on the chromosome */
    private int pos;
    /** Sequence (one or more nucleotides) of the reference */
    private String ref;
    /** Sequence (one or more nucleotides) of the alt (variant)  sequence */
    private String alt;
    /** Integer representation of the rsID */
    private String rsID;

    public VariantCoordinates(byte chromosome, int pos, String rsID, String ref, String alt) {
        this.chromosome = chromosome;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.rsID = rsID;
    }

    
    @Override
    public String toString() {
        return "VariantCoordinates{" + "chr=" + chromosome + ", pos=" + pos + ", ref=" + ref + ", alt=" + alt + ", rsID=" + rsID + '}';
    }
    
    
    
}
