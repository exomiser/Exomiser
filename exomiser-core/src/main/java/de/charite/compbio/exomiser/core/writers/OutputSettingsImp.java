/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Simple bean for storing output format options.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@JsonDeserialize(builder = OutputSettingsBuilder.class)
public class OutputSettingsImp implements OutputSettings {

    @JsonProperty
    private final boolean outputPassVariantsOnly;
    @JsonProperty("numGenes")
    private final int numberOfGenesToShow;
    private final String outputPrefix;
    private final Set<OutputFormat> outputFormats;

    private OutputSettingsImp(OutputSettingsBuilder builder) {
        this.outputPassVariantsOnly = builder.outputPassVariantsOnly;
        this.numberOfGenesToShow = builder.numberOfGenesToShow;
        this.outputPrefix = builder.outputPrefix;
        this.outputFormats = builder.outputFormats;
    }
    
    public static class OutputSettingsBuilder {

        private boolean outputPassVariantsOnly = false;
        private int numberOfGenesToShow = 0;
        private String outputPrefix = "";
        private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML);

        public OutputSettingsImp build() {
            return new OutputSettingsImp(this);
        }
        
        @JsonSetter
        public OutputSettingsBuilder outputPassVariantsOnly(boolean outputPassVariantsOnly) {
            this.outputPassVariantsOnly = outputPassVariantsOnly;
            return this;
        }

        @JsonSetter(value = "numGenes")
        public OutputSettingsBuilder numberOfGenesToShow(int numberOfGenesToShow) {
            this.numberOfGenesToShow = numberOfGenesToShow;
            return this;
        }

        @JsonSetter
        public OutputSettingsBuilder outputPrefix(String outputPrefix) {
            this.outputPrefix = outputPrefix;
            return this;
        }

        @JsonSetter
        public OutputSettingsBuilder outputFormats(Set<OutputFormat> outputFormats) {
            this.outputFormats = outputFormats;
            return this;
        }
    }

    @Override
    public boolean outputPassVariantsOnly() {
        return outputPassVariantsOnly;
    }

    @Override
    public int getNumberOfGenesToShow() {
        return numberOfGenesToShow;
    }

    @Override
    public Set<OutputFormat> getOutputFormats() {
        return outputFormats;
    }

    @Override
    public String getOutputPrefix() {
        return outputPrefix;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.outputPassVariantsOnly ? 1 : 0);
        hash = 79 * hash + this.numberOfGenesToShow;
        hash = 79 * hash + Objects.hashCode(this.outputPrefix);
        hash = 79 * hash + Objects.hashCode(this.outputFormats);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OutputSettingsImp other = (OutputSettingsImp) obj;
        if (this.outputPassVariantsOnly != other.outputPassVariantsOnly) {
            return false;
        }
        if (this.numberOfGenesToShow != other.numberOfGenesToShow) {
            return false;
        }
        if (!Objects.equals(this.outputPrefix, other.outputPrefix)) {
            return false;
        }
        if (!Objects.equals(this.outputFormats, other.outputFormats)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "OutputOptions{" + "outputPassVariantsOnly=" + outputPassVariantsOnly + ", numberOfGenesToShow=" + numberOfGenesToShow + ", outputPrefix=" + outputPrefix + ", outputFormats=" + outputFormats + '}';
    }

}
