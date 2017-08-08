/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.rest.analysis.service;

import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.monarchinitiative.exomiser.rest.analysis.model.AnalysisResponse;

import java.nio.file.Path;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface AnalysisService {

    AnalysisResponse createAnalysisJob(Analysis analysis);

    AnalysisResponse createAnalysisJobFromYaml(String analysisYaml);

    AnalysisResponse createVcf(long id, Path vcfPath);

    AnalysisResponse createPed(long id, Path pedPath);

    AnalysisResponse startAnalysis(long id);

    AnalysisResponse getAnalysisStatus(long id);

    void delete(long id);

    boolean exists(long id);

    Analysis getAnalysis(long id);

    Path getVcf(long id);

    Path getPed(long id);

    Path getResults(long id, OutputFormat outputFormat);

}
