/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.model;

import java.util.Arrays;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OrganismTest {
    
    private Organism instance;
    
    private final Organism[] values = {Organism.HUMAN, Organism.MOUSE, Organism.FISH};
    
    @Test
    public void testValues() {
        assertThat(Organism.values(), equalTo(values));
    }

    @Test
    public void testValueOf() {
        assertThat(Organism.valueOf("HUMAN"), equalTo(Organism.HUMAN));
    }
    
    @Test
    public void testGetHumanIdAndSpeciesNames() {
        Organism organism = Organism.HUMAN;
        assertThat(organism.getNcbiId(), equalTo("9606"));
        assertThat(organism.getSpeciesName(), equalTo("Homo sapiens"));
    }
    
    @Test
    public void testGetMouseIdAndSpeciesNames() {
        Organism organism = Organism.MOUSE;
        assertThat(organism.getNcbiId(), equalTo("10090"));
        assertThat(organism.getSpeciesName(), equalTo("Mus musculus"));
    }
    
    @Test
    public void testGetFishIdAndSpeciesNames() {
        Organism organism = Organism.FISH;
        assertThat(organism.getNcbiId(), equalTo("7955"));
        assertThat(organism.getSpeciesName(), equalTo("Danio rerio"));
    }
    
    
}
