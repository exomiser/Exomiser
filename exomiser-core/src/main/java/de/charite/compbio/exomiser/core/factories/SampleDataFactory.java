/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.util.VariantAnnotator;
import jannovar.exception.JannovarException;
import jannovar.exception.PedParseException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.io.PedFileParser;
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
    @Autowired
    private FrequencyDao frequencyDao;
    @Autowired
    private PathogenicityDao pathogenicityDao;

    public SampleDataFactory() {
    }

    public SampleData createSampleData(Path vcfFile, Path pedigreeFile) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFile, pedigreeFile);

        VCFReader vcfParser = createVcfParser(vcfFile);

        SampleData sampleData = parseVcfHeader(vcfParser);

        Pedigree pedigree = createPedigreeData(pedigreeFile, sampleData);
        sampleData.setPedigree(pedigree);

        List<Variant> variantList = parseVcfBody(vcfParser);

        //Now we've got all the basic bits of data sorted we'll need to fill-in the details needed for analysis
        List<VariantEvaluation> variantEvaluationList = createVariantEvaluations(variantList);
        sampleData.setVariantEvaluations(variantEvaluationList);

        //Don't try and create the Genes before annotating the Variants otherwise you'll have a single gene with all the variants in it...
        List<Gene> geneList = GeneFactory.createGeneList(sampleData.getVariantEvaluations());

        sampleData.setGeneList(geneList);

        return sampleData;
    }

    private List<VariantEvaluation> createVariantEvaluations(List<Variant> variantList) {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>(variantList.size());

        logger.info("Annotating variants, adding frequency and pathogenicity data");
        //Variants are annotated with KnownGene data from UCSC or Ensemble
        for (Variant variant : variantList) {
            VariantEvaluation variantEvaluation = new VariantEvaluation(variant);
            variantAnnotator.annotateVariant(variant);
            variantEvaluation.setFrequencyData(frequencyDao.getFrequencyData(variant));
            variantEvaluation.setPathogenicityData(pathogenicityDao.getPathogenicityData(variant));
            //should have 
            variantEvaluations.add(variantEvaluation);
        }

        return variantEvaluations;
    }

    private VCFReader createVcfParser(Path vcfFile) {
        VCFReader parser;
        try {
            parser = new VCFReader(vcfFile.toString());
        } catch (VCFParseException ve) {
            logger.error("Could not create VCFReader for VCF file {}", vcfFile, ve);
            return null;
        }
        return parser;
    }

    private List<Variant> parseVcfBody(VCFReader vcfParser) {
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
     * @param vcfParser
     * @return a List of Variants
     */
    protected SampleData parseVcfHeader(VCFReader vcfParser) {
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

    /**
     * The Exomiser can perform filtering of VCF data according to pedigree
     * data. If the VCF file contains multiple samples, then the Exomiser
     * requires a corresponding PED file to be able to analyze the inheritance
     * pattern of the variants. The PED file is parsed by the PEDFileParser
     * class in jannovar. This results in a Pedigree object that is used to do
     * the pedigree analysis. The VCF Parser from Jannovar creates Genotype
     * objects for each variant in the VCF file, and these can be compared to
     * the pedigree information. The {@link exomizer.exome.Gene Gene} class
     * coordinates this analysis.
     * <P>
     * Note that for single-sample VCF files, a Pedigree object is still
     * constructed, and we assume that the sample is from an affected person.
     *
     * @param pedigreeFilePath
     * @param sampleData
     * @param sampleNames
     * @return
     */
    protected Pedigree createPedigreeData(Path pedigreeFilePath, SampleData sampleData) {
        logger.info("Processing pedigree data: {}", pedigreeFilePath);
        Pedigree pedigree = null;
        //yes, odd but necessary as the jannovar package explicitly demands an ArrayList
        ArrayList<String> sampleNames = new ArrayList();
        sampleNames.addAll(sampleData.getSampleNames());
        logger.info("SampleData names: {}", sampleNames);
        if (sampleData.getNumberOfSamples() == 1) {
            String sample = "single sample";
            if (!sampleNames.isEmpty()) {
                sample = sampleNames.get(0);
            }
            pedigree = Pedigree.constructSingleSamplePedigree(sample);
            logger.info("Created a single sample pedigree");
        } else {
            PedFileParser parser = new PedFileParser();
            if (pedigreeFilePath == null) {
                logger.error("VCF file has {} samples but no PED file available.", sampleData.getNumberOfSamples());
                logger.error("PED file must be be provided for multi-sample VCF files.");
                //don't terminate the program - the application should decide how to let the user know that it needs the data.
            }
            try {
                pedigree = parser.parseFile(pedigreeFilePath.toString());
                consolidateVCFandPedFileSamples(pedigree, sampleNames);
                logger.info("Created a pedigree for {} people", pedigree.getPedigreeSize());
            } catch (PedParseException e) {
                logger.error("Unable to parse PED file: {}", pedigreeFilePath, e);
            }
        }
        
        if (pedigree == null) {
            //we really need one of these so if we can't create one, fail early and hard.
            //This will simply cause an NPE later on, so we might as well be explicit about the root cause of the problem.
            throw new RuntimeException(String.format("Unable to create a Pedigree object from the path specified: %s", pedigreeFilePath));
        }
        return pedigree;
    }

    /**
     * This function intends to check that PED file data is compatible to VCF
     * sample names. That is, are the names in the PED file identical with the
     * names in the VCF file? If there is a discrepancy, this function will
     * throw an exception. If everything is OK there, this function will
     * additional rearrange the order of the persons represented in the PED file
     * so that it is identical to the order in the VCF file. This will make
     * Pedigree analysis more efficient and the code more straightforward.
     *
     * @param pedigree
     * @param sampleNames
     */
    private void consolidateVCFandPedFileSamples(Pedigree pedigree, ArrayList<String> sampleNames) {

        try {
            pedigree.adjustSampleOrderInPedFile(sampleNames);
        } catch (PedParseException ppe) {
            logger.error("Error incurred while re-ordering ped file: {}", ppe);
        }

    }
}
