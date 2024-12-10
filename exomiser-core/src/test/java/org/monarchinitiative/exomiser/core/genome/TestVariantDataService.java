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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome;

import jakarta.annotation.Nonnull;
import org.monarchinitiative.exomiser.core.model.GeneStatistics;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.svart.GenomicInterval;
import org.monarchinitiative.svart.GenomicVariant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock of VariantDataService to provide canned responses for variants. Enables
 * testing of the service without requiring any database back-end. This is
 * backed by maps mapping the variants to their respective frequency/pathogenicity/whatever data.
 *
 * @author Jules Jacobsen<jules.jacobsen@sanger.ac.uk>
 */
public class TestVariantDataService implements VariantDataService {

    private static final VariantDataService STUB_SERVICE = new VariantDataServiceStub();

    private final Set<Variant> expectedWhiteList;
    private final Map<Variant, FrequencyData> expectedFrequencyData;
    private final Map<Variant, PathogenicityData> expectedPathogenicityData;
    private final Map<Variant, ClinVarData> expectedClinVarData;
    private final Map<String, GeneStatistics> expectedGeneStats;

    private TestVariantDataService(Builder builder) {
        this.expectedWhiteList = Set.copyOf(builder.expectedWhiteList);
        this.expectedFrequencyData = Map.copyOf(builder.expectedFrequencyData);
        this.expectedPathogenicityData = Map.copyOf(builder.expectedPathogenicityData);
        this.expectedClinVarData = Map.copyOf(builder.expectedClinVarData);
        this.expectedGeneStats = Map.copyOf(builder.expectedGeneStats);
    }

    /**
     * Returns a stub service containing no data.
     *
     * @return a stub service instance
     */
    public static VariantDataService stub() {
        return STUB_SERVICE;
    }

    /**
     * Adds the expected FrequencyData for the given Variant to the
     * VariantDataService.
     *
     * @param variant
     * @param frequencyData
     */
    public void put(Variant variant, FrequencyData frequencyData) {
        expectedFrequencyData.put(variant, frequencyData);
    }

    /**
     * Adds the expected PathogenicityData for the given Variant to the
     * VariantDataService.
     *
     * @param variant
     * @param pathogenicityData
     */
    public void put(Variant variant, PathogenicityData pathogenicityData) {
        expectedPathogenicityData.put(variant, pathogenicityData);
    }

    @Override
    public boolean variantIsWhiteListed(Variant variant) {
        return expectedWhiteList.contains(variant);
    }

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        FrequencyData allFrequencyData = expectedFrequencyData.getOrDefault(variant, FrequencyData.empty());

        List<Frequency> wanted = allFrequencyData.frequencies()
                .stream()
                .filter(frequency -> frequencySources.contains(frequency.source()))
                .toList();

        return FrequencyData.of(allFrequencyData.getRsId(), wanted);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        PathogenicityData pathData = expectedPathogenicityData.getOrDefault(variant, PathogenicityData.empty());

        List<PathogenicityScore> wanted = pathData.pathogenicityScores()
                .stream()
                .filter(pathogenicity -> pathogenicitySources.contains(pathogenicity.getSource()))
                .toList();

        return PathogenicityData.of(pathData.clinVarData(), wanted);
    }

    @Override
    public ClinVarData getClinVarData(@Nonnull Variant variant) {
        return expectedClinVarData.get(variant);
    }

    @Override
    public ClinVarData getClinVarData(@Nonnull GenomicVariant genomicVariant) {
        return expectedClinVarData.get(genomicVariant);
    }

    @Override
    public Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(@Nonnull GenomicInterval genomicInterval) {
        return expectedClinVarData.entrySet().stream()
                .filter(entry -> entry.getKey().overlapsWith(genomicInterval))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public GeneStatistics getGeneStatistics(@Nonnull String geneSymbol) {
        return expectedGeneStats.getOrDefault(geneSymbol, GeneStatistics.builder(geneSymbol).build());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Set<Variant> expectedWhiteList = new HashSet<>();
        private Map<Variant, FrequencyData> expectedFrequencyData = new HashMap<>();
        private Map<Variant, PathogenicityData> expectedPathogenicityData = new HashMap<>();
        private Map<Variant, ClinVarData> expectedClinVarData = new HashMap<>();
        private Map<String, GeneStatistics> expectedGeneStats = new HashMap<>();

        public Builder expectedWhiteList(Set<Variant> expectedWhiteList) {
            this.expectedWhiteList = expectedWhiteList;
            return this;
        }

        public Builder addToWhitelist(Variant... variants) {
            this.expectedWhiteList.addAll(Arrays.asList(variants));
            return this;
        }

        public Builder expectedFrequencyData(Map<Variant, FrequencyData> expectedFrequencyData) {
            this.expectedFrequencyData = expectedFrequencyData;
            return this;
        }

        public Builder put(Variant variant, FrequencyData expectedFrequencyData) {
            this.expectedFrequencyData.put(variant, expectedFrequencyData);
            return this;
        }

        public Builder expectedPathogenicityData(Map<Variant, PathogenicityData> expectedPathogenicityData) {
            this.expectedPathogenicityData = expectedPathogenicityData;
            return this;
        }

        public Builder put(Variant variant, PathogenicityData expectedPathogenicityData) {
            this.expectedPathogenicityData.put(variant, expectedPathogenicityData);
            return this;
        }

        public Builder expectedClinVarData(Map<Variant, ClinVarData> expectedClinVarData) {
            this.expectedClinVarData = expectedClinVarData;
            return this;
        }

        public Builder put(Variant variant, ClinVarData clinVarData) {
            this.expectedClinVarData.put(variant, clinVarData);
            return this;
        }

        public Builder expectedGeneStats(Map<String, GeneStatistics> expectedGeneStats) {
            this.expectedGeneStats = expectedGeneStats;
            return this;
        }

        public Builder put(GeneStatistics geneStatistics) {
            this.expectedGeneStats.put(geneStatistics.geneSymbol(), geneStatistics);
            return this;
        }

        public TestVariantDataService build() {
            return new TestVariantDataService(this);
        }
    }


    private static class VariantDataServiceStub implements VariantDataService {

        @Override
        public boolean variantIsWhiteListed(Variant variant) {
            return false;
        }

        @Override
        public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
            return FrequencyData.empty();
        }

        @Override
        public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
            return PathogenicityData.empty();
        }

        @Override
        public ClinVarData getClinVarData(Variant variant) {
            return ClinVarData.empty();
        }

        @Override
        public ClinVarData getClinVarData(GenomicVariant genomicVariant) {
            return ClinVarData.empty();
        }

        @Override
        public Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(GenomicInterval genomicInterval) {
            return Map.of();
        }

        @Override
        public GeneStatistics getGeneStatistics(@Nonnull String geneSymbol) {
            return GeneStatistics.builder(geneSymbol).build();
        }
    }

}
