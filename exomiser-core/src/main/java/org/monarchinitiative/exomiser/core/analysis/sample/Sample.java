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

package org.monarchinitiative.exomiser.core.analysis.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.api.v1.SampleProto;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
@JsonDeserialize(builder = Sample.Builder.class)
@JsonPropertyOrder({"genomeAssembly", "vcf", "proband", "age", "sex", "hpoIds", "pedigree"})
public class Sample {

    private final GenomeAssembly genomeAssembly;
    private final Path vcfPath;
    private final String probandSampleName;
    private final Age age;
    private final Sex sex;
    private final Pedigree pedigree;
    private final List<String> hpoIds;

    public static Sample from(Family family) {
        return PhenopacketConverter.toSample(family);
    }

    public static Sample from(Phenopacket phenopacket) {
        return PhenopacketConverter.toSample(phenopacket);
    }

    public static Sample from(SampleProto.Sample sampleProto) {
        return new SampleProtoConverter().toDomain(sampleProto);
    }

    private Sample(Builder builder) {
        this.genomeAssembly = builder.genomeAssembly;
        this.vcfPath = builder.vcfPath;
        this.probandSampleName = builder.probandSampleName;
        this.age = builder.age;
        this.sex = checkSex(builder.probandSampleName, builder.sex, builder.pedigree);
        this.pedigree = builder.pedigree;
        this.hpoIds = ImmutableList.copyOf(builder.hpoIds);
    }

    private Sex checkSex(String sampleName, Sex sex, Pedigree pedigree) {
        if (pedigree.isEmpty()) {
            return sex;
        }
        Pedigree.Individual proband = pedigree.getIndividualById(sampleName);
        if (proband == null) {
            throw new IllegalArgumentException("Proband '" + sampleName + "' not present in pedigree");
        }
        Sex probandSexInPedigree = proband.getSex();
        if (sex == Sex.UNKNOWN && probandSexInPedigree != Sex.UNKNOWN) {
            return probandSexInPedigree;
        }
        if (sex != probandSexInPedigree) {
            throw new IllegalArgumentException("Proband sex stated as " + sex + " does not match pedigree stated sex of " + probandSexInPedigree);
        }
        // if we get here both stated sex and pedigree sex should be in agreement.
        return sex;
    }

    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    @JsonProperty("vcf")
    @Nullable
    public Path getVcfPath() {
        return vcfPath;
    }

    @JsonIgnore
    public boolean hasVcf() {
        return vcfPath != null;
    }

    @JsonProperty("proband")
    public String getProbandSampleName() {
        return probandSampleName;
    }

    public Age getAge() {
        return age;
    }

    public Sex getSex() {
        return sex;
    }

    public Pedigree getPedigree() {
        return pedigree;
    }

    public List<String> getHpoIds() {
        return hpoIds;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sample)) return false;
        Sample sample = (Sample) o;
        return genomeAssembly == sample.genomeAssembly &&
                Objects.equals(vcfPath, sample.vcfPath) &&
                Objects.equals(probandSampleName, sample.probandSampleName) &&
                age.equals(sample.age) &&
                sex == sample.sex &&
                pedigree.equals(sample.pedigree) &&
                hpoIds.equals(sample.hpoIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, vcfPath, probandSampleName, age, sex, pedigree, hpoIds);
    }

    @Override
    public String toString() {
        return "Sample{" +
                "genomeAssembly=" + genomeAssembly +
                ", vcfPath=" + vcfPath +
                ", probandSampleName='" + probandSampleName + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                ", pedigree=" + pedigree +
                ", hpoIds=" + hpoIds +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();
        private Path vcfPath = null;
        private String probandSampleName = ""; // Specifying this as a default here breaks existing tests. SampleIdentifier.defaultSample().getId();
        private Age age = Age.unknown();
        private Sex sex = Sex.UNKNOWN;
        private Pedigree pedigree = Pedigree.empty();
        private List<String> hpoIds = ImmutableList.of();

        public Builder genomeAssembly(GenomeAssembly genomeAssembly) {
            this.genomeAssembly = Objects.requireNonNull(genomeAssembly);
            return this;
        }

        public Builder vcfPath(@Nullable Path vcfPath) {
            // null is allowed
            this.vcfPath = vcfPath;
            return this;
        }

        public Builder probandSampleName(String probandSampleName) {
            this.probandSampleName = Objects.requireNonNull(probandSampleName);
            return this;
        }

        public Builder age(Age age) {
            this.age = Objects.requireNonNull(age);
            return this;
        }

        public Builder sex(Sex sex) {
            this.sex = Objects.requireNonNull(sex);
            return this;
        }

        public Builder pedigree(Pedigree pedigree) {
            this.pedigree = Objects.requireNonNull(pedigree);
            return this;
        }

        public Builder hpoIds(List<String> hpoIds) {
            this.hpoIds = Objects.requireNonNull(hpoIds);
            return this;
        }

        public Builder from(Sample sample) {
            this.genomeAssembly = sample.genomeAssembly;
            this.vcfPath = sample.vcfPath;
            this.probandSampleName = sample.probandSampleName;
            this.age = sample.age;
            this.sex = sample.sex;
            this.pedigree = sample.pedigree;
            this.hpoIds = sample.hpoIds;
            return this;
        }

        public Sample build() {
            return new Sample(this);
        }
    }
}
