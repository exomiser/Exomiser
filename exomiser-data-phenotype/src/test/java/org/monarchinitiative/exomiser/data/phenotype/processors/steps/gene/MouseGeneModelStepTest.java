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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.EnsemblMouseGeneOrthologReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.ImpcMouseGenePhenotypeReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.MgiMouseGeneOrthologReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.MgiMouseGenePhenotypeReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MouseGeneModelStepTest {

    @Test
    void run(@TempDir Path tempDir) {
        Resource mgiHomMouseHumanSequenceResource = Resource.of("src/test/resources/data/mouse/HOM_MouseHumanSequence_test.rpt");
        MgiMouseGeneOrthologReader mgiMouseGeneOrthologReader = new MgiMouseGeneOrthologReader(mgiHomMouseHumanSequenceResource);

        Resource ensemblMouseGeneOrthologResource = Resource.of("src/test/resources/data/mouse/Query%3E_test");
        EnsemblMouseGeneOrthologReader ensemblMouseGeneOrthologReader = new EnsemblMouseGeneOrthologReader(ensemblMouseGeneOrthologResource);

        Path mouseGeneOrthologsPg = tempDir.resolve("mouseGeneOrthologs_test.pg");
        OutputLineWriter<GeneOrtholog> geneOrthologOutputLineWriter = new OutputLineWriter<>(mouseGeneOrthologsPg);

        Resource mgiGenePhenoResource = Resource.of("src/test/resources/data/mouse/MGI_GenePheno_test.rpt");
        MgiMouseGenePhenotypeReader mgiMouseGenePhenotypeReader = new MgiMouseGenePhenotypeReader(mgiGenePhenoResource);

        Resource allGenotypePhenotypeResource = Resource.of("src/test/resources/data/mouse/ALL_genotype_phenotype_test.csv.gz");
        ImpcMouseGenePhenotypeReader impcMouseGenePhenotypeReader = new ImpcMouseGenePhenotypeReader(allGenotypePhenotypeResource);

        Path mouseGeneModelsPg = tempDir.resolve("mouseGeneModels_test.pg");
        OutputLineWriter<GeneModel> geneModelOutputLineWriter = new OutputLineWriter<>(mouseGeneModelsPg);

        MouseGeneModelStep mouseGeneModelStep = new MouseGeneModelStep(mgiMouseGeneOrthologReader, ensemblMouseGeneOrthologReader, geneOrthologOutputLineWriter, mgiMouseGenePhenotypeReader, impcMouseGenePhenotypeReader, geneModelOutputLineWriter);
        mouseGeneModelStep.run();

        File mouseGeneOrthologsPgFile = mouseGeneOrthologsPg.toFile();
        assertTrue(mouseGeneOrthologsPgFile.exists());
        assertTrue(mouseGeneOrthologsPgFile.length() > 0);

        File mouseGeneModelsPgFile = mouseGeneModelsPg.toFile();
        assertTrue(mouseGeneModelsPgFile.exists());
        assertTrue(mouseGeneModelsPgFile.length() > 0);
    }

    @Test
    void mergeGeneOrthologs() {
        Resource mgiHomMouseHumanSequenceResource = Resource.of("src/test/resources/data/mouse/HOM_MouseHumanSequence_test.rpt");
        MgiMouseGeneOrthologReader mgiMouseGeneOrthologReader = new MgiMouseGeneOrthologReader(mgiHomMouseHumanSequenceResource);

        Resource ensemblMouseGeneOrthologResource = Resource.of("src/test/resources/data/mouse/Query%3E_test");
        EnsemblMouseGeneOrthologReader ensemblMouseGeneOrthologReader = new EnsemblMouseGeneOrthologReader(ensemblMouseGeneOrthologResource);

        List<GeneOrtholog> actual = MouseGeneModelStep.getUniqueGeneOrthologs(mgiMouseGeneOrthologReader.read(), ensemblMouseGeneOrthologReader.read());

        // order is important, this should be reproducible
        List<GeneOrtholog> expected = List.of(
                new GeneOrtholog("MGI:101762","Elk3", "ELK3", 2004),
                new GeneOrtholog("MGI:102556","Tbx4", "TBX4", 9496),
                new GeneOrtholog("MGI:1202301","Itch", "ITCH", 83737),
                new GeneOrtholog("MGI:1316742","Lgals7", "LGALS7", 3963),
                new GeneOrtholog("MGI:1316742","Lgals7", "LGALS7", 653499),
                new GeneOrtholog("MGI:1915220","Slx1b", "SLX1A", 548593),
                new GeneOrtholog("MGI:1915220","Slx1b", "SLX1B", 79008),
                new GeneOrtholog("MGI:2671987","Shank2", "SHANK2", 22941),
                new GeneOrtholog("MGI:3031035","Olfr1201", "OR4C11", 219429),
                new GeneOrtholog("MGI:3031039","Olfr1205", "OR4C11", 219429),
                new GeneOrtholog("MGI:3031040","Olfr1206", "OR4C11", 219429),
                new GeneOrtholog("MGI:87867","Acadm", "ACADM", 34),
                new GeneOrtholog("MGI:895149","Acadvl", "ACADVL", 37),
                new GeneOrtholog("MGI:96522","Rbpj", "RBPJ", 3516),
                new GeneOrtholog("MGI:97874","Rb1", "RB1", 5925)
                );

        assertThat(actual, equalTo(expected));
    }
}