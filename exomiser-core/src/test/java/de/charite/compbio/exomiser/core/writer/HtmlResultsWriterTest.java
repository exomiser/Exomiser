/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writer;

import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HtmlResultsWriterTest {

    HtmlResultsWriter instance;

    public HtmlResultsWriterTest() {
        instance = new HtmlResultsWriter();
    }

    @Test
    public void testWrite() {
        SampleData sampleData = new SampleData();
        sampleData.setGenes(new ArrayList<Gene>());
        sampleData.setVariantEvaluations(new ArrayList<VariantEvaluation>());

        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite.html").build();
        List<Priority> priorityList = null;
        //TODO: make this work! Requires some results to writeFile out...
        // when the Filter and Results have been decoupled this should mean we 
        //don't have to run the entire program just to see some formatted data being written.
        instance.writeFile(sampleData, settings, priorityList);
        assertTrue(Paths.get("testWrite.html").toFile().exists());
//        assertTrue(Paths.get("testWrite.html").toFile().delete());
    }

}
