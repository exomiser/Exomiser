package org.monarchinitiative.exomiser.core.prioritisers.reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This class encapsulates a network derived from the STRING protein-protein
 * interaction network. It is designed to be initialized by the seed genes
 * chosen by the user of ExomeWalker. It then goes and identifies the degree one
 * neighbors of these genes. It can then be used to quickly answer the question
 * whether a candidate gene is related to the seed genes or any of their
 * neighbors. This class gets its information from the Exomiser database and
 * expects to find the <b>string</b> table.
 *
 * @see exomizer.io.STRINGParser
 * @author Peter Robinson
 * @version 0.03 (10 February, 2014)
 */
public class STRINGNetwork implements Network {

    private final Logger logger = LoggerFactory.getLogger(STRINGNetwork.class);
    /**
     * Handle to the SQL database (postgreSQL)
     */
    private Connection connection = null;
    /**
     * This hashmap contains as Keys Entrez Gene ids for genes; The
     * corresponding values are lists of String representing interaction paths
     * that end at the gene and start at one of the seed genes. The paths can be
     * one or two interactions in length (first- or second-degree interactions).
     */
    private Map<Integer, List<String>> interactionPath = null;
    /**
     * A set of the entrez gene ids of the seed genes. We use this to avoid
     * entering paths that cycle back to one of the seed genes from the first
     * level paths (it is OK if a candidate gene is also a seed gene, then we
     * show these paths).
     */
    private Set<Integer> seedGeneIdSet = null;

    /**
     * @param c A connection to the postgreSQL Exomiser database.
     * @param seedGeneList A comma-separated list of Entrez Gene ids
     * representing the seed genes chosen by the user (e.g., all genes belonging
     * to a phenotypic family).
     */
    public STRINGNetwork(Connection c, String seedGeneList) {
        this.connection = c;
        this.interactionPath = new HashMap<>();
        this.seedGeneIdSet = new HashSet<>();
        logger.info("initialising seed genes");
        initializeSeedGenesAndNeighbors(seedGeneList);
    }

    /**
     * This method searches in the table entrez2sym for the Gene Symbol
     * corresponding to a given entrez gene id. For instance, the id 2200
     * corresponds to the symbol FBN1.
     *
     * @param entrezID an NCBI Entrez Gene ID for a gene
     * @return The gene symbol corresponding to the entrez gene or null if no
     * symbol can be found
     */
    private String getSymbol(Integer entrezID) {
        String query = "SELECT symbol FROM entrez2sym WHERE entrezID = ?;";
        try {
            PreparedStatement ps = this.connection.prepareStatement(query);
            ps.setInt(1, entrezID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String symbol = rs.getString(1);
                return symbol;
            } else {
                return null;
            }
        } catch (SQLException e) {
            logger.error("STRINGNetwork, could not retrieve gene symbol", e);
        }
        return null;
    }

    /**
     * Returns an array list with all first and second degree paths to the gene
     * represented by entrezID. If there are no interactions, it returns an
     * empty arraylist.
     *
     * @param entrezID an NCBI Entrez Gene ID for a gene (one of the candidate
     * genes from the exome)
     * @return List of first- and second degree interaction paths connection a
     * seed gene to the candidate gene.
     */
    public List<String> evalCandidateGene(Integer entrezID) {
        if (this.interactionPath.containsKey(entrezID)) {
            return this.interactionPath.get(entrezID);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Adds a one- or two-interaction path to {@link #interactionPath}. The key
     * is the gene at the end of the chain, i.e., on the other side of the path
     * from a seed gene.
     *
     * @param id Entrez ID of a gene interacting (path: 1 or 2) with a seed gene
     * @param path String representation of path, e.g. ABC1 <-> DEF2 <->GHI3.
     */
    private void addPathToMap(Integer id, String path) {
        if (!this.interactionPath.containsKey(id)) {
            this.interactionPath.put(id, new ArrayList<String>());
        }
        List<String> lst = this.interactionPath.get(id);
        lst.add(path);
    }

    /**
     * This method is called after a length-one path has been found from a gene
     * that is connected to a seed gene. The method adds all direct interaction
     * partners, so that in the end we have paths of length two from the seed
     * genes. For example, say we have a seed gene ABC1. Then we might use
     * {@link #addSinglePath} to creat ethe path <code>ABC1 <-> DEF2</code>.
     * This method will extend that path to <code>ABC1 <-> DEF2 <-> GHI3</code>.
     *
     * @param neighborID Entrez Gene id of a gene that is neighbor to a seed
     * gene.
     * @param seedID the seed gene that neighborIDinteracts with
     * @param pathToDate String representation of the degree-one path coming
     * from seedID, e.g., ABC1 <-> DEF2.
     */
    private void addDoublePath(Integer neighborID, Integer seedID, String pathToDate) {
        String query = "SELECT entrezID,symbol FROM entrez2sym,string WHERE "
                + "(entrezID = entreza AND entrezb=?) OR (entrezID = entrezb AND entreza=?);";
        try {
            PreparedStatement ps = this.connection.prepareStatement(query);
            ps.setInt(1, neighborID);
            ps.setInt(2, neighborID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                if (id == seedID) {
                    continue; /* this is a self-loop from a seed gene to another gene and back. 
                     We can skip it since we do not want to display this on the
                     website. */

                }
                String symbol = rs.getString(2);
                if (symbol == null || symbol.length() == 0) {
                    logger.error("Double symbol null in addDoublePath for id {}", id);
                    continue;
                }
                String path = String.format("%s&hArr;%s", pathToDate, symbol);
                addPathToMap(id, path);
            }
        } catch (SQLException e) {
            logger.error("SQLException {}", e.getMessage());
        }
    }

    /**
     * This method is called to add interaction paths from the seed genes
     * themselves.
     *
     * @param seedID NCBI entrez gene id of a seed gene.
     * @param seedSymbol The gene symbol corresponding to the entrezID.
     */
    private void addSinglePath(Integer seedID, String seedSymbol) {
        // H2 seemed to have an issue with doing this query with an OR so separated into 2 queries
//        String query = "SELECT entrezID,symbol FROM entrez2sym,string WHERE "+
//	    "(entrezID = entreza AND entrezb=?) OR (entrezID = entrezb AND entreza=?);";
        String query1 = "SELECT entrezID,symbol FROM entrez2sym,string WHERE "
                + "(entrezID = entreza AND entrezb=?);";
        try {
            PreparedStatement ps = this.connection.prepareStatement(query1);
            ps.setInt(1, seedID);
            ps.setInt(2, seedID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String symbol = rs.getString(2);
                if (symbol == null) {
                    logger.error("id {} symbol is null", id);
                    continue;
                }
                String path = String.format("%s&hArr;%s", seedSymbol, symbol);
                addPathToMap(id, path);
                if (this.seedGeneIdSet.contains(id)) {
                    continue; /* Do not add double path if the first node also belongs to the seed family. */

                }
                addDoublePath(id, seedID, path); /*
                 * id is the Entrez Gene id of the gene that interacts with the
                 * seed gene (seedID).
                 */

            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        String query2 = "SELECT entrezID,symbol FROM entrez2sym,string WHERE "
                + "(entrezID = entrezb AND entreza=?);";
        try {
            PreparedStatement ps = this.connection.prepareStatement(query2);
            ps.setInt(1, seedID);
            ps.setInt(2, seedID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String symbol = rs.getString(2);
                if (symbol == null) {
                    logger.error("id {} symbol is null", id);
                    continue;
                }
                String path = String.format("%s&hArr;%s", seedSymbol, symbol);
                addPathToMap(id, path);
                if (this.seedGeneIdSet.contains(id)) {
                    continue; /* Do not add double path if the first node also belongs to the seed family. */

                }
                addDoublePath(id, seedID, path); /*
                 * id is the Entrez Gene id of the gene that interacts with the
                 * seed gene (seedID).
                 */
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
    }

    /**
     * This method takes a comma separated list of entrez gene ids representing
     * the seed gene (e.g., all genes from some OMIM Phenotypic Series), and
     * finds all paths of lengths one and two that emanate from them.
     *
     * @param seedGenes A comma-separated list of EntrezGene ids
     */
    private void initializeSeedGenesAndNeighbors(String seedGenes) {
        String[] seedGeneValues = seedGenes.split(",");
        for (String geneId : seedGeneValues) {
            Integer entrezID = Integer.parseInt(geneId.trim());
            logger.info("DOING {}", entrezID);
            this.seedGeneIdSet.add(entrezID);
            String symbol = getSymbol(entrezID);
            logger.info("GOT SYMBOL {}", symbol);
            if (symbol == null) {
                continue;
            }
            addSinglePath(entrezID, symbol);
            logger.info("FINISHED");
        }
    }
}
