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

import org.jblas.FloatMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePriorityFactoryStub implements PriorityFactory {

    @Override
    public Prioritiser makePrioritiser(PrioritiserSettings settings) {
        return new NoneTypePrioritiser();
    }

    @Override
    public OMIMPriority makeOmimPrioritiser() {
        return new OMIMPriority(TestPriorityServiceFactory.STUB_SERVICE);
    }

    @Override
    public PhenixPriority makePhenixPrioritiser(List<String> hpoIds) {
        return new PhenixPriority(hpoIds, true);
    }

    @Override
    public PhivePriority makePhivePrioritiser(List<String> hpoIds) {
        return new PhivePriority(hpoIds, TestPriorityServiceFactory.STUB_SERVICE);
    }

    @Override
    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        DataMatrix stubDataMatrix = makeDataMatrixWithGeneIds(entrezSeedGenes);
        return new ExomeWalkerPriority(stubDataMatrix, entrezSeedGenes);
    }

    @Override
    public HiPhivePriority makeHiPhivePrioritiser(List<String> hpoIds, HiPhiveOptions hiPhiveOptions) {
        return new HiPhivePriority(hpoIds, hiPhiveOptions, null, TestPriorityServiceFactory.STUB_SERVICE);
    }

    private DataMatrix makeDataMatrixWithGeneIds(List<Integer> entrezSeedGenes) {
        Map<Integer, Integer> matrixMap = new LinkedHashMap<>();
        
        for (int i = 0; i < entrezSeedGenes.size(); i++) {
            Integer geneId = entrezSeedGenes.get(i);
            matrixMap.put(geneId, i);
        }
                    
        DataMatrix dataMatrix = new DataMatrix(FloatMatrix.zeros(entrezSeedGenes.size(), entrezSeedGenes.size()), matrixMap);
        
        return dataMatrix;
    }

}
