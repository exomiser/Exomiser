/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class MousePhenotypeOntologyDao implements OntologyDao {

    private static final Logger logger = LoggerFactory.getLogger(MousePhenotypeOntologyDao.class);

    @Autowired
    private DataSource dataSource;

    private final OntologyDaoResultSetProcessor rsProcessor = new OntologyDaoResultSetProcessor();

    @Override
    public Set<PhenotypeTerm> getAllTerms() {
        String query = "SELECT mp_id, mp_term FROM mp";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ontologyTermsStatement = connection.prepareStatement(query);
                ResultSet rs = ontologyTermsStatement.executeQuery()) {

            return rsProcessor.processOntologyTermsResultSet(rs);
            
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for MPO terms", query, e);
        }
        return Collections.emptySet();
    }
    
}
