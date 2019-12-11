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
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ProtoParserTest {

    private SampleProto.Sample parseSample(Path path) {
        SampleProto.Sample.Builder builder = ProtoParser.parseFromJsonOrYaml(SampleProto.Sample.newBuilder(), path);
        return builder.build();
    }

    private Phenopacket parsePhenopacket(Path path) {
        Phenopacket.Builder builder = ProtoParser.parseFromJsonOrYaml(Phenopacket.newBuilder(), path);
        return builder.build();
    }

    private Family parseFamily(Path path) {
        Family.Builder builder = ProtoParser.parseFromJsonOrYaml(Family.newBuilder(), path);
        return builder.build();
    }

    @Test
    void readSampleJson() {
        SampleProto.Sample sample = parseSample(Paths.get("src/test/resources/sample/minimal_sample.json"));
        assertThat(sample.getGenomeAssembly(), equalTo("hg19"));
        assertThat(sample.getVcf(), equalTo("Pfeiffer.vcf"));
        assertThat(sample.getProband(), equalTo("manuel"));
        assertThat(sample.getPed(), equalTo(""));
        assertThat(sample.getHpoIdsList(), equalTo(List.of("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055")));
    }

    @Test
    void readSampleJsonWithPedReference() {
        SampleProto.Sample sample = parseSample(Paths.get("src/test/resources/sample/minimal_sample_ped.json"));
        assertThat(sample.getGenomeAssembly(), equalTo("hg19"));
        assertThat(sample.getVcf(), equalTo("Pfeiffer.vcf"));
        assertThat(sample.getProband(), equalTo("manuel"));
        assertThat(sample.getPed(), equalTo("family.ped"));
        assertThat(sample.getHpoIdsList(), equalTo(List.of("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055")));
    }

    @Test
    void readSampleYaml() {
        SampleProto.Sample sample = parseSample(Paths.get("src/test/resources/sample/minimal_sample.yaml"));
        assertThat(sample.getGenomeAssembly(), equalTo("hg19"));
        assertThat(sample.getVcf(), equalTo("Pfeiffer.vcf"));
        assertThat(sample.getProband(), equalTo("manuel"));
        assertThat(sample.getPed(), equalTo(""));
        assertThat(sample.getHpoIdsList(), equalTo(List.of("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055")));
    }

    @Test
    void readPhenopacketJson() {
        Phenopacket phenopacket = parsePhenopacket(Paths.get("src/test/resources/sample/minimal_phenopacket.json"));
        System.out.println(phenopacket);
    }

    @Test
    void readPhenopacketYaml() {
        Phenopacket phenopacket = parsePhenopacket(Paths.get("src/test/resources/sample/minimal_phenopacket.yaml"));
        System.out.println(phenopacket);
    }

    @Test
    void readFamilyJson() {
        Family family = parseFamily(Paths.get("src/test/resources/sample/minimal_family.json"));
        System.out.println(family);
    }

    @Test
    void readFamilyYaml() {
        Family family = parseFamily(Paths.get("src/test/resources/sample/minimal_family.yaml"));
        System.out.println(family);
    }

    @Test
    void testParseYaml() {
        String phenopacketYaml = "---\n" +
                "subject:\n" +
                "  id: \"manuel\"\n" +
                "phenotypicFeatures:\n" +
                "  - type:\n" +
                "      id: \"HP:0001156\"\n" +
                "      label: \"Brachydactyly\"\n" +
                "  - type:\n" +
                "      id: \"HP:0001363\"\n" +
                "      label: \"Craniosynostosis\"\n" +
                "  - type:\n" +
                "      id: \"HP:0011304\"\n" +
                "      label: \"Broad thumb\"\n" +
                "  - type:\n" +
                "      id: \"HP:0010055\"\n" +
                "      label: \"Broad hallux\"\n" +
                "htsFiles:\n" +
                "  - uri: \"file://Pfeiffer.vcf\"\n" +
                "    htsFormat: \"VCF\"\n" +
                "    genomeAssembly: \"GRCh37\"";
        Phenopacket.Builder ppBuilder = ProtoParser.parseFromJsonOrYaml(Phenopacket.newBuilder(), phenopacketYaml);
        System.out.println(ppBuilder.build());
    }

}