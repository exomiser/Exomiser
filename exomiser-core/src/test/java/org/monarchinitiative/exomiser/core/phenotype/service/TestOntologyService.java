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

package org.monarchinitiative.exomiser.core.phenotype.service;

import com.google.common.collect.ImmutableSet;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test helper to provide real, canned data for a small set of
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestOntologyService implements OntologyService {


    private final Map<String, PhenotypeTerm> hpIdPhenotypeTerms;
    private final Map<String, String> obsoleteToCurrentTerms;

    private final Map<PhenotypeTerm, List<PhenotypeMatch>> humanHumanMappings;
    private final Map<PhenotypeTerm, List<PhenotypeMatch>> humanMouseMappings;
    private final Map<PhenotypeTerm, List<PhenotypeMatch>> humanFishMappings;

    private TestOntologyService(Builder builder) {
        this.hpIdPhenotypeTerms = builder.hpIdPhenotypeTerms;
        this.humanHumanMappings = builder.humanHumanMappings;
        this.humanMouseMappings = builder.humanMouseMappings;
        this.humanFishMappings = builder.humanFishMappings;
        this.obsoleteToCurrentTerms = builder.obsoleteToCurrentTerms;
    }

    @Override
    public Set<PhenotypeTerm> getHpoTerms() {
        return Set.copyOf(hpIdPhenotypeTerms.values());
    }

    @Override
    public Set<PhenotypeTerm> getMpoTerms() {
        return Collections.emptySet();
    }

    @Override
    public Set<PhenotypeTerm> getZpoTerms() {
        return Collections.emptySet();
    }

    @Override
    public Set<PhenotypeMatch> getHpoMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        List<PhenotypeMatch> matches = humanHumanMappings.get(hpoTerm);
        return (matches == null) ? Collections.emptySet() : Set.copyOf(matches);
    }

    @Override
    public Set<PhenotypeMatch> getMpoMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        List<PhenotypeMatch> matches = humanMouseMappings.get(hpoTerm);
        return (matches == null) ? Collections.emptySet() : Set.copyOf(matches);
    }

    @Override
    public Set<PhenotypeMatch> getZpoMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        List<PhenotypeMatch> matches = humanFishMappings.get(hpoTerm);
        return (matches == null) ? Collections.emptySet() : Set.copyOf(matches);
    }

    @Override
    public PhenotypeTerm getPhenotypeTermForHpoId(String hpoId) {
        return hpIdPhenotypeTerms.getOrDefault(hpoId, PhenotypeTerm.of(hpoId, "Not set"));
    }

    @Override
    public List<String> getCurrentHpoIds(List<String> hpoIds) {
        return hpoIds.stream()
                .map(hpoId -> obsoleteToCurrentTerms.getOrDefault(hpoId, hpoId))
                .distinct()
                .toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<String, PhenotypeTerm> hpIdPhenotypeTerms = Collections.emptyMap();
        private Map<String, String> obsoleteToCurrentTerms = Collections.emptyMap();

        private Map<PhenotypeTerm, List<PhenotypeMatch>> humanHumanMappings = Collections.emptyMap();
        private Map<PhenotypeTerm, List<PhenotypeMatch>> humanMouseMappings = Collections.emptyMap();
        private Map<PhenotypeTerm, List<PhenotypeMatch>> humanFishMappings = Collections.emptyMap();


        private Builder() {
        }

        public Builder setHpIdPhenotypeTerms(Map<String, PhenotypeTerm> hpIdPhenotypeTerms) {
            this.hpIdPhenotypeTerms = hpIdPhenotypeTerms;
            return this;
        }

        public Builder setHumanHumanMappings(Map<PhenotypeTerm, List<PhenotypeMatch>> humanHumanMappings) {
            this.humanHumanMappings = humanHumanMappings;
            return this;
        }

        public Builder setHumanMouseMappings(Map<PhenotypeTerm, List<PhenotypeMatch>> humanMouseMappings) {
            this.humanMouseMappings = humanMouseMappings;
            return this;
        }

        public Builder setHumanFishMappings(Map<PhenotypeTerm, List<PhenotypeMatch>> humanFishMappings) {
            this.humanFishMappings = humanFishMappings;
            return this;
        }

        public Builder setObsoleteToCurrentTerms(Map<String, String> obsoleteToCurrentTerms) {
            this.obsoleteToCurrentTerms = obsoleteToCurrentTerms;
            return this;
        }

        public TestOntologyService build() {
            return new TestOntologyService(this);
        }
    }
}
