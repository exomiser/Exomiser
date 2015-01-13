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
public class TargetFilterResult extends GenericFilterResult {

    private final static FilterType FILTER_TYPE = FilterType.TARGET_FILTER;
    
    public TargetFilterResult(float score, FilterResultStatus resultStatus) {
        super(FILTER_TYPE, score, resultStatus);
    }
    
}
