/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

/**
 * 
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface VariantCoordinates {

    /**
     * @return integer representation of the chromosome
     */
    int getChromosome();

    /**
     * @return String representation of the chromosome. Chromosomes 1-22 will return
     * a string value of their number. Sex chromosomes 23=X 24=Y and mitochondrial 25=M.
     */
    String getChromosomeName();

    /**
     * @return 1-based position of the variant on the chromosome
     */
    int getPosition();

    /**
     * @return String with the reference allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String getRef();

    /**
     * @return String with the alternative allele in the variant, without common
     * suffix or prefix to reference allele.
     */
    String getAlt();

}
