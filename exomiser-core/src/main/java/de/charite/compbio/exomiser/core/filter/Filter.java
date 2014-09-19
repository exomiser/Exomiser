/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.Filterable;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @param <T>
 */
public interface Filter<T extends Filterable> {

    /**
     * @return an integer constant (as defined in exomizer.common.Constants)
     * that will act as a flag to generate the output HTML dynamically depending
     * on the filters that the user has chosen.
     */
    public FilterType getFilterType();

    /**
     * Take a list of variants and apply the filter to each variant. If a
     * variant does not pass the filter it is marked as having failed that
     * filter.
     *
     * @param filterables
     */
    
    //TODO: this needs to move into the FilterRunner class
//    public void applyFilter(List<T> filterables);

    /**
     * True or false depending on whether the {@code VariantEvaluation} passes
 the runFilter or not.
     *
     * @param filterable
     * @return true if the {@code VariantEvaluation} passes the runFilter.
     */
    public FilterResult runFilter(T filterable);
}
