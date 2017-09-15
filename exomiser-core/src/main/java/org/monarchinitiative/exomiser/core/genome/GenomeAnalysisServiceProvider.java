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

    private final GenomeAnalysisService defaultAssemblyAnalysisService;
    private final GenomeAssembly defaultAssembly;
    private final Map<GenomeAssembly, GenomeAnalysisService> alternateAssemblyServices;

    public GenomeAnalysisServiceProvider(GenomeAnalysisService defaultGenomeAnalysisService) {
        this(defaultGenomeAnalysisService, Collections.emptySet());
    }

    public GenomeAnalysisServiceProvider(GenomeAnalysisService defaultGenomeAnalysisService, Set<GenomeAnalysisService> alternateGenomeAnalysisServices) {
        Objects.requireNonNull(defaultGenomeAnalysisService, "default GenomeAnalysisService cannot be null.");
        Objects.requireNonNull(alternateGenomeAnalysisServices, "alternate GenomeAnalysisServices cannot be null.");
        this.defaultAssemblyAnalysisService = defaultGenomeAnalysisService;
        this.defaultAssembly = defaultGenomeAnalysisService.getGenomeAssembly();

        this.alternateAssemblyServices = new EnumMap<>(GenomeAssembly.class);
        alternateGenomeAnalysisServices.forEach(this::addService);
    }

    private void addService(GenomeAnalysisService genomeAnalysisService) {
        GenomeAssembly genomeAssembly = genomeAnalysisService.getGenomeAssembly();
        if (genomeAssembly == defaultAssembly) {
            String message = String.format("%s cannot be provided as an alternative assembly as this is already defined as the default.", genomeAssembly);
            throw new IllegalArgumentException(message);
        }
        alternateAssemblyServices.put(genomeAssembly, genomeAnalysisService);
    }

    public GenomeAssembly getDefaultGenomeAssembly() {
        return defaultAssembly;
    }

    public Set<GenomeAssembly> getProvidedAssemblies() {
        Set<GenomeAssembly> providedAssemblies = new HashSet<>(alternateAssemblyServices.keySet());
        providedAssemblies.add(defaultAssembly);
        return Sets.immutableEnumSet(providedAssemblies);
    }

    public GenomeAnalysisService getDefaultAssemblyAnalysisService() {
        return defaultAssemblyAnalysisService;
    }

    /**
     * @param genomeAssembly
     * @return A {@link GenomeAnalysisService} instance for the supplied {@link GenomeAssembly}. Will throw an
     * {@link UnsupportedGenomeAssemblyException} if the specified {@link GenomeAssembly} is not available. For cases where
     * an unspecified default is acceptable use the {@link #getOrDefault(GenomeAssembly)} or {@link #getDefaultGenomeAssembly()}
     * methods instead.
     */
    public GenomeAnalysisService get(GenomeAssembly genomeAssembly) {
        if (hasServiceFor(genomeAssembly)) {
            return genomeAssembly == defaultAssembly ? defaultAssemblyAnalysisService : alternateAssemblyServices.get(genomeAssembly);
        }
        throw new UnsupportedGenomeAssemblyException(String.format("Genome assembly %s is not configured for this exomiser instance. Supported assemblies are: %s", genomeAssembly, getProvidedAssemblies()));
    }

    public GenomeAnalysisService getOrDefault(GenomeAssembly genomeAssembly) {
        return alternateAssemblyServices.getOrDefault(genomeAssembly, defaultAssemblyAnalysisService);
    }

    public boolean hasServiceFor(GenomeAssembly genomeAssembly) {
        return (defaultAssembly == genomeAssembly) || alternateAssemblyServices.containsKey(genomeAssembly);
    }

    @Override
    public String toString() {
        return "GenomeAnalysisServiceProvider{" +
                "defaultAssembly=" + defaultAssemblyAnalysisService.getGenomeAssembly() +
                " alternateAssemblies=" + alternateAssemblyServices.keySet() +
                '}';
    }
}
