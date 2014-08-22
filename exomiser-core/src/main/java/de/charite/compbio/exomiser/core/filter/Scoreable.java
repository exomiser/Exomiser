/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filter;

/**
 * Simple bean for storing the scored result of a filter.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Scoreable {
    
    /**
     * @return return a float representation of the filter result [0..1]. If the
     * result is boolean, return 0.0 for false and 1.0 for true
     */
    public float getScore();
    
}
