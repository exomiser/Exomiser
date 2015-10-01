/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.analysis.util.InheritanceModeAnalyser;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyserTest {
    
    private InheritanceModeAnalyser instance;
    
    @Before
    public void setUp() {
        instance = new InheritanceModeAnalyser();
    }

    @Test
    public void testAnalyseInheritanceModesSingleSampleNoVariants() {
        Gene gene = new Gene("ABC", 123);
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree("Adam");
        Set<ModeOfInheritance> compatibleModes = instance.analyseInheritanceModes(gene, pedigree);
        assertThat(compatibleModes.isEmpty(), is(true));
    }
    
}
