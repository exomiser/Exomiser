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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Sets;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.writers.OutputSettings.Builder;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Class for storing output format options.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonDeserialize(builder = Builder.class)
public class OutputSettings {

    private static final OutputSettings DEFAULTS = OutputSettings.builder().build();

    @JsonProperty
    private final boolean outputContributingVariantsOnly;
    @JsonProperty("numGenes")
    private final int numberOfGenesToShow;
    private final float minExomiserGeneScore;
    private final String outputPrefix;
    private final Set<OutputFormat> outputFormats;

    private OutputSettings(Builder builder) {
        this.outputContributingVariantsOnly = builder.outputContributingVariantsOnly;
        this.numberOfGenesToShow = builder.numberOfGenesToShow;
        this.minExomiserGeneScore = builder.minExomiserGeneScore;
        this.outputPrefix = builder.outputPrefix;
        this.outputFormats = Sets.immutableEnumSet(builder.outputFormats);
    }

    @JsonIgnore
    public static OutputSettings defaults() {
        return DEFAULTS;
    }

    public boolean outputContributingVariantsOnly() {
        return outputContributingVariantsOnly;
    }

    public int getNumberOfGenesToShow() {
        return numberOfGenesToShow;
    }

    public float getMinExomiserGeneScore() {
        return minExomiserGeneScore;
    }

    public Set<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    /**
     * Filters the input genes for those meeting the defined minimum Exomiser gene score and number of genes to return.
     * This method DOES NOT filter the contributing variants and will return all genes irrespective of their PASS/FAIL
     * status.
     *
     * @param genes Input list to filter
     * @return A list of genes meeting the output options criteria.
     * @since 13.0.0
     */
    public List<Gene> filterGenesForOutput(List<Gene> genes) {
        return applyOutputSettings(genes)
                .collect(toUnmodifiableList());
    }

    /**
     * Filters the input genes for those meeting the defined minimum Exomiser gene score and number of genes to return.
     * This method DOES NOT filter the contributing variants and will ONLY return all genes with a PASS status.
     *
     * @param genes Input list to filter
     * @return A list of passed genes also meeting the output options criteria.
     * @since 13.0.0
     */
    public List<Gene> filterPassedGenesForOutput(List<Gene> genes) {
        return applyOutputSettings(genes)
                .filter(Gene::passedFilters)
                .collect(toUnmodifiableList());
    }

    /**
     * Filters the input genes for those meeting the criteria defined in the {@link OutputSettings}.
     * This method DOES NOT filter the contributing variants and will return all genes irrespective of their PASS/FAIL
     * status.
     *
     * @param genes Input list to filter
     * @return A {@link Stream} of genes meeting the output options criteria.
     * @since 13.1.0
     */
    public Stream<Gene> applyOutputSettings(List<Gene> genes) {
        return genes.stream()
                .filter(gene -> gene.getCombinedScore() >= minExomiserGeneScore)
                .filter(withinNumberOfGenesToShow(numberOfGenesToShow));
    }

    private Predicate<Gene> withinNumberOfGenesToShow(int limit) {
        AtomicInteger count = new AtomicInteger(1);
        return gene -> {
            if (limit > 0) {
                return count.getAndIncrement() <= limit;
            }
            return true;
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean outputContributingVariantsOnly = false;
        private int numberOfGenesToShow = 0;
        private float minExomiserGeneScore = 0f;
        private String outputPrefix = "";
        private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML);

        private Builder() {}

        public OutputSettings build() {
            return new OutputSettings(this);
        }

        @JsonSetter
        public Builder outputContributingVariantsOnly(boolean outputContributingVariantsOnly) {
            this.outputContributingVariantsOnly = outputContributingVariantsOnly;
            return this;
        }

        @JsonSetter(value = "numGenes")
        public Builder numberOfGenesToShow(int numberOfGenesToShow) {
            this.numberOfGenesToShow = numberOfGenesToShow;
            return this;
        }

        @JsonSetter
        public Builder minExomiserGeneScore(float minExomiserGeneScore) {
            this.minExomiserGeneScore = minExomiserGeneScore;
            return this;
        }

        @JsonSetter
        public Builder outputPrefix(String outputPrefix) {
            this.outputPrefix = outputPrefix;
            return this;
        }

        @JsonSetter
        public Builder outputFormats(Set<OutputFormat> outputFormats) {
            this.outputFormats = outputFormats;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputSettings that = (OutputSettings) o;
        return outputContributingVariantsOnly == that.outputContributingVariantsOnly && numberOfGenesToShow == that.numberOfGenesToShow && minExomiserGeneScore == that.minExomiserGeneScore && outputPrefix.equals(that.outputPrefix) && outputFormats.equals(that.outputFormats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputContributingVariantsOnly, numberOfGenesToShow, minExomiserGeneScore, outputPrefix, outputFormats);
    }

    @Override
    public String toString() {
        return "OutputSettings{" +
                "outputContributingVariantsOnly=" + outputContributingVariantsOnly +
                ", numberOfGenesToShow=" + numberOfGenesToShow +
                ", minExomiserGeneScore=" + minExomiserGeneScore +
                ", outputPrefix='" + outputPrefix + '\'' +
                ", outputFormats=" + outputFormats +
                '}';
    }
}
