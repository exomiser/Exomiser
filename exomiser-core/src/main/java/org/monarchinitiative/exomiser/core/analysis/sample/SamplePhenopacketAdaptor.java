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

import com.google.common.collect.ImmutableList;
import htsjdk.variant.vcf.VCFHeader;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.VcfFiles;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
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
    private final String probandSampleName;
    private final SampleIdentifier probandSampleIdentifier;
    private final List<String> sampleNames;

    private final Pedigree pedigree;

    private final List<String> hpoIds;

    SamplePhenopacketAdaptor(Phenopacket phenopacket) {
        probandSampleName = phenopacket.getSubject().getId();
        hpoIds = phenopacket.getPhenotypicFeaturesList()
                .stream()
                // ensure we only have positive phenotypes
                .filter(phenotypicFeature -> !phenotypicFeature.getNegated())
                .map(PhenotypicFeature::getType)
                .map(OntologyClass::getId)
                .collect(ImmutableList.toImmutableList());


        HtsFile htsFile = validateAndExtractHtsFile(phenopacket.getHtsFilesList());

        genomeAssembly = GenomeAssembly.fromValue(htsFile.getGenomeAssembly());
        vcfPath = Paths.get(URI.create(htsFile.getUri()));//.toAbsolutePath();

        VCFHeader vcfHeader = VcfFiles.readVcfHeader(vcfPath);
        sampleNames = ImmutableList.copyOf(vcfHeader.getGenotypeSamples());

        probandSampleIdentifier = SampleIdentifierUtil.createProbandIdentifier(probandSampleName, sampleNames);

        this.pedigree = PedigreeSampleValidator.validate(Pedigree.justProband(probandSampleName), probandSampleIdentifier, sampleNames);
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
    public String getProbandSampleName() {
        return probandSampleName;
    }

    @Override
    public SampleIdentifier getProbandSampleIdentifier() {
        return probandSampleIdentifier;
    }

    @Override
    public List<String> getSampleNames() {
        return sampleNames;
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
                Objects.equals(probandSampleName, that.probandSampleName) &&
                Objects.equals(probandSampleIdentifier, that.probandSampleIdentifier) &&
                Objects.equals(pedigree, that.pedigree) &&
                Objects.equals(hpoIds, that.hpoIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, vcfPath, probandSampleName, probandSampleIdentifier, pedigree, hpoIds);
    }

    @Override
    public String toString() {
        return "SamplePhenopacketAdaptor{" +
                "genomeAssembly=" + genomeAssembly +
                ", vcfPath=" + vcfPath +
                ", probandSampleName='" + probandSampleName + '\'' +
                ", probandSampleIdentifier=" + probandSampleIdentifier +
                ", sampleNames=" + sampleNames +
                ", pedigree=" + pedigree +
                ", hpoIds=" + hpoIds +
                '}';
    }
}
