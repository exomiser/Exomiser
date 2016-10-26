/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface GeneFilterRunner extends FilterRunner<GeneFilter, Gene>{

    @Override
    List<Gene> run(GeneFilter geneFilter, List<Gene> genes);

    @Override
    List<Gene> run(List<GeneFilter> geneFilters, List<Gene> genes);
    
}
