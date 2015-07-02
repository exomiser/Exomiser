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
public class RegulatoryFeatureDao {

    private final Logger logger = LoggerFactory.getLogger(RegulatoryFeatureDao.class);

    @Autowired
    private DataSource dataSource;

    //@Cacheable(value = "pathogenicity", key = "#variant.chromosomalVariant")

    public VariantEffect getRegulatoryFeatureData(Variant variant) {
        VariantEffect variantEffect = variant.getVariantEffect();
        //logger.info("Testing " + variant.getChromosomalVariant() + " with effect " + variantEffect.toString());
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                //logger.info("Found reg variant");
                return VariantEffect.REGULATORY_REGION_VARIANT;
                // later may set variant object with the type of regulatory feature, associated gene and tissue involved
            }
        } catch (SQLException e) {
            logger.error("Error executing regulatory feature query: ", e);
        }
        return variantEffect;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String enhancerQuery = "SELECT feature_type, tissue FROM regulatory_features WHERE chromosome = ? AND start <  ? AND \"end\" > ?";
        PreparedStatement ps = connection.prepareStatement(enhancerQuery);
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setInt(3, variant.getPosition());
        return ps;
    }
}
