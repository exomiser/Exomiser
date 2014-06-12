/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.ExomiserOptionsCommandLineParser;
import de.charite.compbio.exomiser.cli.config.CommandLineOptionsConfig;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.util.ExomiserSettings;
import de.charite.compbio.exomiser.util.OutputFormat;
import jannovar.common.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
public class ExomiserOptionsCommandLineParserTest {
    
    @Autowired
    private ExomiserOptionsCommandLineParser instance;
        
    @Test
    public void exomiser_settings_are_null_when_a_vcf_file_was_not_specified() {
        String input = "--ped def.ped -D OMIM:101600 --prioritiser=phenodigm-mgi";
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        assertThat(exomiserSettings, nullValue());    }
    
    @Test
    public void exomiser_settings_are_null_when_a_prioritiser_was_not_specified() {
        String input = "-v 123.vcf --ped def.ped -D OMIM:101600";
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        assertThat(exomiserSettings, nullValue());
        
    }
    
    @Test
    public void command_line_should_specify_a_vcf_file_and_a_prioritiser() {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        assertThat(exomiserSettings, notNullValue());
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_vcf_path() {
        String vcfFile = "123.vcf";
        String input = String.format("-v %s --ped def.ped -D OMIM:101600 --prioritiser=phenodigm-mgi", vcfFile);
        String[] args = input.split(" ");
        
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        assertThat(exomiserSettings.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_vcf_path__using_long_option() {
        String vcfFile = "123.vcf";
        String input = String.format("--vcf %s --ped def.ped -D OMIM:101600 --prioritiser=phenodigm-mgi", vcfFile);
        String[] args = input.split(" ");
        
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        assertThat(exomiserSettings.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_ped_path_if_specified() {
        String pedFile = "ped.ped";
        String input = String.format("-v 123.vcf --ped %s -D OMIM:101600 --prioritiser=phenodigm-mgi", pedFile);
        String[] args = input.split(" ");        
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        assertThat(exomiserSettings.getPedPath(), equalTo(Paths.get(pedFile)));
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_null_ped_path_if_not_specified() {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getPedPath(), nullValue());
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_priority_class() {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertEquals(PriorityType.PHENODIGM_MGI_PRIORITY, exomiserSettings.getPrioritiserType()); 
    }
    
    @Test(expected = NumberFormatException.class)
    public void command_line_parser_should_throw_NumberFormatException_when_passed_non_float_max_freq() {
        String input = "-v 123.vcf -F not_a_float --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_default_maximumFrequency_if_not_set() {
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(100.0f)); 

    }

    @Test
    public void command_line_parser_should_produce_options_with_maximumFrequency_when_set() {
        float frequency = 25.23f;
        String input = String.format("-v 123.vcf -F %.2f --prioritiser=phenodigm-mgi", frequency);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getMaximumFrequency(), equalTo(frequency)); 

    }
    
    @Test
    public void command_line_parser_should_produce_options_with_minimumQuality_when_set() {
        float frequency = 73.12f;
        String input = String.format("-v 123.vcf -Q %.2f --prioritiser=phenodigm-mgi", frequency);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getMinimumQuality(), equalTo(frequency)); 
    }
    
    @Test(expected = NumberFormatException.class)
    public void command_line_parser_should_throw_NumberFormatException_when_passed_non_float_min_qual() {
        String input = "-v 123.vcf -Q not_a_float --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
    }
         
    @Test
    public void command_line_parser_should_produce_options_with_restrict_interval_when_set() {
        String option = "--restrict-interval";
        String value =  "chr2:12345-67890";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getGeneticInterval(), equalTo(value)); 
    }

    @Test
    public void command_line_parser_should_produce_options_with_include_pathogenic_when_set() {
        String option = "--include-pathogenic";
        String input = String.format("-v 123.vcf %s --prioritiser=phenodigm-mgi", option);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.includePathogenic(), is(true)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_include_pathogenic_default_when_not_set() {
        String option = "--include-pathogenic";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.includePathogenic(), is(false)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_remove_dbsnp_when_set() {
        String option = "--remove-dbsnp";
        String input = String.format("-v 123.vcf %s --prioritiser=phenodigm-mgi", option);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.removeDbSnp(), is(true)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_remove_dbsnp_default_when_not_set() {
        String option = "--remove-dbsnp";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.removeDbSnp(), is(false)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_when_remove_off_target_syn_is_set() {
        String option = "--remove-off-target-syn";
        String input = String.format("-v 123.vcf %s --prioritiser=phenodigm-mgi", option);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.removeOffTargetVariants(), is(false)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_remove_off_target_syn_default_when_not_set() {
        String option = "--remove-off-target-syn";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.removeOffTargetVariants(), is(true)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_candidate_gene_when_set() {
        String option = "--candidate-gene";
        String value =  "FGFR2";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getCandidateGene(), equalTo(value)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_hpo_ids_when_set() {
        String option = "--hpo-ids";
        String value =  "HP:0000407,HP:0009830,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0009830");
        expectedList.add("HP:0002858");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_only_hpo_ids_when_set_with_invalid_value() {
        String option = "--hpo-ids";
        //OMIM:100100 is not a valid HPO ID
        String value =  "HP:0000407,OMIM:100100,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0002858");

        assertThat(exomiserSettings.getHpoIds(), equalTo(expectedList)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_seed_genes_when_set() {
        String option = "--seed-genes";
        String value =  "123,456,7890";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        List<Integer> expectedList = new ArrayList();
        expectedList.add(123);
        expectedList.add(456);
        expectedList.add(7890);

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList)); 
    }
    
    @Test(expected = NumberFormatException.class)
    public void command_line_parser_should_throw_parse_error_when_seed_genes_incorrectly_specified_and_return_an_empty_list() {
        String option = "--seed-genes";
        String value =  "gene1:gene2,gene3";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        List<Integer> expectedList = new ArrayList();

        assertThat(exomiserSettings.getSeedGeneList(), equalTo(expectedList)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_disease_id_when_set() {
        String option = "--disease-id";
        String value =  "OMIM:101600";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getDiseaseId(), equalTo(value)); 
    }
                    
    @Test
    public void command_line_parser_should_produce_options_with_DOMINANT_inheritance_mode_when_set() {
        String option = "--inheritance-mode";
        String value =  "AD";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT)); 
    } 
    
    @Test
    public void command_line_parser_should_produce_options_with_RECESSIVE_inheritance_mode_when_set() {
        String option = "--inheritance-mode";
        String value =  "AR";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_RECESSIVE)); 
    }                        
    
    @Test
    public void command_line_parser_should_produce_options_with_X_LINKED_inheritance_mode_when_set() {
        String option = "--inheritance-mode";
        String value =  "X";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.X_RECESSIVE)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_UNINITIALIZED_inheritance_mode_when_not_set() {
        String option = "--inheritance-mode";
        String value =  "X";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_UNINITIALIZED_inheritance_mode_when_value_not_recognised() {
        String option = "--inheritance-mode";
        String value =  "wibble";
        String input = "-v 123.vcf --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_num_genes_greater_than_zero_when_specified() {
        String option = "--num-genes";
        String value =  "42";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getNumberOfGenesToShow(), equalTo(Integer.parseInt(value))); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_outfile_value_when_specified() {
        String option = "--out-file";
        String value =  "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getOutFileName(), equalTo(value)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_HTML_output_format_when_value_not_recognised() {
        String option = "--out-format";
        String value =  "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getOutputFormat(), equalTo(OutputFormat.HTML)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_HTML_output_format_when_not_specified() {

        String input = "-v 123.vcf %s %s --prioritiser=phenodigm-mgi";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getOutputFormat(), equalTo(OutputFormat.HTML)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_TAB_output_format_when_specified() {
        String option = "--out-format";
        String value =  "TAB";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getOutputFormat(), equalTo(OutputFormat.TAB)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_VCF_output_format_when_specified() {
        String option = "--out-format";
        String value =  "VCF";
        String input = String.format("-v 123.vcf %s %s --prioritiser=phenodigm-mgi", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserSettings = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserSettings.getOutputFormat(), equalTo(OutputFormat.VCF)); 
    }
     
}

