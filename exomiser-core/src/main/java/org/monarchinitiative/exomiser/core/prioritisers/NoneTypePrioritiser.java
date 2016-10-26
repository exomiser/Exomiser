/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.List;
import java.util.stream.Stream;

/**
 * A non-functional prioritiser to be used as a default stand-in for a real one.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePrioritiser implements Prioritiser {
    
    @Override
    public void prioritizeGenes(List<Gene> geneList) {
        //Deliberately empty - this prioritiser does nothing.
    }

    @Override
    public Stream<PriorityResult> prioritise(List<Gene> genes) {
        return Stream.empty();
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.NONE;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NoneTypePrioritiser other = (NoneTypePrioritiser) obj;
        return true;
    } 

    @Override
    public String toString() {
        return "NoneTypePrioritiser{}";
    }
     
}
