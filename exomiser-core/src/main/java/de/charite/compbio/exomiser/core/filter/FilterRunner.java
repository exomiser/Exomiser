/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

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
public interface FilterRunner<T extends Filterable, U extends Filter> {

    /**
     * Runs the {@code de.charite.compbio.exomiser.core.model.Filterable}
     * objects through the specified List of
     * {@code de.charite.compbio.exomiser.core.model.Filter}
     *
     * @param filters
     * @param filterable
     * @return
     */
    public List<T> run(List<U> filters, List<T> filterable);
}
