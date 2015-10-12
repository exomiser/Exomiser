/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
public class TadDao {

    private final Logger logger = LoggerFactory.getLogger(TadDao.class);

    @Autowired
    private DataSource dataSource;

    @Cacheable(value = "tad", key = "#variant.chromosomalVariant")
    public List<String> getGenesInTad(Variant variant) {
        List<String> genesInTad = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, variant);
                ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        String geneSymbol = rs.getString(1);
                        genesInTad.add(geneSymbol);
                    }
        } catch (SQLException e) {
            logger.error("Error executing regulatory feature query: ", e);
        }
        return genesInTad;
    }

    private PreparedStatement createPreparedStatement(Connection connection, Variant variant) throws SQLException {
        String tadQuery = "select symbol from tad where chromosome = ? and start < ? and \"end\" > ?";
        PreparedStatement ps = connection.prepareStatement(tadQuery);
        ps.setInt(1, variant.getChromosome());
        ps.setInt(2, variant.getPosition());
        ps.setInt(3, variant.getPosition());
        return ps;
    }
}
