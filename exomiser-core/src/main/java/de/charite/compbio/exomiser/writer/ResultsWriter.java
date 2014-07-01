/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import java.util.List;

/**
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface ResultsWriter {
    
    void write(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList);
}
