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
    
    /**
     * Writes the result data out to the file specified in the ExomiserSettings object.
     * 
     * @param sampleData
     * @param settings
     * @param filterList
     * @param priorityList 
     */
    public void writeFile(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList);
    
    /**
     * Writes the result data out to a String.
     * 
     * @param sampleData
     * @param settings
     * @param filterList
     * @param priorityList 
     * @return  
     */
    //TODO: Issue #32 https://bitbucket.org/exomiser/exomiser/issue/32/collect-the-filter-and-priority-summary
    public String writeString(SampleData sampleData, ExomiserSettings settings, List<Filter> filterList, List<Priority> priorityList);
}
