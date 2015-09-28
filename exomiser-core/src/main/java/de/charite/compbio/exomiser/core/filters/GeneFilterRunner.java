/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.Gene;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface GeneFilterRunner extends FilterRunner<GeneFilter, Gene>{

    @Override
    public List<Gene> run(GeneFilter geneFilter, List<Gene> genes);

    @Override
    public List<Gene> run(List<GeneFilter> geneFilters, List<Gene> genes);
    
}
