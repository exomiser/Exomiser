/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.factories.VariantFactory.VcfParseException;
import jannovar.exome.Variant;
import jannovar.io.VCFReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactoryTest {
    
    private VariantFactory instance;
        
    @Before
    public void setUp() {
        instance = new VariantFactory();
    }

    @Test
    public void producesVcfReaderFromFilePath() {
        Path vcfPath = Paths.get("src/test/resources/Pfeiffer.vcf");
        VCFReader vcfReader = instance.createVcfReader(vcfPath);
        assertThat(vcfReader, notNullValue());   
    }
    
    @Test(expected = VcfParseException.class)
    public void testThrowsExceptionWithInvalidPath() {
        Path vcfPath = Paths.get("src/test/resources/wibble.vcf");
        instance.createVcfReader(vcfPath);
    }
    
    @Test(expected = VcfParseException.class)
    public void testThrowsExceptionWithInvalidFile() {
        Path vcfPath = Paths.get("src/test/resources/invalidPedTestFile.ped");
        instance.createVcfReader(vcfPath);
    }
    
    @Test
    public void producesVariantsFromVcfReader() {
        Path vcfPath = Paths.get("src/test/resources/Pfeiffer.vcf");
        VCFReader vcfReader = instance.createVcfReader(vcfPath);
        List<Variant> variants = instance.createVariants(vcfReader);
        assertThat(variants.isEmpty(), is(false));   
    }
    
}
