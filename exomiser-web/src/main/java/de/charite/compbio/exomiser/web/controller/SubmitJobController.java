/*
 * Copyright (C) 2014 Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.charite.compbio.exomiser.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.Analysis;
import de.charite.compbio.exomiser.core.AnalysisRunner;
import de.charite.compbio.exomiser.core.AnalysisRunner.AnalysisMode;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.filters.FilterReport;
import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.filters.FilterSettings;
import de.charite.compbio.exomiser.core.filters.FilterSettingsImpl.FilterSettingsBuilder;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserSettings;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserSettingsImpl.PrioritiserSettingsBuilder;
import de.charite.compbio.exomiser.core.writers.ResultsWriterUtils;
import de.charite.compbio.exomiser.core.writers.VariantEffectCount;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.data.ReferenceDictionaryBuilder;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Controller
public class SubmitJobController {

    private static final Logger logger = LoggerFactory.getLogger(SubmitJobController.class);

    private final ReferenceDictionary referenceDictionary = new ReferenceDictionaryBuilder().build();

    @Autowired
    private SampleDataFactory sampleDataFactory;

    @Autowired
    Exomiser exomiser;

    @Autowired
    private int maxVariants;

    @Autowired
    private int maxGenes;

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
        Path vcfPath = createPathFromMultipartFile(vcfFile);
        Path pedPath = createPathFromMultipartFile(pedFile);

        logger.info("Selected disease: {}", diseaseId);
        logger.info("Selected phenotypes: {}", phenotypes);
        Set<Integer> genesToKeep = makeGenesToKeep(genesToFilter);
        //require a mimimum input of a VCF file and a set of HPO terms - these can come from the diseaseId
        if (vcfPath == null) {
            return "submit";
        }

        int numVariantsInSample = countVariantLinesInVcf(vcfPath);
        if (numVariantsInSample > maxVariants) {
            logger.info("{} contains {} variants - this is more than the allowed maximum of {}."
                    + "Returning user to submit page", vcfPath, numVariantsInSample, maxVariants);
            cleanUpSampleFiles(vcfPath, pedPath);
            model.addAttribute("numVariants", numVariantsInSample);
            return "resubmitWithFewerVariants";
        }

        //TODO: Submit the settings to the ExomiserController to run the job rather than do it here
        Analysis analysis = buildAnalysis(vcfPath, pedPath, diseaseId, phenotypes, geneticInterval, minimumQuality, removeDbSnp, keepOffTarget, keepNonPathogenic, modeOfInheritance, frequency, genesToKeep, prioritiser);
        AnalysisRunner analysisRunner = exomiser.getPassOnlyAnalysisRunner();
        analysisRunner.runAnalysis(analysis);

        buildResultsModel(model, analysis);

        logger.info("Returning {} results to user", vcfPath.getFileName());
        cleanUpSampleFiles(vcfPath, pedPath);
        return "results";
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
        Analysis analysis = new Analysis();
        analysis.setVcfPath(vcfPath);
        analysis.setPedPath(pedPath);
        analysis.setHpoIds(phenotypes);       
        analysis.setModeOfInheritance(ModeOfInheritance.valueOf(modeOfInheritance));

        analysis.setScoringMode(PriorityType.valueOf(prioritiser).getScoringMode());
        analysis.setAnalysisMode(AnalysisMode.PASS_ONLY);

        //Set Filtering Parameters
        FilterSettingsBuilder filterSettingsBuilder = new FilterSettingsBuilder()
                .minimumQuality(minimumQuality == null ? 0 : minimumQuality)
                .removeKnownVariants(removeDbSnp)
                .keepOffTargetVariants(keepOffTarget)
                .keepNonPathogenic(keepNonPathogenic)
                .modeOfInheritance(ModeOfInheritance.valueOf(modeOfInheritance))
                .maximumFrequency(Float.valueOf(frequency))
                .genesToKeep(genesToKeep);
                if (geneticInterval != null) { 
                    filterSettingsBuilder.geneticInterval(GeneticInterval.parseString(HG19RefDictBuilder.build(), geneticInterval));
                }       
        FilterSettings filterSettings = filterSettingsBuilder.build();
        
        PrioritiserSettings prioritiserSettings = new PrioritiserSettingsBuilder()
                .usePrioritiser(PriorityType.valueOf(prioritiser))
                .diseaseId(diseaseId)
                .hpoIdList(phenotypes == null ? new ArrayList<String>() : phenotypes)
                .build();
        
        analysis.addAllSteps(exomiser.makeAnalysisSteps(filterSettings, prioritiserSettings));
        
        SampleData sampleData = sampleDataFactory.createSampleData(vcfPath, pedPath);
        analysis.setSampleData(sampleData);
        
        return analysis;
    }

    private void buildResultsModel(Model model, Analysis analysis) {
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

        SampleData sampleData = analysis.getSampleData();
        //make the user aware of any unanalysed variants
        List<VariantEvaluation> unAnalysedVarEvals = sampleData.getUnAnnotatedVariantEvaluations();
        model.addAttribute("unAnalysedVarEvals", unAnalysedVarEvals);

        //write out the filter reports section
        List<FilterReport> filterReports = ResultsWriterUtils.makeFilterReports(analysis);
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
            logger.info("Truncating number of genes returned - {} ");
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

    private void cleanUpSampleFiles(Path vcfPath, Path pedPath) {
        try {
            Files.deleteIfExists(vcfPath);
            if (pedPath != null) {
                Files.deleteIfExists(pedPath);
            }
        } catch (IOException ex) {
            logger.error(null, ex);
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

    private Path createPathFromMultipartFile(MultipartFile multipartFile) {
        Path path = null;
        if (!multipartFile.isEmpty()) {
            logger.info("Uploading multipart file: {}", multipartFile.getOriginalFilename());
            try {
                path = Paths.get(System.getProperty("java.io.tmpdir"), multipartFile.getOriginalFilename());
                multipartFile.transferTo(path.toFile());
            } catch (IOException e) {
                logger.error("Failed to upload file {}", multipartFile.getOriginalFilename(), e);
            }
        }
        //PED files are permitted to be null so this is OK really.
        return path;
    }
}
