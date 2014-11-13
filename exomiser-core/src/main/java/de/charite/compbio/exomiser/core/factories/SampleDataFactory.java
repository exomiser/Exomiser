/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.util.VariantAnnotator;
import jannovar.exception.JannovarException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.io.VCFReader;
import jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
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

    private final PedigreeFactory pedigreeFactory;
    private final GeneFactory geneFactory;
    
    public SampleDataFactory() {
        pedigreeFactory = new PedigreeFactory();
        geneFactory = new GeneFactory();
    }

    public SampleData createSampleData(Path vcfFilePath, Path pedigreeFilePath) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFilePath, pedigreeFilePath);
        
        SampleData sampleData = parseSampleDataFromVcfFile(vcfFilePath);
        sampleData.setVcfFilePath(vcfFilePath);
        //Don't try and create the Genes before annotating the Variants otherwise you'll have a single gene with all the variants in it...
        List<Gene> geneList = geneFactory.createGenes(sampleData.getVariantEvaluations());
        sampleData.setGenes(geneList);

        Pedigree pedigree = pedigreeFactory.createPedigreeForSampleData(pedigreeFilePath, sampleData);
        sampleData.setPedigree(pedigree);

        return sampleData;
    }

    private SampleData parseSampleDataFromVcfFile(Path vcfFile) {
        VCFReader vcfReader = createVcfParser(vcfFile);
        SampleData sampleData = createSampleDataFromVcf(vcfReader);
        
        List<Variant> variantList = createVariants(vcfReader);
        //Now we've got all the basic bits of data sorted we'll need to fill-in the details needed for analysis
        List<VariantEvaluation> variantEvaluations = createVariantEvaluations(variantList);
        sampleData.setVariantEvaluations(variantEvaluations);
                
        return sampleData;
    }
       
    private VCFReader createVcfParser(Path vcfFile) {
        VCFReader vcfReader = null;
        try {
            vcfReader = new VCFReader(vcfFile.toString());
        } catch (VCFParseException ex) {
            String message = String.format("Could not create VCFReader for VCF file: '%s'", vcfFile);
            logger.error(message, ex);
            throw new SampleDataCreationException(message, ex);
        }
        return vcfReader;
    }
    
    private SampleData createSampleDataFromVcf(VCFReader vcfReader) {
        logger.info("Creating SampleData from VCF");

        try {
            vcfReader.inputVCFheader();
        } catch (VCFParseException ex) {
            String message = String.format("Unable to parse header information from VCF file: '%s'", vcfReader.getVCFFileName());
            logger.error(message, ex);
            throw new SampleDataCreationException(message, ex);
        }

        SampleData sampleData = new SampleData();
        sampleData.setVcfHeader(vcfReader.get_vcf_header());
        sampleData.setSampleNames(vcfReader.getSampleNames());
        sampleData.setNumberOfSamples(vcfReader.getNumberOfSamples());

        return sampleData;
    }

    private List<Variant> createVariants(VCFReader vcfReader) {
        List<Variant> variantList = new ArrayList<>();
        logger.info("Parsing Variants from VCF");
        try {

            Iterator<Variant> variantIterator = vcfReader.getVariantIterator();
            while (variantIterator.hasNext()) {
                variantList.add(variantIterator.next());

            }
        } catch (JannovarException ex) {
            String message = String.format("Error parsing Variants from VCF file '%s'", vcfReader.getVCFFileName());
            logger.error(message, ex);
            throw new SampleDataCreationException(message, ex);
        }

        return variantList;
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

    public class SampleDataCreationException extends RuntimeException {

        public SampleDataCreationException(String format, Exception e) {
            super(format, e);
        }
    }
}
