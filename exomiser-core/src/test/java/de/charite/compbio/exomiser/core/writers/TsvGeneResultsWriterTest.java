/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.AnnotationList;
import de.charite.compbio.jannovar.pedigree.Genotype;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import de.charite.compbio.jannovar.reference.TranscriptModel;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import static org.hamcrest.CoreMatchers.is;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private Gene gene;
    private TsvGeneResultsWriter instance;
    private static final String HEADER = "#GENE_SYMBOL	ENTREZ_GENE_ID	"
            + "EXOMISER_GENE_PHENO_SCORE	EXOMISER_GENE_VARIANT_SCORE	EXOMISER_GENE_COMBINED_SCORE	"
            + "HUMAN_PHENO_SCORE	MOUSE_PHENO_SCORE	FISH_PHENO_SCORE	WALKER_RAW_SCORE	WALKER_SCALED_MAX_SCORE	WALKER_SCORE	"
            + "PHIVE_ALL_SPECIES_SCORE	OMIM_SCORE	MATCHES_CANDIDATE_GENE\n";
    
    private static final String GENE_STRING = "FGFR2	-10	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0\n";
    private SampleData sampleData;
    
    @Before
    public void before() {
        instance = new TsvGeneResultsWriter();
        sampleData = new SampleData();
        List<Gene> geneList = new ArrayList();
        gene = new Gene(getStubVariantEvaluation());
        geneList.add(gene);
        sampleData.setGenes(geneList);
    }
    
    private VariantEvaluation getStubVariantEvaluation() {
        GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, Integer.SIZE);
        byte chr = 1;
        
        Variant variant = new Variant(chr, 1, "A", "T", genotypeCall, 2.2f, "");

        Annotation annotation = new Annotation(TranscriptModel.createTranscriptModel(), "KIAA1751:uc001aim.1:exon18:c.T2287C:p.X763Q", VariantType.UTR3);
        annotation.setGeneSymbol("FGFR2");
        ArrayList<Annotation> annotations = new ArrayList<>();
        annotations.add(annotation);
        AnnotationList annotationList = new AnnotationList(annotations);
        annotationList.setMostPathogenicVariantType(VariantType.STOPGAIN);
        variant.setAnnotation(annotationList);

        VariantEvaluation variantEval = new VariantEvaluation(variant);
        variantEval.addFilterResult(new PathogenicityFilterResult(VariantTypePathogenicityScores.getPathogenicityScoreOf(VariantType.STOPGAIN), FilterResultStatus.PASS));
        variantEval.addFilterResult(new FrequencyFilterResult(0f, FilterResultStatus.PASS));
        
        variantEval.setPathogenicityData(new PathogenicityData(null, null, null, null)); 
        variantEval.setFrequencyData(new FrequencyData(null, null, null, null, null));
        
        return variantEval;
    }

    @Test
    public void testWrite() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
        instance.writeFile(sampleData, settings);
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().delete());
    }
    
    @Test
    public void testWriteString() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
        String outString = instance.writeString(sampleData, settings);
        assertThat(outString, equalTo(HEADER + GENE_STRING));
    }
    
    @Test
    public void testWriteStringStartsWithAHeaderLine() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
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
