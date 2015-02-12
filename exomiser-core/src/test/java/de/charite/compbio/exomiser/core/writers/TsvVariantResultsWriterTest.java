/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.dao.TestVariantFactory;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.Genotype;

/**
 *
 * @author Max Schubach <max.schubach@charite.de>
 */
public class TsvVariantResultsWriterTest {

    private Gene gene;
    private TsvVariantResultsWriter instance;
	private static final String VARIANT_STRING = "#CHROM	POS	REF	ALT	QUAL	FILTER	GENOTYPE	COVERAGE	FUNCTIONAL_CLASS	HGVS	EXOMISER_GENE	CADD(>0.483)	POLYPHEN(>0.956|>0.446)	MUTATIONTASTER(>0.94)	SIFT(<0.06)	DBSNP_ID	MAX_FREQUENCY	DBSNP_FREQUENCY	EVS_EA_FREQUENCY	EVS_AA_FREQUENCY	EXOMISER_VARIANT_SCORE	EXOMISER_GENE_PHENO_SCORE	EXOMISER_GENE_VARIANT_SCORE	EXOMISER_GENE_COMBINED_SCORE\n"
            + "10	123353298	C	G	2.2	PASS	0/1	0	missense_variant	FGFR2:uc021pzz.1:c.34C>G:p.Leu12Val	FGFR2	.	.	.	.	.	0.0	.	.	.	0.0	0.0	0.0	0.0";
	private SampleData sampleData;

    @Before
    public void setUp() {
        instance = new TsvVariantResultsWriter();

        TestVariantFactory varFactory = new TestVariantFactory();
        Variant variant = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);

        VariantEvaluation variantEval = new VariantEvaluation(variant);
        variantEval.addFilterResult(new PathogenicityFilterResult(VariantTypePathogenicityScores
                .getPathogenicityScoreOf(EnumSet.of(VariantEffect.STOP_GAINED)), FilterResultStatus.PASS));
        variantEval.addFilterResult(new FrequencyFilterResult(0f, FilterResultStatus.PASS));

        variantEval.setPathogenicityData(new PathogenicityData(null, null, null, null));
        variantEval.setFrequencyData(new FrequencyData(null, null, null, null, null));

        gene = new Gene(variantEval);

        sampleData = new SampleData();
        sampleData.setGenes(new ArrayList<Gene>());
    }

    @Test
    public void testWrite() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite")
                .outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT)).build();
        instance.writeFile(sampleData, settings);
        assertTrue(Paths.get("testWrite.variants.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.variants.tsv").toFile().delete());
    }

    @Test
    public void testWriteString() {
        List<Gene> geneList = new ArrayList<Gene>();
        geneList.add(gene);
        sampleData.setGenes(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(
                EnumSet.of(OutputFormat.TSV_VARIANT)).build();
        String outString = instance.writeString(sampleData, settings);
        Assert.assertEquals(VARIANT_STRING, outString);
    }

}
