package de.charite.compbio.exomiser.filter;


import java.util.List;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.VariantEvaluation;

/**
 * This interface is implemented by classes that perform filtering of
 * the <b>variants</b> in the VCF file according to various criteria. An ITriage object
 * gets attached to each Variant object. 
 * <P>
 * Note that classees that implement the interface {@link exomizer.priority.IPriority IPriority}
 * are responsible for gene-level filtering.
 * @author Peter N Robinson
 * @version 0.07 (April 28, 2013).
 * @see  exomizer.priority.IPriority
 */
public interface Filter {
    
    /** Set some user supplied parameter for the filter to be used. */
    public void set_parameters(String par) throws ExomizerInitializationException;
    /** Take a list of variants and apply the filter to each variant. If a variant does not
	pass the filter, remove it. */
    public void filter_list_of_variants(List<VariantEvaluation> variant_list);
    /** get a list of messages that represent the process and result of applying the filter. This
	list can be used to make an HTML list for explaining the result to users (for instance).
    */
    public List<String> getMessages();

    public String getFilterName();
    /** @return an integer constant (as defined in exomizer.common.Constants) that will act
     * as a flag to generate the output HTML dynamically depending on the filters that the 
     * user has chosen.
     */
    public FilterType getFilterTypeConstant();

    /**
     * @param connection An SQL (postgres) connection that was initialized elsewhere.
     */
    public void setDatabaseConnection(java.sql.Connection connection) throws ExomizerInitializationException;

    /** Get number of variants before filter was applied */
    public int getBefore();
    /** Get number of variants after filter was applied */
    public int getAfter();

    /** Should this Filter be shown in the HTML output? */
    public boolean display_in_HTML();
    

}