/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

/**
 * Enum representing the species present in the Exomiser database for which
 * there are phenotype mappings. These are few and should not be added to
 * frequently.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum Species {

    //Not sure I really like this as it's not exactly flexible and therea re alot of different species out there.
    HUMAN, MOUSE, FISH;
}
