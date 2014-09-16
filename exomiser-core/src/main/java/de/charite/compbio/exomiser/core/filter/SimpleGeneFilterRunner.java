/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.model.Filterable;
import de.charite.compbio.exomiser.core.model.Gene;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleGeneFilterRunner implements FilterRunner<Gene, GeneFilter> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleGeneFilterRunner.class);

    @Override
    public List<Gene> run(List<GeneFilter> filters, List<Gene> genes) {
        logger.info("Filtering {} genes using non-destructive simple filtering", genes.size());
        int numGenesWhichPassedFilters = 0;
        for (Gene gene : genes) {
            //Gene filtering needs to happen after variant filtering and only on genes which have passed the variant filtering steps 
            if (gene.passesFilters()) {
                numGenesWhichPassedFilters++;
                for (Filter filter : filters) {
                    if (!filter.filter(gene)) {
                        break;
                    }
                }
            }
        }
        logger.info("Ran {} filters over {} genes which passed filtering using non-destructive simple filtering", filters.size(), numGenesWhichPassedFilters);
        return genes;
    }

}
