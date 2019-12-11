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

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.SampleIdentifier;
import org.phenopackets.schema.v1.Phenopacket;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface Sample {

    public GenomeAssembly getGenomeAssembly();

    public Path getVcfPath();

    public String getProbandSampleName();

    public SampleIdentifier getProbandSampleIdentifier();

    public List<String> getSampleNames();

    public default Pedigree getPedigree() {
        return Pedigree.justProband(getProbandSampleName());
    }

    public List<String> getHpoIds();

    static Sample of(Phenopacket phenopacket) {
        return new SamplePhenopacketAdaptor(phenopacket);
    }
}
