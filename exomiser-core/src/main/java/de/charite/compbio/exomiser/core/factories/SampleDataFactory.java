/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;

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

    private final PedigreeFactory pedigreeFactory;
    private final GeneFactory geneFactory;

    public SampleDataFactory() {
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

        // open VCF file (will read header)
        VCFFileReader vcfReader = new VCFFileReader(vcfFilePath.toFile(), false); // false => do not require index

        // load and annotate VCF data
        List<VariantContext> vcfRecords = loadVariantsFromVcf(vcfReader);

        // create sample information from header (names of samples)
        SampleData sampleData = createSampleDataFromVcfMetaData(vcfReader);

        List<VariantEvaluation> variantEvaluations = createVariantEvaluations(vcfRecords);
        sampleData.setVariantEvaluations(variantEvaluations);
        sampleData.setVcfFilePath(vcfFilePath);

        return sampleData;
    }

    private List<VariantContext> loadVariantsFromVcf(VCFFileReader vcfReader) {
        logger.info("Loading variants from VCF...");
        List<VariantContext> records = new ArrayList<>();
        for (VariantContext vc : vcfReader) {
            records.add(vc);
        }
        vcfReader.close();
        return records;
    }

    private SampleData createSampleDataFromVcfMetaData(VCFFileReader vcfReader) {
        SampleData sampleData = new SampleData();
        sampleData.setVcfHeader(vcfReader.getFileHeader());
        sampleData.setSampleNames(vcfReader.getFileHeader().getGenotypeSamples());
        sampleData.setNumberOfSamples(vcfReader.getFileHeader().getNGenotypeSamples());

        return sampleData;
    }

    private List<VariantEvaluation> createVariantEvaluations(List<VariantContext> vcfRecords) {
        List<VariantEvaluation> variantEvaluations = new ArrayList<>(vcfRecords.size());

        // TODO(holtgrewe) issue #55: For now, we throw out variants on unknown references.
        logger.info("Annotating Variants...");
        // build VariantEvaluation objects from Variants
        for (VariantContext vc : vcfRecords) {
            for (Variant variant : variantAnnotator.annotateVariantContext(vc)) {
                variantEvaluations.add(new VariantEvaluation(variant));
            }
        }

        return variantEvaluations;
    }

}
