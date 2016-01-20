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

package de.charite.compbio.exomiser.rest.analysis.service;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface AnalysisService {

    AnalysisResponse createAnalysisJob(Analysis analysis);

    AnalysisResponse createAnalysisJobFromYaml(String analysisYaml);

    //TODO: how best to accept these files - Multipart, Path?
//    AnalysisResponse createVcf(long id, MultipartFile file);
//
//    AnalysisResponse createPed(long id, MultipartFile file);

    Analysis getAnalysis(long id);

    AnalysisResponse startAnalysis(long id);

    AnalysisResponse getAnalysisStatus(long id);

    void delete(long id);

    boolean exists(long id);
}
