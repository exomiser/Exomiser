/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactory {

    private static final Logger logger = LoggerFactory.getLogger(VariantFactory.class);

    public VCFFileReader createVcfReader(Path vcfFilePath) {
        return new VCFFileReader(vcfFilePath.toFile(), false); // false => do not require index
    }

    public List<VariantContext> createVariants(VCFFileReader vcfReader) {
        List<VariantContext> variants = new ArrayList<>();
        logger.info("Parsing records from VCF");
        for (VariantContext vc : vcfReader)
            variants.add(vc);
        return variants;
    }

}
