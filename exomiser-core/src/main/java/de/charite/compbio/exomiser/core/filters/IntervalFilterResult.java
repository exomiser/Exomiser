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
public class IntervalFilterResult extends GenericFilterResult {
    
    private static final FilterType FILTER_TYPE = FilterType.INTERVAL_FILTER;
    
    public IntervalFilterResult(float score, FilterResultStatus resultStatus) {
        super(FILTER_TYPE, score, resultStatus);
    }  
 
}
