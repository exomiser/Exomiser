/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatch {
    
    private String id;
    private String label;
    private String type;
    
    private List<PhenotypeMatch> matches;
    
}
