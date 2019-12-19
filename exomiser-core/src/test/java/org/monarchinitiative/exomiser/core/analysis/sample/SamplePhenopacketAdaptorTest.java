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

package org.monarchinitiative.exomiser.core.analysis.sample;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.PhenotypicFeature;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SamplePhenopacketAdaptorTest {


    @Test
    void wellFormedPhenopacket() {
        Individual subject = Individual.newBuilder()
                .setId("manuel")
                .build();

        Path vcfFile = Paths.get("src/test/resources/Pfeiffer.vcf");

        String uri = vcfFile.toUri().toString();
        System.out.println(uri);

        HtsFile vcf = HtsFile.newBuilder()
                .setGenomeAssembly("GRCh37")
                .setHtsFormat(HtsFile.HtsFormat.VCF)
                .setUri(uri)
                .build();

        PhenotypicFeature craniosynostosis = PhenotypicFeature.newBuilder()
                .setType(OntologyClass.newBuilder().setId("HP:0001363").setLabel("Craniosynostosis").build())
                .build();

        Phenopacket phenopacket = Phenopacket.newBuilder()
                .setSubject(subject)
                .addPhenotypicFeatures(craniosynostosis)
                .addHtsFiles(vcf)
                .build();

        Sample instance = new SamplePhenopacketAdaptor(phenopacket);

        assertThat(instance.getGenomeAssembly(), equalTo(GenomeAssembly.HG19));
        assertThat(instance.getVcfPath(), equalTo(vcfFile.toAbsolutePath()));
        assertThat(instance.getProbandSampleName(), equalTo("manuel"));
        assertThat(instance.getProbandSampleIdentifier(), equalTo(SampleIdentifier.of("manuel", 0)));
        assertThat(instance.getSampleNames(), equalTo(List.of("manuel")));
        assertThat(instance.getPedigree(), equalTo(Pedigree.justProband("manuel")));
        assertThat(instance.getHpoIds(), equalTo(List.of("HP:0001363")));
    }
}