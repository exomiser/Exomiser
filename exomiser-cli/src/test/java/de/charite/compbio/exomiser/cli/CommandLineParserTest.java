/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.config.CommandLineOptionsConfig;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.core.writer.OutputFormat;
import de.charite.compbio.exomiser.priority.PriorityType;
import jannovar.common.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CommandLineOptionsConfig.class)
public class CommandLineParserTest {

    @Autowired
    private CommandLineParser instance;

    @Autowired
    private Options options;

    private CommandLine commandLine;
    private Parser parser;

    @Before
    public void setUp() throws ParseException {
        //check the args for a batch file first
        parser = new GnuParser();
    }

    @Test
    public void exomiser_settings_are_invalid_when_a_vcf_file_was_not_specified() throws ParseException {
        String input = "--ped def.ped -D OMIM:101600 --prioritiser=phenodigm-mgi";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.isValid(), is(false));
    }

    @Test
    public void exomiser_settings_are_invalid_when_a_prioritiser_was_not_specified() throws ParseException {
        String input = "-v 123.vcf --ped def.ped -D OMIM:101600";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.isValid(), is(false));
    }

    @Test
    public void command_line_should_specify_a_vcf_file_and_a_prioritiser() throws ParseException {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.isValid(), is(true));
    }

    @Test
    public void should_throw_caught_exception_when_settings_file_not_found() throws ParseException {
        String input = "--settings-file wibble.settings";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.isValid(), is(false));
    }

    @Test
    public void should_produce_invalid_settings_when_a_settings_file_is_provided() throws ParseException {
        String input = "--settings-file src/test/resources/testInvalidSettings.properties";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        System.out.println(exomiserSettings);
        assertThat(exomiserSettings.isValid(), is(false));
    }

    @Test
    public void should_produce_valid_settings_when_a_valid_settings_file_is_provided() throws ParseException {
        String input = "--settings-file src/test/resources/testValidSettings.properties";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        System.out.println(exomiserSettings);
        assertThat(exomiserSettings.isValid(), is(true));
    }

    @Test
    public void should_produce_valid_default_settings_when_an_incomplete_settings_file_is_provided() throws ParseException {
        String input = "--settings-file src/test/resources/testIncompleteSettings.properties";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        System.out.println(exomiserSettings);
        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(100f));
        assertThat(exomiserSettings.keepNonPathogenicMissense(), is(true));
    }

    @Test
    public void should_produce_settings_when_a_settings_file_is_indicated_and_overwrite_values_when_a_command_line_option_is_specified() throws ParseException {
        String input = " --max-freq=0.1 --settings-file src/test/resources/exomiserSettings.properties";
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(0.1f));
    }

    @Test
    public void should_produce_settings_with_a_vcf_path() throws ParseException {
        String vcfFile = "123.vcf";
        String input = String.format("-v %s --ped def.ped -D OMIM:101600 --prioritiser=phenodigm-mgi", vcfFile);
        String[] args = input.split(" ");

        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }

    @Test
    public void should_produce_settings_with_a_vcf_path__using_long_option() throws ParseException {
        String vcfFile = "123.vcf";
        String input = String.format("--vcf %s --ped def.ped -D OMIM:101600 --prioritiser=phenodigm-mgi", vcfFile);
        String[] args = input.split(" ");

        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }

    @Test
    public void should_produce_settings_with_a_ped_path_if_specified() throws ParseException {
        String pedFile = "ped.ped";
        String input = String.format("-v 123.vcf --ped %s -D OMIM:101600 --prioritiser=phenodigm-mgi", pedFile);
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        assertThat(exomiserSettings.getPedPath(), equalTo(Paths.get(pedFile)));
    }

    @Test
    public void should_produce_settings_with_a_null_ped_path_if_not_specified() throws ParseException {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();

        assertThat(exomiserSettings.getPedPath(), nullValue());
    }

    @Test
    public void should_produce_settings_with_a_priority_class() throws ParseException {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();

        assertEquals(PriorityType.PHENODIGM_MGI_PRIORITY, exomiserSettings.getPrioritiserType());
    }

    @Test(expected = NumberFormatException.class)
    public void should_throw_NumberFormatException_when_passed_non_float_max_freq() throws ParseException {
        String input = "-v 123.vcf -F not_a_float --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
    }

    @Test
    public void should_produce_settings_with_default_maximumFrequency_if_not_set() throws ParseException {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();

        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(100.0f));

    }

    @Test
    public void should_produce_settings_with_maximumFrequency_when_set() throws ParseException {
        float frequency = 25.23f;
        //use the actual value in the string here otherwise it will do weird localisation things.
        String input = String.format("-v 123.vcf -F 25.23 --prioritiser=phenodigm-mgi", frequency);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();

        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(frequency));

    }

    @Test
    public void should_produce_settings_with_minimumQuality_when_set() throws ParseException {
        float frequency = 73.12f;
        //use the actual value in the string here otherwise it will do weird localisation things.
        String input = String.format("-v 123.vcf -Q 73.12 --prioritiser=phenodigm-mgi", frequency);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();

        assertThat(exomiserSettings.getMinimumQuality(), equalTo(frequency));
    }

    @Test(expected = NumberFormatException.class)
    public void should_throw_NumberFormatException_when_passed_non_float_min_qual() throws ParseException {
        String input = "-v 123.vcf -Q not_a_float --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
    }

    @Test
    public void should_produce_settings_with_genetic_interval_when_set() throws ParseException {
        String option = "--restrict-interval";
        GeneticInterval value = new GeneticInterval((byte) 2, 12345, 67890);
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getGeneticInterval(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_include_pathogenic_when_set() throws ParseException {
        String option = ExomiserSettings.KEEP_NON_PATHOGENIC_MISSENSE_OPTION;
        String input = String.format("-v 123.vcf --%s=false --prioritiser=phenodigm-mgi", option);
        System.out.println(input);
        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.keepNonPathogenicMissense(), is(false));
    }

    @Test
    public void should_produce_settings_with_include_pathogenic_default_when_not_set() throws ParseException {
        String option = ExomiserSettings.KEEP_NON_PATHOGENIC_MISSENSE_OPTION;
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.keepNonPathogenicMissense(), is(true));
    }

    @Test
    public void should_produce_settings_with_remove_dbsnp_when_set() throws ParseException {
        String option = "--remove-dbsnp";
        String input = String.format("-v 123.vcf %s --prioritiser=phenodigm-mgi", option);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.removeDbSnp(), is(true));
    }

    @Test
    public void should_produce_settings_with_remove_dbsnp_default_when_not_set() throws ParseException {
        String option = "--remove-dbsnp";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.removeDbSnp(), is(false));
    }

    @Test
    public void should_produce_settings_when_remove_off_target_syn_is_set() throws ParseException {
        String option = "--remove-off-target-syn";
        String input = String.format("-v 123.vcf %s --prioritiser=phenodigm-mgi", option);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.removeOffTargetVariants(), is(false));
    }

    @Test
    public void should_produce_settings_with_remove_off_target_syn_default_when_not_set() throws ParseException {
        String option = "--remove-off-target-syn";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.removeOffTargetVariants(), is(true));
    }

    @Test
    public void should_produce_settings_with_candidate_gene_when_set() throws ParseException {
        String option = "--candidate-gene";
        String value = "FGFR2";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getCandidateGene(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_hpo_ids_when_set() throws ParseException {
        String option = "--hpo-ids";
        String value = "HP:0000407,HP:0009830,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0009830");
        expectedList.add("HP:0002858");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_single_hpo_id_when_set() throws ParseException {
        String option = "--hpo-ids";
        String value = "HP:0000407";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_empty_list_when_invalid_hpo_id_given() throws ParseException {
        String option = "--hpo-ids";
        String value = "HP:000040";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<String> expectedList = new ArrayList();

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_only_hpo_ids_when_set_with_invalid_value() throws ParseException {
        String option = "--hpo-ids";
        //OMIM:100100 is not a valid HPO ID
        String value = "HP:0000407,OMIM:100100,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0002858");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_seed_genes_when_set() throws ParseException {
        String option = "--seed-genes";
        String value = "123,456,7890";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<Integer> expectedList = new ArrayList();
        expectedList.add(123);
        expectedList.add(456);
        expectedList.add(7890);

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_single_seed_gene_when_set() throws ParseException {
        String option = "--seed-genes";
        String value = "123";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<Integer> expectedList = new ArrayList();
        expectedList.add(123);

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }

    @Test
    public void should_return_empty_list_when_seed_genes_incorrectly_specified() throws ParseException {
        String option = "--seed-genes";
        String value = "gene1:gene2,gene3";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        List<Integer> expectedList = new ArrayList();

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList));
    }

    @Test
    public void should_produce_settings_with_disease_id_when_set() throws ParseException {
        String option = "--disease-id";
        String value = "OMIM:101600";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getDiseaseId(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_no_disease_id_when_set_with_empty_value() throws ParseException {
        String option = "--disease-id";
        String value = "";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getDiseaseId(), equalTo(value));
    }

    @Test
    public void should_produce_settings_with_DOMINANT_inheritance_mode_when_set() throws ParseException {
        String option = "--inheritance-mode";
        String value = "AD";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT));
    }

    @Test
    public void should_produce_settings_with_RECESSIVE_inheritance_mode_when_set() throws ParseException {
        String option = "--inheritance-mode";
        String value = "AR";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
    }

    @Test
    public void should_produce_settings_with_X_LINKED_inheritance_mode_when_set() throws ParseException {
        String option = "--inheritance-mode";
        String value = "X";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.X_RECESSIVE));
    }

    @Test
    public void should_produce_settings_with_UNINITIALIZED_inheritance_mode_when_not_set() throws ParseException {
        String option = "--inheritance-mode";
        String value = "X";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED));
    }

    @Test
    public void should_produce_settings_with_UNINITIALIZED_inheritance_mode_when_value_not_recognised() throws ParseException {
        String option = "--inheritance-mode";
        String value = "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED));
    }

    @Test
    public void should_produce_settings_with_num_genes_greater_than_zero_when_specified() throws ParseException {
        String option = "--num-genes";
        String value = "42";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getNumberOfGenesToShow(), equalTo(Integer.parseInt(value)));
    }

    @Test
    public void should_produce_settings_out_file_value_when_specified() throws ParseException {
        String option = "--out-file";
        String value = "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getOutFileName(), equalTo(value));
    }

    @Test
    public void should_produce_settings_out_file_with_specified_suffix() throws ParseException {
        String option = "--out-file";
        String value = "wibble.pflurb";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        assertThat(exomiserSettings.getOutFileName(), equalTo(value));
    }

    @Test
    public void shouldProduceSettingsWithHTMLOutputFormatAsDefaultWhenInputValueNotRecognised() throws ParseException {
        String option = "--out-format";
        String value = "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        Set<OutputFormat> expected = EnumSet.of(OutputFormat.HTML);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithHTMLOutputFormatAsDefaultWhenNoneSpecified() throws ParseException {

        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        Set<OutputFormat> expected = EnumSet.of(OutputFormat.HTML);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithTABOutputFormatWhenSpecified() throws ParseException {
        String option = "--out-format";
        String value = "TAB";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        Set<OutputFormat> expected = EnumSet.of(OutputFormat.TSV);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithVCFOutputFormatWhenSpecified() throws ParseException {
        String option = "--out-format";
        String value = "VCF";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        Set<OutputFormat> expected = EnumSet.of(OutputFormat.VCF);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }

    @Test
    public void shouldProduceSettingsWithTSVAndVCFOutputFormatWhenSpecified() throws ParseException {
        String option = "--out-format";
        String value = "TAB,VCF";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);

        String[] args = input.split(" ");
        commandLine = parser.parse(options, args);
        ExomiserSettings exomiserSettings = instance.parseCommandLine(commandLine).build();
        
        Set<OutputFormat> expected = EnumSet.of(OutputFormat.VCF, OutputFormat.TSV);

        assertThat(exomiserSettings.getOutputFormats(), equalTo(expected));
    }
}
