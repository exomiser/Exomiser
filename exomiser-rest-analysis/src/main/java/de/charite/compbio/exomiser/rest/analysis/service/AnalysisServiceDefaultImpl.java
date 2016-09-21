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
import de.charite.compbio.exomiser.core.writers.OutputFormat;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisResponse;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class AnalysisServiceDefaultImpl implements AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisServiceDefaultImpl.class);

    //TODO: make a better id - something like timestamp + random + serverId. Like the Twitter snowflake
    private static final AtomicLong analysisId = new AtomicLong(System.currentTimeMillis() * 100000);

    //TODO: this should become a DAO
    private final Map<Long, Analysis> analysisMap = new ConcurrentHashMap<>();


    @Override
    public synchronized AnalysisResponse createAnalysisJob(Analysis analysis) {
        final long id = analysisId.incrementAndGet();
        analysisMap.put(id, analysis);
        logger.info("Created analysis job {}", id);
        return new AnalysisResponse(id, AnalysisStatus.AWAITING_VCF, "Analysis received.");
    }

    @Override
    public AnalysisResponse createAnalysisJobFromYaml(String analysisYaml) {
        //TODO: Validate YAML transform into Analysis (use AnalysisFactory)
        return createAnalysisJob(Analysis.builder().build());
    }

    @Override
    public AnalysisResponse createVcf(long id, Path vcfPath) {
        return null;
    }

    @Override
    public AnalysisResponse createPed(long id, Path pedPath) {
        return null;
    }

    @Override
    public Analysis getAnalysis(long id) {
        return analysisMap.get(id);
    }

    @Override
    public Path getVcf(long id) {
        return null;
    }

    @Override
    public Path getPed(long id) {
        return null;
    }

    @Override
    public Path getResults(long id, OutputFormat outputFormat) {
        return null;
    }

    @Override
    public AnalysisResponse startAnalysis(long id) {
        return null;
    }

    @Override
    public AnalysisResponse getAnalysisStatus(long id) {
        return null;
    }

    @Override
    public void delete(long id) {
        logger.info("Deleting analysis job {}", id);
    }

    @Override
    public boolean exists(long id) {
        return false;
    }
}
