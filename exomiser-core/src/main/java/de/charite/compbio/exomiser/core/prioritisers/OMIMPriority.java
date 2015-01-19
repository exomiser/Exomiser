package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to do two things. First, it will add annotations to
 * genes based on their annotations to OMIM or Orphanet disease entries in the
 * exomiser database (Note that the app PopulateExomiserDatabase.jar, from this
 * software package is used to put the data into the database; see there for
 * more information). The tables <b>omim</b> and <b>orphanet</b> are used to
 * store/retrieve this information. The second purpose of this class is to check
 * whether the variants found in the VCF file match with the mode of inheritance
 * listed for the disease (column "inheritance" of the omim table; TODO-add
 * similar functionality for Orphanet). Thus, if we find a heterozygous mutation
 * but the disease is autosomal recessive, then it the corresponding
 * disease/gene is not a good candidate, and its OMIM relevance score is reduced
 * by a factor of 50%. See the function {@link #getInheritanceFactor} for
 * details on this weighting scheme.
 *
 * @author Peter N Robinson
 * @version 0.16 (28 January,2014)
 */
public class OMIMPriority implements Priority {

    private static final Logger logger = LoggerFactory.getLogger(OMIMPriority.class);
    /**
     * Database handle to the postgreSQL database used by this application.
     */
    private Connection connection = null;
    /**
     * A prepared SQL statement for OMIM entries.
     */
    private PreparedStatement omimQuery = null;
    /**
     * A prepared SQL statement for Orphanet entries.
     */
    private PreparedStatement orphanetQuery = null;

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private List<String> messages = null;

    public OMIMPriority() {
        this.messages = new ArrayList<String>();
    }

    @Override
    public String getPriorityName() {
        return "OMIM";
    }

    /**
     * Flag for output field representing OMIM.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.OMIM_PRIORITY;
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of frequency filtering.
     */
    public List<String> getMessages() {
        return this.messages;
    }

    /**
     * For now, this method just annotates each gene with OMIM data, if
     * available, and shows a link in the HTML output. However, we can use this
     * method to implement a Phenomizer-type prioritization at a later time
     * point.
     *
     * @param genes A list of the {@link exomizer.exome.Gene Gene} objects
     * that have suvived the filtering (i.e., have rare, potentially pathogenic
     * variants).
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {
        for (Gene g : genes) {
            OMIMPriorityResult mimrel = retrieveOmimData(g);
            g.addPriorityResult(mimrel);
        }
        closeConnection();
    }

    /**
     * Note that if there is no EntrezGene IDfor this gene, its field
     * entrezGeneID will be set to -10. If this is the case, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     * @param g The gene which is being evaluated.
     */
    private OMIMPriorityResult retrieveOmimData(Gene g) {
        OMIMPriorityResult rel = new OMIMPriorityResult();
        int entrez = g.getEntrezGeneID();
        if (entrez < 0) {
            return rel; /* Return an empty relevance score object. */

        }
        try {
            this.omimQuery.setInt(1, entrez);
            ResultSet rs = omimQuery.executeQuery();

            while (rs.next()) { /* The way the db was constructed, there is just one line for each such query. */

                //  phenmim,genemim,diseasename,type"+

                String phenmim = rs.getString(1);
                String genemim = rs.getString(2);
                String disease = rs.getString(3);
                char typ = rs.getString(4).charAt(0);
                char inheritance = rs.getString(5).charAt(0);
                float factor = getInheritanceFactor(g, inheritance);
                // System.out.println(preparedQuery);
                rel.addRow(phenmim, genemim, disease, typ, inheritance, factor);
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("Error executing OMIM query", e);
        }
        // Now try to get the Orphanet data 
        try {
            orphanetQuery.setInt(1, entrez);
            ResultSet rs = orphanetQuery.executeQuery();
            while (rs.next()) {
                String orphanum = rs.getString(1);
                String disease = rs.getString(2);
                rel.addOrphanetRow(orphanum, disease);
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("Exception caused by Orphanet query!", e);
        }

        return rel;
    }

    /**
     * Prepare the SQL query statements required for this filter.
     * <p>
     * SELECT phenmim,genemim,diseasename,type</br>
     * FROM omim</br>
     * WHERE gene_id = ? </br>
     */
    private void setUpSQLPreparedStatement() {
        String query = "SELECT disease_id, omim_gene_id, diseasename, type, inheritance "
                + "FROM disease "
                + "WHERE disease_id LIKE '%OMIM%' AND gene_id = ?";
        try {
            omimQuery = connection.prepareStatement(query);
        } catch (SQLException e) {
            logger.error("Problem setting up OMIM SQL query: {}", query, e);
        }
        /* Now the same for Orphanet. */
        query = "SELECT disease_id, diseasename "
                + "FROM disease "
                + "WHERE disease_id LIKE '%ORPHA%' AND gene_id = ?";
        try {
            orphanetQuery = connection.prepareStatement(query);
        } catch (SQLException e) {
            logger.error("Problem setting up Orphanet SQL query: {}", query, e);
        }
    }

    /**
     * This function checks whether the mode of inheritance of the disease
     * matches the observed pattern of variants. That is, if the disease is
     * autosomal recessive and we have just one heterozygous mutation, then the
     * disease is probably not the correct diagnosis, and we assign it a factor
     * of 0.5. Note that hemizygous X chromosomal variants are usually called as
     * homozygous ALT in VCF files, and thus it is not reliable to distinguish
     * between X-linked recessive and dominant inheritance. Therefore, we return
     * 1 for any gene with X-linked inheritance if the disease in question is
     * listed as X chromosomal.
     */
    private float getInheritanceFactor(Gene g, char inheritance) {
        if (inheritance == 'U') {
            /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
            return 1f;
        } else if (g.isConsistentWithDominant() && (inheritance == 'D' || inheritance == 'B')) {
            /* inheritance of disease is dominant or both (dominant/recessive) */
            return 1f;
        } else if (g.isConsistentWithRecessive() && (inheritance == 'R' || inheritance == 'B')) {
            /* inheritance of disease is recessive or both (dominant/recessive) */
            return 1f;
        } else if (g.isXChromosomal() && inheritance == 'X') {
            return 1f;
        } else if (inheritance == 'Y') {
            return 1f; /* Y chromosomal, rare. */

        } else if (inheritance == 'M') {
            return 1f; /* mitochondrial. */

        } else if (inheritance == 'S') {
            return 0.5f; /* gene only associated with somatic mutations */

        } else if (inheritance == 'P') {
            return 0.5f; /* gene only associated with polygenic */

        } else {
            return 0.5f;
        }
    }

    /**
     * Initialize the database connection and call
     * {@link #setUpSQLPreparedStatement}
     *
     * @param connection A connection to a postgreSQL database from the exomizer
     * or tomcat.
     */
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
        setUpSQLPreparedStatement();
    }

    @Override
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.error(null, ex);
        }
    }
    
    /**
     * Since no filtering of prioritizing is done with the OMIM data for now, it
     * does not make sense to display this in the HTML table.
     *
     * @return
     */
    @Override
    public boolean displayInHTML() {
        return false;
    }

    @Override
    public String getHTMLCode() {
        return "";
    }

    /**
     * Get number of variants before filter was applied TODO
     */
    public int getBefore() {
        return 0;
    }

    /**
     * Get number of variants after filter was applied TODO
     */
    public int getAfter() {
        return 0;
    }

}
