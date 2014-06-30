package de.charite.compbio.exomiser.filter;

import java.util.List;

/**
 * This interface is implemented by classes that perform filtering of the
 * variants in the VCF file according to various criteria. A VariantScore object gets
 attached to each Variant object. The function passesFilter can be used to
 find out the results of the filter and the variant can be deleted if it did
 not pass the filter.
 *
 * @author Peter N Robinson
 * @version 0.02 (16 April, 2012)
 */
public interface VariantScore {

    /**
     * @return true if the variant being analyzed passes the filter (e.g., is
     * rare, pathogenic, or has high quality reads)
     */
    public boolean passesFilter();

    /**
     * @return return a float representation of the filter result [0..1]. If the
     * result is boolean, return 0.0 for false and 1.0 for true
     */
    public float filterResult();

    /**
     * @return A string with a summary of the filtering results .
     */
    public String getFilterResultSummary();

    /**
     * @return A list with detailed results of filtering. The list is intended
     * to be displayed as an HTML list if desired.
     */
    public List<String> getFilterResultList();

    /**
     * @return HTML code for a cell representing the current triage result.
     */
    public String getHTMLCode();

}
