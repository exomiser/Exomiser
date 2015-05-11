/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.pathogenicity.CaddScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class DefaultPathogenicityDao implements PathogenicityDao {

    private final Logger logger = LoggerFactory.getLogger(DefaultPathogenicityDao.class);

    @Autowired
    private DataSource dataSource;

    @Cacheable(value = "pathogenicity", key = "#variant.chromosomalVariant")
    @Override
    public PathogenicityData getPathogenicityData(Variant variant) {

        //if a variant is not classified as missense then we don't need to hit 
        //the database as we're going to assign it a constant pathogenicity score.
        VariantEffect variantEffect = variant.getVariantEffect();
        if (variantEffect != VariantEffect.MISSENSE_VARIANT) {
            return new PathogenicityData();
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {

            return processResults(rs, variant);

        } catch (SQLException e) {
            logger.error("Error executing pathogenicity query: ", e);
        }
        return null;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String query = String.format("SELECT "
                + "sift,"
                + "polyphen,"
                + "mut_taster "
                //As of 20150511 we're not going to use the CADD data from the database as it requires normalising and hasn't been
                //using it will COMPLETELY FUBAR THE PATHOGENICITY FILTER, so don't add it back until it's normalised on a 0-1 scale.
//                + "cadd "
                + "FROM variant "
                + "WHERE chromosome = ? "
                + "AND position = ? "
                + "AND ref = ? "
                + "AND alt = ? ");
        PreparedStatement ps = connection.prepareStatement(query);

        // FIXME(holtgrewe): See my comment in {@link DefaultFrequencyDao.createPreparedStatement}.
        // Note: when we get here, we have tested above that we have a nonsynonymous substitution
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setString(3, variant.getRef());
        ps.setString(4, variant.getAlt());

        return ps;
    }

    PathogenicityData processResults(ResultSet rs, Variant variant) throws SQLException {

        SiftScore siftScore = null;
        PolyPhenScore polyPhenScore = null;
        MutationTasterScore mutationTasterScore = null;

        /* 
         * Switched db back to potentially having multiple rows per variant
         * if alt transcripts leads to diff aa changes and pathogenicities.
         * In future if know which transcript is more likely in the disease
         * tissue can use the most appropriate row but for now take max
         */
        while (rs.next()) {
            siftScore = getBestSiftScore(rs, siftScore);
            polyPhenScore = getBestPolyPhenScore(rs, polyPhenScore);
            mutationTasterScore = getBestMutationTasterScore(rs, mutationTasterScore);
        }

        return new PathogenicityData(polyPhenScore, mutationTasterScore, siftScore);

    }

    private SiftScore getBestSiftScore(ResultSet rs, SiftScore score) throws SQLException {
        float rowVal = rs.getFloat("sift");
        if (valueNotNullOrNoParseFloat(rs, rowVal)) {
            if (score == null || rowVal < score.getScore()) {
                return new SiftScore(rowVal);
            }
        }
        return score;
    }

    private PolyPhenScore getBestPolyPhenScore(ResultSet rs, PolyPhenScore score) throws SQLException {
        float rowVal = rs.getFloat("polyphen");
        if (valueNotNullOrNoParseFloat(rs, rowVal)) {
            if (score == null || rowVal > score.getScore()) {
                return new PolyPhenScore(rowVal);
            }
        }
        return score;
    }

    private MutationTasterScore getBestMutationTasterScore(ResultSet rs, MutationTasterScore score) throws SQLException {
        float rowVal = rs.getFloat("mut_taster");
        if (valueNotNullOrNoParseFloat(rs, rowVal)) {
            if (score == null || rowVal > score.getScore()) {
                return new MutationTasterScore(rowVal);
            }
        }
        return score;
    }

    private CaddScore getBestCaddScore(ResultSet rs, CaddScore score) throws SQLException {
        float rowVal = rs.getFloat("cadd");
        if (valueNotNullOrNoParseFloat(rs, rowVal)) {
            if (score == null || rowVal > score.getScore()) {
                return new CaddScore(rowVal);
            }
        }
        return score;
    }

    //TODO: this should vanish in the next db build. Check and remove.
    private static final float NOPARSE_FLOAT = -5f;

    private static boolean valueNotNullOrNoParseFloat(ResultSet rs, float rowVal) throws SQLException {
        return !rs.wasNull() && rowVal != NOPARSE_FLOAT;
    }

}
