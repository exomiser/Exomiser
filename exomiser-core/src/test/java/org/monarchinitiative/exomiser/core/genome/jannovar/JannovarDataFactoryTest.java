/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.GeneFactory;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.JannovarVariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.VariantAnnotator;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;
import org.monarchinitiative.svart.CoordinateSystem;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.Strand;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
//@ExtendWith(TempDirectory.class)
class JannovarDataFactoryTest {

    private final Path iniFile = Paths.get("src/test/resources/jannovar/default_sources.ini");

    @Test
    void testBuilder() {
        JannovarDataFactory.builder(iniFile).build();
    }

    @Test
    void testWrite() {
        JannovarDataFactory instance = JannovarDataFactory.builder(iniFile).build();

        assertThrows(NullPointerException.class, () -> instance.buildAndWrite(null, TranscriptSource.UCSC, Paths.get("")));
        assertThrows(NullPointerException.class, () -> instance.buildAndWrite(GenomeAssembly.HG19, null, Paths.get("")));
        assertThrows(NullPointerException.class, () -> instance.buildAndWrite(GenomeAssembly.HG19, TranscriptSource.UCSC, null));
    }

    @Disabled()
    @Test
    void testBuildData(@TempDir Path tempDir) {

        Path jannovarOutputDir = Paths.get("/home/hhx640/Documents/Jannovar/data");
        JannovarDataFactory instance = JannovarDataFactory.builder(iniFile).downloadDir(jannovarOutputDir).build();

        TranscriptSource[] transcriptSources = {TranscriptSource.ENSEMBL};
        Arrays.stream(transcriptSources).forEach(transcriptSource -> {
            String buildString = "1909";
            GenomeAssembly genomeAssembly = GenomeAssembly.HG19;
            String outputName = String.format("%s_%s_transcripts_%s.ser", buildString, genomeAssembly, transcriptSource);
            JannovarData jannovarData = instance.buildData(genomeAssembly, transcriptSource);
            JannovarDataProtoSerialiser.save(jannovarOutputDir.resolve(outputName), jannovarData);

            JannovarData roundTripped = JannovarDataSourceLoader.loadJannovarData(jannovarOutputDir.resolve(outputName));
            JannovarData preOrdered = JannovarDataSourceLoader.loadJannovarData(Path.of("/home/hhx640/Documents/Jannovar/data/old")
                    .resolve(outputName));

            ImmutableMap<String, TranscriptModel> preOrderedTmByAccession = preOrdered.getTmByAccession();
            ImmutableMap<String, TranscriptModel> roundTrippedTmByAccession = roundTripped.getTmByAccession();
            for (Map.Entry<String, TranscriptModel> entry : preOrderedTmByAccession.entrySet()) {
                assertThat(roundTrippedTmByAccession.get(entry.getKey()), equalTo(entry.getValue()));
            }

            roundTripped.getChromosomes()
                    .values()
                    .forEach(chromosome -> System.out.printf("Chrom: %d %s num genes: %d%n", chromosome.getChrID(), chromosome
                            .getChromosomeName(), chromosome.getNumberOfGenes()));

            VariantAnnotator variantAnnotator = new JannovarVariantAnnotator(genomeAssembly, roundTripped, ChromosomalRegionIndex
                    .empty());

            GenomicVariant variant = GenomicVariant.of(GenomeAssembly.HG19.getContigByName("19"), "", Strand.POSITIVE, CoordinateSystem.ONE_BASED, 36227863, "C", "T");
            List<VariantAnnotation> variantAnnotations = variantAnnotator.annotate(variant);
            System.out.println(variantAnnotations);
            assertThat(variantAnnotations.size(), equalTo(1));
            VariantAnnotation variantAnnotation = variantAnnotations.get(0);

            GeneFactory geneFactory = new GeneFactory(roundTripped);
            List<Gene> knownGenes = geneFactory.createKnownGenes();

            for (Gene gene : knownGenes) {
                if (gene.getGeneSymbol().equals(variantAnnotation.getGeneSymbol())) {
                    System.out.println(gene.getGeneIdentifier());
                }
            }
        });
    }

    @Disabled
    @Test
    void testWriteFile(@TempDir Path tempDir) {

        JannovarDataFactory instance = JannovarDataFactory.builder(iniFile).build();

        Path outPath = tempDir.resolve("transcripts_hg19_ucsc.ser");
        instance.buildAndWrite(GenomeAssembly.HG19, TranscriptSource.UCSC, outPath);

        assertThat(outPath.toFile().exists(), is(true));
        assertThat(outPath.toFile().length(), not(equalTo(0L)));
    }

    @Disabled
    @Test
    void testWriteSpecifyDownloadDir(@TempDir Path tempDir) {

        JannovarDataFactory instance = JannovarDataFactory.builder(iniFile)
                .downloadDir(tempDir)
                .build();

        Path outPath = tempDir.resolve("transcripts_hg19_ucsc.ser");
        instance.buildAndWrite(GenomeAssembly.HG19, TranscriptSource.UCSC, outPath);

        assertThat(outPath.toFile().exists(), is(true));
        assertThat(outPath.toFile().length(), not(equalTo(0L)));
    }
}