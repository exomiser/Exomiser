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
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.PriorityScoreFilter;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisResponse;
import de.charite.compbio.exomiser.rest.analysis.model.AnalysisStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Service
public class AnalysisServiceDefault implements AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisServiceDefault.class);

    //TODO: make a better id - something like timestamp + random + serverId. Like the Twitter snowflake
    private static final AtomicLong analysisId = new AtomicLong(System.currentTimeMillis() * 100000);

    private final Map<Long, Analysis> analysisMap = new ConcurrentHashMap<>();

    @Override
    public AnalysisResponse createAnalysisJob(Analysis analysis) {
        long id = analysisId.incrementAndGet();
        analysisMap.put(id, analysis);
        logger.info("Created analysis job {}", id);
        return new AnalysisResponse(id, AnalysisStatus.AWAITING_VCF, "Analysis received.");
    }

    @Override
    public AnalysisResponse createAnalysisJobFromYaml(String analysisYaml) {
        //TODO: Validate YAML transform into Analysis (use AnalysisFactory)
        return createAnalysisJob(new Analysis());
    }

    @Override
    public Analysis getAnalysis(long id) {
        Analysis analysis = new Analysis();
        analysis.setFrequencySources(FrequencySource.ALL_ESP_SOURCES);
        analysis.setPathogenicitySources(EnumSet.of(PathogenicitySource.POLYPHEN, PathogenicitySource.CADD));
        analysis.addStep(new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.501f));
        analysis.addStep(new FrequencyFilter(1.0f));
        analysis.addStep(new PathogenicityFilter(true));
//        register your own MappingJackson2HttpMessageConverter by configuring your own ObjectMapper
        return analysis;
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
