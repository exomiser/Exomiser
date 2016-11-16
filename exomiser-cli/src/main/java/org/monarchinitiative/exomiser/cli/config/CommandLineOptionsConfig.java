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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.config;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.monarchinitiative.exomiser.cli.CommandLineOptionsParser;
import org.monarchinitiative.exomiser.cli.options.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Spring configuration for setting-up the command-line options. If you want a
 * new option on the command line, add it here.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class CommandLineOptionsConfig {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptionsConfig.class);

    @Bean
    public CommandLineOptionsParser commandLineParser() {
        return new CommandLineOptionsParser();
    }

    /**
     * Add the options you want to be made available to the application here.
     *
     * @return the required OptionMarshallers for the system.
     */
    private Set<OptionMarshaller> desiredOptionMarshallers() {
        Set<OptionMarshaller> desiredOptionMarshallers = new LinkedHashSet<>();

        //commandline parser options
        desiredOptionMarshallers.add(new SettingsFileOptionMarshaller());
        desiredOptionMarshallers.add(new BatchFileOptionMarshaller());

        //sample data files 
        desiredOptionMarshallers.add(new VcfFileOptionMarshaller());
        desiredOptionMarshallers.add(new PedFileOptionMarshaller());
        desiredOptionMarshallers.add(new ProbandSampleNameOptionMarshaller());

        //analysis options
        desiredOptionMarshallers.add(new HpoIdsOptionMarshaller());
        desiredOptionMarshallers.add(new InheritanceModeOptionMarshaller());
        desiredOptionMarshallers.add(new FullAnalysisOptionMarshaller());

        //filter options
        desiredOptionMarshallers.add(new FrequencyThresholdOptionMarshaller());
        desiredOptionMarshallers.add(new FrequencyKnownVariantOptionMarshaller());
        desiredOptionMarshallers.add(new GeneticIntervalOptionMarshaller());
        desiredOptionMarshallers.add(new QualityThresholdOptionMarshaller());
        desiredOptionMarshallers.add(new PathogenicityFilterCutOffOptionMarshaller());
        desiredOptionMarshallers.add(new TargetFilterOptionMarshaller());
        desiredOptionMarshallers.add(new GenesToKeepFilterOptionMarshaller());

        //prioritiser options
        desiredOptionMarshallers.add(new PrioritiserOptionMarshaller());
        desiredOptionMarshallers.add(new SeedGenesOptionMarshaller());
        desiredOptionMarshallers.add(new DiseaseIdOptionMarshaller());
        desiredOptionMarshallers.add(new CandidateGeneOptionMarshaller());
        desiredOptionMarshallers.add(new HiPhiveOptionMarshaller());

        //output options
        desiredOptionMarshallers.add(new OutputPassOnlyVariantsOptionMarshaller());
        desiredOptionMarshallers.add(new NumGenesOptionMarshaller());
        desiredOptionMarshallers.add(new OutFilePrefixOptionMarshaller());
        desiredOptionMarshallers.add(new OutFileFormatOptionMarshaller());

        return desiredOptionMarshallers;
    }

    @Bean
    public Map<String, OptionMarshaller> optionMarshallers() {
        Map<String, OptionMarshaller> optionMarshallers = new HashMap<>();

        for (OptionMarshaller optionMarshaller : desiredOptionMarshallers()) {
            String cliParameter = optionMarshaller.getCommandLineParameter();
            logger.debug("Adding {}", optionMarshaller);
            optionMarshallers.put(cliParameter, optionMarshaller);
        }
        return optionMarshallers;
    }

    @Bean
    public Options options() {
        Options options = new Options();

        options.addOption(new Option("h", "help", false, "Shows this help"));
        options.addOption(new Option("H", "help", false, "Shows this help"));
        options.addOption(Option.builder()
                .argName("file")
                .hasArg()
                .desc("Path to analysis script file. This should be in yaml format.")
                .longOpt("analysis")
                .build());
        options.addOption(Option.builder()
                .argName("file")
                .hasArg()
                .desc("Path to analysis batch file. This should be in plain text file with the path to a single analysis script file in yaml format on each line.")
                .longOpt("analysis-batch")
                .build());
        
        for (OptionMarshaller optionMarshaller : desiredOptionMarshallers()) {
            Option option = optionMarshaller.getOption();
            options.addOption(option);
        }

        return options;
    }

}
