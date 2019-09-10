/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.util.List;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePriorityFactoryStub implements PriorityFactory {

    @Override
    public OmimPriority makeOmimPrioritiser() {
        return new OmimPriority(TestPriorityServiceFactory.stubPriorityService());
    }

    @Override
    public PhenixPriority makePhenixPrioritiser() {
        return new PhenixPriority(true);
    }

    @Override
    public PhivePriority makePhivePrioritiser() {
        return new PhivePriority(TestPriorityServiceFactory.stubPriorityService());
    }

    @Override
    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        return new ExomeWalkerPriority(DataMatrix.empty(), entrezSeedGenes);
    }

    @Override
    public HiPhivePriority makeHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions) {
        return new HiPhivePriority(hiPhiveOptions, DataMatrix.empty(), TestPriorityServiceFactory.stubPriorityService());
    }
}
