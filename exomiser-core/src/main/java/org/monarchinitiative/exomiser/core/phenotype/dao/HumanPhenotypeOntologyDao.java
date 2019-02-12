/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.phenotype.dao;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Repository
public class HumanPhenotypeOntologyDao implements OntologyDao {

    private static final Logger logger = LoggerFactory.getLogger(HumanPhenotypeOntologyDao.class);

    private final DataSource dataSource;

    public HumanPhenotypeOntologyDao(DataSource phenotypeDataSource) {
        this.dataSource = phenotypeDataSource;
    }

    @Override
    public Set<PhenotypeTerm> getAllTerms() {
        String query = "select id, lcname as term from hpo";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ontologyTermsStatement = connection.prepareStatement(query);
                ResultSet rs = ontologyTermsStatement.executeQuery()) {

            return OntologyDaoResultSetProcessor.processOntologyTermsResultSet(rs);
            
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for HPO terms", query, e);
        }
        return Collections.emptySet();
    }

    @Override
    public Set<PhenotypeMatch> getPhenotypeMatchesForHpoTerm(PhenotypeTerm hpoTerm) {
        String mappingQuery = "SELECT simj, ic, score, hp_id_hit AS hit_id, hp_hit_term AS hit_term, lcs_id, lcs_term FROM hp_hp_mappings WHERE hp_id = ?";
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = PreparedStatementSetter.prepareStatement(connection, mappingQuery, setter -> setter
                        .setString(1, hpoTerm.getId()));
                ResultSet rs = ps.executeQuery()) {

            return OntologyDaoResultSetProcessor.processOntologyTermMatchResultSet(rs, hpoTerm);
            
        } catch (SQLException e) {
            logger.error("Unable to execute query '{}' for HP-HP match terms", mappingQuery, e);
        }
        return Collections.emptySet();
    }

    public Map<String, PhenotypeTerm> getIdToPhenotypeTerms() {
        String query =
                "SELECT alt.alt_id, alt.primary_id, hp.lcname AS term " +
                "FROM hp_alt_ids alt, hpo hp " +
                "WHERE hp.id = alt.primary_id";

        Map<String, PhenotypeTerm> primaryIdToPhenotypeTerms = new HashMap<>();
        ImmutableMap.Builder<String, PhenotypeTerm> alternateIdToPhenotypeTerms = ImmutableMap.builder();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ontologyTermsStatement = connection.prepareStatement(query);
                ResultSet rs = ontologyTermsStatement.executeQuery()) {

            while(rs.next()) {
                String altId = rs.getString("alt_id");
                String primaryId = rs.getString("primary_id");
                String term = rs.getString("term");
                // avoid creating lots of duplicate objects - these will hang about for ages
                PhenotypeTerm phenotypeTerm = primaryIdToPhenotypeTerms.computeIfAbsent(primaryId, key -> PhenotypeTerm.of(primaryId, term));
                alternateIdToPhenotypeTerms.put(altId, phenotypeTerm);
            }

        } catch (SQLException e) {
            // this will always be thrown with data versions 1811 and below as the table doesn't exist in those releases
            logger.error("Unable to execute query '{}' for HPO terms", query, e);
        }
        // No need to add the existing primary ids as these are already there
        return alternateIdToPhenotypeTerms.build();
    }

}
