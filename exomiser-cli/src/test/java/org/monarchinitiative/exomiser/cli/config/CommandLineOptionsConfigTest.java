/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.cli.config;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.cli.options.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the command line options.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommandLineOptionsConfig.class)
public class CommandLineOptionsConfigTest {
    
    @Autowired
    private Options options;
    
    @Resource
    private Map<String, OptionMarshaller> optionMarshallers;

    private void isCorrectlyConfiguredAndHasOption(OptionMarshaller expected) {
        String commandLineParameter = expected.getCommandLineParameter();

        OptionMarshaller optionMarshaller = optionMarshallers.get(commandLineParameter);
        assertThat(optionMarshallers.containsKey(commandLineParameter), is(true));
        assertThat(expected, equalTo(optionMarshaller));
        
        Option option = options.getOption(commandLineParameter);
        assertThat(option.getLongOpt(), equalTo(commandLineParameter));
    }

    @Test
    public void testContainsOptions() {
        Map<String, OptionMarshaller> expectedOptions = new HashMap<>();

        expectedOptions.put("settings-file", new SettingsFileOptionMarshaller());
        expectedOptions.put("batch-file", new BatchFileOptionMarshaller());

        //sample data files
        expectedOptions.put("vcf", new VcfFileOptionMarshaller());
        expectedOptions.put("ped", new PedFileOptionMarshaller());
        expectedOptions.put("proband", new ProbandSampleNameOptionMarshaller());

        //analysis options
        expectedOptions.put("hpo-ids", new HpoIdsOptionMarshaller());
        expectedOptions.put("inheritance-mode", new InheritanceModeOptionMarshaller());
        expectedOptions.put("full-analysis", new FullAnalysisOptionMarshaller());

        //filter options
        expectedOptions.put("remove-failed", new FailedVariantFilterOptionMarshaller());
        expectedOptions.put("max-freq", new FrequencyThresholdOptionMarshaller());
        expectedOptions.put("remove-known-variants", new FrequencyKnownVariantOptionMarshaller());
        expectedOptions.put("restrict-interval", new GeneticIntervalOptionMarshaller());
        expectedOptions.put("min-qual", new QualityThresholdOptionMarshaller());
        expectedOptions.put("keep-non-pathogenic", new PathogenicityFilterCutOffOptionMarshaller());
        expectedOptions.put("keep-off-target", new TargetFilterOptionMarshaller());
        expectedOptions.put("genes-to-keep", new GenesToKeepFilterOptionMarshaller());

        //prioritiser options
        expectedOptions.put("prioritiser", new PrioritiserOptionMarshaller());
        expectedOptions.put("seed-genes", new SeedGenesOptionMarshaller());
        expectedOptions.put("disease-id", new DiseaseIdOptionMarshaller());
        expectedOptions.put("candidate-gene", new CandidateGeneOptionMarshaller());
        expectedOptions.put("hiphive-params", new HiPhiveOptionMarshaller());

        //output options
        expectedOptions.put("output-pass-variants-only", new OutputPassOnlyVariantsOptionMarshaller());
        expectedOptions.put("num-genes", new NumGenesOptionMarshaller());
        expectedOptions.put("out-prefix", new OutFilePrefixOptionMarshaller());
        expectedOptions.put("out-format", new OutFileFormatOptionMarshaller());

        assertThat(optionMarshallers, equalTo(expectedOptions));
    }
    
    @Test
    public void seedGenesOptionsCanHaveMultipleCommaSeparatedValues() {
        OptionMarshaller optionMarshaller = new SeedGenesOptionMarshaller();

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void hpoIdsOptionsCanHaveMultipleCommaSeparatedValues() {
        OptionMarshaller optionMarshaller = new HpoIdsOptionMarshaller();

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArgs(), is(true));
        assertThat(option.getValueSeparator(), equalTo(','));
    }
    
    @Test
    public void containsInheritanceModeOptionAndTakesArgs() {
        OptionMarshaller optionMarshaller = new InheritanceModeOptionMarshaller();

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArg(), is(true));
    }

    @Test
    public void containsFullAnalysisOptionMarshallerAndTakesArgs() {        
        OptionMarshaller optionMarshaller = new FullAnalysisOptionMarshaller();

        Option option = optionMarshaller.getOption();
        assertThat(option.hasArg(), is(true));
    }
    
}
