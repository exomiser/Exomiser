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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.RegulatoryFeature;
import org.monarchinitiative.exomiser.core.model.TopologicalDomain;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestGenomeDataService implements GenomeDataService {

    private final List<Gene> genes;
    private final Set<GeneIdentifier> geneIdentifiers;
    private final List<RegulatoryFeature> expectedRegulatoryRegions;
    private final List<TopologicalDomain> expectedTopologicalDomains;

    private TestGenomeDataService(Builder builder) {
        this.genes = ImmutableList.copyOf(builder.genes);
        this.geneIdentifiers = ImmutableSet.copyOf(builder.geneIdentifiers);
        this.expectedRegulatoryRegions = ImmutableList.copyOf(builder.expectedRegulatoryRegions);
        this.expectedTopologicalDomains = ImmutableList.copyOf(builder.expectedTopologicalDomains);
    }

    @Override
    public List<Gene> getKnownGenes() {
        return genes;
    }

    @Override
    public Set<GeneIdentifier> getKnownGeneIdentifiers() {
        return geneIdentifiers;
    }

    @Override
    public List<RegulatoryFeature> getRegulatoryFeatures() {
        return expectedRegulatoryRegions;
    }

    @Override
    public List<TopologicalDomain> getTopologicallyAssociatedDomains() {
        return expectedTopologicalDomains;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Gene> genes = new ArrayList<>();
        private Collection<GeneIdentifier> geneIdentifiers = new TreeSet<>();
        private List<RegulatoryFeature> expectedRegulatoryRegions = new ArrayList<>();
        private List<TopologicalDomain> expectedTopologicalDomains = new ArrayList<>();

        public Builder genes(List<Gene> genes) {
            this.genes = genes;
            return this;
        }

        public Builder geneIdentifiers(Collection<GeneIdentifier> geneIdentifiers) {
            this.geneIdentifiers = geneIdentifiers;
            return this;
        }

        public Builder expectedRegulatoryRegions(List<RegulatoryFeature> expectedRegulatoryRegions) {
            this.expectedRegulatoryRegions = expectedRegulatoryRegions;
            return this;
        }

        public Builder expectedTopologicalDomains(List<TopologicalDomain> expectedTopologicalDomains) {
            this.expectedTopologicalDomains = expectedTopologicalDomains;
            return this;
        }

        public TestGenomeDataService build() {
            return new TestGenomeDataService(this);
        }
    }

}
