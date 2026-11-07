/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.QualityFilter;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.exomiser.core.prioritisers.MockPrioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.svart.GenomicVariant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.monarchinitiative.exomiser.core.genome.GenomeAssembly.*;

class VcfAssemblyPreflightTest {

    @TempDir
    Path tempDir;

    // Lengths and accessions from the GRCh37.p13 / GRCh38.p13 reports bundled with Svart.
    @ParameterizedTest
    @CsvSource({
            "HG19, 1,            248956422, 249250621, false",
            "HG38, chr1,         249250621, 248956422, true",
            "HG19, X,            156040895, 155270560, false",
            "HG38, chrY,          59373566,  57227415, false",
            "HG19, NC_000001.10,  248956422, 249250621, false",
            "HG38, CM000663.2,    249250621, 248956422, false"
    })
    void incompatibleHeaderStopsBothRunnersBeforeAnnotationOrPrioritisation(
            GenomeAssembly assembly, String contig, int observed, int expected, boolean gzip) throws IOException {
        // A compatible first entry must not hide a later incompatible entry.
        String metadata = contig("2", assembly.getContigById(2).length()) + contig(contig, observed);
        Path vcf = writeVcf(metadata, false, gzip, true);
        Sample sample = Sample.builder().genomeAssembly(assembly).vcfPath(vcf).build();
        for (boolean passOnly : List.of(false, true)) {
            RecordingService service = new RecordingService(assembly);
            RecordingPrioritiser prioritiser = new RecordingPrioritiser();
            Analysis analysis = Analysis.builder().addStep(prioritiser).addStep(new QualityFilter(1)).build();

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> runner(service, passOnly).run(sample, analysis));

            assertAll(
                    () -> assertTrue(error.getMessage().contains(vcf.toString())),
                    () -> assertTrue(error.getMessage().contains(contig)),
                    () -> assertTrue(error.getMessage().contains(Integer.toString(observed))),
                    () -> assertTrue(error.getMessage().contains(Integer.toString(expected))),
                    () -> assertTrue(error.getMessage().contains(assembly.toString())),
                    () -> assertEquals(0, service.annotatorRequests),
                    () -> assertEquals(0, service.annotations),
                    () -> assertEquals(0, service.geneRequests),
                    () -> assertEquals(0, prioritiser.calls));
        }
    }

    static Stream<Arguments> compatibleOrInconclusiveHeaders() {
        return Stream.of(
                Arguments.of(HG19, contig("1", 249250621), false),
                Arguments.of(HG38, contig("chr1", 248956422), true),
                Arguments.of(HG19, contig("NC_000001.10", 249250621), false),
                Arguments.of(HG38, contig("CM000663.2", 248956422), false),
                Arguments.of(HG19, contig("X", 155270560) + contig("Y", 59373566), false),
                Arguments.of(HG38, contig("chrX", 156040895) + contig("chrY", 57227415), false),
                Arguments.of(HG19, "##contig=<ID=1>\n" + contig("2", 0), false),
                Arguments.of(HG38, contig("custom_decoy", 123) + contig("NC_000001.10", 249250621), false),
                Arguments.of(HG19, contig("GL000250.1", 123) + contig("chr6_apd_hap1", 123), false),
                Arguments.of(HG19, contig("chrM", 16571) + contig("MT", 16569), false),
                Arguments.of(HG38, contig("NC_012920.1", 16569) + contig("chrM", 16571), false),
                Arguments.of(HG19, "", false));
    }

    @ParameterizedTest
    @MethodSource("compatibleOrInconclusiveHeaders")
    void compatibleOrInconclusiveHeaderContinuesExistingAnalysis(
            GenomeAssembly assembly, String metadata, boolean gzip) throws IOException {
        Path vcf = writeVcf(metadata, false, gzip, true);
        Sample sample = Sample.builder().genomeAssembly(assembly).vcfPath(vcf).build();
        for (boolean passOnly : List.of(false, true)) {
            RecordingService service = new RecordingService(assembly);
            RecordingPrioritiser prioritiser = new RecordingPrioritiser();
            Analysis analysis = Analysis.builder().addStep(prioritiser).addStep(new QualityFilter(1)).build();

            AnalysisResults result = runner(service, passOnly).run(sample, analysis);

            assertEquals(List.of("synthetic"), result.sampleNames());
            assertSame(sample, result.sample());
            assertEquals(1, service.annotations);
            assertEquals(2, prioritiser.calls); // Analysis step and existing score-cache preparation.
            assertEquals(1, result.variantEvaluations().size());
            assertTrue(result.variantEvaluations().getFirst().passedFilters());
            assertEquals("RBM8A", result.genes().getFirst().geneSymbol());
        }
    }

    @Test
    void sitesOnlyHeaderIsCheckedWithoutRequiringSamples() throws IOException {
        Path vcf = writeVcf(contig("1", 248956422), true, false, false);
        RecordingService service = new RecordingService(HG19);
        RecordingPrioritiser prioritiser = new RecordingPrioritiser();
        Analysis analysis = Analysis.builder().addStep(prioritiser).build();
        assertThrows(IllegalStateException.class, () -> runner(service, false).run(
                Sample.builder().vcfPath(vcf).build(), analysis));
        assertEquals(0, service.annotatorRequests);
        assertEquals(0, prioritiser.calls);
    }

    @Test
    void matchingSitesOnlyHeaderAndUnknownAssemblyKeepExistingContract() throws IOException {
        for (GenomeAssembly assembly : List.of(HG19, UNKNOWN)) {
            Path vcf = writeVcf(contig("1", 249250621), true, false, false);
            RecordingPrioritiser prioritiser = new RecordingPrioritiser();
            AnalysisResults result = runner(new RecordingService(assembly), false).run(
                    Sample.builder().genomeAssembly(assembly).vcfPath(vcf).build(),
                    Analysis.builder().addStep(prioritiser).build());
            assertTrue(result.sampleNames().isEmpty());
            assertTrue(result.variantEvaluations().isEmpty());
            assertEquals(2, prioritiser.calls); // Analysis step and existing score-cache preparation.
        }
    }

    @Test
    void phenotypeOnlyAnalysisWithoutVcfContinues() {
        RecordingPrioritiser prioritiser = new RecordingPrioritiser();
        AnalysisResults result = runner(new RecordingService(HG19), false).run(
                Sample.builder().hpoIds(List.of("HP:0000001")).build(),
                Analysis.builder().addStep(prioritiser).build());
        assertTrue(result.sampleNames().isEmpty());
        assertTrue(result.variantEvaluations().isEmpty());
        assertEquals(2, prioritiser.calls); // Analysis step and existing score-cache preparation.
    }

    @Test
    void preflightDoesNotParseVariantRecordsForPhenotypeAnalysis() throws IOException {
        Path vcf = writeVcf(contig("1", 249250621), false, false, false);
        Files.writeString(vcf, "not a variant record\n", java.nio.file.StandardOpenOption.APPEND);
        RecordingService service = new RecordingService(HG19);
        RecordingPrioritiser prioritiser = new RecordingPrioritiser();
        runner(service, false).run(Sample.builder().vcfPath(vcf).build(),
                Analysis.builder().addStep(prioritiser).build());
        assertEquals(0, service.annotations);
        assertEquals(2, prioritiser.calls); // Analysis step and existing score-cache preparation.
    }

    @Test
    void malformedContigLengthPropagatesParserErrorBeforeAnalysis() throws IOException {
        Path vcf = writeVcf("##contig=<ID=1,length=not-a-number>\n", false, false, false);
        RecordingService service = new RecordingService(HG19);
        RecordingPrioritiser prioritiser = new RecordingPrioritiser();
        assertThrows(NumberFormatException.class, () -> runner(service, false).run(
                Sample.builder().vcfPath(vcf).build(), Analysis.builder().addStep(prioritiser).build()));
        assertEquals(0, service.annotatorRequests);
        assertEquals(0, prioritiser.calls);
    }

    private static AnalysisRunner runner(RecordingService service, boolean passOnly) {
        return passOnly ? new PassOnlyAnalysisRunner(service) : new SimpleAnalysisRunner(service);
    }

    private static String contig(String name, int length) {
        return "##contig=<ID=" + name + ",length=" + length + ">\n";
    }

    private Path writeVcf(String metadata, boolean sitesOnly, boolean gzip, boolean variant) throws IOException {
        String content = "##fileformat=VCFv4.2\n" + metadata
                + "##FORMAT=<ID=GT,Number=1,Type=String,Description=\"Genotype\">\n"
                + "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO"
                + (sitesOnly ? "\n" : "\tFORMAT\tsynthetic\n")
                + (variant ? "1\t100\t.\tA\tG\t50\tPASS\t.\tGT\t0/1\n" : "");
        Path path = tempDir.resolve(gzip ? "synthetic.vcf.gz" : "synthetic.vcf");
        if (gzip) {
            try (var output = new GZIPOutputStream(Files.newOutputStream(path))) {
                output.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            Files.writeString(path, content);
        }
        return path;
    }

    private static class RecordingPrioritiser extends MockPrioritiser {
        int calls;

        RecordingPrioritiser() {
            super(PriorityType.OMIM_PRIORITY, Map.of());
        }

        @Override
        public void prioritizeGenes(List<String> hpoIds, List<Gene> genes) {
            calls++;
            super.prioritizeGenes(hpoIds, genes);
        }
    }

    private static class RecordingService extends GenomeAnalysisServiceImpl {
        int annotatorRequests;
        int annotations;
        int geneRequests;

        RecordingService(GenomeAssembly assembly) {
            super(assembly, TestFactory.buildDefaultGenomeDataService(), TestVariantDataService.stub(), null);
        }

        @Override
        public VariantAnnotator getVariantAnnotator() {
            annotatorRequests++;
            return new VariantAnnotator() {
                @Override
                public GenomeAssembly genomeAssembly() {
                    return getGenomeAssembly();
                }

                @Override
                public List<VariantAnnotation> annotate(GenomicVariant variant) {
                    annotations++;
                    return List.of(VariantAnnotation.of("RBM8A", "9939", VariantEffect.MISSENSE_VARIANT, List.of()));
                }
            };
        }

        @Override
        public List<Gene> getKnownGenes() {
            geneRequests++;
            return super.getKnownGenes();
        }
    }
}
