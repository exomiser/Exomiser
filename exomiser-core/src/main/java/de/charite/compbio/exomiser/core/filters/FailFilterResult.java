/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class FailFilterResult extends AbstractFilterResult {

    public FailFilterResult(FilterType filterType) {
        super(filterType, FilterResultStatus.FAIL);
    }
    
}
