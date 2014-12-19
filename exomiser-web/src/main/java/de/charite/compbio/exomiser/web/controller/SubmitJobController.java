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
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.filter.FilterReport;
import de.charite.compbio.exomiser.core.model.Exomiser;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.writer.ResultsWriterUtils;
import de.charite.compbio.exomiser.core.writer.VariantTypeCount;
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
    public String configure() {
        return "submit";
    }

    @RequestMapping(value = "submit", method = RequestMethod.POST)
    public String submit(@RequestParam("vcf") MultipartFile vcfFile,
            @RequestParam("ped") MultipartFile pedFile,
            @RequestParam("prioritiser") String prioritiser,
            @RequestParam("frequency") String frequency,
            @RequestParam("remove-dbsnp") String removeDbSnp,
            @RequestParam("remove-non-pathogenic") String removeNonPathogenic,
            @RequestParam("remove-off-target") String removeOffTarget,
            @RequestParam("inheritance") String modeOfInheritance,
            HttpSession session,
            Model model) {

        logger.info("Session id: {}", session.getId());
        Path vcfPath = createPathFromMultipartFile(vcfFile);
        Path pedPath = createPathFromMultipartFile(pedFile);

        SampleData sampleData = sampleDataFactory.createSampleData(vcfPath, pedPath);
                
        //TODO - get some actual user input for the settings 
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder()
                .vcfFilePath(vcfPath)
                .pedFilePath(pedPath)
                .usePrioritiser(PriorityType.valueOf(prioritiser))
                .modeOfInheritance(ModeOfInheritance.valueOf(modeOfInheritance))
                .removeDbSnp(Boolean.parseBoolean(removeDbSnp))
                .removePathFilterCutOff(Boolean.valueOf(removeNonPathogenic))
                .removeOffTargetVariants(Boolean.valueOf(removeOffTarget))
                .diseaseId("OMIM:101600")
                .maximumFrequency(Float.valueOf(frequency))
                .build();

        exomiser.analyse(sampleData, settings);
        
        ObjectMapper mapper = new ObjectMapper();
        //required for correct output of Path types
        mapper.registerModule(new Jdk7Module());
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        String jsonSettings = "";
        try {
            jsonSettings = mapper.writeValueAsString(settings);
        } catch (JsonProcessingException ex) {
            logger.error("Unable to process JSON settings", ex);
        }
        model.addAttribute("settings", jsonSettings);
        
        //make the user aware of any unanalysed variants
        List<VariantEvaluation> unAnalysedVarEvals = sampleData.getUnAnnotatedVariantEvaluations();
        model.addAttribute("unAnalysedVarEvals", unAnalysedVarEvals);
        
        //write out the filter reports section
        List<FilterReport> filterReports = ResultsWriterUtils.makeFilterReports(settings, sampleData);
        List<VariantEvaluation> variantEvaluations = sampleData.getVariantEvaluations();
        model.addAttribute("filterReports", filterReports);
        //write out the variant type counters
        List<VariantTypeCount> variantTypeCounters = ResultsWriterUtils.makeVariantTypeCounters(variantEvaluations);
        List<String> sampleNames= sampleData.getSampleNames();
        String sampleName = "Anonymous";
        if(!sampleNames.isEmpty()) {
            sampleName = sampleNames.get(0);
        }
        model.addAttribute("sampleName", sampleName);
        model.addAttribute("sampleNames", sampleNames);
        model.addAttribute("variantTypeCounters", variantTypeCounters);
        List<Gene> passedGenes = new ArrayList<>();
        int numGenesToShow = settings.getNumberOfGenesToShow();
        if (numGenesToShow == 0) {
            numGenesToShow = sampleData.getGenes().size();
        } 
        int genesShown = 0;
        for (Gene gene : sampleData.getGenes()) {
            if(genesShown <= numGenesToShow) {
                if (gene.passedFilters()) {
                    passedGenes.add(gene);
                    genesShown++;
                }
            }
        }
        model.addAttribute("genes", passedGenes);
        return "results";
    }

    private Path createPathFromMultipartFile(MultipartFile multipartFile) {
        Path path = null;
        if (!multipartFile.isEmpty()) {
            logger.info("Uploading multipart file: {}", multipartFile.getOriginalFilename());
            try{
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
