/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.filters;

/**
 *
 * @author jj8
 */
public class InheritanceFilterResult extends GenericFilterResult {

    private static final FilterType FILTER_TYPE = FilterType.INHERITANCE_FILTER;
    
    public InheritanceFilterResult(float score, FilterResultStatus resultStatus) {
        super(FILTER_TYPE, score, resultStatus);
    }  
}
