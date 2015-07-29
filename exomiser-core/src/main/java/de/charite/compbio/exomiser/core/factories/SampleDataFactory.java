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

    public SampleDataFactory(VariantFactory variantFactory) {
        this.variantFactory = variantFactory;
        pedigreeFactory = new PedigreeFactory();
        geneFactory = new GeneFactory();
    }

    /**
     * Creates a SampleData object from the given VCF and a Pedigree files. This method will eagerly read in all variants
     * from the VCF file, annotate them, create Genes from the variants and set these in the SampleData. Due to the eager
     * creation of VariantEvaluations this is a poor choice for analysing whole genomes if RAM is limited.
     *
     * @param vcfFilePath
     * @param pedigreeFilePath
     * @return A fully populated SampleData object with the VCF meta-data, annotated VariantEvaluations and Genes.
     */
    public SampleData createSampleData(Path vcfFilePath, Path pedigreeFilePath) {
        SampleData sampleData = createSampleDataWithoutVariantsOrGenes(vcfFilePath, pedigreeFilePath);

        // load and annotate VCF data
        //Issue #56 This will load ALL the VCF data into memory and hold it in the sampleData
        //this requires a lot of RAM for large exomes and especially whole genomes. We could have some way of
        //applying the variant filters at this stage. Make a FilteringVariantFactory?
        List<VariantEvaluation> variantEvaluations = variantFactory.createVariantEvaluations(vcfFilePath);
        sampleData.setVariantEvaluations(variantEvaluations);

        //Don't try and create the Genes before annotating the Variants otherwise you'll have a single gene with all the variants in it...
        List<Gene> geneList = createGenes(variantEvaluations);
        sampleData.setGenes(geneList);

        return sampleData;
    }

    /**
     * Creates a bare-bones SampleData object containing VCF metadata and Pedigree. The VariantEvaluations and Genes
     * will be empty.
     *
     * @param vcfFilePath
     * @param pedigreeFilePath
     * @return A bare-bones SampleData object without VariantEvaluations or Genes.
     */
    public SampleData createSampleDataWithoutVariantsOrGenes(Path vcfFilePath, Path pedigreeFilePath) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFilePath, pedigreeFilePath);
        SampleData sampleData = new SampleData(vcfFilePath, pedigreeFilePath);

        setSampleDataVcfHeader(sampleData, vcfFilePath);

        Pedigree pedigree = createPedigree(pedigreeFilePath, sampleData);
        sampleData.setPedigree(pedigree);

        return sampleData;
    }

    private SampleData setSampleDataVcfHeader(SampleData sampleData, Path vcfFilePath) {
        VCFFileReader vcfReader = new VCFFileReader(vcfFilePath.toFile(), false); // false => do not require index
        VCFHeader vcfHeader = vcfReader.getFileHeader();

        // create sample information from header (names of samples)
        sampleData.setVcfHeader(vcfHeader);
        sampleData.setSampleNames(vcfHeader.getGenotypeSamples());
        sampleData.setNumberOfSamples(vcfHeader.getNGenotypeSamples());

        return sampleData;
    }

    public Pedigree createPedigree(Path pedigreeFilePath, SampleData sampleData) {
        return pedigreeFactory.createPedigreeForSampleData(pedigreeFilePath, sampleData);
    }

    public List<Gene> createGenes(List<VariantEvaluation> variantEvaluations) {
        return geneFactory.createGenes(variantEvaluations);
    }

}
