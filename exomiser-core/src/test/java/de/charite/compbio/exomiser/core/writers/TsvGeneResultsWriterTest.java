/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.Genotype;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Variant;
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

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private Gene gene;
    private TsvGeneResultsWriter instance;
    private static final String HEADER = "#GENE_SYMBOL	ENTREZ_GENE_ID	"
            + "EXOMISER_GENE_PHENO_SCORE	EXOMISER_GENE_VARIANT_SCORE	EXOMISER_GENE_COMBINED_SCORE	"
            + "HUMAN_PHENO_SCORE	MOUSE_PHENO_SCORE	FISH_PHENO_SCORE	WALKER_SCORE	"
            + "PHIVE_ALL_SPECIES_SCORE	OMIM_SCORE	MATCHES_CANDIDATE_GENE	HUMAN_PHENO_EVIDENCE	MOUSE_PHENO_EVIDENCE	FISH_PHENO_EVIDENCE	HUMAN_PPI_EVIDENCE	MOUSE_PPI_EVIDENCE	FISH_PPI_EVIDENCE\n";
    
    private static final String GENE_STRING = "FGFR2	2263	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0	\n";
    private SampleData sampleData;
    
    @Before
    public void setUp() {
        instance = new TsvGeneResultsWriter();

        TestVariantFactory varFactory = new TestVariantFactory();

        VariantEvaluation variantEval = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        variantEval.addFilterResult(new PathogenicityFilterResult(VariantTypePathogenicityScores
                .getPathogenicityScoreOf(EnumSet.of(VariantEffect.STOP_GAINED)), FilterResultStatus.PASS));
        variantEval.addFilterResult(new FrequencyFilterResult(0f, FilterResultStatus.PASS));

        variantEval.setPathogenicityData(new PathogenicityData(null, null, null, null));
        variantEval.setFrequencyData(new FrequencyData(null));

        gene = new Gene(variantEval.getGeneSymbol(), variantEval.getEntrezGeneId());
        gene.addVariant(variantEval);
        
        sampleData = new SampleData();
        sampleData.setGenes(Arrays.asList(gene));
    }

    @Test
    public void testWrite() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputPrefix("testWrite")
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
        instance.writeFile(sampleData, settings);
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().delete());
    }

    @Test
    public void testWriteString() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(
                EnumSet.of(OutputFormat.TSV_GENE)).build();
        String outString = instance.writeString(sampleData, settings);
        Assert.assertEquals(HEADER + GENE_STRING, outString);
        // assertThat(outString, equalTo(HEADER + GENE_STRING));
    }

    @Test
    public void testWriteStringStartsWithAHeaderLine() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(
                EnumSet.of(OutputFormat.TSV_GENE)).build();
        String outString = instance.writeString(sampleData, settings);
        String[] lines = outString.split("\n");
        assertThat(lines[0] + "\n", equalTo(HEADER));
    }
    
    @Test
    public void testMakeGeneLine() {
        String candidateGene = "";
        String result = instance.makeGeneLine(gene, candidateGene);
        assertThat(result, equalTo(GENE_STRING));
    }

}
