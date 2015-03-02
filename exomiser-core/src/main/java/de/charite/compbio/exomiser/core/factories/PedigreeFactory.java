/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.PedFileContents;
import de.charite.compbio.jannovar.pedigree.PedFileReader;
import de.charite.compbio.jannovar.pedigree.PedParseException;
import de.charite.compbio.jannovar.pedigree.PedPerson;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.PedigreeExtractor;
import de.charite.compbio.jannovar.pedigree.Person;
import de.charite.compbio.jannovar.pedigree.Sex;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

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
        return new ArrayList(sampleDataNames);
    }

    private Pedigree createSingleSamplePedigree(ArrayList<String> sampleNames) {
        String sampleName = DEFAULT_SAMPLE_NAME;
        if (!sampleNames.isEmpty()) {
            sampleName = sampleNames.get(0);
        }

        final Person person = new Person(sampleName, null, null, Sex.UNKNOWN, Disease.AFFECTED);
        return new Pedigree("family", ImmutableList.of(person));
    }

    private Pedigree createMultiSamplePedigree(Path pedigreeFilePath, ArrayList<String> sampleNames) {
        try {
            logger.info("Processing pedigree file: {}", pedigreeFilePath);
            checkPedigreePathIsNotNull(pedigreeFilePath);
            // read contents of PED file
            PedFileContents contents;
            contents = new PedFileReader(pedigreeFilePath.toFile()).read();
            // filter contents to the individuals from sampleNames
            ImmutableList.Builder<PedPerson> samplePersonsBuilder = new ImmutableList.Builder<PedPerson>();
            for (PedPerson person : contents.individuals) {
                if (sampleNames.contains(person.name)) {
                    samplePersonsBuilder.add(person);
                }
            }
            PedFileContents sampleContents = new PedFileContents(ImmutableList.<String>of(),
                    samplePersonsBuilder.build());
            final String pedName = sampleContents.individuals.get(0).pedigree;
            logger.info("Created a pedigree for {} having pedigree name ", new Object[]{sampleNames, pedName});
            return new Pedigree(pedName, new PedigreeExtractor(pedName, sampleContents).run());
        } catch (PedParseException e) {
            throw new PedigreeCreationException("Problem parsing the PED file", e);
        } catch (IOException e) {
            throw new PedigreeCreationException("Problem reading the PED file", e);
        }
    }

    private void checkPedigreePathIsNotNull(Path pedigreeFilePath) {
        if (pedigreeFilePath == null) {
            logger.error("PED file must be be provided for multi-sample VCF files.");
            //terminate the program - we really need one of these.
            throw new PedigreeCreationException("Pedigree file path cannot be null.", new NullPointerException());
        }
    }

    public class PedigreeCreationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

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
