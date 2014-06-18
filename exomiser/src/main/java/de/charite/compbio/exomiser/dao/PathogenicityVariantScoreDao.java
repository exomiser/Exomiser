/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.dao;

import de.charite.compbio.exomiser.filter.PathogenicityVariantScore;
import jannovar.common.Constants;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO class for retrieving
 * {@code de.charite.compbio.exomiser.fPathogenicityVariantScoreTriage} data from the
 * database.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class PathogenicityVariantScoreDao implements VariantScoreDao {

    private final Logger logger = LoggerFactory.getLogger(PathogenicityVariantScoreDao.class);

    @Autowired
    private DataSource dataSource;

    public PathogenicityVariantScoreDao() {
    }

    public PathogenicityVariantScoreDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PathogenicityVariantScore getVariantScore(Variant variant) {

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedFrequencyQuery = createPreparedStatement(connection, variant);
                ResultSet rs = preparedFrequencyQuery.executeQuery()) {

            return processVariantScoreResults(rs, variant);

        } catch (SQLException e) {
            logger.error("Error executing pathogenicity query: ", e);
        }
        return null;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String query = String.format("SELECT sift,"
                + "polyphen, mut_taster, cadd_raw, phyloP "
                + "FROM variant "
                + "WHERE chromosome = ? "
                + "AND position = ? "
                + "AND ref = ? "
                + "AND alt = ? ");

        PreparedStatement ps = connection.prepareStatement(query);
        int chrom = variant.get_chromosome();
        int position = variant.get_position();
        // Note: when we get here, we have tested above that we have a nonsynonymous substitution
        char ref = variant.get_ref().charAt(0);
        char alt = variant.get_alt().charAt(0);

        ps.setInt(1, chrom);
        ps.setInt(2, position);
        ps.setString(3, Character.toString(ref));
        ps.setString(4, Character.toString(alt));

        return ps;
    }
    
    PathogenicityVariantScore processVariantScoreResults(ResultSet rs, Variant variant) throws SQLException {
        
        float polyphen = Constants.UNINITIALIZED_FLOAT;
        float mutation_taster = Constants.UNINITIALIZED_FLOAT;
        float sift = Constants.UNINITIALIZED_FLOAT;
        float cadd_raw = Constants.UNINITIALIZED_FLOAT;
        
        /* 
         * Switched db back to potentially having multiple rows per variant
         * if alt transcripts leads to diff aa changes and pathogenicities.
         * In future if know which transcript is more likely in the disease
         * tissue can use the most appropriate row but for now take max
         */
        while (rs.next()) {
            if (sift == Constants.UNINITIALIZED_FLOAT || rs.getFloat(1) < sift) {
                sift = rs.getFloat(1);
            }
            if (polyphen == Constants.UNINITIALIZED_FLOAT || rs.getFloat(2) > polyphen) {
                polyphen = rs.getFloat(2);
            }
            if (mutation_taster == Constants.UNINITIALIZED_FLOAT || rs.getFloat(3) > mutation_taster) {
                mutation_taster = rs.getFloat(3);
            }
            if (cadd_raw == Constants.UNINITIALIZED_FLOAT || rs.getFloat(4) > cadd_raw) {
                cadd_raw = rs.getFloat(4);
            }
        }
        /**
         * The following classifies variants based upon their variant class
         * (MISSENSE, NONSENSE, INTRONIC). The actual logic for assigning
         * pathogenicity scores is in the PathogenicityTriage class.
         */
        if (!variant.is_missense_variant()) {
            return PathogenicityVariantScore.evaluateVariantClass(variant, cadd_raw);
        }
        else {
            PathogenicityVariantScore pt = new PathogenicityVariantScore(polyphen, mutation_taster, sift, cadd_raw);
            return pt;
        }
    }

}
