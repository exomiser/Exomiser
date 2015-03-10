/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.dao;

import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
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
        String query = "SELECT mp_id as id, mp_term as term FROM mp";
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

    @Override
    public Set<PhenotypeMatch> getPhenotypeMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        String mappingQuery = "SELECT simj, ic, score, mp_id AS hit_id, mp_term AS hit_term, lcs_id, lcs_term FROM hp_mp_mappings WHERE hp_id = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = setQueryHpId(connection, mappingQuery, hpoTerm);
                ResultSet rs = ps.executeQuery()) {

            return rsProcessor.processOntologyTermMatchResultSet(rs, hpoTerm);
            
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for HP-MP match terms", mappingQuery, e);
        }
        return Collections.emptySet();
    }

    private PreparedStatement setQueryHpId(final Connection connection, String mappingQuery, PhenotypeTerm hpoTerm) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(mappingQuery);
        ps.setString(1, hpoTerm.getId());
        return ps;
    }
    
    
}
