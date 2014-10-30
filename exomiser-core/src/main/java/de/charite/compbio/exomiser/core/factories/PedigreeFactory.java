/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import jannovar.exception.PedParseException;
import jannovar.io.PedFileParser;
import jannovar.pedigree.Pedigree;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the creation of Pedigree objects.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PedigreeFactory {

    private static final Logger logger = LoggerFactory.getLogger(PedigreeFactory.class);

    public static final String DEFAULT_SAMPLE_NAME = "unnamed sample";

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
     * @return
     */
    public Pedigree createPedigreeForSampleData(Path pedigreeFilePath, SampleData sampleData) {
        ArrayList<String> sampleNames = createSampleNames(sampleData);
        int numberOfSamples = sampleData.getNumberOfSamples();
        logger.info("Processing pedigree for {} sample(s)", numberOfSamples);
        switch (numberOfSamples) {
            case 0:
                throw new PedigreeCreationException("No data present in sampleData");
            case 1:
                return createSingleSamplePedigree(sampleNames);
            default:
                return createMultiSamplePedigree(pedigreeFilePath, sampleNames);
        }
    }

    private ArrayList<String> createSampleNames(SampleData sampleData) {
        //yes, odd but necessary as the jannovar package explicitly demands an ArrayList
        List<String> sampleDataNames = sampleData.getSampleNames();
        ArrayList<String> sampleNames = new ArrayList(sampleDataNames);
        return sampleNames;
    }

    private Pedigree createSingleSamplePedigree(ArrayList<String> sampleNames) {
        String sampleName = DEFAULT_SAMPLE_NAME;
        if (!sampleNames.isEmpty()) {
            sampleName = sampleNames.get(0);
        }
        Pedigree pedigree = Pedigree.constructSingleSamplePedigree(sampleName);
        checkPedigreeIsNotNull(pedigree);
        logger.info("Created a single sample pedigree for {}", sampleName);
        return pedigree;
    }

    private Pedigree createMultiSamplePedigree(Path pedigreeFilePath, ArrayList<String> sampleNames) {
        logger.info("Processing pedigree file: {}", pedigreeFilePath);
        checkPedigreePathIsNotNull(pedigreeFilePath);
        Pedigree pedigree = parsePedigreeFile(pedigreeFilePath, sampleNames);
        checkPedigreeIsNotNull(pedigree);
        return pedigree;
    }

    private Pedigree parsePedigreeFile(Path pedigreeFilePath, ArrayList<String> sampleNames) throws PedigreeCreationException {
        try {
            PedFileParser parser = new PedFileParser();
            Pedigree pedigree = parser.parseFile(pedigreeFilePath.toString());
            //check that the names in the sample match those in the pedigree 
            //This function intends to check that PED file data is compatible to VCF
            //sample names. That is, are the names in the PED file identical with the
            //names in the VCF file? If there is a discrepancy, this function will
            //throw an exception. If everything is OK there, this function will
            //additional rearrange the order of the persons represented in the PED file
            //so that it is identical to the order in the VCF file. This will make
            //Pedigree analysis more efficient and the code more straightforward.
            pedigree.adjustSampleOrderInPedFile(sampleNames);

            logger.info("Created a pedigree for {}", sampleNames);
            return pedigree;
        } catch (PedParseException e) {
            throw new PedigreeCreationException(String.format("Unable to parse PED file from the path specified: %s", pedigreeFilePath), e);
        }
    }

    private void checkPedigreePathIsNotNull(Path pedigreeFilePath) {
        if (pedigreeFilePath == null) {
            logger.error("PED file must be be provided for multi-sample VCF files.");
            //terminate the program - we really need one of these.
            throw new PedigreeCreationException("Pedigree file path cannot be null.", new NullPointerException());
        }
    }

    private void checkPedigreeIsNotNull(Pedigree pedigree) throws PedigreeCreationException {
        if (pedigree == null) {
            //we really need one of these so if we can't create one, fail early and hard.
            //This will simply cause an NPE later on, so we might as well be explicit about the root cause of the problem.
            throw new PedigreeCreationException();
        }
    }

    public class PedigreeCreationException extends RuntimeException {

        public PedigreeCreationException() {
            super("Error creating Pedigree");
        }

        public PedigreeCreationException(String message) {
            super(message);
        }

        public PedigreeCreationException(String message, Exception e) {
            super(message, e);
        }
    }
}
