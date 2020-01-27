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
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Adaptor around an {@link Analysis} to extract and validate the {@link Sample} data.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class SampleAnalysisAdaptor implements Sample {

    private final GenomeAssembly genomeAssembly;
    private final Path vcfPath;
    private final String probandSampleName;
    private final Pedigree pedigree;
    private final List<String> hpoIds;

    public SampleAnalysisAdaptor(Analysis analysis) {
        genomeAssembly = analysis.getGenomeAssembly();
        vcfPath = Objects.requireNonNull(analysis.getVcfPath());
        probandSampleName = analysis.getProbandSampleName();
        hpoIds = ImmutableList.copyOf(analysis.getHpoIds());
        pedigree = analysis.getPedigree();
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
        if (!(o instanceof SampleAnalysisAdaptor)) return false;
        SampleAnalysisAdaptor that = (SampleAnalysisAdaptor) o;
        return genomeAssembly == that.genomeAssembly &&
                Objects.equals(vcfPath, that.vcfPath) &&
                Objects.equals(probandSampleName, that.probandSampleName) &&
                Objects.equals(pedigree, that.pedigree) &&
                Objects.equals(hpoIds, that.hpoIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, vcfPath, probandSampleName, pedigree, hpoIds);
    }

    @Override
    public String toString() {
        return "AnalysisSampleAdaptor{" +
                "genomeAssembly=" + genomeAssembly +
                ", vcfPath=" + vcfPath +
                ", probandSampleName='" + probandSampleName + '\'' +
                ", pedigree=" + pedigree +
                ", hpoIds=" + hpoIds +
                '}';
    }
}
