/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers.phenogrid;

import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhenoGridMatchGroup {
    
    private final List<PhenoGridMatch> matches;

    public PhenoGridMatchGroup(List<PhenoGridMatch> matches) {
        this.matches = matches;
    }

    public List<PhenoGridMatch> getMatches() {
        return matches;
    }
    
    
}
