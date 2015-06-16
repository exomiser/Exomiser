/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core;

import de.charite.compbio.exomiser.core.AnalysisParser.AnalysisFileNotFoundException;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class AnalysisParserTest {

    private AnalysisParser instance;

    @Before
    public void setUp() {
        instance = new AnalysisParser();
    }

    @Test
    public void testParse() {
        Analysis analysis  = instance.parse("src/test/resources/analysisExample.yml");
        System.out.println(analysis);
    }

    @Test(expected = AnalysisFileNotFoundException.class)
    public void testParseNonExistentFile() {
        instance.parse("src/test/resources/wibble");
    }

}
