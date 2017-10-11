/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome;

import com.google.common.collect.Sets;

import java.util.*;

/**
 * Container class for providing instances of {@link GenomeAnalysisService} for a given {@link GenomeAssembly}. These
 * are exposed using map-like semantics. Will guarantee that at least a default service is available.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 9.0.0
 */
public class GenomeAnalysisServiceProvider {

    private final Map<GenomeAssembly, GenomeAnalysisService> genomeAnalysisServices;

    public GenomeAnalysisServiceProvider(GenomeAnalysisService... genomeAnalysisServices) {
        Objects.requireNonNull(genomeAnalysisServices, "genomeAnalysisServices cannot be null.");
        if (genomeAnalysisServices.length == 0) {
            throw new IllegalArgumentException("genomeAnalysisServices cannot be empty.");
        }

        this.genomeAnalysisServices = new EnumMap<>(GenomeAssembly.class);
        for (GenomeAnalysisService genomeAnalysisService : genomeAnalysisServices) {
            addService(genomeAnalysisService);
        }
    }

    private void addService(GenomeAnalysisService genomeAnalysisService) {
        GenomeAssembly genomeAssembly = genomeAnalysisService.getGenomeAssembly();
        genomeAnalysisServices.put(genomeAssembly, genomeAnalysisService);
    }

    public Set<GenomeAssembly> getProvidedAssemblies() {
        Set<GenomeAssembly> providedAssemblies = new HashSet<>(genomeAnalysisServices.keySet());
        return Sets.immutableEnumSet(providedAssemblies);
    }

    /**
     * @param genomeAssembly the genomeAssembly whose associated value is to be returned.
     * @return A {@link GenomeAnalysisService} instance for the supplied {@link GenomeAssembly}.
     * @throws UnsupportedGenomeAssemblyException if the specified {@link GenomeAssembly} is not available.
     */
    public GenomeAnalysisService get(GenomeAssembly genomeAssembly) {
        if (hasServiceFor(genomeAssembly)) {
            return genomeAnalysisServices.get(genomeAssembly);
        }
        throw new UnsupportedGenomeAssemblyException(String.format("Genome assembly %s is not configured for this exomiser instance. Supported assemblies are: %s", genomeAssembly, getProvidedAssemblies()));
    }

    /**
     * Returns the value to which the specified key is mapped, or
     * {@code defaultAnalysisService} if this provider contains no mapping for the key.
     *
     * @param genomeAssembly         the genomeAssembly whose associated value is to be returned
     * @param defaultAnalysisService the default genomeAssembly of the key
     * @return the genomeAnalysisService to which the specified genomeAssembly is mapped, or
     * {@code defaultAnalysisService} if this provider contains no genomeAnalysisService for the genomeAssembly.
     */
    public GenomeAnalysisService getOrDefault(GenomeAssembly genomeAssembly, GenomeAnalysisService defaultAnalysisService) {
        return genomeAnalysisServices.getOrDefault(genomeAssembly, defaultAnalysisService);
    }

    public boolean hasServiceFor(GenomeAssembly genomeAssembly) {
        return genomeAnalysisServices.containsKey(genomeAssembly);
    }

    @Override
    public String toString() {
        return "GenomeAnalysisServiceProvider{" +
                "assemblies=" + genomeAnalysisServices.keySet() +
                '}';
    }
}
