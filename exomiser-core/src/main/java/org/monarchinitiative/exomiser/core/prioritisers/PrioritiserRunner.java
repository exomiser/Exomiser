/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PrioritiserRunner {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserRunner.class);

    private PrioritiserRunner(){}

    /**
     * Applies the prioritisation criteria from the prioritisers to only those
     * genes which have passed the filtering steps. If no filtering has been
     * applied this will be equivalent to simply prioritising all genes.
     *
     * @param prioritisers
     * @param genes
     * @return
     */
    public static List<Gene> prioritiseFilteredGenes(List<Prioritiser> prioritisers, List<Gene> genes) {
        List<Gene> filteredGenes = genes.stream().filter(Gene::passedFilters).collect(Collectors.toList());
        logger.info("{} of {} genes passed all filters", filteredGenes.size(), genes.size());        
        return prioritiseGenes(prioritisers, filteredGenes);
    }

    /**
     * Applies the prioritisation criteria from the prioritisers to all
     * genes irrespective of whether they have been filtered or not.
     *
     * @param prioritisers
     * @param genes
     * @return
     */
    public static List<Gene> prioritiseGenes(List<Prioritiser> prioritisers, List<Gene> genes) {
        logger.info("Running prioritisers over {} genes", genes.size());
        prioritisers.forEach(prioritiser -> run(prioritiser, genes));
        logger.info("Done prioritising genes");
        return genes;
    }

    public static void run(Prioritiser prioritiser, List<Gene> genes) {
        prioritiser.prioritizeGenes(genes);
    }
   
}
