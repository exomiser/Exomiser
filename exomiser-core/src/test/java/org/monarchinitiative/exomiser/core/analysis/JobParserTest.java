/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.*;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.analysis.util.TestPedigrees;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.service.TestOntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.*;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class JobParserTest {

    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(TestFactory
            .buildDefaultHg19GenomeAnalysisService());
    private final PriorityService priorityService = TestPriorityServiceFactory.stubPriorityService();

    private final JobParser instance = new JobParser(genomeAnalysisServiceProvider, new NoneTypePriorityFactoryStub(), TestOntologyService
            .builder()
            .build());

    private Analysis initialiseAnalysisWithStep(AnalysisProto.AnalysisStep analysisStep) {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addSteps(analysisStep)
                .build();

        return instance.parseAnalysis(jobWith(protoAnalysis));
    }

    private JobProto.Job jobWith(SampleProto.Sample protoSample) {
        return JobProto.Job.newBuilder().setSample(protoSample).build();
    }

    private JobProto.Job jobWith(AnalysisProto.Analysis protoAnalysis) {
        return JobProto.Job.newBuilder().setAnalysis(protoAnalysis).build();
    }

    @Test
    void emptyJobThrowsException() {
        assertThrows(IllegalStateException.class, () -> instance.parseSample(JobProto.Job.newBuilder().build()));
    }

    @Test
    void testEmptyVcf() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .setVcf("")
                .build();
        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getVcfPath(), equalTo(null));
    }

    @Test
    void testVcf() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .setVcf("pfeiffer.vcf")
                .build();
        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getVcfPath(), equalTo(Paths.get("pfeiffer.vcf")));
    }

    @Test
    public void testNoGenomeAssembly() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getGenomeAssembly(), equalTo(GenomeAssembly.defaultBuild()));
    }

    @Test
    public void testUnsupportedGenomeAssembly() {
        GenomeAnalysisService hg19AnalysisService = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG19);
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(hg19AnalysisService);
        JobParser instance = new JobParser(genomeAnalysisServiceProvider, new NoneTypePriorityFactoryStub(),
                TestOntologyService.builder().build());

        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .setGenomeAssembly("GRCh38")
                .build();

        assertThrows(UnsupportedGenomeAssemblyException.class, () -> instance.parseSample(jobWith(protoSample)), "Assembly hg38 not supported in this instance. Supported assemblies are: [hg19]");
    }

    @Test
    public void testSupportedGenomeAssembly() {
        GenomeAnalysisService hg38AnalysisService = TestFactory.buildStubGenomeAnalysisService(GenomeAssembly.HG38);

        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = new GenomeAnalysisServiceProvider(hg38AnalysisService);
        JobParser instance = new JobParser(genomeAnalysisServiceProvider, new NoneTypePriorityFactoryStub(), TestOntologyService
                .builder()
                .build());

        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .setGenomeAssembly("GRCh38")
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
    }

    @Test
    void testPed() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .setProband(TestPedigrees.affectedChild().getId())
                .setPed(TestPedigrees.trioChildAffectedPedPath().toString())
                .build();
        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getPedigree(), equalTo(TestPedigrees.trioChildAffected()));
    }

    @Test
    public void testPedPathEmpty() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getPedigree(), equalTo(Pedigree.empty()));
    }

    @Test
    public void testParseAnalysisProbandSampleNameNotSpecified() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getProbandSampleName(), equalTo(""));
    }

    @Test
    public void testParseAnalysisProbandSampleNameSpecified() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .setProband("Bod")
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getProbandSampleName(), equalTo("Bod"));
    }

    @Test
    public void testParseAnalysisNoHpoIds() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getHpoIds(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testParseAnalysisHpoIdsSpecified() {
        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .addAllHpoIds(List.of("HP:0000001", "HP:0000002"))
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getHpoIds(), equalTo(List.of("HP:0000001", "HP:0000002")));
    }

    @Test
    public void testParseAnalysisHpoIdsAreNotCurrent() {
        String oldId = "HP:0000001";
        String currentId = "HP:9999999";
        TestOntologyService ontologyService = TestOntologyService.builder()
                .setObsoleteToCurrentTerms(Map.of(oldId, currentId))
                .build();
        JobParser instance = new JobParser(genomeAnalysisServiceProvider, new NoneTypePriorityFactoryStub(), ontologyService);

        SampleProto.Sample protoSample = SampleProto.Sample.newBuilder()
                .addAllHpoIds(List.of(oldId, "HP:0000002"))
                .build();

        Sample sample = instance.parseSample(jobWith(protoSample));
        assertThat(sample.getHpoIds(), equalTo(List.of(currentId, "HP:0000002")));
    }

    // test with phenopacket

    // with family

    // with old analysis


    @Test
    void sampleExtractedFormLegacyAnalysis() {
        AnalysisProto.Analysis legacyAnalysis = AnalysisProto.Analysis.newBuilder()
                .setGenomeAssembly("hg19")
                .setVcf(TestPedigrees.trioVcfPath().toString())
                .setProband(TestPedigrees.affectedChild().getId())
                .setPed(TestPedigrees.trioChildAffectedPedPath().toString())
                .addAllHpoIds(List.of("HP:0000001", "HP:0000002"))
                .build();

        Sample expected = Sample.builder()
                .genomeAssembly(GenomeAssembly.HG19)
                .vcfPath(TestPedigrees.trioVcfPath())
                .probandSampleName(TestPedigrees.affectedChild().getId())
                .pedigree(TestPedigrees.trioChildAffected())
                .hpoIds(List.of("HP:0000001", "HP:0000002"))
                .build();
        assertThat(instance.parseSample(jobWith(legacyAnalysis)), equalTo(expected));
    }

    @Test
    void testEmptyAnalysis() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.getDefaultInstance();
        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis, equalTo(Analysis.builder().build()));
    }

    // presets
    @Test
    public void testParseAnalysisNoAnalysisDefaultsToExomePreset() {
        Analysis analysis = instance.parseAnalysis(JobProto.Job.newBuilder().build());
        assertThat(analysis, equalTo(instance.exomePreset()));
    }

    @Test
    public void testParseAnalysisGenomePreset() {
        Analysis analysis = instance.parseAnalysis(JobProto.Job.newBuilder()
                .setPreset(AnalysisProto.Preset.GENOME)
                .build());
        assertThat(analysis, equalTo(instance.genomePreset()));
    }

    @Test
    void initialiseAnalysis() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .putInheritanceModes("AUTOSOMAL_RECESSIVE_COMP_HET", 2.0f)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getInheritanceModeOptions(), equalTo(InheritanceModeOptions.of(Map.of(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2f))));
    }

    @Test
    void initialiseAnalysisUnrecognisedMoi() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .putInheritanceModes("WIBBLE", 2.0f)
                .build();

        assertThrows(IllegalArgumentException.class, () -> instance.parseAnalysis(jobWith(protoAnalysis)),
                "No enum constant de.charite.compbio.jannovar.mendel.SubModeOfInheritance.WIBBLE");
    }

    @Test
    void testAnalysisModeDefault() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.PASS_ONLY));
    }

    @Test
    void testAnalysisModeFull() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .setAnalysisMode(AnalysisProto.AnalysisMode.FULL)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getAnalysisMode(), equalTo(AnalysisMode.FULL));
    }

    @Test
    void testFrequencySourceEmpty() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getFrequencySources().isEmpty(), is(true));
    }

    @Test
    void testUnrecognisedFrequencySource() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addFrequencySources("ESP")
                .build();

        assertThrows(IllegalArgumentException.class, () -> instance.parseAnalysis(jobWith(protoAnalysis)),
                "No enum constant org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.ESP");
    }

    @Test
    void testFrequencySource() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addFrequencySources("ESP_ALL")
                .addFrequencySources("GNOMAD_E_AFR")
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getFrequencySources(), equalTo(EnumSet.of(FrequencySource.ESP_ALL, FrequencySource.GNOMAD_E_AFR)));
    }

    @Test
    void testPathogenicitySourceEmpty() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getPathogenicitySources().isEmpty(), is(true));
    }

    @Test
    void testUnrecognisedPathogenicitySource() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addPathogenicitySources("ESP")
                .build();

        assertThrows(IllegalArgumentException.class, () -> instance.parseAnalysis(jobWith(protoAnalysis)),
                "No enum constant org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.ESP");
    }

    @Test
    void testPathogenicitySource() {
        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addPathogenicitySources("SIFT")
                .addPathogenicitySources("POLYPHEN")
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getPathogenicitySources(), equalTo(EnumSet.of(PathogenicitySource.SIFT, PathogenicitySource.POLYPHEN)));
    }

    @Test
    void testAddFailedVariantFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFailedVariantFilter(FiltersProto.FailedVariantFilter.newBuilder().build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new FailedVariantFilter())));
    }

    @Test
    public void testIntervalFilterEmpty() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setIntervalFilter(FiltersProto.IntervalFilter.newBuilder()
                        .build())
                .build();

        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    public void testIntervalFilterFromInterval() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setIntervalFilter(FiltersProto.IntervalFilter.newBuilder()
                        .setInterval("chr10:122892600-122892700")
                        .build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        List<IntervalFilter> expected = List.of(new IntervalFilter(new GeneticInterval(10, 122892600, 122892700)));
        assertThat(analysis.getAnalysisSteps(), equalTo(expected));
    }

    @Test
    public void testIntervalFilterFromList() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setIntervalFilter(FiltersProto.IntervalFilter.newBuilder()
                        .addIntervals("chr10:122892600-122892700")
                        .addIntervals("chr10:122892900-122893000")
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);

        List<ChromosomalRegion> expectedIntervals = List.of(
                new GeneticInterval(10, 122892600, 122892700),
                new GeneticInterval(10, 122892900, 122893000)
        );

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new IntervalFilter(expectedIntervals))));
    }

    @Test
    public void testIntervalFilterFromBedFile() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setIntervalFilter(FiltersProto.IntervalFilter.newBuilder()
                        .setBed("src/test/resources/intervals.bed")
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);

        List<ChromosomalRegion> expectedIntervals = new ArrayList<>();
        expectedIntervals.add(new GeneticInterval(7, 127471197, 127472363));
        expectedIntervals.add(new GeneticInterval(7, 127472364, 127473530));
        expectedIntervals.add(new GeneticInterval(7, 127475865, 127477031));
        expectedIntervals.add(new GeneticInterval(7, 127479366, 127480532));
        expectedIntervals.add(new GeneticInterval(7, 127480533, 127481699));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new IntervalFilter(expectedIntervals))));
    }

    @Test
    public void testGenePanelFilterEmpty() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setGenePanelFilter(FiltersProto.GenePanelFilter.newBuilder()
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    public void testGenePanelFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setGenePanelFilter(FiltersProto.GenePanelFilter.newBuilder()
                        .addGeneSymbols("FGFR1")
                        .addGeneSymbols("FGFR2")
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new GeneSymbolFilter(new LinkedHashSet<>(Arrays.asList("FGFR1", "FGFR2"))))));
    }

    @Test
    public void testGeneBlacklistFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setGeneBlacklistFilter(FiltersProto.GeneBlacklistFilter.newBuilder())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new GeneBlacklistFilter())));
    }

    @Test
    public void testVariantEffectFilterEmpty() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setVariantEffectFilter(FiltersProto.VariantEffectFilter.newBuilder()
                        .build())
                .build();
        assertThrows(IllegalStateException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    public void testVariantEffectFilterUnrecognisedValue() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setVariantEffectFilter(FiltersProto.VariantEffectFilter.newBuilder()
                        .addRemove("WIBBLE")
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    public void testVariantEffectFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setVariantEffectFilter(FiltersProto.VariantEffectFilter.newBuilder()
                        .addRemove("SYNONYMOUS_VARIANT")
                        .addRemove("UPSTREAM_GENE_VARIANT")
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new VariantEffectFilter(
                EnumSet.of(VariantEffect.SYNONYMOUS_VARIANT, VariantEffect.UPSTREAM_GENE_VARIANT))))
        );
    }

    @Test
    public void testQualityFilterUnInitalised() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setQualityFilter(FiltersProto.QualityFilter.newBuilder()
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    public void testQualityFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setQualityFilter(FiltersProto.QualityFilter.newBuilder()
                        .setMinQuality(50)
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new QualityFilter(50))));
    }

    @Test
    void testKnownVariantFilterNoFrequencySources() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setKnownVariantFilter(FiltersProto.KnownVariantFilter.newBuilder()
                        .build())
                .build();
        assertThrows(IllegalStateException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    void testKnownVariantFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setKnownVariantFilter(FiltersProto.KnownVariantFilter.newBuilder()
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addAllFrequencySources(List.of("THOUSAND_GENOMES", "TOPMED"))
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new KnownVariantFilter())));
    }

    @Test
    void testFrequencyFilterNoSourcesNoMois() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                        .build())
                .build();

        assertThrows(IllegalStateException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    void testFrequencyFilterEmptyNoSourcesWithMoi() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .putInheritanceModes("AUTOSOMAL_DOMINANT", 0.1f)
                .addSteps(analysisStep)
                .build();

        assertThrows(IllegalStateException.class, () -> instance.parseAnalysis(jobWith(protoAnalysis)));
    }

    @Test
    void testFrequencyFilterEmpty() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addAllFrequencySources(List.of("THOUSAND_GENOMES", "TOPMED"))
                .putInheritanceModes("AUTOSOMAL_DOMINANT", 0.1f)
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new FrequencyFilter(0.1f))));
    }

    @Test
    void testFrequencyFilterInheritanceModesEmpty() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                        .setMaxFrequency(2f)
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addAllFrequencySources(List.of("THOUSAND_GENOMES", "TOPMED"))
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new FrequencyFilter(2f))));
    }

    @Test
    void testFrequencyFilterOverridesInheritanceModeFrequencyLarger() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                        .setMaxFrequency(2f)
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addAllFrequencySources(List.of("THOUSAND_GENOMES", "TOPMED"))
                .putInheritanceModes("AUTOSOMAL_DOMINANT", 0.1f)
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new FrequencyFilter(2f))));
    }

    @Test
    void testFrequencyFilterOverridesInheritanceModeFrequencySmaller() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setFrequencyFilter(FiltersProto.FrequencyFilter.newBuilder()
                        .setMaxFrequency(0.01f)
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addAllFrequencySources(List.of("THOUSAND_GENOMES", "TOPMED"))
                .putInheritanceModes("AUTOSOMAL_DOMINANT", 0.1f)
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new FrequencyFilter(0.01f))));
    }

    @Test
    void testPathogenicityFilterEmptyNoSources() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder()
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addSteps(analysisStep)
                .build();

        assertThrows(IllegalStateException.class, () -> instance.parseAnalysis(jobWith(protoAnalysis)));
    }

    @Test
    void testPathogenicityFilterEmptyDefaultsToFalse() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder()
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addPathogenicitySources("POLYPHEN")
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new PathogenicityFilter(false))));
    }

    @Test
    void testPathogenicityFilterSetTrue() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder()
                        .setKeepNonPathogenic(true)
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addPathogenicitySources("POLYPHEN")
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new PathogenicityFilter(true))));
    }

    @Test
    void testPathogenicityFilterSetFalse() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPathogenicityFilter(FiltersProto.PathogenicityFilter.newBuilder()
                        .setKeepNonPathogenic(false)
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .addPathogenicitySources("POLYPHEN")
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new PathogenicityFilter(false))));
    }

    @Test
    void testInheritanceFilterNoInheritanceModeOptions() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setInheritanceFilter(FiltersProto.InheritanceFilter.newBuilder()
                        .build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of()));
    }

    @Test
    void testInheritanceFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setInheritanceFilter(FiltersProto.InheritanceFilter.newBuilder()
                        .build())
                .build();

        AnalysisProto.Analysis protoAnalysis = AnalysisProto.Analysis.newBuilder()
                .putInheritanceModes("AUTOSOMAL_DOMINANT", 0.1f)
                .addSteps(analysisStep)
                .build();

        Analysis analysis = instance.parseAnalysis(jobWith(protoAnalysis));

        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new InheritanceFilter(ModeOfInheritance.AUTOSOMAL_DOMINANT))));
    }

    @Test
    void testPriorityScoreFilterEmpty() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    void testPriorityScoreFilterEmptyScore() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .setPriorityType("HIPHIVE_PRIORITY")
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    void testPriorityScoreFilterEmptyType() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .setMinPriorityScore(0.501f)
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    void testPriorityScoreFilterUnkonwnType() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .setPriorityType("WIBBLE")
                        .setMinPriorityScore(0.501f)
                        .build())
                .build();
        assertThrows(IllegalArgumentException.class, () -> initialiseAnalysisWithStep(analysisStep));
    }

    @Test
    void testPriorityScoreFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPriorityScoreFilter(FiltersProto.PriorityScoreFilter.newBuilder()
                        .setPriorityType("HIPHIVE_PRIORITY")
                        .setMinPriorityScore(0.501f)
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501f))));
    }

    @Test
    void testRegulatoryFeatureFilter() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setRegulatoryFeatureFilter(FiltersProto.RegulatoryFeatureFilter.newBuilder()
                        .build())
                .build();
        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new RegulatoryFeatureFilter())));
    }

    @Test
    void testOmimPrioritiser() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setOmimPrioritiser(PrioritisersProto.OmimPrioritiser.newBuilder()
                        .build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new OmimPriority(priorityService))));
    }

    @Test
    void testHiPhivePrioritiser() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setHiPhivePrioritiser(PrioritisersProto.HiPhivePrioritiser.newBuilder()
                        .build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new HiPhivePriority(HiPhiveOptions.defaults(), DataMatrix
                .empty(), priorityService))));
    }

    @Test
    void testPhivePrioritiser() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPhivePrioritiser(PrioritisersProto.PhivePrioritiser.newBuilder()
                        .build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps(), equalTo(List.of(new PhivePriority(priorityService))));
    }

    @Test
    void testPhenixPrioritiser() {
        AnalysisProto.AnalysisStep analysisStep = AnalysisProto.AnalysisStep.newBuilder()
                .setPhenixPrioritiser(PrioritisersProto.PhenixPrioritiser.newBuilder()
                        .build())
                .build();

        Analysis analysis = initialiseAnalysisWithStep(analysisStep);
        assertThat(analysis.getAnalysisSteps().get(0) instanceof PhenixPriority, is(true));
    }
}