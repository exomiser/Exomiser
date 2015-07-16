/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;

import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles creating the {@code de.charite.compbio.exomiser.common.SampleData}
 * from a VCF file and an optional pedigree file. This will annotate the
 * variants and produce a fully functional {@code SampleData} object for
 * analysis.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class SampleDataFactory {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataFactory.class);

    @Autowired
    private VariantFactory variantFactory;

    private final PedigreeFactory pedigreeFactory;
    private final GeneFactory geneFactory;
    
    public SampleDataFactory() {
        pedigreeFactory = new PedigreeFactory();
        geneFactory = new GeneFactory();
    }

    public SampleData createSampleData(Path vcfFilePath, Path pedigreeFilePath) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFilePath, pedigreeFilePath);
        SampleData sampleData = new SampleData(vcfFilePath, pedigreeFilePath); 

        VCFFileReader vcfReader = new VCFFileReader(vcfFilePath.toFile(), false); // false => do not require index
        setSampleVcfMetaData(vcfReader.getFileHeader(), sampleData);

        // load and annotate VCF data
        //Issue #56 This will load ALL the VCF data into memory and hold it in the sampleData
        //this requires a lot of RAM for large exomes and especially whole genomes. We could have some way of
        //applying the variant filters at this stage. Make a FilteringVariantFactory?
        List<VariantEvaluation> variantEvaluations = variantFactory.createVariantEvaluations(vcfReader);
        sampleData.setVariantEvaluations(variantEvaluations);
        
        //Don't try and create the Genes before annotating the Variants otherwise you'll have a single gene with all the variants in it...
        List<Gene> geneList = geneFactory.createGenes(variantEvaluations);
        sampleData.setGenes(geneList);

        Pedigree pedigree = pedigreeFactory.createPedigreeForSampleData(pedigreeFilePath, sampleData);
        sampleData.setPedigree(pedigree);

        return sampleData;
    }

    private void setSampleVcfMetaData(VCFHeader vcfHeader, SampleData sampleData) {
        // create sample information from header (names of samples)
        sampleData.setVcfHeader(vcfHeader);
        sampleData.setSampleNames(vcfHeader.getGenotypeSamples());
        sampleData.setNumberOfSamples(vcfHeader.getNGenotypeSamples());
    }
    
}
