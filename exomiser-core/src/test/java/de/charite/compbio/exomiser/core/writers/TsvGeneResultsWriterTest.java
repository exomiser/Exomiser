/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private Gene gene;
    private static final String GENE_SYMBOL = "FGFR2";
    private static final int GENE_ID = 2263;
    private TsvGeneResultsWriter instance;
    private static final String HEADER = "#GENE_SYMBOL	ENTREZ_GENE_ID	"
            + "EXOMISER_GENE_PHENO_SCORE	EXOMISER_GENE_VARIANT_SCORE	EXOMISER_GENE_COMBINED_SCORE	"
            + "HUMAN_PHENO_SCORE	MOUSE_PHENO_SCORE	FISH_PHENO_SCORE	WALKER_SCORE	"
            + "PHIVE_ALL_SPECIES_SCORE	OMIM_SCORE	MATCHES_CANDIDATE_GENE	HUMAN_PHENO_EVIDENCE	MOUSE_PHENO_EVIDENCE	FISH_PHENO_EVIDENCE	HUMAN_PPI_EVIDENCE	MOUSE_PPI_EVIDENCE	FISH_PPI_EVIDENCE\n";
    
    private static final String GENE_STRING = "FGFR2	2263	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0	\n";
    
    private SampleData sampleData;
    private Analysis analysis;
    
    @Before
    public void setUp() {
        instance = new TsvGeneResultsWriter();

        gene = new Gene(GENE_SYMBOL, GENE_ID);        
        sampleData = new SampleData();
        sampleData.setGenes(Arrays.asList(gene));
        analysis = new Analysis();
        analysis.setSampleData(sampleData);
    }

    @Test
    public void testWrite() {
        OutputSettings settings = new OutputSettingsBuilder().outputPrefix("testWrite")
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
        instance.writeFile(analysis, settings);
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().delete());
    }

    @Test
    public void testWriteString() {
        OutputSettings settings = new OutputSettingsBuilder().outputFormats(
                EnumSet.of(OutputFormat.TSV_GENE)).build();
        String outString = instance.writeString(analysis, settings);
        assertThat(outString, equalTo(HEADER + GENE_STRING));
    }

    @Test
    public void testWriteStringStartsWithAHeaderLine() {
        OutputSettings settings = new OutputSettingsBuilder().outputFormats(
                EnumSet.of(OutputFormat.TSV_GENE)).build();
        String outString = instance.writeString(analysis, settings);
        String[] lines = outString.split("\n");
        assertThat(lines[0] + "\n", equalTo(HEADER));
    }
    
    @Test
    public void testMakeGeneLine() {
        String result = instance.makeGeneLine(gene);
        assertThat(result, equalTo(GENE_STRING));
    }

}
