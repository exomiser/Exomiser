/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import jannovar.exome.VariantTypeCounter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.instanceOf;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OriginalHtmlResultsWriterTest {

    OriginalHtmlResultsWriter instance;

    public OriginalHtmlResultsWriterTest() {
        instance = new OriginalHtmlResultsWriter();
    }

    @Test
    public void testWrite() {
        SampleData sampleData = new SampleData();
        sampleData.setGeneList(new ArrayList<Gene>());
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWriteOriiginal.html").build();
        List<Filter> filterList = new ArrayList<>();
        List<Priority> priorityList = new ArrayList<>();
        //TODO: make this work!
        // when the Filter and Results have been decoupled this should mean we 
        //don't have to run the entire program just to see some formatted data being written.   
//        instance.writeFile(sampleData, settings, filterList, priorityList);
//        assertTrue(Paths.get("testWriteOriiginal.html").toFile().exists());
//        assertTrue(Paths.get("testWriteOriiginal.html").toFile().delete());
    }

    @Test
    public void testGetVariantTypeCounter() {
        System.out.println("getVariantTypeCounter");
        List<Filter> filterList = new ArrayList<>();
        List<VariantEvaluation> variantList = new ArrayList<>();
        //TODO: make this work!
//        VariantTypeCounter result = instance.getVariantTypeCounter(filterList, variantList);
//        assertThat(result, instanceOf(VariantTypeCounter.class));
    }

}
