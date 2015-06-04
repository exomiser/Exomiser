package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
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
public class OMIMPriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(OMIMPriority.class);

    private DataSource dataSource;

    /**
     * Flag for output field representing OMIM.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.OMIM_PRIORITY;
    }

    /**
     * For now, this method just annotates each gene with OMIM data, if
     * available, and shows a link in the HTML output. However, we can use this
     * method to implement a Phenomizer-type prioritization at a later time
     * point.
     *
     * @param genes A list of the {@link exomizer.exome.Gene Gene} objects that
     * have suvived the filtering (i.e., have rare, potentially pathogenic
     * variants).
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {
        for (Gene g : genes) {
            OMIMPriorityResult mimrel = retrieveOmimData(g);
            g.addPriorityResult(mimrel);
        }
    }

    /**
     * Note that if there is no EntrezGene IDfor this gene, its field
     * entrezGeneID will be set to -10. If this is the case, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     * @param gene The gene which is being evaluated.
     */
    private OMIMPriorityResult retrieveOmimData(Gene gene) {
        OMIMPriorityResult priorityResult = new OMIMPriorityResult();
        int entrez = gene.getEntrezGeneID();
        if (entrez < 0) {
            return priorityResult; 

        }
        getOmimDiseasesForGene(gene, priorityResult);
//        findOrphaNetDiseasesForGene(gene, priorityResult);

        return priorityResult;
    }

    private void getOmimDiseasesForGene(Gene gene, OMIMPriorityResult rel) {
        String omimQuery = "SELECT disease_id, omim_gene_id, diseasename, type, inheritance "
                + "FROM disease "
                + "WHERE gene_id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(omimQuery);
            preparedStatement.setInt(1, gene.getEntrezGeneID());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                // The way the db was constructed, there is just one line for each such query.
                //  phenmim,genemim,diseasename,type"+
                String diseaseId = rs.getString(1);
                String diseaseName = rs.getString(3);
                if (diseaseId.startsWith("OMIM")) {
                    String omimGeneId = rs.getString(2);
                    char typ = rs.getString(4).charAt(0);
                    char inheritance = rs.getString(5).charAt(0);
                    float factor = getInheritanceFactor(gene, inheritance);
                    rel.addRow(diseaseId, omimGeneId, diseaseName, typ, inheritance, factor);            
                } else {
                    rel.addOrphanetRow(diseaseId, diseaseName);

                }
            }
        } catch (SQLException e) {
            logger.error("Error executing OMIM query", e);
        }
    }

    private void findOrphaNetDiseasesForGene(Gene gene, OMIMPriorityResult rel) {
        // Now try to get the Orphanet data
        String orphanetQuery = "SELECT disease_id, diseasename "
                + "FROM disease "
                + "WHERE disease_id LIKE '%ORPHA%' AND gene_id = ?";
        
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(orphanetQuery);
            preparedStatement.setInt(1, gene.getEntrezGeneID());
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String orphanum = rs.getString(1);
                String disease = rs.getString(2);
                rel.addOrphanetRow(orphanum, disease);
            }
        } catch (SQLException e) {
            logger.error("Exception caused by Orphanet query!", e);
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
    private float getInheritanceFactor(Gene gene, char inheritance) {
        if (inheritance == 'U') {
            /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
            return 1f;
        } else if (gene.isConsistentWithDominant() && (inheritance == 'D' || inheritance == 'B')) {
            /* inheritance of disease is dominant or both (dominant/recessive) */
            return 1f;
        } else if (gene.isConsistentWithRecessive() && (inheritance == 'R' || inheritance == 'B')) {
            /* inheritance of disease is recessive or both (dominant/recessive) */
            return 1f;
        } else if (gene.isXChromosomal() && inheritance == 'X') {
            return 1f;
        } else if (inheritance == 'Y') {
            /* Y chromosomal, rare. */
            return 1f; 

        } else if (inheritance == 'M') {
            /* mitochondrial. */
            return 1f; 

        } else if (inheritance == 'S') {
            /* gene only associated with somatic mutations */
            return 0.5f; 

        } else if (inheritance == 'P') {
            /* gene only associated with polygenic */
            return 0.5f; 

        } else {
            return 0.5f;
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of frequency filtering.
     */
    @Override
    public List<String> getMessages() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OMIMPriority other = (OMIMPriority) obj;
        return true;
    }
  
    @Override
    public String toString() {
        return getPriorityType().getCommandLineValue();
    } 
}
