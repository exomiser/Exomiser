package de.charite.compbio.exomiser.priority;




import java.util.List;

import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import java.sql.Connection;

/**
 * This interface is implemented by classes that perform prioritization of
 * genes (i.e., {@link exomizer.exome.Gene Gene} objects). In contrast to the
 * classes that implement {@code exomizer.filter.Filter}, which remove variants
 from further consideration (e.g., because they are not predicted to be at all
 pathogenic), FilterType is inteded to work on genes (predict the relevance of the 
 gene to the disease, without taking the nature or pathogenicity of any variant into account).
 <P>
 It is expected that the Exomizer will combine the evaluations of the Filter and the FilterType
 evaluations in order to reach a final ranking of the genes and variants into candidate
 disease-causing mutations.  
 * @author Peter N Robinson
 * @version 0.13 (13 May, 2013).
 * @see  exomizer.filter.Filter
 */
public interface Priority {

    /** 
     * Apply a prioritization algorithm to a list of {@link exomizer.exome.Gene Gene} objects.
     * This will have the side effect of setting the Class variable {@link exomizer.exome.Gene#priorityScore} 
     * correspondingly. This, together with the filter scores of the {@link jannovar.exome.Variant Variant} 
     * {@link exomizer.exome.Gene Gene} objects can then be used to sort the 
     * {@link exomizer.exome.Gene Gene} objects.
     * <p>
     * Note that this may result in the removal of
     * {@link exomizer.exome.Gene Gene} objects if they do not conform to the Prioritizer.
     * @param geneList
    */ 
    public void prioritizeGenes(List<Gene> geneList);

    /**
     * @return an enum constant representing the type of the implementing class.
     */
    public PriorityType getPriorityTypeConstant();

    /**
     * @return name of the prioritization method used by the implementing class, e.g., "OMIM"
     */
    public String getPriorityName();

    /**
     * Set parameters of prioritizer if needed.
     * @param par A String with the parameters (usually extracted from the cmd line) for this prioiritizer)
     */
    public void setParameters(String par)  throws ExomizerInitializationException;

    /**
     * Should this prioritizer be displayed in the HTML page? 
     */
    public boolean displayInHTML();
    /** 
     * @return HTML code for display in box "Summary of Exome Filtering"
     */
    public String getHTMLCode();

    public List<String> getMessages();

     /** Get number of variants before filter was applied */
    public int getBefore();
    /** Get number of variants after filter was applied */
    public int getAfter();

    /**
     * @param connection An SQL connection that was initialized elsewhere.
     */
    public void setDatabaseConnection(Connection connection) throws ExomizerInitializationException;


}