/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.ExomiserSettings;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NoneTypePriorityFactoryStub implements PriorityFactory {

    @Override
    public Prioritiser makePrioritiser(PriorityType priorityType, ExomiserSettings settings) {
        return new NoneTypePrioritiser();
    }

}
