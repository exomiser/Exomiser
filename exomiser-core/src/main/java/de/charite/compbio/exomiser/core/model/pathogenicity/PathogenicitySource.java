/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model.pathogenicity;

/**
 * Enum representing the pathogenicity prediction method/database used to
 * calculate a given score.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum PathogenicitySource {
    //variant type is from Jannovar
    VARIANT_TYPE,
    //these guys are calculated from other sources
    POLYPHEN,
    MUTATION_TASTER,
    SIFT,
    CADD,
    NCDS;
    
}
