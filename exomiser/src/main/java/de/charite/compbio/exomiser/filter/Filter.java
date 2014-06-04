package de.charite.compbio.exomiser.filter;


import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import java.util.List;

/**
 * This interface is implemented by classes that perform filtering of
 * the <b>variants</b> in the VCF file according to various criteria. A Triage object
 * gets attached to each Variant object. 
 * <P>
 * Note that classes that implement the interface {@link exomizer.priority.Priority Priority}
 * are responsible for gene-level filtering.
 * @author Peter N Robinson
 * @version 0.07 (April 28, 2013).
 * @see  exomizer.priority.IPriority
 */
public interface Filter {
    
    /**
     * Set some user supplied parameter for the filter to be used.
     * @param par
     */
    public void setParameters(String par) throws ExomizerInitializationException;
    
    /**
     * Take a list of variants and apply the filter to each variant. If a
     * variant does not pass the filter, remove it.
     * @param variantEvaluationtList
     */
    public void filterVariants(List<VariantEvaluation> variantEvaluationtList);
    
    /**
     * get a list of messages that represent the process and result of applying
     * the filter. This list can be used to make an HTML list for explaining the
     * result to users (for instance).
     * @return 
     */
    public List<String> getMessages();

    public String getFilterName();
    
    /**
     * @return an integer constant (as defined in exomizer.common.Constants)
     * that will act as a flag to generate the output HTML dynamically depending
     * on the filters that the user has chosen.
     */
    public FilterType getFilterType();

//    /**
//     * @param connection An SQL (postgres) connection that was initialized elsewhere.
//     * @throws de.charite.compbio.exomiser.exception.ExomizerInitializationException
//     */
//    //TODO: remove and use DI framework
//    public void setDatabaseConnection(Connection connection) throws ExomizerInitializationException;

    /**
     * Get number of variants before filter was applied
     * @return 
     */
    public int getBefore();
    /**
     * Get number of variants after filter was applied
     * @return 
     */
    public int getAfter();

    /** Should this Filter be shown in the HTML output?
     * @return  */
    public boolean displayInHTML();
    

}