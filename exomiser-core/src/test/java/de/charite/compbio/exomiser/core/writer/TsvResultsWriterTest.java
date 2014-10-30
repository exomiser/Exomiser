/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.filter.FilterResultStatus;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.pathogenicity.VariantTypePathogenicityScores;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filter.FilterType;
import de.charite.compbio.exomiser.core.filter.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filter.PathogenicityFilterResult;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvResultsWriterTest {

    private final Gene gene;
    private final TsvResultsWriter instance;
    private static final String GENE_STRING = "FGFR2	-10	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0\n";
    private SampleData sampleData;
    
    public TsvResultsWriterTest() {
        instance = new TsvResultsWriter();
        
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
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormats(EnumSet.of(OutputFormat.TSV)).build();
        instance.writeFile(sampleData, settings, null);
        assertTrue(Paths.get("testWrite.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.tsv").toFile().delete());
    }
    
    @Test
    public void testWriteString() {
        List<Gene> geneList = new ArrayList();
        geneList.add(gene);
        sampleData.setGenes(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.TSV)).build();
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
