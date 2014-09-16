/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.Gene;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface GeneFilter extends Filter<Gene> {

    /**
     * Take a list of genes and apply the filter against. If a gene does not
     * pass the filter it is marked as having failed that filter.
     *
     * @param genes to be filtered
     */
    @Override
    public void filter(List<Gene> genes);

    /**
     * True or false depending on whether the {@code Gene} passes the filter or
     * not.
     *
     * @param gene to be filtered
     * @return true if the {@code Gene} passes the filter.
     */
    @Override
    public boolean filter(Gene gene);

}
