/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import jannovar.annotation.Annotation;
import jannovar.annotation.AnnotationList;
import jannovar.common.Genotype;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import jannovar.reference.TranscriptModel;

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

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private final Gene gene;
    private final TsvGeneResultsWriter instance;
    private static final String GENE_STRING = "FGFR2	-10	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0\n";
    private SampleData sampleData;
    
    public TsvGeneResultsWriterTest() {
        instance = new TsvGeneResultsWriter();
        
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
        
        gene = new Gene(variantEval);
    }

    @Before
    public void before() {
        sampleData = new SampleData();
        sampleData.setGenes(new ArrayList<Gene>());
    }
    
    @Test
    public void testWrite() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
        instance.writeFile(sampleData, settings, null);
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().delete());
    }
    
    @Test
    public void testWriteString() {
        List<Gene> geneList = new ArrayList();
        geneList.add(gene);
        sampleData.setGenes(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.TSV_GENE)).build();
        String outString = instance.writeString(sampleData, settings, null);
        assertThat(outString, equalTo(GENE_STRING));
    }

    @Test
    public void testMakeGeneLine() {
        String candidateGene = "";
        String result = instance.makeGeneLine(gene, candidateGene);
        assertThat(result, equalTo(GENE_STRING));
    }

}
