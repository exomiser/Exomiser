/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;
import org.monarchinitiative.exomiser.core.genome.GeneFactory;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.JannovarVariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.VariantAnnotator;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegionIndex;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantAnnotation;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@ExtendWith(TempDirectory.class)
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

    @Disabled
    @Test
    void testBuildData(@TempDir Path tempDir) {

        Path jannovarOutputDir = Paths.get("C:/Users/hhx640/Documents/Jannovar/data");
        JannovarDataFactory instance = JannovarDataFactory.builder(iniFile).downloadDir(jannovarOutputDir).build();

        JannovarData jannovarData = instance.buildData(GenomeAssembly.HG19, TranscriptSource.ENSEMBL);

        JannovarDataProtoSerialiser.save(jannovarOutputDir.resolve("1902_transcripts_ensembl.ser"), jannovarData);

        JannovarData roundTripped = JannovarDataSourceLoader.loadJannovarData(jannovarOutputDir.resolve("1902_transcripts_ensembl.ser"));
        roundTripped.getChromosomes()
                .values()
                .forEach(chromosome -> System.out.printf("Chrom: %d %s num genes: %d%n", chromosome.getChrID(), chromosome
                        .getChromosomeName(), chromosome.getNumberOfGenes()));

        VariantAnnotator variantAnnotator = new JannovarVariantAnnotator(GenomeAssembly.HG19, roundTripped, ChromosomalRegionIndex
                .empty());

        VariantAnnotation variantAnnotation = variantAnnotator.annotate("19", 36227863, "C", "T");
        System.out.println(variantAnnotation);

        GeneFactory geneFactory = new GeneFactory(roundTripped);
        for (Gene gene : geneFactory.createKnownGenes()) {
            if (gene.getGeneSymbol().equals(variantAnnotation.getGeneSymbol())) {
                System.out.println(gene.getGeneIdentifier());
            }
            if (!gene.getGeneSymbol().contains(".") && !gene.getGeneIdentifier().hasEntrezId()) {
                System.out.println(gene.getGeneIdentifier());
            }
        }

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