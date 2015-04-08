/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

/**
 * Status of a Filterable - has it been filtered? Did it pass or fail?
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public enum FilterStatus {
   
    UNFILTERED, FAILED, PASSED;
}
