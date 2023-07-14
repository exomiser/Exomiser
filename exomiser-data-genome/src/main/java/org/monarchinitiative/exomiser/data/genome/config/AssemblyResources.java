/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.config;

import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.model.AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.ClinVarAlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.sv.SvResource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AssemblyResources {

    private final GenomeAssembly genomeAssembly;
    private final Path genomeDataPath;
    private final Path genomeProcessedPath;
    private final ClinVarAlleleResource clinVarAlleleResource;
    private final Map<String, AlleleResource> alleleResources;
    private final List<SvResource> svResources;

    public AssemblyResources(GenomeAssembly genomeAssembly, Path genomeDataPath, Path genomeProcessedPath, ClinVarAlleleResource clinVarAlleleResource, Map<String, AlleleResource> alleleResources, List<SvResource> svResources) {
        this.genomeAssembly = genomeAssembly;
        this.genomeDataPath = genomeDataPath;
        this.genomeProcessedPath = genomeProcessedPath;
        this.alleleResources = alleleResources;
        this.svResources = svResources;
        this.clinVarAlleleResource = clinVarAlleleResource;
    }

    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    public Path getGenomeDataPath() {
        return genomeDataPath;
    }

    public Path getGenomeProcessedPath() {
        return genomeProcessedPath;
    }

    public Map<String, AlleleResource> getAlleleResources() {
        return alleleResources;
    }

    public ClinVarAlleleResource getClinVarResource() {
        return clinVarAlleleResource;
    }

    public List<AlleleResource> getUserDefinedResources(List<String> optionValues) {
        if (optionValues.isEmpty()) {
            return new ArrayList<>(alleleResources.values());
        }
        return optionValues.stream()
                .filter(alleleResources::containsKey)
                .map(alleleResources::get)
                .collect(toList());
    }

    public List<SvResource> getSvResources() {
        return svResources;
    }
}
