/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.exome.Variant;
import jannovar.io.VCFReader;
import jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private VariantAnnotator variantAnnotator;

    private final VariantFactory variantFactory;
    private final PedigreeFactory pedigreeFactory;
    private final GeneFactory geneFactory;
    
    public SampleDataFactory() {
        variantFactory = new VariantFactory();
        pedigreeFactory = new PedigreeFactory();
        geneFactory = new GeneFactory();
    }

    public SampleData createSampleData(Path vcfFilePath, Path pedigreeFilePath) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFilePath, pedigreeFilePath);
        
        SampleData sampleData = createSampleDataFromVcf(vcfFilePath);
        
        //Don't try and create the Genes before annotating the Variants otherwise you'll have a single gene with all the variants in it...
        List<Gene> geneList = geneFactory.createGenes(sampleData.getVariantEvaluations());
        sampleData.setGenes(geneList);

        Pedigree pedigree = pedigreeFactory.createPedigreeForSampleData(pedigreeFilePath, sampleData);
        sampleData.setPedigree(pedigree);

        return sampleData;
    }

    private SampleData createSampleDataFromVcf(Path vcfFilePath) {
        logger.info("Creating SampleData from VCF");
        
        VCFReader vcfReader = variantFactory.createVcfReader(vcfFilePath);
        List<Variant> variantList = variantFactory.createVariants(vcfReader);

        SampleData sampleData = createSampleDataFromVcfMetaData(vcfReader);
        List<VariantEvaluation> variantEvaluations = createVariantEvaluations(variantList);
        sampleData.setVariantEvaluations(variantEvaluations);
        sampleData.setVcfFilePath(vcfFilePath);
        
        return sampleData;
    }

    private SampleData createSampleDataFromVcfMetaData(VCFReader vcfReader) {
        SampleData sampleData = new SampleData();
        sampleData.setVcfHeader(vcfReader.get_vcf_header());
        sampleData.setSampleNames(vcfReader.getSampleNames());
        sampleData.setNumberOfSamples(vcfReader.getNumberOfSamples());

        return sampleData;
    }

    private List<VariantEvaluation> createVariantEvaluations(List<Variant> variantList) {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>(variantList.size());

        logger.info("Creating sample VariantEvaluations");
        //Variants are annotated with KnownGene data from UCSC or Ensemble
        for (Variant variant : variantList) {
            VariantEvaluation variantEvaluation = new VariantEvaluation(variant);
            variantAnnotator.annotateVariant(variant);
            variantEvaluations.add(variantEvaluation);
        }

        return variantEvaluations;
    }

}
