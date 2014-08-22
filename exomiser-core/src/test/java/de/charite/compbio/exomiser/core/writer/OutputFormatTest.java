/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.writer.OutputFormat;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link de.charite.compbio.exomiser.util.OutputFormat}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutputFormatTest {
    
    public OutputFormatTest() {
    }

    @Test
    public void testValues() {
        System.out.println("values");
        OutputFormat[] expResult = {OutputFormat.HTML, OutputFormat.VCF, OutputFormat.TSV};
        OutputFormat[] result = OutputFormat.values();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testValueOf() {
        String name = "VCF";
        OutputFormat expResult = OutputFormat.VCF;
        OutputFormat result = OutputFormat.valueOf(name);
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testGetFileExtension() {
        OutputFormat instance = OutputFormat.HTML;
        String expResult = "html";
        String result = instance.getFileExtension();
        assertThat(result, equalTo(expResult));
    }
    
}
