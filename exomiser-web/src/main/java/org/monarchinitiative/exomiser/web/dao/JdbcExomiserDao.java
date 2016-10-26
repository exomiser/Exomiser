/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.monarchinitiative.exomiser.web.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class JdbcExomiserDao implements ExomiserDao {

    Logger logger = LoggerFactory.getLogger(JdbcExomiserDao.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public Map<String, String> getDiseases() {
        String query = "SELECT disease_id, diseasename FROM disease";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, query);
                ResultSet rs = preparedStatement.executeQuery()) {

            return processGetDiseasesResults(rs);

        } catch (SQLException e) {
            logger.error("Error executing getDiseases query: ", e);
        }

        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getHpoTerms() {
        String query = "select id, lcname from hpo";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, query);
                ResultSet rs = preparedStatement.executeQuery()) {

            return processGetHpoTermsResults(rs);

        } catch (SQLException e) {
            logger.error("Error executing getHpoTerms query: ", e);
        }

        return Collections.emptyMap();
    }
    
    @Override
    public Map<String, String> getGenes() {
        String query = "select entrez_id, human_gene_symbol from human2mouse_orthologs";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = createPreparedStatement(connection, query);
                ResultSet rs = preparedStatement.executeQuery()) {

            return processGetHpoTermsResults(rs);

        } catch (SQLException e) {
            logger.error("Error executing getGenes query: ", e);
        }

        return Collections.emptyMap();
    }

    private PreparedStatement createPreparedStatement(Connection connection, String query) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        return preparedStatement;
    }

    private Map<String, String> processGetDiseasesResults(ResultSet rs) throws SQLException {
        Map<String, String> diseaseIdToTerms = new ConcurrentHashMap<>();

        while (rs.next()) {
            String diseaseId = rs.getString(1);
            String diseaseTerm = (rs.getString(2) == null)? "": rs.getString(2);
            if (diseaseId != null) {
                diseaseId = diseaseId.trim();
                diseaseIdToTerms.put(diseaseId, diseaseTerm);
            }
        }

        return diseaseIdToTerms;
    }

    private Map<String, String> processGetHpoTermsResults(ResultSet rs) throws SQLException {
        Map<String, String> hpoTerms = new ConcurrentHashMap<>();

        while (rs.next()) {
            String hpId = rs.getString(1);
            String hpTerm = rs.getString(2);
            hpId = hpId.trim();
            hpoTerms.put(hpId, hpTerm);
        }
        
        return hpoTerms;
    }

}
