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
import org.phenopackets.schema.v1.Family;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Individual;
import org.phenopackets.schema.v1.core.OntologyClass;
import org.phenopackets.schema.v1.core.PhenotypicFeature;

import javax.annotation.Nullable;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class PhenopacketSampleConverter {

    public static Sample toExomiserSample(Family family) {
        Phenopacket probandPhenopacket = family.getProband();
        Individual proband = probandPhenopacket.getSubject();

        Pedigree.Individual.Sex sex = PhenopacketPedigreeConverter.toExomiserSex(proband.getSex());
        Pedigree pedigree = findFamilyOrProbandOnlyPedigree(family);
        VcfFile vcfFile = findFamilyOrProbandVcf(family);

        return Sample.builder()
                .probandSampleName(proband.getId())
                .sex(sex)
                .age(extractAge(probandPhenopacket))
                .hpoIds(extractPositivePhenotypes(probandPhenopacket))
                .genomeAssembly(vcfFile.getGenomeAssembly())
                .vcfPath(vcfFile.getPath())
                .pedigree(pedigree)
                .build();
    }

    private static Pedigree findFamilyOrProbandOnlyPedigree(Family family) {
        if (family.hasPedigree()) {
            return PhenopacketPedigreeConverter.toExomiserPedigree(family.getPedigree());
        }
        return PhenopacketPedigreeConverter.createSingleSamplePedigree(family.getProband().getSubject());
    }

    private static VcfFile findFamilyOrProbandVcf(Family family) {
        if (containsVcf(family.getHtsFilesList())) {
            return findFirstVcfFile(family.getHtsFilesList());
        }
        return findFirstVcfFile(family.getProband().getHtsFilesList());
    }

    private static boolean containsVcf(List<HtsFile> htsFilesList) {
        return htsFilesList.stream()
                .anyMatch(file -> file.getHtsFormat().equals(HtsFile.HtsFormat.VCF));
    }

    static Sample toExomiserSample(Phenopacket phenopacket) {
        // TODO: test creation of the pedigree when the htsFile.getIndividualToSampleIdentifiersMap() is not empty such that
        //  the pedigree is made from the sampleIdentifiers so that Exomiser can validate the sample identifiers in the
        //  pedigree against the sample identifiers in the VCF.
        Individual proband = phenopacket.getSubject();

        Pedigree.Individual.Sex sex = PhenopacketPedigreeConverter.toExomiserSex(proband.getSex());
        Pedigree pedigree = PhenopacketPedigreeConverter.createSingleSamplePedigree(proband);
        VcfFile vcfFile = findFirstVcfFile(phenopacket.getHtsFilesList());

        return Sample.builder()
                .probandSampleName(proband.getId())
                .sex(sex)
                .age(extractAge(phenopacket))
                .hpoIds(extractPositivePhenotypes(phenopacket))
                .genomeAssembly(vcfFile.getGenomeAssembly())
                .vcfPath(vcfFile.getPath())
                .pedigree(pedigree)
                .build();
    }

    private static List<String> extractPositivePhenotypes(Phenopacket phenopacket) {
        return phenopacket.getPhenotypicFeaturesList()
                .stream()
                // ensure we only have positive phenotypes
                .filter(phenotypicFeature -> !phenotypicFeature.getNegated())
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .collect(ImmutableList.toImmutableList());
    }

    private static Age extractAge(Phenopacket phenopacket) {
        if (phenopacket.hasSubject() && phenopacket.getSubject().hasAgeAtCollection()) {
            org.phenopackets.schema.v1.core.Age ageAtCollection = phenopacket.getSubject().getAgeAtCollection();
            return Age.parse(ageAtCollection.getAge());
        }
        return Age.unknown();
    }

    private static VcfFile findFirstVcfFile(List<HtsFile> htsFiles) {
        return htsFiles.stream()
                .filter(file -> file.getHtsFormat().equals(HtsFile.HtsFormat.VCF))
                .findFirst()
                .map(VcfFile::new)
                .orElse(VcfFile.DEFAULT_VCF);
    }

    private static class VcfFile {

        private static final VcfFile DEFAULT_VCF = new VcfFile(HtsFile.getDefaultInstance());

        private final HtsFile htsFile;

        public VcfFile(HtsFile htsFile) {
            this.htsFile = htsFile;
        }

        public GenomeAssembly getGenomeAssembly() {
            String assembly = htsFile.getGenomeAssembly();
            return assembly.isEmpty() ? GenomeAssembly.defaultBuild() : GenomeAssembly.parseAssembly(assembly);
        }

        @Nullable
        public Path getPath() {
            String uriString = htsFile.getUri();
            return uriString.isEmpty() ? null : Paths.get(URI.create(uriString));
        }
    }
}
