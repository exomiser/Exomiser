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
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvGeneResultsWriterTest {

    private final TsvGeneResultsWriter instance = new TsvGeneResultsWriter();

    private AnalysisResults analysisResults;

    @BeforeEach
    public void setUp() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
//        fgfr2.addPriorityResult(new HiPhivePriorityResult());
        fgfr2.addPriorityResult(new OmimPriorityResult(fgfr2.getEntrezGeneID(), fgfr2.getGeneSymbol(), 1d, List.of(), Map.of()));
        fgfr2.addGeneScore(GeneScore.builder().geneIdentifier(fgfr2.getGeneIdentifier()).modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT).build());

        Gene rbm8a = TestFactory.newGeneRBM8A();
        rbm8a.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        rbm8a.addGeneScore(GeneScore.builder().geneIdentifier(rbm8a.getGeneIdentifier()).modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT).build());

        analysisResults = AnalysisResults.builder()
                .genes(Arrays.asList(fgfr2, rbm8a))
                .build();
    }

    @Test
    public void testWrite(@TempDir Path tempDir) throws Exception {
        String outPrefix = tempDir.resolve("testWrite").toString();

        OutputSettings settings = OutputSettings.builder()
                .outputPrefix(outPrefix)
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();

        instance.writeFile(ModeOfInheritance.ANY, analysisResults, settings);

        Path outputPath = tempDir.resolve("testWrite.genes.tsv");
        assertThat(Files.exists(outputPath), is(true));
    }

    @Test
    public void testWriteString() {
        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_GENE))
                .build();
        String actual = instance.writeString(ModeOfInheritance.ANY, analysisResults, settings);

        String expected =
                "#RANK\tID\tGENE_SYMBOL\tENTREZ_GENE_ID\tMOI\tP-VALUE\tEXOMISER_GENE_COMBINED_SCORE\tEXOMISER_GENE_PHENO_SCORE\tEXOMISER_GENE_VARIANT_SCORE\tHUMAN_PHENO_SCORE\tMOUSE_PHENO_SCORE\tFISH_PHENO_SCORE\tWALKER_SCORE\tPHIVE_ALL_SPECIES_SCORE\tOMIM_SCORE\tMATCHES_CANDIDATE_GENE\tHUMAN_PHENO_EVIDENCE\tMOUSE_PHENO_EVIDENCE\tFISH_PHENO_EVIDENCE\tHUMAN_PPI_EVIDENCE\tMOUSE_PPI_EVIDENCE\tFISH_PPI_EVIDENCE\n" +
        "1\tFGFR2_AD\tFGFR2\t2263\tAD\t1.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t1.0000\t0\t\n" +
        "1\tRBM8A_AD\tRBM8A\t9939\tAD\t1.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0.0000\t0\t\n";
        assertThat(actual, equalTo(expected));
    }

}
