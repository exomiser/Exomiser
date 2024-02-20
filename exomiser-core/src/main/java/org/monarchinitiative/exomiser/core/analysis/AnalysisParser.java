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

package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.analysis.util.PedFiles;
import org.monarchinitiative.exomiser.core.genome.BedFiles;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;
import org.monarchinitiative.exomiser.core.writers.OutputSettingsProtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

import static java.nio.file.Files.newInputStream;

/**
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class AnalysisParser {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisParser.class);

    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider;
    private final PriorityFactory prioritiserFactory;
    private final OntologyService ontologyService;

    @Autowired
    public AnalysisParser(GenomeAnalysisServiceProvider genomeAnalysisServiceProvider, PriorityFactory prioritiserFactory, OntologyService ontologyService) {
        this.genomeAnalysisServiceProvider = genomeAnalysisServiceProvider;
        this.prioritiserFactory = prioritiserFactory;
        this.ontologyService = ontologyService;
    }

    public Sample parseSample(Path analysisScript) {
        JobProto.Job job = JobReader.readJob(analysisScript);
        return parseSample(job);
    }

    public Sample parseSample(String analysisDoc) {
        JobProto.Job job = JobReader.readJob(analysisDoc);
        return parseSample(job);
    }

    public Sample parseSample(JobProto.Job job) {
        JobParser jobParser = new JobParser(genomeAnalysisServiceProvider, prioritiserFactory, ontologyService);
        return jobParser.parseSample(job);
    }

    public Analysis parseAnalysis(Path analysisScript) {
        JobProto.Job job = JobReader.readJob(analysisScript);
        return parseAnalysis(job);
    }

    public Analysis parseAnalysis(String analysisDoc) {
        JobProto.Job job = JobReader.readJob(analysisDoc);
        return parseAnalysis(job);
    }

    public Analysis parseAnalysis(JobProto.Job job) {
        JobParser jobParser = new JobParser(genomeAnalysisServiceProvider, prioritiserFactory, ontologyService);
        return jobParser.parseAnalysis(job);
    }

    public OutputSettings parseOutputSettings(Path analysisScript) {
        JobProto.Job job = JobReader.readJob(analysisScript);
        return parseOutputSettings(job);
    }

    public OutputSettings parseOutputSettings(String analysisDoc) {
        JobProto.Job job = JobReader.readJob(analysisDoc);
        return parseOutputSettings(job);
    }

    public OutputSettings parseOutputSettings(JobProto.Job job) {
        return new OutputSettingsProtoConverter().toDomain(job.getOutputOptions());
    }

    private Map loadMap(String analysisDoc) {
        Yaml yaml = new Yaml();
        return yaml.load(analysisDoc);
    }

    private Map loadMap(Path analysisScript) {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = newInputStream(analysisScript)) {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, decoder));
            return yaml.load(bufferedReader);
        } catch (IOException ex) {
            throw new AnalysisFileNotFoundException("Unable to find analysis file: " + ex.getMessage(), ex);
        }
    }

    private Analysis constructAnalysisFromMap(Map settingsMap) {
        AnalysisConstructor analysisConstructor = new AnalysisConstructor();
        return analysisConstructor.construct((Map) settingsMap.get("analysis"));
    }

    private OutputSettings constructOutputSettingsFromMap(Map settingsMap) {
        OutputSettingsConstructor outputSettingsConstructor = new OutputSettingsConstructor();
        return outputSettingsConstructor.construct((Map) settingsMap.get("outputOptions"));
    }

    private Sample constructSampleFromMap(Map settingsMap) {
        SampleConstructor sampleConstructor = new SampleConstructor();
        if (settingsMap.containsKey("sample")) {
            return sampleConstructor.construct((Map) settingsMap.get("sample"));
        }
        return sampleConstructor.construct((Map) settingsMap.get("analysis"));
    }

    protected static class AnalysisFileNotFoundException extends RuntimeException {
        AnalysisFileNotFoundException(String message, Exception e) {
            super(message, e);
        }
    }

    private static class SampleConstructor {

        private Sample construct(Map analysisMap) {
            Sample sample = Sample.builder()
                    .vcfPath(parseVcf(analysisMap))
                    .genomeAssembly(parseGenomeAssembly(analysisMap))
                    .pedigree(parsePed(analysisMap))
                    .probandSampleName(parseProbandSampleName(analysisMap))
                    .hpoIds(parseHpoIds(analysisMap))
                    .build();
            logger.info("parsed sample {}", sample);
            return sample;
        }


        Path parseVcf(Map<String, String> analysisMap) {
            String vcfValue = analysisMap.get("vcf");
            //VCF file paths are not allowed to be null
            if (vcfValue == null) {
                return null;
//                throw new AnalysisParserException("VCF path cannot be null.", analysisMap);
            }
            return Paths.get(vcfValue);
        }

        GenomeAssembly parseGenomeAssembly(Map<String, String> analysisMap) {
            String genomeAssemblyValue = analysisMap.get("genomeAssembly");
            //VCF file paths are not allowed to be null
            if (genomeAssemblyValue == null || genomeAssemblyValue.isEmpty()) {
                logger.info("genomeAssembly not specified - will use default: {}", GenomeAssembly.defaultBuild());
                return GenomeAssembly.defaultBuild();
            }
            return GenomeAssembly.parseAssembly(genomeAssemblyValue);
        }

        Pedigree parsePed(Map<String, String> analysisMap) {
            String pedValue = analysisMap.get("ped");
            //PED file paths are allowed to be null
            if (pedValue == null || pedValue.isEmpty()) {
                return Pedigree.empty();
            }
            Path pedFile = Paths.get(pedValue);
            return PedFiles.readPedigree(pedFile);
        }

        String parseProbandSampleName(Map<String, String> analysisMap) {
            String probandSampleName = analysisMap.get("proband");
            //probandSampleName is allowed to be null, but may throw exceptions when the VCF/PED file is checked
            return Objects.requireNonNullElse(probandSampleName, "");
        }

        List<String> parseHpoIds(Map<String, List<String>> analysisMap) {
            List<String> hpoIds = analysisMap.get("hpoIds");
            return Objects.requireNonNullElseGet(hpoIds, ArrayList::new);
        }
    }

    // TODO should these be split out into a parsers sub-package along with the JobParser and
    private static class OutputSettingsConstructor {

        public OutputSettings construct(Map analysisMap) {
            return OutputSettings.builder()
                    .outputContributingVariantsOnly(parseOutputVariantsOption(analysisMap))
                    .numberOfGenesToShow(parseNumberOfGenesToShow(analysisMap))
                    .minExomiserGeneScore(parseMinExomiserScore(analysisMap))
                    .outputPrefix(parseOutputPrefix(analysisMap))
                    .outputFormats(parseOutputFormats(analysisMap))
                    .build();
        }

        private boolean parseOutputVariantsOption(Map<String, Boolean> analysisMap) {
            String deprecatedOption = "outputPassVariantsOnly";
            String outputContributingVariantsOnly = "outputContributingVariantsOnly";
            if (analysisMap.containsKey(deprecatedOption)) {
                logger.warn("{} option has been deprecated - please replace with '{}'", deprecatedOption, outputContributingVariantsOnly);
                //despite being deprecated and functionally different, this has the same return value
                return parseBooleanValue(deprecatedOption, analysisMap);
            }
            if (analysisMap.containsKey(outputContributingVariantsOnly)) {
                return parseBooleanValue(outputContributingVariantsOnly, analysisMap);
            }
            return false;
        }

        private Boolean parseBooleanValue(String key, Map<String, Boolean> analysisMap) {
            Boolean booleanValue = analysisMap.get(key);
            if (booleanValue == null) {
                throw new AnalysisParserException("'" + key + "' value cannot be null.", analysisMap);
            }
            return booleanValue;
        }

        private int parseNumberOfGenesToShow(Map<String, Integer> analysisMap) {
            Integer genesToShow = analysisMap.get("numGenes");
            if (genesToShow == null) {
                genesToShow = 0;
            }
            return genesToShow;
        }

        private float parseMinExomiserScore(Map<String, Float> analysisMap) {
            Float minExomiserGeneScore = analysisMap.get("minExomiserGeneScore");
            if (minExomiserGeneScore == null) {
                minExomiserGeneScore = 0f;
            }
            return minExomiserGeneScore;
        }

        private String parseOutputPrefix(Map<String, String> analysisMap) {
            String outputPrefix = analysisMap.get("outputPrefix");
            if (outputPrefix == null) {
                outputPrefix = "exomiser-results";
            }
            return outputPrefix;
        }

        private Set<OutputFormat> parseOutputFormats(Map<String, List<String>> analysisMap) {
            List<String> givenOutputFormats = analysisMap.getOrDefault("outputFormats", Collections.emptyList());
            if (givenOutputFormats == null || givenOutputFormats.isEmpty()) {
                logger.info("No output format options supplied.");
                return EnumSet.noneOf(OutputFormat.class);
            }
            return givenOutputFormats.stream()
                    .map(OutputFormat::parseFormat)
                    .collect(Sets.toImmutableEnumSet());
        }
    }

    private class AnalysisConstructor {

        public Analysis construct(Map analysisMap) {
            Analysis analysis = constructBuilder(analysisMap).build();
            logger.debug("Made analysis: {}", analysis);
            return analysis;
        }

        public AnalysisBuilder constructBuilder(Map analysisMap) {

            //this method is only here to provide a warning to users that their script is out of date.
            warnUserAboutDeprecatedGeneScoreMode(analysisMap);

            AnalysisBuilder analysisBuilder = new AnalysisBuilder(genomeAnalysisServiceProvider, prioritiserFactory, ontologyService)
                    // these are part of the Sample
//                    .vcfPath(parseVcf(analysisMap))
//                    .genomeAssembly(parseGenomeAssembly(analysisMap))
//                    .pedigree(parsePed(analysisMap))
//                    .probandSampleName(parseProbandSampleName(analysisMap))
//                    .hpoIds(parseHpoIds(analysisMap))

                    .analysisMode(parseAnalysisMode(analysisMap))
                    .inheritanceModes(inheritanceModeOptions(analysisMap))
                    .frequencySources(parseFrequencySources(analysisMap))
                    .pathogenicitySources(parsePathogenicitySources(analysisMap));

            addAnalysisSteps(analysisMap, analysisBuilder);

            return analysisBuilder;
        }

        private InheritanceModeOptions inheritanceModeOptions(Map<String, Object> analysisMap) {
            String modeOfInheritanceInput = (String) analysisMap.get("modeOfInheritance");

            //Pre 10.0.0 version - only expected a single string value
            if (modeOfInheritanceInput != null) {
                logger.info("modeOfInheritance option no longer supported. Please supply a map of inheritanceModes. See examples for details.");
                if (modeOfInheritanceInput.equals("UNDEFINED") || modeOfInheritanceInput.equals("UNINITIALIZED") || modeOfInheritanceInput.equals("ANY")) {
                    return InheritanceModeOptions.empty();
                }
                ModeOfInheritance moi = parseValueOfInheritanceMode(modeOfInheritanceInput);
                InheritanceModeOptions inheritanceModeOptions = InheritanceModeOptions.defaultForModes(moi);
                logger.info("'modeOfInheritance: {}' has been converted to 'inheritanceModes: {{}: {}}'", modeOfInheritanceInput, moi, inheritanceModeOptions.getMaxFreqForMode(moi));
                return inheritanceModeOptions;
            }

            //Version 10.0.0 - expect a map of SubModeOfInheritance
            Map<String, Double> inheritanceModesInput = (Map<String, Double>) analysisMap.get("inheritanceModes");

            if (inheritanceModesInput != null) {
                Map<SubModeOfInheritance, Float> inheritanceModes = new EnumMap<>(SubModeOfInheritance.class);
                for (Entry<String, Double> entry : inheritanceModesInput.entrySet()) {
                    SubModeOfInheritance subMode = parseValueOfSubInheritanceMode(entry.getKey());
                    Double value = entry.getValue();
                    logger.debug("Adding inheritance mode {} max MAF {}", subMode, value);
                    inheritanceModes.put(subMode, value.floatValue());
                }

                return InheritanceModeOptions.of(inheritanceModes);
            }
            return InheritanceModeOptions.empty();
        }

        private SubModeOfInheritance parseValueOfSubInheritanceMode(String value) {
            try {
                return SubModeOfInheritance.valueOf(value);
            } catch (IllegalArgumentException e) {
                List<SubModeOfInheritance> permitted = List.of(SubModeOfInheritance.values());
                throw new AnalysisParserException(String.format("'%s' is not a valid mode of inheritance. Use one of: %s", value, permitted));
            }
        }

        private ModeOfInheritance parseValueOfInheritanceMode(String value) {
            try {
                return ModeOfInheritance.valueOf(value);
            } catch (IllegalArgumentException e) {
                List<ModeOfInheritance> permitted = List.of(ModeOfInheritance.values());
                throw new AnalysisParserException(String.format("'%s' is not a valid mode of inheritance. Use one of: %s", value, permitted));
            }
        }

        private AnalysisMode parseAnalysisMode(Map<String, String> analysisMap) {
            String value = analysisMap.get("analysisMode");
            if (value == null) {
                return AnalysisMode.PASS_ONLY;
            }
            if ("SPARSE".equalsIgnoreCase(value)) {
                logger.warn("Analysis mode 'SPARSE' is no longer supported - defaulting to {}", AnalysisMode.PASS_ONLY);
                return AnalysisMode.PASS_ONLY;
            }
            return AnalysisMode.valueOf(value);
        }

        private void warnUserAboutDeprecatedGeneScoreMode(Map analysisMap) {
            if (analysisMap.containsKey("geneScoreMode")) {
                logger.warn("geneScoreMode is deprecated and will have no effect. " +
                        "Please consider removing this from your analysis script to prevent this message from showing again.");
            }
        }

        private void addAnalysisSteps(Map analysisMap, AnalysisBuilder analysisBuilder) {
            List<Map<String, Map>> steps = (List<Map<String, Map>>) analysisMap.getOrDefault("steps", Collections.emptyList());
            for (Map<String, Map> analysisStepMap : steps) {
                logger.debug("Analysis step: {}", analysisStepMap);
                for (Entry<String, Map> entry : analysisStepMap.entrySet()) {
                    addAnalysisStep(entry, analysisMap, analysisBuilder);
                }
            }
        }

        private AnalysisBuilder addAnalysisStep(Entry<String, Map> entry, Map analysisMap, AnalysisBuilder analysisBuilder) {
            String key = entry.getKey();
            Map analysisStepOptions = entry.getValue();
            switch (key) {
                case "failedVariantFilter":
                    return analysisBuilder.addFailedVariantFilter();
                case "intervalFilter":
                    return makeIntervalFilter(analysisStepOptions, analysisBuilder);
                case "genePanelFilter":
                    return makeGeneSymbolFilter(analysisStepOptions, analysisBuilder);
                case "variantEffectFilter":
                    return makeVariantEffectFilter(analysisStepOptions, analysisBuilder);
                case "qualityFilter":
                    return makeQualityFilter(analysisStepOptions, analysisBuilder);
                case "knownVariantFilter":
                    return makeKnownVariantFilter(analysisStepOptions, parseFrequencySources(analysisMap), analysisBuilder);
                case "frequencyFilter":
                    return makeFrequencyFilter(analysisStepOptions, parseFrequencySources(analysisMap), inheritanceModeOptions(analysisMap), analysisBuilder);
                case "pathogenicityFilter":
                    return makePathogenicityFilter(analysisStepOptions, parsePathogenicitySources(analysisMap), analysisBuilder);
                case "inheritanceFilter":
                    return makeInheritanceFilter(inheritanceModeOptions(analysisMap), analysisBuilder);
                case "priorityScoreFilter":
                    return makePriorityScoreFilter(analysisStepOptions, analysisBuilder);
                case "regulatoryFeatureFilter":
                    return analysisBuilder.addRegulatoryFeatureFilter();
                case "geneBlacklistFilter":
                    return makeGeneBlacklistFilter(analysisStepOptions, analysisBuilder);
                case "omimPrioritiser":
                    return analysisBuilder.addOmimPrioritiser();
                case "hiPhivePrioritiser":
                    return makeHiPhivePrioritiser(analysisStepOptions, analysisBuilder);
                case "phivePrioritiser":
                    return analysisBuilder.addPhivePrioritiser();
                case "phenixPrioritiser":
//                    throw new IllegalArgumentException("phenixPrioritiser is not supported in this release. Please use hiPhivePrioritiser instead.");
                    return analysisBuilder.addPhenixPrioritiser();
                case "exomeWalkerPrioritiser":
                    return makeWalkerPrioritiser(analysisStepOptions, analysisBuilder);
                default:
                    //throw exception here?
                    logger.error("Unsupported exomiser step: {}", key);
                    return analysisBuilder;
            }
        }

        private AnalysisBuilder makeIntervalFilter(Map<String, Object> options, AnalysisBuilder analysisBuilder) {
            List<ChromosomalRegion> chromosomalRegions = parseIntervalFilterOptions(options);
            return analysisBuilder.addIntervalFilter(chromosomalRegions);
        }

        private List<ChromosomalRegion> parseIntervalFilterOptions(Map<String, Object> options){
            if (options.containsKey("interval")) {
                String interval = (String) options.get("interval");
                return List.of(GeneticInterval.parseString(interval));
            }
            if (options.containsKey("intervals")) {
                List<String> intervalStrings = (List<String>) options.get("intervals");
                List<ChromosomalRegion> intervals = new ArrayList<>();
                intervalStrings.forEach(string -> intervals.add(GeneticInterval.parseString(string)));
                return intervals;
            }
            if (options.containsKey("bed")) {
                String bedPath = (String) options.get("bed");
                return BedFiles.readChromosomalRegions(Paths.get(bedPath)).toList();
            }
            throw new AnalysisParserException("Interval filter requires a valid genetic interval e.g. {interval: 'chr10:122892600-122892700'} or bed file path {bed: /data/intervals.bed}", options);
        }

        /**
         * @since 10.1.0
         */
        private AnalysisBuilder makeGeneSymbolFilter(Map<String, List<String>> options, AnalysisBuilder analysisBuilder) {
            Set<String> genesToKeep = parseGeneSymbolFilterOptions(options);
            return analysisBuilder.addGeneIdFilter(genesToKeep);
        }

        private Set<String> parseGeneSymbolFilterOptions(Map<String, List<String>> options) {
            List<String> geneSymbols = options.get("geneSymbols");
            if (geneSymbols == null || geneSymbols.isEmpty()) {
                throw new AnalysisParserException("Gene panel filter requires a list of HGNC gene symbols e.g. {geneSymbols: [FGFR1, FGFR2]}", options);
            }
            return new LinkedHashSet<>(geneSymbols);
        }

        private AnalysisBuilder makeVariantEffectFilter(Map<String, List<String>> options, AnalysisBuilder analysisBuilder) {
            Set<VariantEffect> variantEffects = parseVariantEffectFilterOptions(options);
            return analysisBuilder.addVariantEffectFilter(variantEffects);
        }

        private Set<VariantEffect> parseVariantEffectFilterOptions(Map<String, List<String>> options) {
            List<String> effectsToRemove = options.get("remove");
            if (effectsToRemove == null) {
                throw new AnalysisParserException("VariantEffect filter requires a list of VariantEffects to be removed e.g. {remove: [UPSTREAM_GENE_VARIANT, INTERGENIC_VARIANT, SYNONYMOUS_VARIANT]}", options);
            }
            List<VariantEffect> variantEffects = new ArrayList<>();
            for (String effect : effectsToRemove) {
                try {
                    VariantEffect variantEffect = VariantEffect.valueOf(effect);
                    variantEffects.add(variantEffect);
                } catch (IllegalArgumentException ex) {
                    throw new AnalysisParserException(String.format("Illegal VariantEffect: '%s'.%nPermitted effects are any of: %s.", effect, EnumSet.allOf(VariantEffect.class)), options, ex);
                }
            }
            return EnumSet.copyOf(variantEffects);
        }


        private AnalysisBuilder makeQualityFilter(Map<String, Double> options, AnalysisBuilder analysisBuilder) {
            double quality = parseQualityFilterOptions(options);
            return analysisBuilder.addQualityFilter(quality);
        }

        private double parseQualityFilterOptions(Map<String, Double> options) {
            Double quality = options.get("minQuality");
            if (quality == null) {
                throw new AnalysisParserException("Quality filter requires a floating point value for the minimum PHRED score e.g. {minQuality: 50.0}", options);
            }
            return quality;
        }

        private AnalysisBuilder makeKnownVariantFilter(Map<String, Object> options, Set<FrequencySource> sources, AnalysisBuilder analysisBuilder) {
            //nothing special to do here, apart from wrap the filter with a DataProvider, this is a boolean filter.
            if (sources.isEmpty()) {
                throw new AnalysisParserException("Known variant filter requires a list of frequency sources for the analysis e.g. frequencySources: [THOUSAND_GENOMES, ESP_ALL]", options);
            }
            return analysisBuilder.addKnownVariantFilter();
        }

        private AnalysisBuilder makeFrequencyFilter(Map<String, Object> options, Set<FrequencySource> sources, InheritanceModeOptions inheritanceModeOptions, AnalysisBuilder analysisBuilder) {
            Double maxFreq = getMaxFreq(options, inheritanceModeOptions);
            if (maxFreq == null) {
                //this shouldn't be the case, but to be on the safe side...
                throw new AnalysisParserException("Frequency filter requires a floating point value for the maximum frequency e.g. {maxFrequency: 2.0} if inheritanceModes have not been defined.", options);
            }
            if (sources.isEmpty()) {
                throw new AnalysisParserException("Frequency filter requires a list of frequency sources for the analysis e.g. frequencySources: [THOUSAND_GENOMES, ESP_ALL]", options);
            }
            return analysisBuilder.addFrequencyFilter(maxFreq.floatValue());
        }

        private Double getMaxFreq(Map<String, Object> options, InheritanceModeOptions inheritanceModeOptions) {
            Double maxFreq = (Double) options.get("maxFrequency");
            if (maxFreq == null && inheritanceModeOptions.isEmpty()) {
                throw new AnalysisParserException("Frequency filter requires a floating point value for the maximum frequency e.g. {maxFrequency: 2.0} if inheritanceModes have not been defined.", options);
            }
            if (maxFreq == null && !inheritanceModeOptions.isEmpty()) {
                logger.debug("maxFrequency not defined - using inheritanceModeOptions max frequency.");
                return (double) inheritanceModeOptions.getMaxFreq();
            }
            // maxFreq should not be null at this point
            return maxFreq;
        }

        private Set<FrequencySource> parseFrequencySources(Map<String, List<String>> options) {
            List<String> frequencySources = options.get("frequencySources");
            if (frequencySources == null) {
                return EnumSet.noneOf(FrequencySource.class);
            }
            List<FrequencySource> sources = new ArrayList<>();
            for (String source : frequencySources) {
                try {
                    // legacy long-form ESP names
                    if (source.startsWith("ESP")) {
                        source = switch (source) {
                            case "ESP_AFRICAN_AMERICAN" -> "ESP_AA";
                            case "ESP_EUROPEAN_AMERICAN" -> "ESP_EA";
                            default -> source;
                        };
                    }
                    FrequencySource frequencySource = FrequencySource.valueOf(source);
                    sources.add(frequencySource);
                } catch (IllegalArgumentException ex) {
                    throw new AnalysisParserException(String.format("Illegal FrequencySource: '%s'.%nPermitted sources are any of: %s.", source, EnumSet.allOf(FrequencySource.class)), options, ex);
                }
            }
            if (sources.isEmpty()) {
                return EnumSet.noneOf(FrequencySource.class);
            }
            return EnumSet.copyOf(sources);
        }

        private AnalysisBuilder makePathogenicityFilter(Map<String, Object> options, Set<PathogenicitySource> sources, AnalysisBuilder analysisBuilder) {
            Boolean keepNonPathogenic = (Boolean) options.get("keepNonPathogenic");
            if (keepNonPathogenic == null) {
                throw new AnalysisParserException("Pathogenicity filter requires a boolean value for keepNonPathogenic e.g. {keepNonPathogenic: false}", options);
            }
            if (sources.isEmpty()) {
                throw new AnalysisParserException("Pathogenicity filter requires a list of pathogenicity sources for the analysis e.g. {pathogenicitySources: [SIFT, POLYPHEN, MUTATION_TASTER]}", options);
            }
            return analysisBuilder.addPathogenicityFilter(keepNonPathogenic);
        }

        private Set<PathogenicitySource> parsePathogenicitySources(Map<String, List<String>> options) {
            List<String> pathogenicitySources = options.get("pathogenicitySources");
            if (pathogenicitySources == null) {
                return EnumSet.noneOf(PathogenicitySource.class);
            }
            List<PathogenicitySource> sources = new ArrayList<>();
            for (String source : pathogenicitySources) {
                try {
                    PathogenicitySource pathogenicitySource = PathogenicitySource.valueOf(source);
                    sources.add(pathogenicitySource);
                } catch (IllegalArgumentException ex) {
                    throw new AnalysisParserException(String.format("Illegal PathogenicitySource: '%s'.%nPermitted sources are any of: %s.", source, EnumSet.allOf(PathogenicitySource.class)), options, ex);
                }
            }
            if (sources.isEmpty()) {
                return EnumSet.noneOf(PathogenicitySource.class);
            }
            return EnumSet.copyOf(sources);
        }

        private AnalysisBuilder makePriorityScoreFilter(Map<String, Object> options, AnalysisBuilder analysisBuilder) {
            String priorityTypeString = (String) options.get("priorityType");
            if (priorityTypeString == null) {
                throw new AnalysisParserException("Priority score filter requires a string value for the prioritiser type e.g. {priorityType: HIPHIVE_PRIORITY}", options);
            }
            PriorityType priorityType = PriorityType.valueOf(priorityTypeString);

            Double minPriorityScore = (Double) options.get("minPriorityScore");
            if (minPriorityScore == null) {
                throw new AnalysisParserException("Priority score filter requires a floating point value for the minimum prioritiser score e.g. {minPriorityScore: 0.65}", options);
            }
            return analysisBuilder.addPriorityScoreFilter(priorityType, minPriorityScore.floatValue());
        }

        private AnalysisBuilder makeInheritanceFilter(InheritanceModeOptions inheritanceModeOptions, AnalysisBuilder analysisBuilder) {
            if (inheritanceModeOptions.isEmpty()) {
                logger.info("Not making an inheritance filter for undefined mode of inheritance");
                return analysisBuilder;
            }
            return analysisBuilder.addInheritanceFilter();
        }

        private AnalysisBuilder makeGeneBlacklistFilter(Map<String, String> options, AnalysisBuilder analysisBuilder) {
            return analysisBuilder.addGeneBlacklistFilter();
        }

        private AnalysisBuilder makeHiPhivePrioritiser(Map<String, String> options, AnalysisBuilder analysisBuilder) {
            HiPhiveOptions hiPhiveOptions = makeHiPhiveOptions(options);
            logger.debug("Made {}", hiPhiveOptions);
            return analysisBuilder.addHiPhivePrioritiser(hiPhiveOptions);
        }

        private HiPhiveOptions makeHiPhiveOptions(Map<String, String> options) {
            if (!options.isEmpty()) {
                String diseaseId = options.getOrDefault("diseaseId", "");
                String candidateGeneSymbol = options.getOrDefault("candidateGeneSymbol", "");
                String runParams = options.getOrDefault("runParams", "");

                return HiPhiveOptions.builder()
                        .diseaseId(diseaseId)
                        .candidateGeneSymbol(candidateGeneSymbol)
                        .runParams(runParams)
                        .build();
            }
            return HiPhiveOptions.defaults();
        }

        private AnalysisBuilder makeWalkerPrioritiser(Map<String, List<Integer>> options, AnalysisBuilder analysisBuilder) {
            List<Integer> geneIds = options.get("seedGeneIds");
            if (geneIds == null || geneIds.isEmpty()) {
                throw new AnalysisParserException("ExomeWalker prioritiser requires a list of ENTREZ geneIds e.g. {seedGeneIds: [11111, 22222, 33333]}", options);
            }
            return analysisBuilder.addExomeWalkerPrioritiser(geneIds);
        }

    }

    protected static class AnalysisParserException extends RuntimeException {

        AnalysisParserException(String message) {
            super(message);
        }

        AnalysisParserException(String message, Map options) {
            super(message + " was " + new Yaml().dump(options));
        }

        AnalysisParserException(String message, Map options, Exception e) {
            super(message + " was " + new Yaml().dump(options), e);
        }
    }

}
