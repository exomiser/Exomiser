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
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.writers.OutputSettings.Builder;

import java.nio.file.Path;
import java.util.*;
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

    public static final Path DEFAULT_OUTPUT_DIR = Path.of("results");

    private static final OutputSettings DEFAULTS = OutputSettings.builder().build();

    @JsonProperty
    private final boolean outputContributingVariantsOnly;
    @JsonProperty("numGenes")
    private final int numberOfGenesToShow;
    private final float minExomiserGeneScore;
    private final Path outputDirectory;
    private final String outputFileName;
    private final Set<OutputFormat> outputFormats;

    private OutputSettings(Builder builder) {
        this.outputContributingVariantsOnly = builder.outputContributingVariantsOnly;
        this.numberOfGenesToShow = builder.numberOfGenesToShow;
        this.minExomiserGeneScore = builder.minExomiserGeneScore;
        this.outputDirectory = builder.outputDirectory.normalize();
        this.outputFileName = builder.outputFileName;
        this.outputFormats = Collections.unmodifiableSet(EnumSet.copyOf(builder.outputFormats));
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

    /**
     *
     * @deprecated Use {@link OutputSettings#makeOutputFilePath(Path, OutputFormat)} as a replacement.
     * @return a String
     */
    @Deprecated(forRemoval = true)
    @JsonIgnore
    public String getOutputPrefix() {
        return outputFileName.isEmpty() ? outDirString(outputDirectory) : outputDirectory.resolve(outputFileName).toString();
    }

    private static String outDirString(Path outDir) {
        String separator = outDir.getFileSystem().getSeparator();
        String outDirString = outDir.toString();
        // e.g. don't return '//', otherwise return a path ending in the file separator
        return outDirString.equals(separator) ? outDirString : outDirString + separator;
    }

    /**
     * Returns a Path to the directory where the output files are to be written.
     *
     * @return a Path to the directory where the output files are to be written.
     * @since 13.2.0
     */
    public Path getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns the output file name prefix to be used for the output files.
     *
     * @return a name for the output files.
     * @since 13.2.0
     */
    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * <p>
     * Determines the correct file extension for a file given that was specified by the user, or a sensible default if
     * not. Where the filename has not been specified the input VCF filename will be used as a base and the .vcf
     * (or .vcf.gz) extension will be replaced by the pattern '-exomiser.{outputFormat#fileExtension}'.
     * </p>
     * <p>
     * For example, in the case of no user-specified output filename a VCF file named <pre>'sample-1.vcf.gz'</pre> will be
     * returned as <pre>'sample-1-exomiser.variants.tsv'</pre> when combined with the {@link OutputFormat#TSV_VARIANT} input.
     *</p>
     * <p>The output directory will be as specified by the value of {@link OutputSettings#outputDirectory}</p>
     *
     * @param vcfPath      Path to the input VCF file
     * @param outputFormat The desired output format.
     * @return A file path based on the {@link OutputSettings#outputDirectory} and {@link OutputSettings#outputFileName},
     * or one generated from the sample VCF name with an appropriate file extension for the output format.
     * @since 13.2.0
     */
    public Path makeOutputFilePath(Path vcfPath, OutputFormat outputFormat) {
        String baseFileName = outputFileName;
        if (baseFileName.isEmpty() && vcfPath == null) {
            baseFileName = "exomiser";
        } else if (baseFileName.isEmpty()) {
            String vcfFileName = vcfPath.getFileName().toString().replace(".vcf", "").replace(".gz", "");
            baseFileName = vcfFileName + "-exomiser";
        }
        return outputDirectory.resolve(baseFileName + '.' + outputFormat.getFileExtension());
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
        return applyOutputSettings(genes).toList();
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
                .toList();
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

        private static final String FILE_SEPERATOR = System.getProperty("file.separator");


        private boolean outputContributingVariantsOnly = false;
        private int numberOfGenesToShow = 0;
        private float minExomiserGeneScore = 0f;
        private Path outputDirectory = DEFAULT_OUTPUT_DIR;
        private String outputFileName = "";
        private Set<OutputFormat> outputFormats = EnumSet.of(OutputFormat.HTML, OutputFormat.JSON);

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

        /**
         *
         * @deprecated  Use {@link OutputSettings.Builder#outputDirectory(Path)} and/or
         *              {@link OutputSettings.Builder#outputFileName(String)} instead.
         * @param outputPrefix  A string representing the absolute or relative directory or filename path for the output files.
         * @return this builder instance with the outputDirectory and outputFileName parsed from the outputPrefix
         */
        @JsonSetter
        @Deprecated(forRemoval = true)
        public Builder outputPrefix(String outputPrefix) {
            String localOutputPrefix = Objects.requireNonNullElse(outputPrefix, "");
            this.outputDirectory = resolveOutputDir(localOutputPrefix);
            this.outputFileName = resolveBaseFileName(localOutputPrefix);
            return this;
        }

        private Path resolveOutputDir(String outputPrefix) {
            if (outputPrefix.isEmpty()) {
                return this.outputDirectory;
            }
            Path outputPrefixPath = Path.of(outputPrefix);
            if (outputPrefix.endsWith("..") || outputPrefix.endsWith(".")) {
                return outputPrefixPath.normalize();
            }
            if (outputPrefix.endsWith(FILE_SEPERATOR)) {
                return outputPrefixPath;
            }
            return Objects.requireNonNullElse(outputPrefixPath.getParent(), Path.of(""));
        }

        private String resolveBaseFileName(String outputPrefix) {
            if (outputPrefix.endsWith(FILE_SEPERATOR) || outputPrefix.endsWith(".") || outputPrefix.endsWith("..")) {
                return "";
            }
            return Path.of(outputPrefix).getFileName().toString();
        }


        /**
         * Sets the output directory to be used for the analysis results. Will provide a default if not set.
         *
         * @param outputDirectory   A path to the desired output directory
         * @return this builder instance with the outputDirectory set
         * @since 13.2.0
         */
        @JsonSetter
        public Builder outputDirectory(Path outputDirectory) {
            this.outputDirectory = Objects.requireNonNullElse(outputDirectory, this.outputDirectory);
            return this;
        }

        /**
         * Sets the output file name to be used for the analysis results.
         *
         * @param outputFileName    A string to be used for the output filename
         * @return this builder instance with the outputFileName set
         * @since 13.2.0
         */
        @JsonSetter
        public Builder outputFileName(String outputFileName) {
            this.outputFileName = Objects.requireNonNullElse(outputFileName, this.outputFileName);
            return this;
        }

        @JsonSetter
        public Builder outputFormats(Set<OutputFormat> outputFormats) {
            this.outputFormats = outputFormats.isEmpty() ? EnumSet.noneOf(OutputFormat.class) : EnumSet.copyOf(outputFormats);
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputSettings that = (OutputSettings) o;
        return outputContributingVariantsOnly == that.outputContributingVariantsOnly && numberOfGenesToShow == that.numberOfGenesToShow && Float.compare(that.minExomiserGeneScore, minExomiserGeneScore) == 0 && outputDirectory.equals(that.outputDirectory) && outputFileName.equals(that.outputFileName) && outputFormats.equals(that.outputFormats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outputContributingVariantsOnly, numberOfGenesToShow, minExomiserGeneScore, outputDirectory, outputFileName, outputFormats);
    }

    @Override
    public String toString() {
        return "OutputSettings{" +
                "outputContributingVariantsOnly=" + outputContributingVariantsOnly +
                ", numberOfGenesToShow=" + numberOfGenesToShow +
                ", minExomiserGeneScore=" + minExomiserGeneScore +
                ", outputDirectory=" + outputDirectory +
                ", outputFileName='" + outputFileName + '\'' +
                ", outputFormats=" + outputFormats +
                '}';
    }
}
