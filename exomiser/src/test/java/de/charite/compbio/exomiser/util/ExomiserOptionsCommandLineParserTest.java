/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.util;

import de.charite.compbio.exomiser.config.CommandLineOptionsConfig;
import de.charite.compbio.exomiser.priority.MGIPhenodigmPriority;
import jannovar.common.ModeOfInheritance;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.ParseException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
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
        
    @Test(expected = ParseException.class)
    public void command_line_should_specify_a_vcf_file() throws ParseException {
        String input = "--ped def.ped -D OMIM:101600 --prioritiser=mgi-phenodigm";
        String[] args = input.split(" ");
        instance.parseCommandLineArguments(args);
    }
    
    @Test(expected = ParseException.class)
    public void command_line_should_specify_a_prioritiser() throws ParseException {
        String input = "-v 123.vcf --ped def.ped -D OMIM:101600";
        String[] args = input.split(" ");
        instance.parseCommandLineArguments(args);
    }
    
    @Test
    public void command_line_should_specify_a_vcf_file_and_a_prioritiser() throws ParseException {
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        assertThat(exomiserOptions, notNullValue());
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_vcf_path() throws ParseException {
        String vcfFile = "123.vcf";
        String input = String.format("-v %s --ped def.ped -D OMIM:101600 --prioritiser=mgi-phenodigm", vcfFile);
        String[] args = input.split(" ");
        
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        assertThat(exomiserOptions.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_vcf_path__using_long_option() throws ParseException {
        String vcfFile = "123.vcf";
        String input = String.format("--vcf %s --ped def.ped -D OMIM:101600 --prioritiser=mgi-phenodigm", vcfFile);
        String[] args = input.split(" ");
        
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        assertThat(exomiserOptions.getVcfPath(), equalTo(Paths.get(vcfFile)));
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_ped_path_if_specified() throws ParseException {
        String pedFile = "ped.ped";
        String input = String.format("-v 123.vcf --ped %s -D OMIM:101600 --prioritiser=mgi-phenodigm", pedFile);
        String[] args = input.split(" ");        
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        assertThat(exomiserOptions.getPedPath(), equalTo(Paths.get(pedFile)));
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_null_ped_path_if_not_specified() throws ParseException {
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getPedPath(), nullValue());
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_a_priority_class() throws ParseException {
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertEquals(MGIPhenodigmPriority.class, exomiserOptions.getPrioritiserClass()); 
    }
    
    @Test(expected = NumberFormatException.class)
    public void command_line_parser_should_throw_NumberFormatException_when_passed_non_float_max_freq() throws ParseException {
        String input = "-v 123.vcf -F not_a_float --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_default_maximumFrequency_if_not_set() throws ParseException {
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getMaximumFrequency(), equalTo(100.0f)); 

    }

    @Test
    public void command_line_parser_should_produce_options_with_maximumFrequency_when_set() throws ParseException {
        float frequency = 25.23f;
        String input = String.format("-v 123.vcf -F %.2f --prioritiser=mgi-phenodigm", frequency);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getMaximumFrequency(), equalTo(frequency)); 

    }
    
    @Test
    public void command_line_parser_should_produce_options_with_minimumQuality_when_set() throws ParseException {
        float frequency = 73.12f;
        String input = String.format("-v 123.vcf -Q %.2f --prioritiser=mgi-phenodigm", frequency);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getMinimumQuality(), equalTo(frequency)); 
    }
    
    @Test(expected = NumberFormatException.class)
    public void command_line_parser_should_throw_NumberFormatException_when_passed_non_float_min_qual() throws ParseException {
        String input = "-v 123.vcf -Q not_a_float --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
    }
         
    @Test
    public void command_line_parser_should_produce_options_with_restrict_interval_when_set() throws ParseException {
        String option = "--restrict-interval";
        String value =  "chr2:12345-67890";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getGeneticInterval(), equalTo(value)); 
    }

    @Test
    public void command_line_parser_should_produce_options_with_include_pathogenic_when_set() throws ParseException {
        String option = "--include-pathogenic";
        String input = String.format("-v 123.vcf %s --prioritiser=mgi-phenodigm", option);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.includePathogenic(), is(true)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_include_pathogenic_default_when_not_set() throws ParseException {
        String option = "--include-pathogenic";
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.includePathogenic(), is(false)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_remove_dbsnp_when_set() throws ParseException {
        String option = "--remove-dbsnp";
        String input = String.format("-v 123.vcf %s --prioritiser=mgi-phenodigm", option);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.removeDbSnp(), is(false)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_remove_dbsnp_default_when_not_set() throws ParseException {
        String option = "--remove-dbsnp";
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.removeDbSnp(), is(true)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_when_remove_off_target_syn_is_set() throws ParseException {
        String option = "--remove-off-target-syn";
        String input = String.format("-v 123.vcf %s --prioritiser=mgi-phenodigm", option);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.removeOffTargetVariants(), is(false)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_remove_off_target_syn_default_when_not_set() throws ParseException {
        String option = "--remove-off-target-syn";
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.removeOffTargetVariants(), is(true)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_candidate_gene_when_set() throws ParseException {
        String option = "--candidate-gene";
        String value =  "FGFR2";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getCandidateGene(), equalTo(value)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_hpo_ids_when_set() throws ParseException {
        String option = "--hpo-ids";
        String value =  "HP:0000407,HP:0009830,HP:0002858";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        List<String> expectedList = new ArrayList();
        expectedList.add("HP:0000407");
        expectedList.add("HP:0009830");
        expectedList.add("HP:0002858");

        assertThat(exomiserOptions.getHpoIds(), equalTo(expectedList)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_seed_genes_when_set() throws ParseException {
        String option = "--seed-genes";
        String value =  "gene1,gene2,gene3";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        List<String> expectedList = new ArrayList();
        expectedList.add("gene1");
        expectedList.add("gene2");
        expectedList.add("gene3");

        assertThat(exomiserOptions.getSeedGeneList(), equalTo(expectedList)); 
    }
    
    @Test
    public void command_line_parser_should_throw_parse_error_when_seed_genes_incorrectly_specified() throws ParseException {
        String option = "--seed-genes";
        String value =  "gene1:gene2;gene3";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        List<String> expectedList = new ArrayList();

        assertThat(exomiserOptions.getSeedGeneList(), equalTo(expectedList)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_disease_id_when_set() throws ParseException {
        String option = "--disease-id";
        String value =  "OMIM:101600";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getDiseaseId(), equalTo(value)); 
    }
                    
    @Test
    public void command_line_parser_should_produce_options_with_DOMINANT_inheritance_mode_when_set() throws ParseException {
        String option = "--inheritance-mode";
        String value =  "AD";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_DOMINANT)); 
    } 
    
    @Test
    public void command_line_parser_should_produce_options_with_RECESSIVE_inheritance_mode_when_set() throws ParseException {
        String option = "--inheritance-mode";
        String value =  "AR";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getModeOfInheritance(), equalTo(ModeOfInheritance.AUTOSOMAL_RECESSIVE)); 
    }                        
    
    @Test
    public void command_line_parser_should_produce_options_with_X_LINKED_inheritance_mode_when_set() throws ParseException {
        String option = "--inheritance-mode";
        String value =  "X";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getModeOfInheritance(), equalTo(ModeOfInheritance.X_RECESSIVE)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_UNINITIALIZED_inheritance_mode_when_not_set() throws ParseException {
        String option = "--inheritance-mode";
        String value =  "X";
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_UNINITIALIZED_inheritance_mode_when_value_not_recognised() throws ParseException {
        String option = "--inheritance-mode";
        String value =  "wibble";
        String input = "-v 123.vcf --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getModeOfInheritance(), equalTo(ModeOfInheritance.UNINITIALIZED)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_num_genes_greater_than_zero_when_specified() throws ParseException {
        String option = "--num-genes";
        String value =  "42";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getNumberOfGenesToShow(), equalTo(Integer.parseInt(value))); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_outfile_value_when_specified() throws ParseException {
        String option = "--out-file";
        String value =  "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getOutFileName(), equalTo(value)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_HTML_output_format_when_value_not_recognised() throws ParseException {
        String option = "--out-format";
        String value =  "wibble";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getOutputFormat(), equalTo(OutputFormat.HTML)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_HTML_output_format_when_not_specified() throws ParseException {

        String input = "-v 123.vcf %s %s --prioritiser=mgi-phenodigm";
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getOutputFormat(), equalTo(OutputFormat.HTML)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_TAB_output_format_when_specified() throws ParseException {
        String option = "--out-format";
        String value =  "TAB";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getOutputFormat(), equalTo(OutputFormat.TAB)); 
    }
    
    @Test
    public void command_line_parser_should_produce_options_with_VCF_output_format_when_specified() throws ParseException {
        String option = "--out-format";
        String value =  "VCF";
        String input = String.format("-v 123.vcf %s %s --prioritiser=mgi-phenodigm", option, value);
        
        String[] args = input.split(" ");
        ExomiserSettings exomiserOptions = instance.parseCommandLineArguments(args);
        
        assertThat(exomiserOptions.getOutputFormat(), equalTo(OutputFormat.VCF)); 
    }
     
}

