/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.cli;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CommandLineOptionTest {
    
    public CommandLineOptionTest() {
    }

    @Test
    public void testValueOf() {
        String name = "CANDIDATE_GENE_OPTION";
        CommandLineOption result = CommandLineOption.valueOf(name);
        assertThat(result, equalTo(CommandLineOption.CANDIDATE_GENE_OPTION));
    }

    @Test
    public void testGetLongOption() {
        CommandLineOption instance = CommandLineOption.CANDIDATE_GENE_OPTION;
        String expResult = "candidate-gene";
        String result = instance.getLongOption();
        assertThat(result, equalTo(expResult));
    }

    @Test
    public void testValueOfLongOption() {
        String value = "disease-id";
        CommandLineOption result = CommandLineOption.valueOfLongOption(value);
        assertThat(result, equalTo(CommandLineOption.DISEASE_ID_OPTION));
    }
    
}
