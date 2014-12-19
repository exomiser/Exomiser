/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import jannovar.exception.JannovarException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.io.VCFReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(VariantFactory.class);

    public VCFReader createVcfReader(Path vcfFilePath) {
        VCFReader vcfReader = null;
        
        try {
            vcfReader = new VCFReader(vcfFilePath.toString());
        } catch (VCFParseException ex) {
            String message = String.format("Could not create VCFReader for VCF file: '%s'", vcfFilePath);
            logger.error(message, ex);
            throw new VcfParseException(message, ex);
        }
        
        try {
            vcfReader.inputVCFheader();
        } catch (VCFParseException ex) {
            String message = String.format("Unable to parse header information from VCF file: '%s'", vcfReader.getVCFFileName());
            logger.error(message, ex);
            throw new VcfParseException(message, ex);
        }
        
        return vcfReader;    
    }

    public List<Variant> createVariants(VCFReader vcfReader) {
        List<Variant> variants = new ArrayList<>();
        logger.info("Parsing Variants from VCF");
        try {
            Iterator<Variant> variantIterator = vcfReader.getVariantIterator();
            while (variantIterator.hasNext()) {
                variants.add(variantIterator.next());
            }
        } catch (JannovarException ex) {
            String message = String.format("Error parsing Variants from VCF file '%s'", vcfReader.getVCFFileName());
            logger.error(message, ex);
            throw new VcfParseException(message, ex);
        }
        return variants;
    }
 
    
    public class VcfParseException extends RuntimeException {

        public VcfParseException(String format, Exception e) {
            super(format, e);
        }
    }
    
}
