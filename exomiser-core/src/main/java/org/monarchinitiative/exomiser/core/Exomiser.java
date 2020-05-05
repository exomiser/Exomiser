/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core;

import org.monarchinitiative.exomiser.api.v1.JobProto;
import org.monarchinitiative.exomiser.core.analysis.*;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the main entry point for analysing data using the Exomiser. An {@link Analysis}
 * should be built with an {@link AnalysisParser} or programmatically using the {@link AnalysisBuilder}. The {@link JobProto}
 * should be read from file or created using the the fluent API provided by the {@link AnalysisProtoBuilder}
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class Exomiser {

    private static final Logger logger = LoggerFactory.getLogger(Exomiser.class);

    private final AnalysisFactory analysisFactory;

    @Autowired
    public Exomiser(AnalysisFactory analysisFactory) {
        this.analysisFactory = analysisFactory;
    }

    public AnalysisBuilder getAnalysisBuilder() {
        return analysisFactory.getAnalysisBuilder();
    }

    /**
     * @param job a {@link JobProto.Job} specifying how Exomiser should analyse the sample
     * @return an {@link AnalysisResults} instance
     * @since 13.0.0
     */
    public AnalysisResults run(JobProto.Job job) {
        AnalysisParser analysisParser = analysisFactory.getAnalysisParser();
        Sample sample = analysisParser.parseSample(job);
        Analysis analysis = analysisParser.parseAnalysis(job);
        return run(sample, analysis);
    }

    /**
     * @param sample   The {@link Sample} representing the proband and possibly the proband's family to be analysed
     * @param analysis The {@link Analysis} through which a {@link Sample} is to be run.
     * @return an {@link AnalysisResults} instance
     * @since 13.0.0
     */
    public AnalysisResults run(Sample sample, Analysis analysis) {
        GenomeAssembly genomeAssembly = sample.getGenomeAssembly();
        AnalysisMode analysisMode = analysis.getAnalysisMode();
        logger.info("Running analysis using {} assembly with mode: {}", genomeAssembly, analysisMode);
        AnalysisRunner analysisRunner = analysisFactory.getAnalysisRunner(genomeAssembly, analysisMode);
        return analysisRunner.run(sample, analysis);
    }
}
