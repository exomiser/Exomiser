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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private static final String HEADER =
            String.join("\t", "#RANK", "ID", "GENE_SYMBOL", "ENTREZ_GENE_ID", "MOI", "P-VALUE",
                    "EXOMISER_GENE_COMBINED_SCORE", "EXOMISER_GENE_PHENO_SCORE", "EXOMISER_GENE_VARIANT_SCORE",
                    "HUMAN_PHENO_SCORE", "MOUSE_PHENO_SCORE", "FISH_PHENO_SCORE", "WALKER_SCORE",
                    "PHIVE_ALL_SPECIES_SCORE", "OMIM_SCORE", "MATCHES_CANDIDATE_GENE",
                    "HUMAN_PHENO_EVIDENCE", "MOUSE_PHENO_EVIDENCE", "FISH_PHENO_EVIDENCE",
                    "HUMAN_PPI_EVIDENCE", "MOUSE_PPI_EVIDENCE", "FISH_PPI_EVIDENCE");

    private static final String FGFR2_GENE_STRING = "1\tFGFR2_AD\tFGFR2\t2263\tAD\t1.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0\t";
    private static final String RBM8A_GENE_STRING = "1\tRBM8A_AD\tRBM8A\t9939\tAD\t1.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0\t";

    private final TsvGeneResultsWriter instance = new TsvGeneResultsWriter();

    private AnalysisResults analysisResults;
    private final Analysis analysis = Analysis.builder().build();
    private final Sample sample = Sample.builder().build();

    @BeforeEach
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addGeneScore(GeneScore.builder().geneIdentifier(fgfr2.getGeneIdentifier()).modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT).build());
        Gene rbm8a = TestFactory.newGeneRBM8A();
        rbm8a.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        rbm8a.addGeneScore(GeneScore.builder().geneIdentifier(rbm8a.getGeneIdentifier()).modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT).build());
        analysisResults = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Arrays.asList(fgfr2, rbm8a))
                .build();
    }

    @Test
    public void testWrite() throws Exception {
        Path tempFolder = Files.createTempDirectory("exomiser_test");
        String outPrefix = tempFolder.resolve("testWrite").toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();

        instance.writeFile(analysisResults, settings);

        Path outputPath = tempFolder.resolve("testWrite.genes.tsv");
        assertThat(Files.exists(outputPath), is(true));
        assertThat(Files.deleteIfExists(outputPath), is(true));
        Files.delete(tempFolder);
    }

    @Test
    public void testWriteString() {
        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();
        String outString = instance.writeString(analysisResults, settings);
        assertThat(outString, equalTo(String.join("\n", HEADER, FGFR2_GENE_STRING, RBM8A_GENE_STRING) + "\n"));
    }

    @Test
    public void testWriteStringStartsWithAHeaderLine() {
        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();
        String outString = instance.writeString(analysisResults, settings);
        String[] lines = outString.split("\n");
        assertThat(lines[0], equalTo(HEADER));
    }

}
