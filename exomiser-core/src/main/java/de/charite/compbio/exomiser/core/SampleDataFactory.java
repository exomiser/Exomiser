/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import static de.charite.compbio.exomiser.core.SampleDataFactory.createPedigreeData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import jannovar.exception.PedParseException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.io.PedFileParser;
import jannovar.io.VCFReader;
import jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles creating the {@code de.charite.compbio.exomiser.common.SampleData}
 * from a VCF file and an optional pedigree file.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SampleDataFactory {

    private static final Logger logger = LoggerFactory.getLogger(SampleDataFactory.class);
    
    public SampleDataFactory() {
    }

    public static SampleData createSampleData(Path vcfFile, Path pedigreeFile) {
        logger.info("Creating sample data from VCF and PED files: {}, {}", vcfFile, pedigreeFile);
        
        SampleData sampleData = createSampleData(vcfFile);
        Pedigree pedigree = createPedigreeData(pedigreeFile, sampleData);
        
        sampleData.setPedigree(pedigree);

        return sampleData;
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
     * @param vcfFile
     * @return a List of Variants
     */
    protected static SampleData createSampleData(Path vcfFile) {
        logger.info("Reading VCF file: {}", vcfFile);
        
        // 1) Input the VCF file from filepath or stream
        
        /*
         * Now decide whether the user has passed a file path or a
         * BufferedReader handle (the latter is likely to happen if the Exomizer
         * is being used by a tomcat server).
         */
        VCFReader parser = null;
        try {

            parser = new VCFReader(vcfFile.toString());
            parser.parseFile();
        } catch (VCFParseException ve) {
            logger.error("Could not parse the VCF file", ve);
        }
        ArrayList<Variant> variantList = parser.getVariantList();
        //this is the List we're going to return
        //changed from ArrayList to LinkedList as this is frequently resized
        // by the filters and prioritisers removing elements
        List<VariantEvaluation> variantEvaluationList = new LinkedList<>();

        for (Variant v : variantList) {
            variantEvaluationList.add(new VariantEvaluation(v));
        }
        logger.info("Processed {} variants from VCF file", variantEvaluationList.size());

        SampleData sampleData = new SampleData();
        sampleData.setVcfHeader(parser.get_vcf_header());
        sampleData.setSampleNames(parser.getSampleNames());
        sampleData.setNumberOfSamples(parser.getNumberOfSamples());
        
//        this.status_message = parser.get_html_message();
//        this.before_NS_SS_I = parser.get_total_number_of_variants();
        
        sampleData.setVariantEvaluations(variantEvaluationList);
        
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
    protected static Pedigree createPedigreeData(Path pedigreeFilePath, SampleData sampleData) {
        logger.info("Processing pedigree data: {}", pedigreeFilePath );
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
        /**
         * The Gene class uses the pedigree for segregation analysis, and thus
         * gets a static reference to the Pedigree object.
         * 
         * This is really dangerous - what if there are more than one samples being processed?
         */
        Gene.setPedigree(pedigree);
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
    protected static void consolidateVCFandPedFileSamples(Pedigree pedigree, ArrayList<String> sampleNames) {
        
        try {
            pedigree.adjustSampleOrderInPedFile(sampleNames);
        } catch (PedParseException ppe) {
            logger.error("Error incurred while re-ordering ped file: {}", ppe);
        }
        
    }

}
