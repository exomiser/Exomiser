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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OwlSimPhenodigmProcessorTest {

    @Test
    void processHpHpCache(@TempDir Path tempDir) throws IOException {
        Resource hpHpPhenodigmCache = Resource.of("src/test/resources/data/hp-hp-phenodigm-cache-test.txt");
        Path processedCacheFile = tempDir.resolve("hpHpMapping.pg");
        OwlSimPhenodigmProcessor instance = new OwlSimPhenodigmProcessor(hpHpPhenodigmCache, processedCacheFile);

        List<OboOntologyTerm> hpTerms = List.of(
                OboOntologyTerm.builder().id("HP:0002651").label("Spondyloepimetaphyseal dysplasia").build(),
                OboOntologyTerm.builder().id("HP:0002652").label("Skeletal dysplasia").build(),
                OboOntologyTerm.builder().id("HP:0002657").label("Spondylometaphyseal dysplasia").build(),
                OboOntologyTerm.builder().id("HP:0030744").label("Hyaloid vascular remnant and retrolental mass").build(),
                OboOntologyTerm.builder().id("HP:0000648").label("Optic atrophy").build(),
                OboOntologyTerm.builder().id("HP:0012778").label("Retinal astrocytic hamartoma").build(),
                OboOntologyTerm.builder().id("HP:0012643").label("Foveal hypopigmentation").build(),
                OboOntologyTerm.builder().id("HP:0002190").label("Choroid plexus cyst").build()
        );

        instance.process(hpTerms, hpTerms);
        assertTrue(Files.size(processedCacheFile) > 0);
    }

    @Test
    void processHpHpCacheLine() {
        List<OboOntologyTerm> hpTerms = List.of(
                OboOntologyTerm.builder().id("HP:0002651").label("Spondyloepimetaphyseal dysplasia").build()
        );

        OwlSimPhenodigmProcessor.PhenodigmCacheLineProcessor instance = new OwlSimPhenodigmProcessor.PhenodigmCacheLineProcessor(hpTerms, hpTerms);
        assertThat(instance.processLine("HP_0002651\tHP_0002651\t1.0\t8.829843768215113\tHP_0002651;"),
                equalTo("0|HP:0002651|Spondyloepimetaphyseal dysplasia|HP:0002651|Spondyloepimetaphyseal dysplasia|1.0|8.829843768215113|2.9715053034135934|HP:0002651|Spondyloepimetaphyseal dysplasia"));
        assertThat(instance.processLine("HP_0002651\tHP_0002651\t1.0\t8.829843768215113\tHP_0002651;"),
                equalTo("1|HP:0002651|Spondyloepimetaphyseal dysplasia|HP:0002651|Spondyloepimetaphyseal dysplasia|1.0|8.829843768215113|2.9715053034135934|HP:0002651|Spondyloepimetaphyseal dysplasia"));
        assertThat(instance.linesProcessed(), equalTo(2));
    }

    @Test
    void processHpMpCacheLine() {
        List<OboOntologyTerm> hpTerms = List.of(
                OboOntologyTerm.builder().id("HP:0010495").label("Amniotic constriction rings of legs").build(),
                OboOntologyTerm.builder().id("HP:0001194").label("Abnormalities of placenta or umbilical cord").build()
        );

        List<OboOntologyTerm> mpTerms = List.of(
                OboOntologyTerm.builder().id("MP:0001711").label("abnormal placenta morphology").build(),
                OboOntologyTerm.builder().id("MP:0001712").label("abnormal placenta development").build()
        );

        OwlSimPhenodigmProcessor.PhenodigmCacheLineProcessor instance = new OwlSimPhenodigmProcessor.PhenodigmCacheLineProcessor(hpTerms, mpTerms);
        assertThat(instance.processLine("HP_0010495\tMP_0001711\t0.3125\t5.055448136114318\tHP_0001194;"),
                equalTo("0|HP:0010495|Amniotic constriction rings of legs|MP:0001711|abnormal placenta morphology|0.3125|5.055448136114318|1.256911907229669|HP:0001194|Abnormalities of placenta or umbilical cord"));
        assertThat(instance.processLine("HP_0010495\tMP_0001712\t0.20833333333333334\t5.055448136114318\tHP_0001194;"),
                equalTo("1|HP:0010495|Amniotic constriction rings of legs|MP:0001712|abnormal placenta development|0.20833333333333334|5.055448136114318|1.026264274780372|HP:0001194|Abnormalities of placenta or umbilical cord"));
        assertThat(instance.linesProcessed(), equalTo(2));
    }
}