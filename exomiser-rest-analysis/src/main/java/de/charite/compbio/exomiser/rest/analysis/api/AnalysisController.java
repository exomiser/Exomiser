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

package de.charite.compbio.exomiser.rest.analysis.api;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisResponse;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisStatus;
import de.charite.compbio.exomiser.rest.analysis.service.AnalysisService;
import de.charite.compbio.exomiser.rest.analysis.service.AnalysisServiceDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    @Autowired
    private Path analysisPath;

    @Autowired
    private AnalysisService analysisService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String getAnalysis() {
        return "You can upload an analysis YAML file by posting to this URL.\n";
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AnalysisResponse postAnalysis(@RequestBody Analysis analysis) throws FileUploadException {
        logger.info("Receiving new analysis: {}", analysis);
        return analysisService.createAnalysisJob(analysis);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE)
    public AnalysisResponse postAnalysis(@RequestBody String analysisYaml) throws FileUploadException {
        logger.info("Receiving new YAML analysis: {}", analysisYaml);
        return analysisService.createAnalysisJobFromYaml(analysisYaml);
    }

    @RequestMapping(value = "/{analysisId}", method = RequestMethod.GET)
    public Analysis getAnalysis(@PathVariable long analysisId) {
        logger.info("Request for analysisId: {}", analysisId);
        return analysisService.getAnalysis(analysisId);
    }

    @RequestMapping(value = "/{analysisId}/upload", method = RequestMethod.POST)
    public AnalysisResponse postVcf(@PathVariable("analysisId") long id,
                                    @RequestParam(value = "vcf", required = true) MultipartFile file,
                                    @RequestParam(value = "ped", required = false) MultipartFile pedFile) throws FileUploadException {

        if (!file.isEmpty()) {
            Path analysisDir = getAnalysisDirectory(id);
            Path outputFile = analysisDir.resolve(file.getOriginalFilename());
            try {
                Files.createFile(outputFile);
            } catch (IOException ex) {
                throw new FileUploadException("Error uploading file " + file.getOriginalFilename());
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
                 BufferedWriter fileWriter = Files.newBufferedWriter(outputFile)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    fileWriter.write(line + "\n");
                }
                logger.info("Successfully saved input file for id {} to {}", id, outputFile);
                return new AnalysisResponse(id, AnalysisStatus.READY, "Successfully uploaded file " + file.getOriginalFilename());
            } catch (IOException e) {
                throw new FileUploadException("Error uploading file " + file.getOriginalFilename());
            }
        }
        throw new FileUploadException("Error uploading file " + file.getOriginalFilename() + " - File was empty.");
    }


    @RequestMapping(value = "/{analysisId}/vcf", method = RequestMethod.POST)
    public AnalysisResponse postAnalysis(@PathVariable("analysisId") long id, @RequestBody String file) throws FileUploadException {
        logger.info(file);
        return new AnalysisResponse(id, AnalysisStatus.ERROR, "testing");
    }

    private Path createAnalysisDirectory(long id) {
        Path analysisDir = analysisPath.resolve(Long.toUnsignedString(id));
        try {
            Files.createDirectories(analysisDir);
        } catch (IOException e) {
            logger.error("Can't create analysis directory for id {}", id, e);
            throw new AnalysisServerError("Can't create analysis directory for id " + id);
        }
        return analysisDir;
    }

    private Path getAnalysisDirectory(long id) {
        Path analysisDir = analysisPath.resolve(Long.toUnsignedString(id));
        if (!Files.exists(analysisDir)) {
            throw new UnknownAnalysisException("AnalysisId not found: " + id);
        }
        return analysisDir;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class FileUploadException extends RuntimeException {

        public FileUploadException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class UnknownAnalysisException extends RuntimeException {

        public UnknownAnalysisException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    private class AnalysisServerError extends RuntimeException {
        public AnalysisServerError(String message) {
            super(message);
        }
    }
}
