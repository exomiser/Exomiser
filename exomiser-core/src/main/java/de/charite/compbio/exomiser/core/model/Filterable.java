/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filter.FilterType;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface Filterable {
    
    public boolean passesFilters();
    
    public boolean passedFilter(FilterType filterType);
}
