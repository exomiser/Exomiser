/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jblas.FloatMatrix;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePriorityFactoryStub implements PriorityFactory {

    @Override
    public Prioritiser makePrioritiser(PriorityType priorityType, PrioritiserSettings settings) {
        return new NoneTypePrioritiser();
    }

    @Override
    public OMIMPriority makeOmimPrioritiser() {
        return new OMIMPriority();
    }

    @Override
    public PhenixPriority makePhenixPrioritiser(List<String> hpoIds) {
        return new PhenixPriority(hpoIds, true);
    }

    @Override
    public PhivePriority makePhivePrioritiser(List<String> hpoIds) {
        return new PhivePriority(hpoIds);
    }

    @Override
    public ExomeWalkerPriority makeExomeWalkerPrioritiser(List<Integer> entrezSeedGenes) {
        DataMatrix stubDataMatrix = makeDataMatrixWithGeneIds(entrezSeedGenes);
        return new ExomeWalkerPriority(stubDataMatrix, entrezSeedGenes);
    }

    @Override
    public HiPhivePriority makeHiPhivePrioritiser(List<String> hpoIds, HiPhiveOptions hiPhiveOptions) {
        return new HiPhivePriority(hpoIds, hiPhiveOptions, null);
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
