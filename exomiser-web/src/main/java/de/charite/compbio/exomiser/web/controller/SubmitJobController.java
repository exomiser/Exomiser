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

import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.filter.FilterReport;
import de.charite.compbio.exomiser.core.model.Exomiser;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.writer.ResultsWriterUtils;
import de.charite.compbio.exomiser.priority.PriorityType;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.VariantTypeCounter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private SampleDataFactory sampleDataFactory;

    @Autowired
    private Exomiser exomiser;

    @RequestMapping(value = "submit", method = RequestMethod.GET)
    public String configureExomiserJob(Model model) {
        return "submit";
    }

    @RequestMapping(value = "submit", method = RequestMethod.POST)
    public String submit(
            @RequestParam(value="vcf") MultipartFile vcfFile,
            @RequestParam(value="ped", required = false) MultipartFile pedFile,

            @RequestParam("disease") String diseaseId,
            @RequestParam(value="phenotypes", required = false) List<String> phenotypes,
            
            @RequestParam(value="min-call-quality", required = false) Float minimumQuality,
            @RequestParam(value="genetic-interval", required = false) String geneticInterval,
            @RequestParam("frequency") String frequency,
            @RequestParam("remove-dbsnp") Boolean removeDbSnp,
            @RequestParam("remove-non-pathogenic") Boolean removeNonPathogenic,
            @RequestParam("remove-off-target") Boolean removeOffTarget,
            @RequestParam("inheritance") String modeOfInheritance,
            
            @RequestParam("prioritiser") String prioritiser,
            HttpSession session,
            Model model) {

        logger.info("Session id: {}", session.getId());
        Path vcfPath = createPathFromMultipartFile(vcfFile);
        Path pedPath = createPathFromMultipartFile(pedFile);

        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder()
                //Upload Sample Files input
                .vcfFilePath(vcfPath)
                .pedFilePath(pedPath)
                //Enter Sample Phenotype input
                .diseaseId(diseaseId)
                .hpoIdList(phenotypes == null ? new ArrayList<String>() : phenotypes)
                //Set Filtering Parameters
                .minimumQuality(minimumQuality == null? 0 : minimumQuality)                
                
                .removeDbSnp(removeDbSnp)
                .removeOffTargetVariants(removeOffTarget)
                //make this work for nulls....
//                .geneticInterval(GeneticInterval.parseString(geneticInterval))
                .removePathFilterCutOff(removeNonPathogenic)
                .modeOfInheritance(ModeOfInheritance.valueOf(modeOfInheritance))
                .maximumFrequency(Float.valueOf(frequency))
                //Choose Prioritiser
                .usePrioritiser(PriorityType.valueOf(prioritiser))
                .build();

        //TODO: Submit the settings to the ExomiserController to run the job rather than do it here
         
        SampleData sampleData = sampleDataFactory.createSampleData(vcfPath, pedPath);
        exomiser.analyse(sampleData, settings);
        model.addAttribute("settings", settings);
        //TODO: remove priorityList - this should become another report
        //write out the filter reports section
        List<FilterReport> filterReports = ResultsWriterUtils.makeFilterReports(settings, sampleData);
        List<VariantEvaluation> variantEvaluations = sampleData.getVariantEvaluations();
        model.addAttribute("filterReports", filterReports);
        //write out the variant type counters
        VariantTypeCounter vtc = ResultsWriterUtils.makeVariantTypeCounter(variantEvaluations);
        model.addAttribute("variantTypeCounter", vtc);
        List<Gene> passedGenes = new ArrayList<>();
        for (Gene gene : sampleData.getGenes()) {
            if (gene.passedFilters()) {
                passedGenes.add(gene);
            }
        }
        model.addAttribute("genes", passedGenes);
        return "results";
    }

    private Path createPathFromMultipartFile(MultipartFile multipartFile) {
        Path path = null;
        if (!multipartFile.isEmpty()) {
            logger.info("Uploading multipart file: {}", multipartFile.getOriginalFilename());
            try {
                path = Paths.get(multipartFile.getOriginalFilename());
                multipartFile.transferTo(path.toFile());
            } catch (IOException e) {
                logger.error("Failed to upload file {}", multipartFile.getOriginalFilename(), e);
            }
        }
        //PED files are expected to be null so this is OK really.
        return path;
    }
}
