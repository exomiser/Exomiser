/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.StringJoiner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private static final String HEADER = new StringJoiner("\t")
            .add("#GENE_SYMBOL")
            .add("ENTREZ_GENE_ID")
            .add("EXOMISER_GENE_PHENO_SCORE")
            .add("EXOMISER_GENE_VARIANT_SCORE")
            .add("EXOMISER_GENE_COMBINED_SCORE")
            .add("HUMAN_PHENO_SCORE")
            .add("MOUSE_PHENO_SCORE")
            .add("FISH_PHENO_SCORE")
            .add("WALKER_SCORE")
            .add("PHIVE_ALL_SPECIES_SCORE")
            .add("OMIM_SCORE")
            .add("MATCHES_CANDIDATE_GENE")
            .add("HUMAN_PHENO_EVIDENCE")
            .add("MOUSE_PHENO_EVIDENCE")
            .add("FISH_PHENO_EVIDENCE")
            .add("HUMAN_PPI_EVIDENCE")
            .add("MOUSE_PPI_EVIDENCE")
            .add("FISH_PPI_EVIDENCE\n")
            .toString();
    
    private static final String FGFR2_GENE_STRING = "FGFR2	2263	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0	\n";
    private static final String RBM8A_GENE_STRING = "RBM8A	9939	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0.0000	0	\n";

    private final TsvGeneResultsWriter instance = new TsvGeneResultsWriter();

    private AnalysisResults analysisResults;
    private Analysis analysis = Analysis.builder().build();
    
    @Before
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        Gene rbm8a = TestFactory.newGeneRBM8A();
        analysisResults = AnalysisResults.builder().genes(Arrays.asList(fgfr2, rbm8a)).build();
    }

    @Test
    public void testWrite() {
        OutputSettings settings = OutputSettings.builder()
                .outputPrefix("testWrite")
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();
        instance.writeFile(analysis, analysisResults, settings);
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.genes.tsv").toFile().delete());
    }

    @Test
    public void testWriteString() {
        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();
        String outString = instance.writeString(analysis, analysisResults, settings);
        assertThat(outString, equalTo(HEADER + FGFR2_GENE_STRING + RBM8A_GENE_STRING));
    }

    @Test
    public void testWriteStringStartsWithAHeaderLine() {
        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();
        String outString = instance.writeString(analysis, analysisResults, settings);
        String[] lines = outString.split("\n");
        assertThat(lines[0] + "\n", equalTo(HEADER));
    }
    
    @Test
    public void testMakeGeneLine() {
        String result = instance.makeGeneLine(TestFactory.newGeneFGFR2());
        assertThat(result, equalTo(FGFR2_GENE_STRING));
    }

}
