/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.filter.FrequencyVariantScore;
import de.charite.compbio.exomiser.filter.PathogenicityVariantScore;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.util.OutputFormat;
import jannovar.annotation.Annotation;
import jannovar.annotation.AnnotationList;
import jannovar.common.Genotype;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import jannovar.reference.TranscriptModel;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static final String GENE_STRING = "FGFR2	-10	-10.0000	-10.0000	-10.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	1.0000	1.0000	1.0000	1.0000	1.0000	1.0000	STOPGAIN	0\n";
    private SampleData sampleData;
    
    public TsvResultsWriterTest() {
        instance = new TsvResultsWriter();
        
        GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, Integer.SIZE);
        byte chr = 1;
        
        Variant variant = new Variant(chr, 1, "A", "T", genotypeCall, 2.2f);

        Annotation annotation = new Annotation(TranscriptModel.createTranscriptModel(), "KIAA1751:uc001aim.1:exon18:c.T2287C:p.X763Q", VariantType.UTR3);
        annotation.setGeneSymbol("FGFR2");
        ArrayList<Annotation> annotations = new ArrayList<>();
        annotations.add(annotation);
        AnnotationList annotationList = new AnnotationList(annotations);
        annotationList.setMostPathogenicVariantType(VariantType.STOPGAIN);
        variant.setAnnotation(annotationList);

        VariantEvaluation variantEval = new VariantEvaluation(variant);
        variantEval.addFilterTriage(new PathogenicityVariantScore(chr, chr, chr, chr), FilterType.PATHOGENICITY_FILTER);
        variantEval.addFilterTriage(new FrequencyVariantScore(chr, chr, chr, chr, chr), FilterType.FREQUENCY_FILTER);
        
        gene = new Gene(variantEval);
    }

    @Before
    public void before() {
        sampleData = new SampleData();
        sampleData.setGeneList(new ArrayList<Gene>());
    }
    
    @Test
    public void testWrite() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormat(OutputFormat.TSV).build();
        instance.writeFile(sampleData, settings, null, null);
        assertTrue(Paths.get("testWrite.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.tsv").toFile().delete());
    }
    
    @Test
    public void testWriteString() {
        List<Gene> geneList = new ArrayList();
        geneList.add(gene);
        sampleData.setGeneList(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormat(OutputFormat.TSV).build();
        String outString = instance.writeString(sampleData, settings, null, null);
        assertThat(outString, equalTo(GENE_STRING));
    }

    @Test
    public void testMakeGeneLine() {
        String candidateGene = "";
        String result = instance.makeGeneLine(gene, candidateGene);
        assertThat(result, equalTo(GENE_STRING));
    }

}
