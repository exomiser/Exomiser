package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.SampleData;

import java.nio.file.Path;

/**
 * Stub class for testing purposes.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SampleDataFactoryStub extends SampleDataFactory {

    /**
     * Will return an empty SampleData with the VCF and PED file paths set to those supplied.
     *
     * @param vcfFilePath
     * @param pedigreeFilePath
     * @return
     */
    @Override
    public SampleData createSampleData(Path vcfFilePath, Path pedigreeFilePath) {
        return new SampleData(vcfFilePath, pedigreeFilePath);
    }

}
