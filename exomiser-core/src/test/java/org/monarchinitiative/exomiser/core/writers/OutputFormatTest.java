/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.core.writers;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link org.monarchinitiative.exomiser.core.writers.OutputFormat}
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OutputFormatTest {
    
    public OutputFormatTest() {
    }

    @Test
    public void testValues() {
        System.out.println("values");
        OutputFormat[] expResult = {OutputFormat.HTML, OutputFormat.VCF, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.PHENOGRID};
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
    
    @Test
    public void testHasPhenogridJsonOutput() {
        OutputFormat instance = OutputFormat.PHENOGRID;
        String expResult = "phenogrid.json";
        String result = instance.getFileExtension();
        assertThat(result, equalTo(expResult));
    }
}
