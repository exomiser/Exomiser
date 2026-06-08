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

import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.writers.OutputSettings;
import org.monarchinitiative.exomiser.core.writers.OutputSettingsProtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

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

}
