package de.charite.compbio.exomiser.core.prioritisers;

import java.util.List;

import de.charite.compbio.exomiser.core.model.Gene;
import java.sql.Connection;

/**
 * This interface is implemented by classes that perform prioritization of genes
 * (i.e., {@link de.charite.compbio.exomiser.exome.Gene Gene} objects). In contrast to the classes
 * that implement {@code de.charite.compbio.exomiser.filter.Filter}, which remove variants from
 * further consideration (e.g., because they are not predicted to be at all
 * pathogenic), FilterType is inteded to work on genes (predict the relevance of
 * the gene to the disease, without taking the nature or pathogenicity of any
 * variant into account).
 * <P>
 * It is expected that the Exomizer will combine the evaluations of the Filter
 * and the FilterType evaluations in order to reach a final ranking of the genes
 * and variants into candidate disease-causing mutations.
 *
 * @author Peter N Robinson
 * @version 0.13 (13 May, 2013).
 * @see de.charite.compbio.exomiser.filter.Filter
 */
public interface Priority {

    /**
     * Apply a prioritization algorithm to a list of
     * {@link de.charite.compbio.exomiser.exome.Gene Gene} objects. This will have the side effect
     * of setting the Class variable {@link de.charite.compbio.exomiser.exome.Gene#priorityScore}
     * correspondingly. This, together with the filter scores of the {@link jannovar.exome.Variant Variant}
     * {@link de.charite.compbio.exomiser.exome.Gene Gene} objects can then be used to sort the
     * {@link de.charite.compbio.exomiser.exome.Gene Gene} objects.
     * <p>
     * Note that this may result in the removal of
     * {@link de.charite.compbio.exomiser.exome.Gene Gene} objects if they do not conform to the
     * Prioritizer.
     *
     * @param geneList
     */
    public void prioritizeGenes(List<Gene> geneList);

    /**
     * @return an enum constant representing the type of the implementing class.
     */
    public PriorityType getPriorityType();

    /**
     * @return name of the prioritization method used by the implementing class,
     * e.g., "OMIM"
     */
    public String getPriorityName();


    /**
     * Should this prioritizer be displayed in the HTML page?
     * @return 
     */
    public boolean displayInHTML();

    /**
     * @return HTML code for display in box "Summary of Exome Filtering"
     */
    public String getHTMLCode();

    public List<String> getMessages();

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

    /**
     * @param connection An SQL connection that was initialized elsewhere.
     */
    public void setConnection(Connection connection);

    /**
     * Close the database connection.
     */
    public void closeConnection();

}
