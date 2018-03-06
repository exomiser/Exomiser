/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Factory class for handling creation of FilterType objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class PriorityFactoryImpl implements PriorityFactory {

    private static final Logger logger = LoggerFactory.getLogger(PriorityFactoryImpl.class);

    private final PriorityService priorityService;
    private final DataMatrix randomWalkMatrix;
    private final Path phenixDataDirectory;

    @Autowired
    public PriorityFactoryImpl(PriorityService priorityService, DataMatrix randomWalkMatrix, Path phenixDataDirectory) {
        this.priorityService = priorityService;
        this.randomWalkMatrix = randomWalkMatrix;
        this.phenixDataDirectory = phenixDataDirectory;
    }

    @Override
    public OmimPriority makeOmimPrioritiser() {
        return new OmimPriority(priorityService);
    }

    @Override
    public PhenixPriority makePhenixPrioritiser() {
        boolean symmetric = false;
        return new PhenixPriority(phenixDataDirectory.toString(), symmetric);
    }

    @Override
    public PhivePriority makePhivePrioritiser() {
        return new PhivePriority(priorityService);
    }

    @Override
    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        return new ExomeWalkerPriority(randomWalkMatrix, entrezSeedGenes);
    }

    @Override
    public HiPhivePriority makeHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
        return new HiPhivePriority(hiPhiveOptions, randomWalkMatrix, priorityService);
    }

}
