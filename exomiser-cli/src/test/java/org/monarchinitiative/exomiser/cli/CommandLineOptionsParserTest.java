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
package org.monarchinitiative.exomiser.cli;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.commons.cli.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.cli.config.CommandLineOptionsConfig;
import org.monarchinitiative.exomiser.core.analysis.Settings;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.writers.OutputFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CommandLineOptionsConfig.class)
public class CommandLineOptionsParserTest {

    @Autowired
    private CommandLineOptionsParser instance;

    @Autowired
    private Options options;
    
    /**
     * Utility method for parsing input strings and producing ExomiserSettings
     * for test asserts.
     *
     * @param input
     * @return
     * @throws ParseException
     */
    private Settings parseSettingsFromInput(String input) {
        String[] args = input.split(" ");
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException ex) {
            Logger.getLogger(CommandLineOptionsParserTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return instance.parseCommandLine(commandLine);
    }
    
    @Test
    public void exomiserSettingsAreInvalidWhenAVcfFileWasNotSpecified() {
        String input = "--ped def.ped -D OMIM:101600 --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.isValid(), is(false));
    }

    @Test
    public void exomiserSettingsAreValidWhenAPrioritiserWasNotSpecified() {
        String input = "-v 123.vcf --ped def.ped -D OMIM:101600";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.isValid(), is(true));
    }

    @Test
    public void exomiserSettingsAreValidWhenOnlyAVcfFileAndAPrioritiserAreSpecified() {
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.isValid(), is(true));
    }

    @Test
    public void should_throw_caught_exception_when_settings_file_not_found() {
        String input = "--settings-file wibble.settings";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.isValid(), is(false));
    }

    @Test(expected = CommandLineParseError.class)
    public void throwsExceptionsWhenInvalidSettingsFileIsProvided() {
        String input = "--settings-file src/test/resources/testInvalidSettings.properties";

        parseSettingsFromInput(input);
    }

    @Test
    public void shouldProduceValidSettingsWhenValidSettingsFileIsProvided() {
        String input = "--settings-file src/test/resources/testValidSettings.properties";
        SettingsBuilder settingsBuilder = Settings.builder();
        settingsBuilder.vcfFilePath(Paths.get("sampleData.vcf"));
        settingsBuilder.pedFilePath(Paths.get(""));
        settingsBuilder.usePrioritiser(PriorityType.PHIVE_PRIORITY);
        settingsBuilder.maximumFrequency(0.01f);
        settingsBuilder.minimumQuality(0f);
        settingsBuilder.keepNonPathogenic(true);
        settingsBuilder.removeKnownVariants(true);
        settingsBuilder.keepOffTargetVariants(true);
        settingsBuilder.candidateGene("FGFR2");
        settingsBuilder.hpoIdList(Arrays.asList("HP:0000001","HP:0000002","HP:0000003"));
        settingsBuilder.seedGeneList(Arrays.asList(12345,2345,3456,1234567));
        settingsBuilder.diseaseId("OMIM:101500");
        settingsBuilder.modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        settingsBuilder.numberOfGenesToShow(345);
        settingsBuilder.outputPrefix("/users/jules/exomes/vcf/analysis");
        settingsBuilder.outputFormats(EnumSet.of(OutputFormat.VCF));
        
        Settings expectedSettings = settingsBuilder.build();
        
        Settings exomiserSettings = parseSettingsFromInput(input);

        System.out.println(exomiserSettings);
        assertThat(exomiserSettings.isValid(), is(true));
        assertThat(exomiserSettings, equalTo(expectedSettings));
    }

    @Test
    public void shouldProduceValidSettingsWhenIncompleteSettingsFileIsProvided() {
        String input = "--settings-file src/test/resources/testIncompleteSettings.properties";

        Settings exomiserSettings = parseSettingsFromInput(input);

        System.out.println(exomiserSettings);
        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(100f));
        assertThat(exomiserSettings.keepNonPathogenicVariants(), is(false));
    }

    @Test
    public void shouldProduceWhenSettingsFileIsIndicatedAndOverwriteValuesWhenCommandLineOptionIsSpecified() {
        String input = " --max-freq=0.1 --settings-file src/test/resources/exomiserSettings.properties";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(0.1f));
    }

    @Test
    public void should_produce_settings_with_a_vcf_path() {
        String vcfFile = "123.vcf";
        String input = String.format("-v %s --ped def.ped -D OMIM:101600 --prioritiser=phive", vcfFile);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }

    @Test
    public void should_produce_settings_with_a_vcf_path__using_long_option() {
        String vcfFile = "123.vcf";
        String input = String.format("--vcf %s --ped def.ped -D OMIM:101600 --prioritiser=phive", vcfFile);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }

    @Test
    public void should_produce_settings_with_a_ped_path_if_specified() {
        String pedFile = "ped.ped";
        String input = String.format("-v 123.vcf --ped %s -D OMIM:101600 --prioritiser=phive", pedFile);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getPedPath(), equalTo(Paths.get(pedFile)));
    }

    @Test
    public void should_produce_settings_with_a_null_ped_path_if_not_specified() {
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getPedPath(), nullValue());
    }

    @Test
    public void parsesProbandSampleName() {
        String probandSampleName = "AGENT-47";
        String input = String.format("-v 123.vcf --proband %s", probandSampleName);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getProbandSampleName(), equalTo(probandSampleName));
    }

    @Test
    public void should_produce_settings_with_a_priority_class() {
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertEquals(PriorityType.PHIVE_PRIORITY, exomiserSettings.getPrioritiserType());
    }

    @Test(expected = NumberFormatException.class)
    public void should_throw_NumberFormatException_when_passed_non_float_max_freq() {
        String input = "-v 123.vcf -F not_a_float --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);
    }

    @Test
    public void should_produce_settings_with_default_maximumFrequency_if_not_set() {
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(100.0f));

    }

    @Test
    public void should_produce_settings_with_maximumFrequency_when_set() {
        float frequency = 25.23f;
        //use the actual value in the string here otherwise it will do weird localisation things.
        String input = String.format("-v 123.vcf -F 25.23 --prioritiser=phive", frequency);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(frequency));

    }

    @Test
    public void should_produce_settings_with_minimumQuality_when_set() {
        float frequency = 73.12f;
        //use the actual value in the string here otherwise it will do weird localisation things.
        String input = String.format("-v 123.vcf -Q 73.12 --prioritiser=phive", frequency);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getMinimumQuality(), equalTo(frequency));
    }

    @Test(expected = NumberFormatException.class)
    public void should_throw_NumberFormatException_when_passed_non_float_min_qual() {
        String input = "-v 123.vcf -Q not_a_float --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);
    }

    @Test
    public void should_produce_settings_with_genetic_interval_when_set() {
        String option = "--restrict-interval";
        GeneticInterval value = new GeneticInterval((byte) 2, 12345, 67890);
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getGeneticInterval(), equalTo(value));
    }

    @Test
    public void shouldProduceSettingsWithKeepNonPathogenicVariantsDefaultAsTrueWhenSet() {
        String option = "keep-non-pathogenic";
        String input = String.format("-v 123.vcf --%s --prioritiser=phive", option);
        System.out.println(input);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.keepNonPathogenicVariants(), equalTo(true));
    }

    @Test
    public void shouldProduceSettingsWithKeepNonPathogenicVariantsDefaultAsFalseWhenNotSet() {
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.keepNonPathogenicVariants(), is(false));
    }

    @Test
    public void should_produce_settings_with_remove_dbsnp_when_set() {
        String option = "--remove-known-variants";
        String input = String.format("-v 123.vcf %s --prioritiser=phive", option);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.removeKnownVariants(), is(true));
    }

    @Test
    public void should_produce_settings_with_remove_dbsnp_default_when_not_set() {
        String option = "--remove-known-variants";
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.removeKnownVariants(), is(false));
    }

    @Test
    public void shouldProduceSettingsWithRemoveOffTargetWhenSet() {
        String option = "--keep-off-target";
        String input = String.format("-v 123.vcf %s --prioritiser=phive", option);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.keepOffTargetVariants(), is(true));
    }

    @Test
    public void shouldProduceSettingsWithKeepOffTargetDefaultWhenNotSet() {
        String option = "--keep-off-target";
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.keepOffTargetVariants(), is(false));
    }

    @Test
    public void should_produce_settings_with_candidate_gene_when_set() {
        String option = "--candidate-gene";
        String value = "FGFR2";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getCandidateGene(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_hpo_ids_when_set() {
        String option = "--hpo-ids";
        String value = "HP:0000407,HP:0009830,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0009830");
        expectedList.add("HP:0002858");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_single_hpo_id_when_set() {
        String option = "--hpo-ids";
        String value = "HP:0000407";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_empty_list_when_invalid_hpo_id_given() {
        String option = "--hpo-ids";
        String value = "HP:000040";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<String> expectedList = new ArrayList();

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_only_hpo_ids_when_set_with_invalid_value() {
        String option = "--hpo-ids";
        //OMIM:100100 is not a valid HPO ID
        String value = "HP:0000407,OMIM:100100,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0002858");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_seed_genes_when_set() {
        String option = "--seed-genes";
        String value = "123,456,7890";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<Integer> expectedList = new ArrayList();
        expectedList.add(123);
        expectedList.add(456);
        expectedList.add(7890);

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_single_seed_gene_when_set() {
        String option = "--seed-genes";
        String value = "123";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<Integer> expectedList = new ArrayList();
        expectedList.add(123);

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }
    
    @Test
    public void should_produce_settings_when_seed_gene_specified_but_not_set() {
        String option = "--seed-genes";
        String value = "";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<Integer> expectedList = new ArrayList();

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }

    @Test
    public void should_return_empty_list_when_seed_genes_incorrectly_specified() {
        String option = "--seed-genes";
        String value = "gene1:gene2,gene3";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        List<Integer> expectedList = new ArrayList();

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_disease_id_when_set() {
        String option = "--disease-id";
        String value = "OMIM:101600";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getDiseaseId(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_no_disease_id_when_set_with_empty_value() {
        String option = "--disease-id";
        String value = "";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getDiseaseId(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_DOMINANT_inheritance_mode_when_set() {
        String option = "--inheritance-mode";
        String value = "AD";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }

    @Test
    public void should_produce_settings_with_RECESSIVE_inheritance_mode_when_set() {
        String option = "--inheritance-mode";
        String value = "AR";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
    }

    @Test
    public void should_produce_settings_with_X_LINKED_inheritance_mode_when_set() {
        String option = "--inheritance-mode";
        String value = "X";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.X_RECESSIVE));
    }

    @Test
    public void should_produce_settings_with_UNINITIALIZED_inheritance_mode_when_not_set() {
        String option = "--inheritance-mode";
        String value = "X";
        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.ANY));
    }

    @Test(expected = CommandLineParseError.class)
    public void throwsExceptionWhenInheritanceModeValueNotRecognised() {
        String option = "--inheritance-mode";
        String value = "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);
    }

    @Test
    public void should_produce_settings_with_num_genes_greater_than_zero_when_specified() {
        String option = "--num-genes";
        String value = "42";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getNumberOfGenesToShow(), equalTo(Integer.parseInt(value)));
    }

    @Test
    public void should_produce_settings_out_file_value_when_specified() {
        String option = "--out-prefix";
        String value = "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getOutputPrefix(), equalTo(value));
    }

    @Test
    public void should_produce_settings_out_file_with_specified_suffix() {
        String option = "--out-prefix";
        String value = "/users/jules/vcf/analysis/wibble_20150328.pflurb.vcf";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.getOutputPrefix(), equalTo(value));
    }

    @Test
    public void shouldProduceSettingsWithHTMLOutputFormatAsDefaultWhenInputValueNotRecognised() {
        String option = "--out-format";
        String value = "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        Set<OutputFormat> expected = EnumSet.of(OutputFormat.HTML);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithHTMLOutputFormatAsDefaultWhenNoneSpecified() {

        String input = "-v 123.vcf --prioritiser=phive";

        Settings exomiserSettings = parseSettingsFromInput(input);

        Set<OutputFormat> expected = EnumSet.of(OutputFormat.HTML);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithTABOutputFormatWhenSpecified() {
        String option = "--out-format";
        String value = "TAB-GENE";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        Set<OutputFormat> expected = EnumSet.of(OutputFormat.TSV_GENE);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithVCFOutputFormatWhenSpecified() {
        String option = "--out-format";
        String value = "VCF";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        Set<OutputFormat> expected = EnumSet.of(OutputFormat.VCF);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithTSVAndVCFOutputFormatWhenSpecified() {
        String option = "--out-format";
        String value = "TAB-GENE,VCF";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phive", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        Set<OutputFormat> expected = EnumSet.of(OutputFormat.VCF, OutputFormat.TSV_GENE);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWhereRunFullAnalysisIsTrueWhenSpecifiedTrue() {
        String option ="full-analysis";
        String value = "true";
        String input = String.format("-v 123.vcf --prioritiser=phive --%s=%s", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.runFullAnalysis(), is(true));
    }

    @Test
    public void shouldProduceSettingsWhereRunFullAnalysisIsFalseWhenSpecifiedFalse() {
        String option = "full-analysis";
        String value = "false";
        String input = String.format("-v 123.vcf --prioritiser=phive --%s=%s", option, value);

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.runFullAnalysis(), is(false));
    }

    @Test
    public void shouldProduceSettingsWhereRunFullAnalysisIsFalseWhenNotSpecified() {
        String input = String.format("-v 123.vcf --prioritiser=phive");

        Settings exomiserSettings = parseSettingsFromInput(input);

        assertThat(exomiserSettings.runFullAnalysis(), is(false));
    }
}
