/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ChromosomeMapFactoryTest {
    

    @Test(expected = RuntimeException.class)
    public void testDeserializeKnownGeneDataWithIncorrectPath() {
        Path incorrectPath = Paths.get("wibble");
        ChromosomeMapFactory.deserializeKnownGeneData(incorrectPath);
    }
    
}
