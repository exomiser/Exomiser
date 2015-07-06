/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Filterable;
import java.util.List;

/**
 * Generic interface for classes which handle running
 * {@code de.charite.compbio.exomiser.core.model.Filter} over
 * {@code de.charite.compbio.exomiser.core.model.Filterable} objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @param <T>
 * @param <U>
 */
public interface FilterRunner<T extends Filter, U extends Filterable> {

    /**
     * Runs the {@code de.charite.compbio.exomiser.core.model.Filterable}
     * objects through the specified List of
     * {@code de.charite.compbio.exomiser.core.model.Filter}
     *
     * @param filters
     * @param filterables
     * @return
     */
    public List<U> run(List<T> filters, List<U> filterables);

    public List<U> run(T filter, List<U> filterables);

}
