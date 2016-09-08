/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.analysis.AnalysisMode;
import de.charite.compbio.exomiser.core.analysis.Settings;
import de.charite.compbio.exomiser.core.analysis.SettingsParser;
import de.charite.compbio.exomiser.core.filters.FilterReport;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.exomiser.core.writers.*;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Controller
public class SubmitJobController {

    private static final Logger logger = LoggerFactory.getLogger(SubmitJobController.class);

    @Autowired
    private Integer maxVariants;
    @Autowired
    private Integer maxGenes;

    @Autowired
    private Exomiser exomiser;
    @Autowired
    private SettingsParser settingsParser;
    @Autowired
    private PriorityService priorityService;
    @Autowired
    private ResultsWriterFactory resultsWriterFactory;

    @RequestMapping(value = "submit", method = RequestMethod.GET)
    public String configureExomiserJob(Model model) {
        return "submit";
    }

    @RequestMapping(value = "submit", method = RequestMethod.POST)
    public String submit(
            @RequestParam(value = "vcf") MultipartFile vcfFile,
            @RequestParam(value = "ped", required = false) MultipartFile pedFile,
            @RequestParam(value = "disease", required = false) String diseaseId,
            @RequestParam(value = "phenotypes", required = false) List<String> phenotypes,
            @RequestParam(value = "min-call-quality", required = false) Float minimumQuality,
            @RequestParam(value = "genetic-interval", required = false) String geneticInterval,
            @RequestParam("frequency") String frequency,
            @RequestParam("remove-dbsnp") Boolean removeDbSnp,
            @RequestParam("keep-non-pathogenic") Boolean keepNonPathogenic,
            @RequestParam("keep-off-target") Boolean keepOffTarget,
            @RequestParam("inheritance") String modeOfInheritance,
            @RequestParam(value = "genes-to-keep", required = false) List<String> genesToFilter,
            @RequestParam("prioritiser") String prioritiser,
            HttpSession session,
            Model model) {

        logger.info("Session id: {}", session.getId());
        Path vcfPath = createVcfPathFromMultipartFile(vcfFile);
        Path pedPath = createPedPathFromMultipartFile(pedFile);
        //require a mimimum input of a VCF file and a set of HPO terms - these can come from the diseaseId
        if (vcfPath == null) {
            logger.info("User did not submit a VCF - returning to submission page");
            return "submit";
        }

        if (phenotypes == null && diseaseId == null) {
            logger.info("User did not provide a disease or phenotype set - returning to submission page");
            return "submit";
        }

        if(phenotypes == null) {
            logger.info("No phenotypes provided - trying to use disease phenotypes");
            phenotypes = getDiseasePhenotypes(diseaseId);
        }

        logger.info("Using disease: {}", diseaseId);
        logger.info("Using phenotypes: {}", phenotypes);

        int numVariantsInSample = countVariantLinesInVcf(vcfPath);
        if (numVariantsInSample > maxVariants) {
            logger.info("{} contains {} variants - this is more than the allowed maximum of {}."
                    + "Returning user to submit page", vcfPath, numVariantsInSample, maxVariants);
            cleanUpSampleFiles(vcfPath, pedPath);
            model.addAttribute("numVariants", numVariantsInSample);
            return "resubmitWithFewerVariants";
        }

        Analysis analysis = buildAnalysis(vcfPath, pedPath, diseaseId, phenotypes, geneticInterval, minimumQuality, removeDbSnp, keepOffTarget, keepNonPathogenic, modeOfInheritance, frequency, makeGenesToKeep(genesToFilter), prioritiser);
        SampleData sampleData = exomiser.run(analysis);

        Path outputDir = Paths.get(System.getProperty("java.io.tmpdir"), session.getId());
        try {
            Files.createDirectory(outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Output dir: {}", outputDir);
        String outFileName = outputDir.toString() + "/results";
        OutputSettings outputSettings = new OutputSettingsImp.OutputSettingsBuilder()
                .numberOfGenesToShow(20)
                .outputPrefix(outFileName)
                .outputFormats(EnumSet.of(OutputFormat.HTML, OutputFormat.TSV_GENE, OutputFormat.TSV_VARIANT, OutputFormat.VCF))
                .build();

        for (OutputFormat outFormat : outputSettings.getOutputFormats()) {
            ResultsWriter resultsWriter = resultsWriterFactory.getResultsWriter(outFormat);
            resultsWriter.writeFile(analysis, sampleData, outputSettings);
        }

        buildResultsModel(model, analysis, sampleData);
        logger.info("Returning {} results to user", vcfPath.getFileName());
        cleanUpSampleFiles(vcfPath, pedPath);
        return "results";
    }

    private List<String> getDiseasePhenotypes(String diseaseId) {
        if (diseaseId != null && !diseaseId.isEmpty()) {
            return priorityService.getHpoIdsForDiseaseId(diseaseId);
        }
        return Collections.emptyList();
    }

    private int countVariantLinesInVcf(Path vcfPath) {
        int variantCount = 0;
        try {
            BufferedReader fileReader = Files.newBufferedReader(vcfPath, StandardCharsets.UTF_8);
            boolean readingVariants = false;
            String line;
            for (line = fileReader.readLine(); fileReader.readLine() != null;) {
                if (line.startsWith("#CHROM")) {
                    readingVariants = true;
                }
                while (readingVariants == true) {
                    variantCount++;
                }
            }
        } catch (IOException ex) {
            logger.error("", ex);
        }
        logger.info("Vcf {} contains {} variants", vcfPath, variantCount);
        return variantCount;
    }

    private Analysis buildAnalysis(Path vcfPath, Path pedPath, String diseaseId, List<String> phenotypes, String geneticInterval, Float minimumQuality, Boolean removeDbSnp, Boolean keepOffTarget, Boolean keepNonPathogenic, String modeOfInheritance, String frequency, Set<Integer> genesToKeep, String prioritiser) throws NumberFormatException {

        Settings settings = new Settings.SettingsBuilder()
                .vcfFilePath(vcfPath)
                .pedFilePath(pedPath)
                .hpoIdList(phenotypes)
                .modeOfInheritance(ModeOfInheritance.valueOf(modeOfInheritance))
                .minimumQuality(minimumQuality == null ? 0 : minimumQuality)
                .removeKnownVariants(removeDbSnp)
                .keepOffTargetVariants(keepOffTarget)
                .keepNonPathogenic(keepNonPathogenic)
                .modeOfInheritance(ModeOfInheritance.valueOf(modeOfInheritance))
                .maximumFrequency(Float.valueOf(frequency))
                .genesToKeep(genesToKeep)
                .geneticInterval(geneticInterval != null ? GeneticInterval.parseString(HG19RefDictBuilder.build(), geneticInterval) : null)
                .usePrioritiser(PriorityType.valueOf(prioritiser))
                .diseaseId(diseaseId)
                .build();

        Analysis sparseAnalysis = settingsParser.parse(settings);

        return sparseAnalysis
                .copy()
                .analysisMode(AnalysisMode.PASS_ONLY)
                .build();
    }

    private void buildResultsModel(Model model, Analysis analysis, SampleData sampleData) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        //required for correct output of Path types
        mapper.registerModule(new Jdk7Module());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        String jsonSettings = "";
        try {
            jsonSettings = mapper.writeValueAsString(analysis);
        } catch (JsonProcessingException ex) {
            logger.error("Unable to process JSON settings", ex);
        }
        model.addAttribute("settings", jsonSettings);

        //make the user aware of any unanalysed variants
        List<VariantEvaluation> unAnalysedVarEvals = sampleData.getUnAnnotatedVariantEvaluations();
        model.addAttribute("unAnalysedVarEvals", unAnalysedVarEvals);

        //write out the filter reports section
        List<FilterReport> filterReports = ResultsWriterUtils.makeFilterReports(analysis, sampleData);
        model.addAttribute("filterReports", filterReports);

        List<VariantEvaluation> variantEvaluations = sampleData.getVariantEvaluations();
        List<VariantEffectCount> variantEffectCounters = ResultsWriterUtils.makeVariantEffectCounters(variantEvaluations);
        model.addAttribute("variantTypeCounters", variantEffectCounters);

        //write out the variant type counters
        List<String> sampleNames = sampleData.getSampleNames();
        String sampleName = "Anonymous";
        if (!sampleNames.isEmpty()) {
            sampleName = sampleNames.get(0);
        }
        model.addAttribute("sampleName", sampleName);
        model.addAttribute("sampleNames", sampleNames);

        List<Gene> sampleGenes = sampleData.getGenes();
        model.addAttribute("geneResultsTruncated", false);
        int numCandidateGenes = numGenesPassedFilters(sampleGenes);
        if (numCandidateGenes > maxGenes) {
            logger.info("Truncating number of genes returned to {} ", maxGenes);
            model.addAttribute("geneResultsTruncated", true);
            model.addAttribute("numCandidateGenes", numCandidateGenes);
            model.addAttribute("totalGenes", sampleGenes.size());
        }

        List<Gene> passedGenes = ResultsWriterUtils.getMaxPassedGenes(sampleGenes, maxGenes);
        model.addAttribute("genes", passedGenes);
    }

    private int numGenesPassedFilters(List<Gene> genes) {
        int numCandidateGenes = 0;
        for (Gene gene : genes) {
            if (gene.passedFilters()) {
                numCandidateGenes++;
            }
        }
        return numCandidateGenes;
    }

    //This throws 'java.nio.file.FileSystemException: The process cannot access the file because it is being used by another process.'
    // when on Windows as it seems tha Tomcat is locking the files/not setting the correct owner permissions.
    private void cleanUpSampleFiles(Path vcfPath, Path pedPath) {
        deleteSampleFile(vcfPath);
        deleteSampleFile(pedPath);
    }

    private void deleteSampleFile(Path sampleFile) {
        try {
            if (sampleFile != null) {
                logger.info("Deleting sample input file {}", sampleFile);
                Files.deleteIfExists(sampleFile);
            }
        } catch (IOException ex) {
            logger.error("Unable to delete sample file {}", sampleFile, ex);
        }
    }

    private Set<Integer> makeGenesToKeep(List<String> genesToFilter) {
        logger.info("Genes to filter: {}", genesToFilter);
        if (genesToFilter == null) {
            return new HashSet<>();
        }
        Set<Integer> genesToKeep = new TreeSet<>();
        for (String geneId : genesToFilter) {
            try {
                Integer entrezId = Integer.parseInt(geneId);
                logger.info("Adding gene {} to genesToFilter", entrezId);
                genesToKeep.add(entrezId);
            } catch (NumberFormatException ex) {
                logger.error("'{}' not added to genesToKeep as this is not a number.", geneId);
            }
        }
        return genesToKeep;
    }

    private Path createVcfPathFromMultipartFile(MultipartFile multipartVcfFile) {
        return createPathFromMultipartFile(multipartVcfFile, ".vcf");
    }

    private Path createPedPathFromMultipartFile(MultipartFile multipartPedFile) {
        return createPathFromMultipartFile(multipartPedFile, ".ped");
    }

    private Path createPathFromMultipartFile(MultipartFile multipartFile, String suffix) {
        Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir"));
        if (!multipartFile.isEmpty()) {
            logger.info("Uploading multipart file: {}", multipartFile.getOriginalFilename());
            try {
                Path path = Files.createTempFile(tempDirPath, "exomiser-", suffix);
                multipartFile.transferTo(path.toFile());
                return path;
            } catch (IOException e) {
                logger.error("Failed to upload file {}", multipartFile.getOriginalFilename(), e);
            }
        }
        //PED files are permitted to be null so this is OK really.
        return null;
    }
}
