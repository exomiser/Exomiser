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
    
    public SampleDataFactory() {
        pedigreeFactory = new PedigreeFactory();
    }

    public SampleData createSampleData(Path vcfFile, Path pedigreeFile) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFile, pedigreeFile);
        
        SampleData sampleData = parseSampleDataFromVcfFile(vcfFile);

        Pedigree pedigree = pedigreeFactory.createPedigreeForSampleData(pedigreeFile, sampleData);
        sampleData.setPedigree(pedigree);

        return sampleData;
    }

    private SampleData parseSampleDataFromVcfFile(Path vcfFile) {
        VCFReader vcfParser = createVcfParser(vcfFile);
        SampleData sampleData = createSampleDataFromVcf(vcfParser);
        List<Variant> variantList = createVariants(vcfParser);
        //Now we've got all the basic bits of data sorted we'll need to fill-in the details needed for analysis
        List<VariantEvaluation> variantEvaluationList = createVariantEvaluations(variantList);
        sampleData.setVariantEvaluations(variantEvaluationList);
        
        //Don't try and create the Genes before annotating the Variants otherwise you'll have a single gene with all the variants in it...
        List<Gene> geneList = GeneFactory.createGeneList(sampleData.getVariantEvaluations());
        sampleData.setGenes(geneList);
        
        return sampleData;
    }

        
    private VCFReader createVcfParser(Path vcfFile) {
        VCFReader parser = null;
        try {
            parser = new VCFReader(vcfFile.toString());
        } catch (VCFParseException ve) {
            logger.error("Could not create VCFReader for VCF file {}", vcfFile, ve);
        }
        return parser;
    }
    
    /**
     * Input the VCF file using the VCFReader class. The method will initialize
     * the snv_list, which contains one item for each variant in the VCF file,
     * as well as the header, which contains a list of the header lines of the
     * VCF file that will be used for printing the output filtered VCF. Note
     * that the {@code VCFReader} class is from the jannovar library.
     * <P>
     * The {@code Variant} class is also from the Jannovar library, and it
     * contains all of the relevant information about variants that can be
     * obtained from the VCF file. The exomizer package has a class called
     * {@link exomizer.exome.VariantEvaluation VariantEvaluation}, which is used
     * to capture all of the evaluations (pathogenicity, frequency) etc., that
     * are not represented in the VCF file itself.
     *
     */
    private SampleData createSampleDataFromVcf(VCFReader vcfParser) {
        logger.info("Creating SampleData from VCF");

        try {
            vcfParser.inputVCFheader();
        } catch (VCFParseException ex) {
            logger.error("Unable to parser header information from VCF file {}", vcfParser.getVCFFileName(), ex);
        }

        SampleData sampleData = new SampleData();
        sampleData.setVcfHeader(vcfParser.get_vcf_header());
        sampleData.setSampleNames(vcfParser.getSampleNames());
        sampleData.setNumberOfSamples(vcfParser.getNumberOfSamples());

        return sampleData;
    }

    private List<Variant> createVariants(VCFReader vcfParser) {
        List<Variant> variantList = new ArrayList<>();
        logger.info("Parsing Variants from VCF");
        try {

            Iterator<Variant> variantIterator = vcfParser.getVariantIterator();
            while (variantIterator.hasNext()) {
                variantList.add(variantIterator.next());

            }
        } catch (JannovarException ex) {
            logger.error("Error parsing Variants from VCF file {}", vcfParser.getVCFFileName(), ex);
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

}
