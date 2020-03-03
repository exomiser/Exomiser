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

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.PhenotypicFeature;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class SamplePhenopacketAdaptor implements Sample {

    private final GenomeAssembly genomeAssembly;
    private final Path vcfPath;
    private final Pedigree pedigree;
    private final Age age;
    private final Pedigree.Individual.Sex sex;
    private final String probandSampleName;
    private final List<String> hpoIds;

    SamplePhenopacketAdaptor(Phenopacket phenopacket) {
        probandSampleName = phenopacket.getSubject().getId();
        age = extractAge(phenopacket);
        sex = extractSex(phenopacket);
        hpoIds = phenopacket.getPhenotypicFeaturesList()
                .stream()
                // ensure we only have positive phenotypes
                .filter(phenotypicFeature -> !phenotypicFeature.getNegated())
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .collect(ImmutableList.toImmutableList());


        HtsFile htsFile = validateAndExtractHtsFile(phenopacket.getHtsFilesList());
        //TODO: allow HtsFile to be null as vcfPath can be null
        genomeAssembly = GenomeAssembly.fromValue(htsFile.getGenomeAssembly());
        vcfPath = Paths.get(URI.create(htsFile.getUri()));//.toAbsolutePath();
        // pedigree = unvalidated pedigree. The AnalysisRunner will validate this against the VCF just before the
        // analysis is run. The reason being that its possible that the sample creation part could be happening
        // sometime before the analysis happens and not in a process which has access to the VCF file.

        // TODO: test creation of the pedigree when the htsFile.getIndividualToSampleIdentifiersMap() is not empty such that
        //  the pedigree is made from the sampleIdentifiers so that Exomiser can validate the sample identifiers in the
        //  pedigree against the sample identifiers in the VCF.
        pedigree = Pedigree.justProband(probandSampleName);
    }

    private Pedigree.Individual.Sex extractSex(Phenopacket phenopacket) {
        Individual subject = phenopacket.getSubject();
        switch (subject.getSex()) {
            case MALE:
                return Pedigree.Individual.Sex.MALE;
            case FEMALE:
                return Pedigree.Individual.Sex.FEMALE;
        }
        return Pedigree.Individual.Sex.UNKNOWN;
    }

    private Age extractAge(Phenopacket phenopacket) {
        if (phenopacket.getSubject().hasAgeAtCollection()) {
            org.phenopackets.schema.v1.core.Age ageAtCollection = phenopacket.getSubject().getAgeAtCollection();
            return Age.parse(ageAtCollection.getAge());
        }
        return Age.unknown();
    }

    private HtsFile validateAndExtractHtsFile(List<HtsFile> htsFiles) {
        if (htsFiles.isEmpty()) {
            throw new SampleValidationException("Sample must have a VCF file path");
        }

        Optional<HtsFile> htsFile = htsFiles.stream()
                .filter(file -> file.getHtsFormat().equals(HtsFile.HtsFormat.VCF))
                .findFirst();

        return htsFile.orElseThrow(() -> new SampleValidationException("Sample must reference a VCF file"));
    }

    @Override
    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public Path getVcfPath() {
        return vcfPath;
    }

    @Override
    public boolean hasVcf() {
        return vcfPath != null;
    }

    @Override
    public String getProbandSampleName() {
        return probandSampleName;
    }

    @Override
    public Age getAge() {
        return age;
    }

    @Override
    public Pedigree.Individual.Sex getSex() {
        return sex;
    }

    @Override
    public Pedigree getPedigree() {
        return pedigree;
    }

    @Override
    public List<String> getHpoIds() {
        return hpoIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SamplePhenopacketAdaptor)) return false;
        SamplePhenopacketAdaptor that = (SamplePhenopacketAdaptor) o;
        return genomeAssembly == that.genomeAssembly &&
                Objects.equals(vcfPath, that.vcfPath) &&
                Objects.equals(pedigree, that.pedigree) &&
                Objects.equals(age, that.age) &&
                sex == that.sex &&
                Objects.equals(probandSampleName, that.probandSampleName) &&
                Objects.equals(hpoIds, that.hpoIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, vcfPath, pedigree, age, sex, probandSampleName, hpoIds);
    }

    @Override
    public String toString() {
        return "SamplePhenopacketAdaptor{" +
                "genomeAssembly=" + genomeAssembly +
                ", vcfPath=" + vcfPath +
                ", pedigree=" + pedigree +
                ", age=" + age +
                ", sex=" + sex +
                ", probandSampleName='" + probandSampleName + '\'' +
                ", hpoIds=" + hpoIds +
                '}';
    }
}
