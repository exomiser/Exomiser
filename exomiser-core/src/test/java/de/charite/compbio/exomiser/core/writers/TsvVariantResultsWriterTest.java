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
 * @author Max Schubach <max.schubach@charite.de>
 */
public class TsvVariantResultsWriterTest {

	private final Gene gene;
	private final TsvVariantResultsWriter instance;
	private static final String VARIANT_STRING = "#CHROM	POS	REF	ALT	QUAL	FILTER	GENOTYPE	COVERAGE	FUNCTIONAL_CLASS	HGVS	EXOMISER_GENE	CADD(>0.483)	POLYPHEN(>0.956|>0.446)	MUTATIONTASTER(>0.94)	SIFT(<0.06)	DBSNP_ID	MAX_FREQUENCY	DBSNP_FREQUENCY	EVS_EA_FREQUENCY	EVS_AA_FREQUENCY	EXOMISER_VARIANT_SCORE	EXOMISER_GENE_PHENO_SCORE	EXOMISER_GENE_VARIANT_SCORE	EXOMISER_GENE_COMBINED_SCORE\n"
			+ "chr1	1	A	T	2.2	PASS	0/1	0	STOPGAIN	FGFR2:KIAA1751:uc001aim.1:exon18:c.T2287C:p.X763Q	FGFR2	.	.	.	.	.	0.0	.	.	.	0.0	0.0	0.0	0.0";
	private SampleData sampleData;

	public TsvVariantResultsWriterTest() {
		instance = new TsvVariantResultsWriter();

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
		ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT)).build();
		instance.writeFile(sampleData, settings);
		assertTrue(Paths.get("testWrite.variants.tsv").toFile().exists());
		assertTrue(Paths.get("testWrite.variants.tsv").toFile().delete());
	}

	@Test
	public void testWriteString() {
		List<Gene> geneList = new ArrayList<Gene>();
		geneList.add(gene);
		sampleData.setGenes(geneList);
		ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT)).build();
		String outString = instance.writeString(sampleData, settings);
		assertThat(outString, equalTo(VARIANT_STRING));
	}

}
